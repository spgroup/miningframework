package jenkins.model;

import antlr.ANTLRException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.thoughtworks.xstream.XStream;
import hudson.BulkChange;
import hudson.DNSMultiCast;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.ExtensionComponent;
import hudson.ExtensionFinder;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher;
import hudson.Launcher.LocalLauncher;
import hudson.Lookup;
import hudson.Main;
import hudson.Plugin;
import hudson.PluginManager;
import hudson.PluginWrapper;
import hudson.ProxyConfiguration;
import jenkins.AgentProtocol;
import jenkins.util.SystemProperties;
import hudson.TcpSlaveAgentListener;
import hudson.UDPBroadcastThread;
import hudson.Util;
import hudson.WebAppMain;
import hudson.XmlFile;
import hudson.cli.declarative.CLIMethod;
import hudson.cli.declarative.CLIResolver;
import hudson.init.InitMilestone;
import hudson.init.InitStrategy;
import hudson.init.TermMilestone;
import hudson.init.TerminatorFinder;
import hudson.lifecycle.Lifecycle;
import hudson.lifecycle.RestartNotSupportedException;
import hudson.logging.LogRecorderManager;
import hudson.markup.EscapedMarkupFormatter;
import hudson.markup.MarkupFormatter;
import hudson.model.AbstractCIBase;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.AdministrativeMonitor;
import hudson.model.AllView;
import hudson.model.Api;
import hudson.model.Computer;
import hudson.model.ComputerSet;
import hudson.model.DependencyGraph;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.model.DescriptorByNameOwner;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.Failure;
import hudson.model.Fingerprint;
import hudson.model.FingerprintCleanupThread;
import hudson.model.FingerprintMap;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.ItemGroupMixIn;
import hudson.model.Items;
import hudson.model.JDK;
import hudson.model.Job;
import hudson.model.JobPropertyDescriptor;
import hudson.model.Label;
import hudson.model.ListView;
import hudson.model.LoadBalancer;
import hudson.model.LoadStatistics;
import hudson.model.ManagementLink;
import hudson.model.Messages;
import hudson.model.ModifiableViewGroup;
import hudson.model.NoFingerprintMatch;
import hudson.model.Node;
import hudson.model.OverallLoadStatistics;
import hudson.model.PaneStatusProperties;
import hudson.model.Project;
import hudson.model.Queue;
import hudson.model.Queue.FlyweightTask;
import hudson.model.RestartListener;
import hudson.model.RootAction;
import hudson.model.Slave;
import hudson.model.TaskListener;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import hudson.model.UnprotectedRootAction;
import hudson.model.UpdateCenter;
import hudson.model.User;
import hudson.model.View;
import hudson.model.ViewGroupMixIn;
import hudson.model.WorkspaceCleanupThread;
import hudson.model.labels.LabelAtom;
import hudson.model.listeners.ItemListener;
import hudson.model.listeners.SCMListener;
import hudson.model.listeners.SaveableListener;
import hudson.remoting.Callable;
import hudson.remoting.LocalChannel;
import hudson.remoting.VirtualChannel;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SCM;
import hudson.search.CollectionSearchIndex;
import hudson.search.SearchIndexBuilder;
import hudson.search.SearchItem;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import hudson.security.AuthorizationStrategy;
import hudson.security.BasicAuthenticationFilter;
import hudson.security.FederatedLoginService;
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy;
import hudson.security.HudsonFilter;
import hudson.security.LegacyAuthorizationStrategy;
import hudson.security.LegacySecurityRealm;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.PermissionScope;
import hudson.security.SecurityMode;
import hudson.security.SecurityRealm;
import hudson.security.csrf.CrumbIssuer;
import hudson.slaves.Cloud;
import hudson.slaves.ComputerListener;
import hudson.slaves.DumbSlave;
import hudson.slaves.NodeDescriptor;
import hudson.slaves.NodeList;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.slaves.NodeProvisioner;
import hudson.slaves.OfflineCause;
import hudson.slaves.RetentionStrategy;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.triggers.SafeTimerTask;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.AdministrativeError;
import hudson.util.CaseInsensitiveComparator;
import hudson.util.ClockDifference;
import hudson.util.CopyOnWriteList;
import hudson.util.CopyOnWriteMap;
import hudson.util.DaemonThreadFactory;
import hudson.util.DescribableList;
import hudson.util.FormApply;
import hudson.util.FormValidation;
import hudson.util.Futures;
import hudson.util.HudsonIsLoading;
import hudson.util.HudsonIsRestarting;
import hudson.util.IOUtils;
import hudson.util.Iterators;
import hudson.util.JenkinsReloadFailed;
import hudson.util.Memoizer;
import hudson.util.MultipartFormDataParser;
import hudson.util.NamingThreadFactory;
import hudson.util.PluginServletFilter;
import hudson.util.RemotingDiagnostics;
import hudson.util.RemotingDiagnostics.HeapDump;
import hudson.util.TextFile;
import hudson.util.TimeUnit2;
import hudson.util.VersionNumber;
import hudson.util.XStream2;
import hudson.views.DefaultMyViewsTabBar;
import hudson.views.DefaultViewsTabBar;
import hudson.views.MyViewsTabBar;
import hudson.views.ViewsTabBar;
import hudson.widgets.Widget;
import java.util.Objects;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import jenkins.ExtensionComponentSet;
import jenkins.ExtensionRefreshException;
import jenkins.InitReactorRunner;
import jenkins.install.InstallState;
import jenkins.install.InstallUtil;
import jenkins.install.SetupWizard;
import jenkins.model.ProjectNamingStrategy.DefaultProjectNamingStrategy;
import jenkins.security.ConfidentialKey;
import jenkins.security.ConfidentialStore;
import jenkins.security.SecurityListener;
import jenkins.security.MasterToSlaveCallable;
import jenkins.slaves.WorkspaceLocator;
import jenkins.util.JenkinsJVM;
import jenkins.util.Timer;
import jenkins.util.io.FileBoolean;
import jenkins.util.io.OnMaster;
import jenkins.util.xml.XMLUtils;
import net.jcip.annotations.GuardedBy;
import net.sf.json.JSONObject;
import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.AcegiSecurityException;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.anonymous.AnonymousAuthenticationToken;
import org.acegisecurity.ui.AbstractProcessingFilter;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.apache.commons.logging.LogFactory;
import org.jvnet.hudson.reactor.Executable;
import org.jvnet.hudson.reactor.Milestone;
import org.jvnet.hudson.reactor.Reactor;
import org.jvnet.hudson.reactor.ReactorException;
import org.jvnet.hudson.reactor.ReactorListener;
import org.jvnet.hudson.reactor.Task;
import org.jvnet.hudson.reactor.TaskBuilder;
import org.jvnet.hudson.reactor.TaskGraphBuilder;
import org.jvnet.hudson.reactor.TaskGraphBuilder.Handle;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.args4j.Argument;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerFallback;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.framework.adjunct.AdjunctManager;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.jelly.JellyClassLoaderTearOff;
import org.kohsuke.stapler.jelly.JellyRequestDispatcher;
import org.xml.sax.InputSource;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import static hudson.Util.*;
import static hudson.init.InitMilestone.*;
import hudson.init.Initializer;
import hudson.util.LogTaskListener;
import static java.util.logging.Level.*;
import static javax.servlet.http.HttpServletResponse.*;
import org.kohsuke.stapler.WebMethod;

@ExportedBean
public class Jenkins extends AbstractCIBase implements DirectlyModifiableTopLevelItemGroup, StaplerProxy, StaplerFallback, ModifiableViewGroup, AccessControlled, DescriptorByNameOwner, ModelObjectWithContextMenu, ModelObjectWithChildren, OnMaster {

    private transient final Queue queue;

    public transient final Lookup lookup = new Lookup();

    private String version = "1.0";

    private transient InstallState installState = InstallState.UNKNOWN;

    private transient SetupWizard setupWizard;

    private int numExecutors = 2;

    private Mode mode = Mode.NORMAL;

    private Boolean useSecurity;

    private volatile AuthorizationStrategy authorizationStrategy = AuthorizationStrategy.UNSECURED;

    private volatile SecurityRealm securityRealm = SecurityRealm.NO_AUTHENTICATION;

    private volatile boolean disableRememberMe;

    private ProjectNamingStrategy projectNamingStrategy = DefaultProjectNamingStrategy.DEFAULT_NAMING_STRATEGY;

    private String workspaceDir = "${ITEM_ROOTDIR}/" + WORKSPACE_DIRNAME;

    private String buildsDir = "${ITEM_ROOTDIR}/builds";

    private String systemMessage;

    private MarkupFormatter markupFormatter;

    public transient final File root;

    private transient volatile InitMilestone initLevel = InitMilestone.STARTED;

    transient final Map<String, TopLevelItem> items = new CopyOnWriteMap.Tree<String, TopLevelItem>(CaseInsensitiveComparator.INSTANCE);

    private static Jenkins theInstance;

    private transient volatile boolean isQuietingDown;

    private transient volatile boolean terminating;

    @GuardedBy("Jenkins.class")
    private transient boolean cleanUpStarted;

    private volatile List<JDK> jdks = new ArrayList<JDK>();

    private transient volatile DependencyGraph dependencyGraph;

    private final transient AtomicBoolean dependencyGraphDirty = new AtomicBoolean();

    private volatile ViewsTabBar viewsTabBar = new DefaultViewsTabBar();

    private volatile MyViewsTabBar myViewsTabBar = new DefaultMyViewsTabBar();

    private transient final Memoizer<Class, ExtensionList> extensionLists = new Memoizer<Class, ExtensionList>() {

        public ExtensionList compute(Class key) {
            return ExtensionList.create(Jenkins.this, key);
        }
    };

    private transient final Memoizer<Class, DescriptorExtensionList> descriptorLists = new Memoizer<Class, DescriptorExtensionList>() {

        public DescriptorExtensionList compute(Class key) {
            return DescriptorExtensionList.createDescriptorList(Jenkins.this, key);
        }
    };

    protected transient final Map<Node, Computer> computers = new CopyOnWriteMap.Hash<Node, Computer>();

    public final Hudson.CloudList clouds = new Hudson.CloudList(this);

    public static class CloudList extends DescribableList<Cloud, Descriptor<Cloud>> {

        public CloudList(Jenkins h) {
            super(h);
        }

        public CloudList() {
        }

        public Cloud getByName(String name) {
            for (Cloud c : this) if (c.name.equals(name))
                return c;
            return null;
        }

        @Override
        protected void onModified() throws IOException {
            super.onModified();
            Jenkins.getInstance().trimLabels();
        }
    }

    @Deprecated
    protected transient volatile NodeList slaves;

    private transient final Nodes nodes = new Nodes(this);

    Integer quietPeriod;

    int scmCheckoutRetryCount;

    private final CopyOnWriteArrayList<View> views = new CopyOnWriteArrayList<View>();

    private volatile String primaryView;

    private transient final ViewGroupMixIn viewGroupMixIn = new ViewGroupMixIn(this) {

        protected List<View> views() {
            return views;
        }

        protected String primaryView() {
            return primaryView;
        }

        protected void primaryView(String name) {
            primaryView = name;
        }
    };

    private transient final FingerprintMap fingerprintMap = new FingerprintMap();

    public transient final PluginManager pluginManager;

    public transient volatile TcpSlaveAgentListener tcpSlaveAgentListener;

    private transient final Object tcpSlaveAgentListenerLock = new Object();

    private transient UDPBroadcastThread udpBroadcastThread;

    private transient DNSMultiCast dnsMultiCast;

    private transient final CopyOnWriteList<SCMListener> scmListeners = new CopyOnWriteList<SCMListener>();

    private int slaveAgentPort = getSlaveAgentPortInitialValue(0);

    private static int getSlaveAgentPortInitialValue(int def) {
        return SystemProperties.getInteger(Jenkins.class.getName() + ".slaveAgentPort", def);
    }

    private static final boolean SLAVE_AGENT_PORT_ENFORCE = SystemProperties.getBoolean(Jenkins.class.getName() + ".slaveAgentPortEnforce", false);

    @CheckForNull
    private List<String> disabledAgentProtocols;

    @Deprecated
    private transient String[] _disabledAgentProtocols;

    @CheckForNull
    private List<String> enabledAgentProtocols;

    @Deprecated
    private transient String[] _enabledAgentProtocols;

    private transient Set<String> agentProtocols;

    private String label = "";

    private volatile CrumbIssuer crumbIssuer;

    private transient final ConcurrentHashMap<String, Label> labels = new ConcurrentHashMap<String, Label>();

    @Exported
    public transient final OverallLoadStatistics overallLoad = new OverallLoadStatistics();

    @Exported
    public transient final LoadStatistics unlabeledLoad = new UnlabeledLoadStatistics();

    public transient final NodeProvisioner unlabeledNodeProvisioner = new NodeProvisioner(null, unlabeledLoad);

    @Restricted(NoExternalUse.class)
    @Deprecated
    public transient final NodeProvisioner overallNodeProvisioner = unlabeledNodeProvisioner;

    public transient final ServletContext servletContext;

    private transient final List<Action> actions = new CopyOnWriteArrayList<Action>();

    private DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties = new DescribableList<NodeProperty<?>, NodePropertyDescriptor>(this);

    private DescribableList<NodeProperty<?>, NodePropertyDescriptor> globalNodeProperties = new DescribableList<NodeProperty<?>, NodePropertyDescriptor>(this);

    public transient final List<AdministrativeMonitor> administrativeMonitors = getExtensionList(AdministrativeMonitor.class);

    private transient final List<Widget> widgets = getExtensionList(Widget.class);

    private transient final AdjunctManager adjuncts;

    private transient final ItemGroupMixIn itemGroupMixIn = new ItemGroupMixIn(this, this) {

        @Override
        protected void add(TopLevelItem item) {
            items.put(item.getName(), item);
        }

        @Override
        protected File getRootDirFor(String name) {
            return Jenkins.this.getRootDirFor(name);
        }
    };

    public interface JenkinsHolder {

        @CheckForNull
        Jenkins getInstance();
    }

    static JenkinsHolder HOLDER = new JenkinsHolder() {

        @CheckForNull
        public Jenkins getInstance() {
            return theInstance;
        }
    };

    @Deprecated
    @Nonnull
    public static Jenkins getActiveInstance() throws IllegalStateException {
        Jenkins instance = HOLDER.getInstance();
        if (instance == null) {
            throw new IllegalStateException("Jenkins has not been started, or was already shut down");
        }
        return instance;
    }

    @CheckForNull
    public static Jenkins getInstanceOrNull() {
        return HOLDER.getInstance();
    }

    @CLIResolver
    @Nonnull
    public static Jenkins getInstance() {
        Jenkins instance = HOLDER.getInstance();
        if (instance == null) {
            if (SystemProperties.getBoolean(Jenkins.class.getName() + ".enableExceptionOnNullInstance")) {
                throw new IllegalStateException("Jenkins has not been started, or was already shut down");
            }
        }
        return instance;
    }

    private transient final String secretKey;

    private transient final UpdateCenter updateCenter = UpdateCenter.createUpdateCenter(null);

    private Boolean noUsageStatistics;

    public transient volatile ProxyConfiguration proxy;

    private transient final LogRecorderManager log = new LogRecorderManager();

    private transient final boolean oldJenkinsJVM;

    protected Jenkins(File root, ServletContext context) throws IOException, InterruptedException, ReactorException {
        this(root, context, null);
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({ "SC_START_IN_CTOR", "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD" })
    protected Jenkins(File root, ServletContext context, PluginManager pluginManager) throws IOException, InterruptedException, ReactorException {
        oldJenkinsJVM = JenkinsJVM.isJenkinsJVM();
        JenkinsJVMAccess._setJenkinsJVM(true);
        long start = System.currentTimeMillis();
        ACL.impersonate(ACL.SYSTEM);
        try {
            this.root = root;
            this.servletContext = context;
            computeVersion(context);
            if (theInstance != null)
                throw new IllegalStateException("second instance");
            theInstance = this;
            if (!new File(root, "jobs").exists()) {
                workspaceDir = "${JENKINS_HOME}/workspace/${ITEM_FULLNAME}";
            }
            final InitStrategy is = InitStrategy.get(Thread.currentThread().getContextClassLoader());
            Trigger.timer = new java.util.Timer("Jenkins cron thread");
            queue = new Queue(LoadBalancer.CONSISTENT_HASH);
            try {
                dependencyGraph = DependencyGraph.EMPTY;
            } catch (InternalError e) {
                if (e.getMessage().contains("window server")) {
                    throw new Error("Looks like the server runs without X. Please specify -Djava.awt.headless=true as JVM option", e);
                }
                throw e;
            }
            TextFile secretFile = new TextFile(new File(getRootDir(), "secret.key"));
            if (secretFile.exists()) {
                secretKey = secretFile.readTrim();
            } else {
                SecureRandom sr = new SecureRandom();
                byte[] random = new byte[32];
                sr.nextBytes(random);
                secretKey = Util.toHexString(random);
                secretFile.write(secretKey);
                new FileBoolean(new File(root, "secret.key.not-so-secret")).on();
            }
            try {
                proxy = ProxyConfiguration.load();
            } catch (IOException e) {
                LOGGER.log(SEVERE, "Failed to load proxy configuration", e);
            }
            if (pluginManager == null)
                pluginManager = PluginManager.createDefault(this);
            this.pluginManager = pluginManager;
            WebApp.get(servletContext).setClassLoader(pluginManager.uberClassLoader);
            adjuncts = new AdjunctManager(servletContext, pluginManager.uberClassLoader, "adjuncts/" + SESSION_HASH, TimeUnit2.DAYS.toMillis(365));
            executeReactor(is, pluginManager.initTasks(is), loadTasks(), InitMilestone.ordering());
            if (initLevel != InitMilestone.COMPLETED) {
                LOGGER.log(SEVERE, "Jenkins initialization has not reached the COMPLETED initialization milestone after the startup. " + "Current state: {0}. " + "It may cause undefined incorrect behavior in Jenkins plugin relying on this state. " + "It is likely an issue with the Initialization task graph. " + "Example: usage of @Initializer(after = InitMilestone.COMPLETED) in a plugin (JENKINS-37759). " + "Please create a bug in Jenkins bugtracker. ", initLevel);
            }
            if (KILL_AFTER_LOAD)
                System.exit(0);
            setupWizard = new SetupWizard();
            InstallUtil.proceedToNextStateFrom(InstallState.UNKNOWN);
            launchTcpSlaveAgentListener();
            if (UDPBroadcastThread.PORT != -1) {
                try {
                    udpBroadcastThread = new UDPBroadcastThread(this);
                    udpBroadcastThread.start();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to broadcast over UDP (use -Dhudson.udp=-1 to disable)", e);
                }
            }
            dnsMultiCast = new DNSMultiCast(this);
            Timer.get().scheduleAtFixedRate(new SafeTimerTask() {

                @Override
                protected void doRun() throws Exception {
                    trimLabels();
                }
            }, TimeUnit2.MINUTES.toMillis(5), TimeUnit2.MINUTES.toMillis(5), TimeUnit.MILLISECONDS);
            updateComputerList();
            {
                final Computer c = toComputer();
                if (c != null) {
                    for (ComputerListener cl : ComputerListener.all()) {
                        try {
                            cl.onOnline(c, new LogTaskListener(LOGGER, INFO));
                        } catch (Throwable t) {
                            if (t instanceof Error) {
                                throw t;
                            }
                            LOGGER.log(SEVERE, String.format("Invocation of the computer listener %s failed for the Jenkins master node", cl.getClass()), t);
                        }
                    }
                }
            }
            for (ItemListener l : ItemListener.all()) {
                long itemListenerStart = System.currentTimeMillis();
                try {
                    l.onLoaded();
                } catch (RuntimeException x) {
                    LOGGER.log(Level.WARNING, null, x);
                }
                if (LOG_STARTUP_PERFORMANCE)
                    LOGGER.info(String.format("Took %dms for item listener %s startup", System.currentTimeMillis() - itemListenerStart, l.getClass().getName()));
            }
            if (LOG_STARTUP_PERFORMANCE)
                LOGGER.info(String.format("Took %dms for complete Jenkins startup", System.currentTimeMillis() - start));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @SuppressWarnings({ "unused" })
    private Object readResolve() {
        if (jdks == null) {
            jdks = new ArrayList<>();
        }
        if (SLAVE_AGENT_PORT_ENFORCE) {
            slaveAgentPort = getSlaveAgentPortInitialValue(slaveAgentPort);
        }
        if (disabledAgentProtocols == null && _disabledAgentProtocols != null) {
            disabledAgentProtocols = Arrays.asList(_disabledAgentProtocols);
            _disabledAgentProtocols = null;
        }
        if (enabledAgentProtocols == null && _enabledAgentProtocols != null) {
            enabledAgentProtocols = Arrays.asList(_enabledAgentProtocols);
            _enabledAgentProtocols = null;
        }
        agentProtocols = null;
        return this;
    }

    @Nonnull
    @Restricted(NoExternalUse.class)
    public InstallState getInstallState() {
        if (installState == null || installState.name() == null) {
            return InstallState.UNKNOWN;
        }
        return installState;
    }

    @Restricted(NoExternalUse.class)
    public void setInstallState(@Nonnull InstallState newState) {
        InstallState prior = installState;
        installState = newState;
        if (!prior.equals(newState)) {
            newState.initializeState();
        }
    }

    private void executeReactor(final InitStrategy is, TaskBuilder... builders) throws IOException, InterruptedException, ReactorException {
        Reactor reactor = new Reactor(builders) {

            @Override
            protected void runTask(Task task) throws Exception {
                if (is != null && is.skipInitTask(task))
                    return;
                ACL.impersonate(ACL.SYSTEM);
                String taskName = InitReactorRunner.getDisplayName(task);
                Thread t = Thread.currentThread();
                String name = t.getName();
                if (taskName != null)
                    t.setName(taskName);
                try {
                    long start = System.currentTimeMillis();
                    super.runTask(task);
                    if (LOG_STARTUP_PERFORMANCE)
                        LOGGER.info(String.format("Took %dms for %s by %s", System.currentTimeMillis() - start, taskName, name));
                } catch (Exception | Error x) {
                    if (containsLinkageError(x)) {
                        LOGGER.log(Level.WARNING, taskName + " failed perhaps due to plugin dependency issues", x);
                    } else {
                        throw x;
                    }
                } finally {
                    t.setName(name);
                    SecurityContextHolder.clearContext();
                }
            }

            private boolean containsLinkageError(Throwable x) {
                if (x instanceof LinkageError) {
                    return true;
                }
                Throwable x2 = x.getCause();
                return x2 != null && containsLinkageError(x2);
            }
        };
        new InitReactorRunner() {

            @Override
            protected void onInitMilestoneAttained(InitMilestone milestone) {
                initLevel = milestone;
                if (milestone == PLUGINS_PREPARED) {
                    ExtensionList.lookup(ExtensionFinder.class).getComponents();
                }
            }
        }.run(reactor);
    }

    public TcpSlaveAgentListener getTcpSlaveAgentListener() {
        return tcpSlaveAgentListener;
    }

    public AdjunctManager getAdjuncts(String dummy) {
        return adjuncts;
    }

    @Exported
    public int getSlaveAgentPort() {
        return slaveAgentPort;
    }

    public boolean isSlaveAgentPortEnforced() {
        return Jenkins.SLAVE_AGENT_PORT_ENFORCE;
    }

    public void setSlaveAgentPort(int port) throws IOException {
        if (SLAVE_AGENT_PORT_ENFORCE) {
            LOGGER.log(Level.WARNING, "setSlaveAgentPort({0}) call ignored because system property {1} is true", new String[] { Integer.toString(port), Jenkins.class.getName() + ".slaveAgentPortEnforce" });
        } else {
            forceSetSlaveAgentPort(port);
        }
    }

    private void forceSetSlaveAgentPort(int port) throws IOException {
        this.slaveAgentPort = port;
        launchTcpSlaveAgentListener();
    }

    public Set<String> getAgentProtocols() {
        if (agentProtocols == null) {
            Set<String> result = new TreeSet<>();
            Set<String> disabled = new TreeSet<>();
            for (String p : Util.fixNull(disabledAgentProtocols)) {
                disabled.add(p.trim());
            }
            Set<String> enabled = new TreeSet<>();
            for (String p : Util.fixNull(enabledAgentProtocols)) {
                enabled.add(p.trim());
            }
            for (AgentProtocol p : AgentProtocol.all()) {
                String name = p.getName();
                if (name != null && (p.isRequired() || (!disabled.contains(name) && (!p.isOptIn() || enabled.contains(name))))) {
                    result.add(name);
                }
            }
            agentProtocols = result;
            return result;
        }
        return agentProtocols;
    }

    public void setAgentProtocols(Set<String> protocols) {
        Set<String> disabled = new TreeSet<>();
        Set<String> enabled = new TreeSet<>();
        for (AgentProtocol p : AgentProtocol.all()) {
            String name = p.getName();
            if (name != null && !p.isRequired()) {
                if (p.isOptIn()) {
                    if (protocols.contains(name)) {
                        enabled.add(name);
                    }
                } else {
                    if (!protocols.contains(name)) {
                        disabled.add(name);
                    }
                }
            }
        }
        disabledAgentProtocols = disabled.isEmpty() ? null : new ArrayList<>(disabled);
        enabledAgentProtocols = enabled.isEmpty() ? null : new ArrayList<>(enabled);
        agentProtocols = null;
    }

    private void launchTcpSlaveAgentListener() throws IOException {
        synchronized (tcpSlaveAgentListenerLock) {
            if (tcpSlaveAgentListener != null && tcpSlaveAgentListener.configuredPort != slaveAgentPort) {
                tcpSlaveAgentListener.shutdown();
                tcpSlaveAgentListener = null;
            }
            if (slaveAgentPort != -1 && tcpSlaveAgentListener == null) {
                final String administrativeMonitorId = getClass().getName() + ".tcpBind";
                try {
                    tcpSlaveAgentListener = new TcpSlaveAgentListener(slaveAgentPort);
                    AdministrativeMonitor toBeRemoved = null;
                    ExtensionList<AdministrativeMonitor> all = AdministrativeMonitor.all();
                    for (AdministrativeMonitor am : all) {
                        if (administrativeMonitorId.equals(am.id)) {
                            toBeRemoved = am;
                            break;
                        }
                    }
                    all.remove(toBeRemoved);
                } catch (BindException e) {
                    LOGGER.log(Level.WARNING, String.format("Failed to listen to incoming agent connections through JNLP port %s. Change the JNLP port number", slaveAgentPort), e);
                    new AdministrativeError(administrativeMonitorId, "Failed to listen to incoming agent connections through JNLP", "Failed to listen to incoming agent connections through JNLP. <a href='configureSecurity'>Change the JNLP port number</a> to solve the problem.", e);
                }
            }
        }
    }

    @Extension
    @Restricted(NoExternalUse.class)
    public static class EnforceSlaveAgentPortAdministrativeMonitor extends AdministrativeMonitor {

        @Inject
        Jenkins j;

        @Override
        public String getDisplayName() {
            return jenkins.model.Messages.EnforceSlaveAgentPortAdministrativeMonitor_displayName();
        }

        public String getSystemPropertyName() {
            return Jenkins.class.getName() + ".slaveAgentPort";
        }

        public int getExpectedPort() {
            int slaveAgentPort = j.slaveAgentPort;
            return Jenkins.getSlaveAgentPortInitialValue(slaveAgentPort);
        }

        public void doAct(StaplerRequest req, StaplerResponse rsp) throws IOException {
            j.forceSetSlaveAgentPort(getExpectedPort());
            rsp.sendRedirect2(req.getContextPath() + "/manage");
        }

        @Override
        public boolean isActivated() {
            int slaveAgentPort = Jenkins.getInstance().slaveAgentPort;
            return SLAVE_AGENT_PORT_ENFORCE && slaveAgentPort != Jenkins.getSlaveAgentPortInitialValue(slaveAgentPort);
        }
    }

    public void setNodeName(String name) {
        throw new UnsupportedOperationException();
    }

    public String getNodeDescription() {
        return Messages.Hudson_NodeDescription();
    }

    @Exported
    public String getDescription() {
        return systemMessage;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public UpdateCenter getUpdateCenter() {
        return updateCenter;
    }

    public boolean isUsageStatisticsCollected() {
        return noUsageStatistics == null || !noUsageStatistics;
    }

    public void setNoUsageStatistics(Boolean noUsageStatistics) throws IOException {
        this.noUsageStatistics = noUsageStatistics;
        save();
    }

    public View.People getPeople() {
        return new View.People(this);
    }

    public View.AsynchPeople getAsynchPeople() {
        return new View.AsynchPeople(this);
    }

    @Deprecated
    public boolean hasPeople() {
        return View.People.isApplicable(items.values());
    }

    public Api getApi() {
        return new Api(this);
    }

    @Deprecated
    public String getSecretKey() {
        return secretKey;
    }

    @Deprecated
    public SecretKey getSecretKeyAsAES128() {
        return Util.toAes128Key(secretKey);
    }

    @SuppressWarnings("deprecation")
    public String getLegacyInstanceId() {
        return Util.getDigestOf(getSecretKey());
    }

    public Descriptor<SCM> getScm(String shortClassName) {
        return findDescriptor(shortClassName, SCM.all());
    }

    public Descriptor<RepositoryBrowser<?>> getRepositoryBrowser(String shortClassName) {
        return findDescriptor(shortClassName, RepositoryBrowser.all());
    }

    public Descriptor<Builder> getBuilder(String shortClassName) {
        return findDescriptor(shortClassName, Builder.all());
    }

    public Descriptor<BuildWrapper> getBuildWrapper(String shortClassName) {
        return findDescriptor(shortClassName, BuildWrapper.all());
    }

    public Descriptor<Publisher> getPublisher(String shortClassName) {
        return findDescriptor(shortClassName, Publisher.all());
    }

    public TriggerDescriptor getTrigger(String shortClassName) {
        return (TriggerDescriptor) findDescriptor(shortClassName, Trigger.all());
    }

    public Descriptor<RetentionStrategy<?>> getRetentionStrategy(String shortClassName) {
        return findDescriptor(shortClassName, RetentionStrategy.all());
    }

    public JobPropertyDescriptor getJobProperty(String shortClassName) {
        Descriptor d = findDescriptor(shortClassName, JobPropertyDescriptor.all());
        return (JobPropertyDescriptor) d;
    }

    @Deprecated
    public ComputerSet getComputer() {
        return new ComputerSet();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Descriptor getDescriptor(String id) {
        Iterable<Descriptor> descriptors = Iterators.sequence(getExtensionList(Descriptor.class), DescriptorExtensionList.listLegacyInstances());
        for (Descriptor d : descriptors) {
            if (d.getId().equals(id)) {
                return d;
            }
        }
        Descriptor candidate = null;
        for (Descriptor d : descriptors) {
            String name = d.getId();
            if (name.substring(name.lastIndexOf('.') + 1).equals(id)) {
                if (candidate == null) {
                    candidate = d;
                } else {
                    throw new IllegalArgumentException(id + " is ambiguous; matches both " + name + " and " + candidate.getId());
                }
            }
        }
        return candidate;
    }

    public Descriptor getDescriptorByName(String id) {
        return getDescriptor(id);
    }

    @CheckForNull
public Descriptor getDescriptor(Class<? extends Describable> type) {
        for (Descriptor d : getExtensionList(Descriptor.class)) if (d.clazz == type)
            return d;
        return null;
    }

    public Descriptor getDescriptorOrDie(Class<? extends Describable> type) {
        Descriptor d = getDescriptor(type);
        if (d == null)
            throw new AssertionError(type + " is missing its descriptor");
        return d;
    }

    public <T extends Descriptor> T getDescriptorByType(Class<T> type) {
        for (Descriptor d : getExtensionList(Descriptor.class)) if (d.getClass() == type)
            return type.cast(d);
        return null;
    }

    public Descriptor<SecurityRealm> getSecurityRealms(String shortClassName) {
        return findDescriptor(shortClassName, SecurityRealm.all());
    }

    private <T extends Describable<T>> Descriptor<T> findDescriptor(String shortClassName, Collection<? extends Descriptor<T>> descriptors) {
        String name = '.' + shortClassName;
        for (Descriptor<T> d : descriptors) {
            if (d.clazz.getName().endsWith(name))
                return d;
        }
        return null;
    }

    protected void updateComputerList() {
        updateComputerList(AUTOMATIC_SLAVE_LAUNCH);
    }

    @Deprecated
    public CopyOnWriteList<SCMListener> getSCMListeners() {
        return scmListeners;
    }

    public Plugin getPlugin(String shortName) {
        PluginWrapper p = pluginManager.getPlugin(shortName);
        if (p == null)
            return null;
        return p.getPlugin();
    }

    @SuppressWarnings("unchecked")
    public <P extends Plugin> P getPlugin(Class<P> clazz) {
        PluginWrapper p = pluginManager.getPlugin(clazz);
        if (p == null)
            return null;
        return (P) p.getPlugin();
    }

    public <P extends Plugin> List<P> getPlugins(Class<P> clazz) {
        List<P> result = new ArrayList<P>();
        for (PluginWrapper w : pluginManager.getPlugins(clazz)) {
            result.add((P) w.getPlugin());
        }
        return Collections.unmodifiableList(result);
    }

    public String getSystemMessage() {
        return systemMessage;
    }

    @Nonnull
    public MarkupFormatter getMarkupFormatter() {
        MarkupFormatter f = markupFormatter;
        return f != null ? f : new EscapedMarkupFormatter();
    }

    public void setMarkupFormatter(MarkupFormatter f) {
        this.markupFormatter = f;
    }

    public void setSystemMessage(String message) throws IOException {
        this.systemMessage = message;
        save();
    }

    public FederatedLoginService getFederatedLoginService(String name) {
        for (FederatedLoginService fls : FederatedLoginService.all()) {
            if (fls.getUrlName().equals(name))
                return fls;
        }
        return null;
    }

    public List<FederatedLoginService> getFederatedLoginServices() {
        return FederatedLoginService.all();
    }

    public Launcher createLauncher(TaskListener listener) {
        return new LocalLauncher(listener).decorateFor(this);
    }

    public String getFullName() {
        return "";
    }

    public String getFullDisplayName() {
        return "";
    }

    public List<Action> getActions() {
        return actions;
    }

    @Exported(name = "jobs")
    public List<TopLevelItem> getItems() {
        if (authorizationStrategy instanceof AuthorizationStrategy.Unsecured || authorizationStrategy instanceof FullControlOnceLoggedInAuthorizationStrategy) {
            return new ArrayList(items.values());
        }
        List<TopLevelItem> viewableItems = new ArrayList<TopLevelItem>();
        for (TopLevelItem item : items.values()) {
            if (item.hasPermission(Item.READ))
                viewableItems.add(item);
        }
        return viewableItems;
    }

    public Map<String, TopLevelItem> getItemMap() {
        return Collections.unmodifiableMap(items);
    }

    public <T> List<T> getItems(Class<T> type) {
        List<T> r = new ArrayList<T>();
        for (TopLevelItem i : getItems()) if (type.isInstance(i))
            r.add(type.cast(i));
        return r;
    }

    public <T extends Item> List<T> getAllItems(Class<T> type) {
        return Items.getAllItems(this, type);
    }

    public List<Item> getAllItems() {
        return getAllItems(Item.class);
    }

    @Deprecated
    public List<Project> getProjects() {
        return Util.createSubList(items.values(), Project.class);
    }

    public Collection<String> getJobNames() {
        List<String> names = new ArrayList<String>();
        for (Job j : getAllItems(Job.class)) names.add(j.getFullName());
        return names;
    }

    public List<Action> getViewActions() {
        return getActions();
    }

    public Collection<String> getTopLevelItemNames() {
        List<String> names = new ArrayList<String>();
        for (TopLevelItem j : items.values()) names.add(j.getName());
        return names;
    }

    public View getView(String name) {
        return viewGroupMixIn.getView(name);
    }

    @Exported
    public Collection<View> getViews() {
        return viewGroupMixIn.getViews();
    }

    @Override
    public void addView(View v) throws IOException {
        viewGroupMixIn.addView(v);
    }

    public void setViews(Collection<View> views) throws IOException {
        BulkChange bc = new BulkChange(this);
        try {
            this.views.clear();
            for (View v : views) {
                addView(v);
            }
        } finally {
            bc.commit();
        }
    }

    public boolean canDelete(View view) {
        return viewGroupMixIn.canDelete(view);
    }

    public synchronized void deleteView(View view) throws IOException {
        viewGroupMixIn.deleteView(view);
    }

    public void onViewRenamed(View view, String oldName, String newName) {
        viewGroupMixIn.onViewRenamed(view, oldName, newName);
    }

    @Exported
    public View getPrimaryView() {
        return viewGroupMixIn.getPrimaryView();
    }

    public void setPrimaryView(View v) {
        this.primaryView = v.getViewName();
    }

    public ViewsTabBar getViewsTabBar() {
        return viewsTabBar;
    }

    public void setViewsTabBar(ViewsTabBar viewsTabBar) {
        this.viewsTabBar = viewsTabBar;
    }

    public Jenkins getItemGroup() {
        return this;
    }

    public MyViewsTabBar getMyViewsTabBar() {
        return myViewsTabBar;
    }

    public void setMyViewsTabBar(MyViewsTabBar myViewsTabBar) {
        this.myViewsTabBar = myViewsTabBar;
    }

    public boolean isUpgradedFromBefore(VersionNumber v) {
        try {
            return new VersionNumber(version).isOlderThan(v);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public Computer[] getComputers() {
        Computer[] r = computers.values().toArray(new Computer[computers.size()]);
        Arrays.sort(r, new Comparator<Computer>() {

            @Override
            public int compare(Computer lhs, Computer rhs) {
                if (lhs.getNode() == Jenkins.this)
                    return -1;
                if (rhs.getNode() == Jenkins.this)
                    return 1;
                return lhs.getName().compareTo(rhs.getName());
            }
        });
        return r;
    }

    @CLIResolver
    @CheckForNull
    public Computer getComputer(@Argument(required = true, metaVar = "NAME", usage = "Node name") @Nonnull String name) {
        if (name.equals("(master)"))
            name = "";
        for (Computer c : computers.values()) {
            if (c.getName().equals(name))
                return c;
        }
        return null;
    }

    public Label getLabel(String expr) {
        if (expr == null)
            return null;
        expr = hudson.util.QuotedStringTokenizer.unquote(expr);
        while (true) {
            Label l = labels.get(expr);
            if (l != null)
                return l;
            try {
                labels.putIfAbsent(expr, Label.parseExpression(expr));
            } catch (ANTLRException e) {
                return getLabelAtom(expr);
            }
        }
    }

    @Nullable
    public LabelAtom getLabelAtom(@CheckForNull String name) {
        if (name == null)
            return null;
        while (true) {
            Label l = labels.get(name);
            if (l != null)
                return (LabelAtom) l;
            LabelAtom la = new LabelAtom(name);
            if (labels.putIfAbsent(name, la) == null)
                la.load();
        }
    }

    public Set<Label> getLabels() {
        Set<Label> r = new TreeSet<Label>();
        for (Label l : labels.values()) {
            if (!l.isEmpty())
                r.add(l);
        }
        return r;
    }

    public Set<LabelAtom> getLabelAtoms() {
        Set<LabelAtom> r = new TreeSet<LabelAtom>();
        for (Label l : labels.values()) {
            if (!l.isEmpty() && l instanceof LabelAtom)
                r.add((LabelAtom) l);
        }
        return r;
    }

    public Queue getQueue() {
        return queue;
    }

    @Override
    public String getDisplayName() {
        return Messages.Hudson_DisplayName();
    }

    public List<JDK> getJDKs() {
        return jdks;
    }

    @Restricted(NoExternalUse.class)
    public void setJDKs(Collection<? extends JDK> jdks) {
        this.jdks = new ArrayList<JDK>(jdks);
    }

    public JDK getJDK(String name) {
        if (name == null) {
            List<JDK> jdks = getJDKs();
            if (jdks.size() == 1)
                return jdks.get(0);
            return null;
        }
        for (JDK j : getJDKs()) {
            if (j.getName().equals(name))
                return j;
        }
        return null;
    }

    @CheckForNull
    public Node getNode(String name) {
        return nodes.getNode(name);
    }

    public Cloud getCloud(String name) {
        return clouds.getByName(name);
    }

    protected Map<Node, Computer> getComputerMap() {
        return computers;
    }

    public List<Node> getNodes() {
        return nodes.getNodes();
    }

    @Restricted(NoExternalUse.class)
    public Nodes getNodesObject() {
        return nodes;
    }

    public void addNode(Node n) throws IOException {
        nodes.addNode(n);
    }

    public void removeNode(@Nonnull Node n) throws IOException {
        nodes.removeNode(n);
    }

    public boolean updateNode(Node n) throws IOException {
        return nodes.updateNode(n);
    }

    public void setNodes(final List<? extends Node> n) throws IOException {
        nodes.setNodes(n);
    }

    public DescribableList<NodeProperty<?>, NodePropertyDescriptor> getNodeProperties() {
        return nodeProperties;
    }

    public DescribableList<NodeProperty<?>, NodePropertyDescriptor> getGlobalNodeProperties() {
        return globalNodeProperties;
    }

    void trimLabels() {
        for (Iterator<Label> itr = labels.values().iterator(); itr.hasNext(); ) {
            Label l = itr.next();
            resetLabel(l);
            if (l.isEmpty())
                itr.remove();
        }
    }

    public AdministrativeMonitor getAdministrativeMonitor(String id) {
        for (AdministrativeMonitor m : administrativeMonitors) if (m.id.equals(id))
            return m;
        return null;
    }

    public NodeDescriptor getDescriptor() {
        return DescriptorImpl.INSTANCE;
    }

    public static final class DescriptorImpl extends NodeDescriptor {

        @Extension
        public static final DescriptorImpl INSTANCE = new DescriptorImpl();

        @Override
        public boolean isInstantiable() {
            return false;
        }

        public FormValidation doCheckNumExecutors(@QueryParameter String value) {
            return FormValidation.validateNonNegativeInteger(value);
        }

        public FormValidation doCheckRawBuildsDir(@QueryParameter String value) {
            String replacedValue = expandVariablesForDirectory(value, "doCheckRawBuildsDir-Marker:foo", Jenkins.getInstance().getRootDir().getPath() + "/jobs/doCheckRawBuildsDir-Marker$foo");
            File replacedFile = new File(replacedValue);
            if (!replacedFile.isAbsolute()) {
                return FormValidation.error(value + " does not resolve to an absolute path");
            }
            if (!replacedValue.contains("doCheckRawBuildsDir-Marker")) {
                return FormValidation.error(value + " does not contain ${ITEM_FULL_NAME} or ${ITEM_ROOTDIR}, cannot distinguish between projects");
            }
            if (replacedValue.contains("doCheckRawBuildsDir-Marker:foo")) {
                try {
                    File tmp = File.createTempFile("Jenkins-doCheckRawBuildsDir", "foo:bar");
                    tmp.delete();
                } catch (IOException e) {
                    return FormValidation.error(value + " contains ${ITEM_FULLNAME} but your system does not support it (JENKINS-12251). Use ${ITEM_FULL_NAME} instead");
                }
            }
            File d = new File(replacedValue);
            if (!d.isDirectory()) {
                d = d.getParentFile();
                while (!d.exists()) {
                    d = d.getParentFile();
                }
                if (!d.canWrite()) {
                    return FormValidation.error(value + " does not exist and probably cannot be created");
                }
            }
            return FormValidation.ok();
        }

        public Object getDynamic(String token) {
            return Jenkins.getInstance().getDescriptor(token);
        }
    }

    public int getQuietPeriod() {
        return quietPeriod != null ? quietPeriod : 5;
    }

    public void setQuietPeriod(Integer quietPeriod) throws IOException {
        this.quietPeriod = quietPeriod;
        save();
    }

    public int getScmCheckoutRetryCount() {
        return scmCheckoutRetryCount;
    }

    public void setScmCheckoutRetryCount(int scmCheckoutRetryCount) throws IOException {
        this.scmCheckoutRetryCount = scmCheckoutRetryCount;
        save();
    }

    @Override
    public String getSearchUrl() {
        return "";
    }

    @Override
    public SearchIndexBuilder makeSearchIndex() {
        return super.makeSearchIndex().add("configure", "config", "configure").add("manage").add("log").add(new CollectionSearchIndex<TopLevelItem>() {

            protected SearchItem get(String key) {
                return getItemByFullName(key, TopLevelItem.class);
            }

            protected Collection<TopLevelItem> all() {
                return getAllItems(TopLevelItem.class);
            }
        }).add(getPrimaryView().makeSearchIndex()).add(new CollectionSearchIndex() {

            protected Computer get(String key) {
                return getComputer(key);
            }

            protected Collection<Computer> all() {
                return computers.values();
            }
        }).add(new CollectionSearchIndex() {

            protected User get(String key) {
                return User.get(key, false);
            }

            protected Collection<User> all() {
                return User.getAll();
            }
        }).add(new CollectionSearchIndex() {

            protected View get(String key) {
                return getView(key);
            }

            protected Collection<View> all() {
                return views;
            }
        });
    }

    public String getUrlChildPrefix() {
        return "job";
    }

    @Nullable
    public String getRootUrl() {
        String url = JenkinsLocationConfiguration.get().getUrl();
        if (url != null) {
            return Util.ensureEndsWith(url, "/");
        }
        StaplerRequest req = Stapler.getCurrentRequest();
        if (req != null)
            return getRootUrlFromRequest();
        return null;
    }

    public boolean isRootUrlSecure() {
        String url = getRootUrl();
        return url != null && url.startsWith("https");
    }

    @Nonnull
    public String getRootUrlFromRequest() {
        StaplerRequest req = Stapler.getCurrentRequest();
        if (req == null) {
            throw new IllegalStateException("cannot call getRootUrlFromRequest from outside a request handling thread");
        }
        StringBuilder buf = new StringBuilder();
        String scheme = getXForwardedHeader(req, "X-Forwarded-Proto", req.getScheme());
        buf.append(scheme).append("://");
        String host = getXForwardedHeader(req, "X-Forwarded-Host", req.getServerName());
        int index = host.indexOf(':');
        int port = req.getServerPort();
        if (index == -1) {
            buf.append(host);
        } else {
            buf.append(host.substring(0, index));
            if (index + 1 < host.length()) {
                try {
                    port = Integer.parseInt(host.substring(index + 1));
                } catch (NumberFormatException e) {
                }
            }
        }
        String forwardedPort = getXForwardedHeader(req, "X-Forwarded-Port", null);
        if (forwardedPort != null) {
            try {
                port = Integer.parseInt(forwardedPort);
            } catch (NumberFormatException e) {
            }
        }
        if (port != ("https".equals(scheme) ? 443 : 80)) {
            buf.append(':').append(port);
        }
        buf.append(req.getContextPath()).append('/');
        return buf.toString();
    }

    private static String getXForwardedHeader(StaplerRequest req, String header, String defaultValue) {
        String value = req.getHeader(header);
        if (value != null) {
            int index = value.indexOf(',');
            return index == -1 ? value.trim() : value.substring(0, index).trim();
        }
        return defaultValue;
    }

    public File getRootDir() {
        return root;
    }

    public FilePath getWorkspaceFor(TopLevelItem item) {
        for (WorkspaceLocator l : WorkspaceLocator.all()) {
            FilePath workspace = l.locate(item, this);
            if (workspace != null) {
                return workspace;
            }
        }
        return new FilePath(expandVariablesForDirectory(workspaceDir, item));
    }

    public File getBuildDirFor(Job job) {
        return expandVariablesForDirectory(buildsDir, job);
    }

    private File expandVariablesForDirectory(String base, Item item) {
        return new File(expandVariablesForDirectory(base, item.getFullName(), item.getRootDir().getPath()));
    }

    @Restricted(NoExternalUse.class)
    static String expandVariablesForDirectory(String base, String itemFullName, String itemRootDir) {
        return Util.replaceMacro(base, ImmutableMap.of("JENKINS_HOME", Jenkins.getInstance().getRootDir().getPath(), "ITEM_ROOTDIR", itemRootDir, "ITEM_FULLNAME", itemFullName, "ITEM_FULL_NAME", itemFullName.replace(':', '$')));
    }

    public String getRawWorkspaceDir() {
        return workspaceDir;
    }

    public String getRawBuildsDir() {
        return buildsDir;
    }

    @Restricted(NoExternalUse.class)
    public void setRawBuildsDir(String buildsDir) {
        this.buildsDir = buildsDir;
    }

    @Override
    @Nonnull
    public FilePath getRootPath() {
        return new FilePath(getRootDir());
    }

    @Override
    public FilePath createPath(String absolutePath) {
        return new FilePath((VirtualChannel) null, absolutePath);
    }

    public ClockDifference getClockDifference() {
        return ClockDifference.ZERO;
    }

    @Override
    public Callable<ClockDifference, IOException> getClockDifferenceCallable() {
        return new MasterToSlaveCallable<ClockDifference, IOException>() {

            public ClockDifference call() throws IOException {
                return new ClockDifference(0);
            }
        };
    }

    public LogRecorderManager getLog() {
        checkPermission(ADMINISTER);
        return log;
    }

    @Exported
    public boolean isUseSecurity() {
        return securityRealm != SecurityRealm.NO_AUTHENTICATION || authorizationStrategy != AuthorizationStrategy.UNSECURED;
    }

    public boolean isUseProjectNamingStrategy() {
        return projectNamingStrategy != DefaultProjectNamingStrategy.DEFAULT_NAMING_STRATEGY;
    }

    @Exported
    public boolean isUseCrumbs() {
        return crumbIssuer != null;
    }

    public SecurityMode getSecurity() {
        SecurityRealm realm = securityRealm;
        if (realm == SecurityRealm.NO_AUTHENTICATION)
            return SecurityMode.UNSECURED;
        if (realm instanceof LegacySecurityRealm)
            return SecurityMode.LEGACY;
        return SecurityMode.SECURED;
    }

    public SecurityRealm getSecurityRealm() {
        return securityRealm;
    }

    public void setSecurityRealm(SecurityRealm securityRealm) {
        if (securityRealm == null)
            securityRealm = SecurityRealm.NO_AUTHENTICATION;
        this.useSecurity = true;
        IdStrategy oldUserIdStrategy = this.securityRealm == null ? securityRealm.getUserIdStrategy() : this.securityRealm.getUserIdStrategy();
        this.securityRealm = securityRealm;
        try {
            HudsonFilter filter = HudsonFilter.get(servletContext);
            if (filter == null) {
                LOGGER.fine("HudsonFilter has not yet been initialized: Can't perform security setup for now");
            } else {
                LOGGER.fine("HudsonFilter has been previously initialized: Setting security up");
                filter.reset(securityRealm);
                LOGGER.fine("Security is now fully set up");
            }
            if (!oldUserIdStrategy.equals(this.securityRealm.getUserIdStrategy())) {
                User.rekey();
            }
        } catch (ServletException e) {
            throw new AcegiSecurityException("Failed to configure filter", e) {
            };
        }
    }

    public void setAuthorizationStrategy(AuthorizationStrategy a) {
        if (a == null)
            a = AuthorizationStrategy.UNSECURED;
        useSecurity = true;
        authorizationStrategy = a;
    }

    public boolean isDisableRememberMe() {
        return disableRememberMe;
    }

    public void setDisableRememberMe(boolean disableRememberMe) {
        this.disableRememberMe = disableRememberMe;
    }

    public void disableSecurity() {
        useSecurity = null;
        setSecurityRealm(SecurityRealm.NO_AUTHENTICATION);
        authorizationStrategy = AuthorizationStrategy.UNSECURED;
    }

    public void setProjectNamingStrategy(ProjectNamingStrategy ns) {
        if (ns == null) {
            ns = DefaultProjectNamingStrategy.DEFAULT_NAMING_STRATEGY;
        }
        projectNamingStrategy = ns;
    }

    public Lifecycle getLifecycle() {
        return Lifecycle.get();
    }

    public Injector getInjector() {
        return lookup(Injector.class);
    }

    @SuppressWarnings({ "unchecked" })
    public <T> ExtensionList<T> getExtensionList(Class<T> extensionType) {
        return extensionLists.get(extensionType);
    }

    public ExtensionList getExtensionList(String extensionType) throws ClassNotFoundException {
        return getExtensionList(pluginManager.uberClassLoader.loadClass(extensionType));
    }

    @SuppressWarnings({ "unchecked" })
    public <T extends Describable<T>, D extends Descriptor<T>> DescriptorExtensionList<T, D> getDescriptorList(Class<T> type) {
        return descriptorLists.get(type);
    }

    public void refreshExtensions() throws ExtensionRefreshException {
        ExtensionList<ExtensionFinder> finders = getExtensionList(ExtensionFinder.class);
        for (ExtensionFinder ef : finders) {
            if (!ef.isRefreshable())
                throw new ExtensionRefreshException(ef + " doesn't support refresh");
        }
        List<ExtensionComponentSet> fragments = Lists.newArrayList();
        for (ExtensionFinder ef : finders) {
            fragments.add(ef.refresh());
        }
        ExtensionComponentSet delta = ExtensionComponentSet.union(fragments).filtered();
        List<ExtensionComponent<ExtensionFinder>> newFinders = Lists.newArrayList(delta.find(ExtensionFinder.class));
        while (!newFinders.isEmpty()) {
            ExtensionFinder f = newFinders.remove(newFinders.size() - 1).getInstance();
            ExtensionComponentSet ecs = ExtensionComponentSet.allOf(f).filtered();
            newFinders.addAll(ecs.find(ExtensionFinder.class));
            delta = ExtensionComponentSet.union(delta, ecs);
        }
        for (ExtensionList el : extensionLists.values()) {
            el.refresh(delta);
        }
        for (ExtensionList el : descriptorLists.values()) {
            el.refresh(delta);
        }
        for (ExtensionComponent<RootAction> ea : delta.find(RootAction.class)) {
            Action a = ea.getInstance();
            if (!actions.contains(a))
                actions.add(a);
        }
    }

    @Override
    public ACL getACL() {
        return authorizationStrategy.getRootACL();
    }

    public AuthorizationStrategy getAuthorizationStrategy() {
        return authorizationStrategy;
    }

    public ProjectNamingStrategy getProjectNamingStrategy() {
        return projectNamingStrategy == null ? ProjectNamingStrategy.DEFAULT_NAMING_STRATEGY : projectNamingStrategy;
    }

    @Exported
    public boolean isQuietingDown() {
        return isQuietingDown;
    }

    public boolean isTerminating() {
        return terminating;
    }

    public InitMilestone getInitLevel() {
        return initLevel;
    }

    public void setNumExecutors(int n) throws IOException {
        if (this.numExecutors != n) {
            this.numExecutors = n;
            updateComputerList();
            save();
        }
    }

    @Override
    public TopLevelItem getItem(String name) throws AccessDeniedException {
        if (name == null)
            return null;
        TopLevelItem item = items.get(name);
        if (item == null)
            return null;
        if (!item.hasPermission(Item.READ)) {
            if (item.hasPermission(Item.DISCOVER)) {
                throw new AccessDeniedException("Please login to access job " + name);
            }
            return null;
        }
        return item;
    }

    public Item getItem(String pathName, ItemGroup context) {
        if (context == null)
            context = this;
        if (pathName == null)
            return null;
        if (pathName.startsWith("/"))
            return getItemByFullName(pathName);
        Object ctx = context;
        StringTokenizer tokens = new StringTokenizer(pathName, "/");
        while (tokens.hasMoreTokens()) {
            String s = tokens.nextToken();
            if (s.equals("..")) {
                if (ctx instanceof Item) {
                    ctx = ((Item) ctx).getParent();
                    continue;
                }
                ctx = null;
                break;
            }
            if (s.equals(".")) {
                continue;
            }
            if (ctx instanceof ItemGroup) {
                ItemGroup g = (ItemGroup) ctx;
                Item i = g.getItem(s);
                if (i == null || !i.hasPermission(Item.READ)) {
                    ctx = null;
                    break;
                }
                ctx = i;
            } else {
                return null;
            }
        }
        if (ctx instanceof Item)
            return (Item) ctx;
        return getItemByFullName(pathName);
    }

    public final Item getItem(String pathName, Item context) {
        return getItem(pathName, context != null ? context.getParent() : null);
    }

    public final <T extends Item> T getItem(String pathName, ItemGroup context, @Nonnull Class<T> type) {
        Item r = getItem(pathName, context);
        if (type.isInstance(r))
            return type.cast(r);
        return null;
    }

    public final <T extends Item> T getItem(String pathName, Item context, Class<T> type) {
        return getItem(pathName, context != null ? context.getParent() : null, type);
    }

    public File getRootDirFor(TopLevelItem child) {
        return getRootDirFor(child.getName());
    }

    private File getRootDirFor(String name) {
        return new File(new File(getRootDir(), "jobs"), name);
    }

    @CheckForNull
    public <T extends Item> T getItemByFullName(String fullName, Class<T> type) throws AccessDeniedException {
        StringTokenizer tokens = new StringTokenizer(fullName, "/");
        ItemGroup parent = this;
        if (!tokens.hasMoreTokens())
            return null;
        while (true) {
            Item item = parent.getItem(tokens.nextToken());
            if (!tokens.hasMoreTokens()) {
                if (type.isInstance(item))
                    return type.cast(item);
                else
                    return null;
            }
            if (!(item instanceof ItemGroup))
                return null;
            if (!item.hasPermission(Item.READ))
                return null;
            parent = (ItemGroup) item;
        }
    }

    @CheckForNull
    public Item getItemByFullName(String fullName) {
        return getItemByFullName(fullName, Item.class);
    }

    @CheckForNull
    public User getUser(String name) {
        return User.get(name, hasPermission(ADMINISTER));
    }

    public synchronized TopLevelItem createProject(TopLevelItemDescriptor type, String name) throws IOException {
        return createProject(type, name, true);
    }

    public synchronized TopLevelItem createProject(TopLevelItemDescriptor type, String name, boolean notify) throws IOException {
        return itemGroupMixIn.createProject(type, name, notify);
    }

    public synchronized void putItem(TopLevelItem item) throws IOException, InterruptedException {
        String name = item.getName();
        TopLevelItem old = items.get(name);
        if (old == item)
            return;
        checkPermission(Item.CREATE);
        if (old != null)
            old.delete();
        items.put(name, item);
        ItemListener.fireOnCreated(item);
    }

    public synchronized <T extends TopLevelItem> T createProject(Class<T> type, String name) throws IOException {
        return type.cast(createProject((TopLevelItemDescriptor) getDescriptor(type), name));
    }

    public void onRenamed(TopLevelItem job, String oldName, String newName) throws IOException {
        items.remove(oldName);
        items.put(newName, job);
        for (View v : views) v.onJobRenamed(job, oldName, newName);
    }

    public void onDeleted(TopLevelItem item) throws IOException {
        ItemListener.fireOnDeleted(item);
        items.remove(item.getName());
        for (View v : views) v.onJobRenamed(item, item.getName(), null);
    }

    @Override
    public boolean canAdd(TopLevelItem item) {
        return true;
    }

    @Override
    synchronized public <I extends TopLevelItem> I add(I item, String name) throws IOException, IllegalArgumentException {
        if (items.containsKey(name)) {
            throw new IllegalArgumentException("already an item '" + name + "'");
        }
        items.put(name, item);
        return item;
    }

    @Override
    public void remove(TopLevelItem item) throws IOException, IllegalArgumentException {
        items.remove(item.getName());
    }

    public FingerprintMap getFingerprintMap() {
        return fingerprintMap;
    }

    public Object getFingerprint(String md5sum) throws IOException {
        Fingerprint r = fingerprintMap.get(md5sum);
        if (r == null)
            return new NoFingerprintMatch(md5sum);
        else
            return r;
    }

    public Fingerprint _getFingerprint(String md5sum) throws IOException {
        return fingerprintMap.get(md5sum);
    }

    private XmlFile getConfigFile() {
        return new XmlFile(XSTREAM, new File(root, "config.xml"));
    }

    public int getNumExecutors() {
        return numExecutors;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode m) throws IOException {
        this.mode = m;
        save();
    }

    public String getLabelString() {
        return fixNull(label).trim();
    }

    @Override
    public void setLabelString(String label) throws IOException {
        this.label = label;
        save();
    }

    @Override
    public LabelAtom getSelfLabel() {
        return getLabelAtom("master");
    }

    public Computer createComputer() {
        return new Hudson.MasterComputer();
    }

    private void loadConfig() throws IOException {
        XmlFile cfg = getConfigFile();
        if (cfg.exists()) {
            primaryView = null;
            views.clear();
            cfg.unmarshal(Jenkins.this);
        }
    }

    private synchronized TaskBuilder loadTasks() throws IOException {
        File projectsDir = new File(root, "jobs");
        if (!projectsDir.getCanonicalFile().isDirectory() && !projectsDir.mkdirs()) {
            if (projectsDir.exists())
                throw new IOException(projectsDir + " is not a directory");
            throw new IOException("Unable to create " + projectsDir + "\nPermission issue? Please create this directory manually.");
        }
        File[] subdirs = projectsDir.listFiles();
        final Set<String> loadedNames = Collections.synchronizedSet(new HashSet<String>());
        TaskGraphBuilder g = new TaskGraphBuilder();
        Handle loadJenkins = g.requires(EXTENSIONS_AUGMENTED).attains(JOB_LOADED).add("Loading global config", new Executable() {

            public void run(Reactor session) throws Exception {
                loadConfig();
                if (slaves != null && !slaves.isEmpty() && nodes.isLegacy()) {
                    nodes.setNodes(slaves);
                    slaves = null;
                } else {
                    nodes.load();
                }
                clouds.setOwner(Jenkins.this);
            }
        });
        for (final File subdir : subdirs) {
            g.requires(loadJenkins).attains(JOB_LOADED).notFatal().add("Loading job " + subdir.getName(), new Executable() {

                public void run(Reactor session) throws Exception {
                    if (!Items.getConfigFile(subdir).exists()) {
                        return;
                    }
                    TopLevelItem item = (TopLevelItem) Items.load(Jenkins.this, subdir);
                    items.put(item.getName(), item);
                    loadedNames.add(item.getName());
                }
            });
        }
        g.requires(JOB_LOADED).add("Cleaning up old builds", new Executable() {

            public void run(Reactor reactor) throws Exception {
                for (String name : items.keySet()) {
                    if (!loadedNames.contains(name))
                        items.remove(name);
                }
            }
        });
        g.requires(JOB_LOADED).add("Finalizing set up", new Executable() {

            public void run(Reactor session) throws Exception {
                rebuildDependencyGraph();
                {
                    for (Node slave : nodes.getNodes()) slave.getAssignedLabels();
                    getAssignedLabels();
                }
                if (views.size() == 0 || primaryView == null) {
                    View v = new AllView(Messages.Hudson_ViewName());
                    setViewOwner(v);
                    views.add(0, v);
                    primaryView = v.getViewName();
                }
                if (useSecurity != null && !useSecurity) {
                    authorizationStrategy = AuthorizationStrategy.UNSECURED;
                    setSecurityRealm(SecurityRealm.NO_AUTHENTICATION);
                } else {
                    if (authorizationStrategy == null) {
                        if (useSecurity == null)
                            authorizationStrategy = AuthorizationStrategy.UNSECURED;
                        else
                            authorizationStrategy = new LegacyAuthorizationStrategy();
                    }
                    if (securityRealm == null) {
                        if (useSecurity == null)
                            setSecurityRealm(SecurityRealm.NO_AUTHENTICATION);
                        else
                            setSecurityRealm(new LegacySecurityRealm());
                    } else {
                        setSecurityRealm(securityRealm);
                    }
                }
                setCrumbIssuer(crumbIssuer);
                for (Action a : getExtensionList(RootAction.class)) if (!actions.contains(a))
                    actions.add(a);
            }
        });
        return g;
    }

    public synchronized void save() throws IOException {
        if (BulkChange.contains(this))
            return;
        getConfigFile().write(this);
        SaveableListener.fireOnChange(this, getConfigFile());
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public void cleanUp() {
        if (theInstance != this && theInstance != null) {
            LOGGER.log(Level.WARNING, "This instance is no longer the singleton, ignoring cleanUp()");
            return;
        }
        synchronized (Jenkins.class) {
            if (cleanUpStarted) {
                LOGGER.log(Level.WARNING, "Jenkins.cleanUp() already started, ignoring repeated cleanUp()");
                return;
            }
            cleanUpStarted = true;
        }
        try {
            LOGGER.log(Level.INFO, "Stopping Jenkins");
            final List<Throwable> errors = new ArrayList<>();
            fireBeforeShutdown(errors);
            _cleanUpRunTerminators(errors);
            terminating = true;
            final Set<Future<?>> pending = _cleanUpDisconnectComputers(errors);
            _cleanUpShutdownUDPBroadcast(errors);
            _cleanUpCloseDNSMulticast(errors);
            _cleanUpInterruptReloadThread(errors);
            _cleanUpShutdownTriggers(errors);
            _cleanUpShutdownTimer(errors);
            _cleanUpShutdownTcpSlaveAgent(errors);
            _cleanUpShutdownPluginManager(errors);
            _cleanUpPersistQueue(errors);
            _cleanUpShutdownThreadPoolForLoad(errors);
            _cleanUpAwaitDisconnects(errors, pending);
            _cleanUpPluginServletFilters(errors);
            _cleanUpReleaseAllLoggers(errors);
            LOGGER.log(Level.INFO, "Jenkins stopped");
            if (!errors.isEmpty()) {
                StringBuilder message = new StringBuilder("Unexpected issues encountered during cleanUp: ");
                Iterator<Throwable> iterator = errors.iterator();
                message.append(iterator.next().getMessage());
                while (iterator.hasNext()) {
                    message.append("; ");
                    message.append(iterator.next().getMessage());
                }
                iterator = errors.iterator();
                RuntimeException exception = new RuntimeException(message.toString(), iterator.next());
                while (iterator.hasNext()) {
                    exception.addSuppressed(iterator.next());
                }
                throw exception;
            }
        } finally {
            theInstance = null;
            if (JenkinsJVM.isJenkinsJVM()) {
                JenkinsJVMAccess._setJenkinsJVM(oldJenkinsJVM);
            }
        }
    }

    private void fireBeforeShutdown(List<Throwable> errors) {
        LOGGER.log(Level.FINE, "Notifying termination");
        for (ItemListener l : ItemListener.all()) {
            try {
                l.onBeforeShutdown();
            } catch (OutOfMemoryError e) {
                throw e;
            } catch (LinkageError e) {
                LOGGER.log(Level.WARNING, "ItemListener " + l + ": " + e.getMessage(), e);
            } catch (Throwable e) {
                LOGGER.log(Level.WARNING, "ItemListener " + l + ": " + e.getMessage(), e);
                errors.add(e);
            }
        }
    }

    private void _cleanUpRunTerminators(List<Throwable> errors) {
        try {
            final TerminatorFinder tf = new TerminatorFinder(pluginManager != null ? pluginManager.uberClassLoader : Thread.currentThread().getContextClassLoader());
            new Reactor(tf).execute(new Executor() {

                @Override
                public void execute(Runnable command) {
                    command.run();
                }
            }, new ReactorListener() {

                final Level level = Level.parse(Configuration.getStringConfigParameter("termLogLevel", "FINE"));

                public void onTaskStarted(Task t) {
                    LOGGER.log(level, "Started {0}", InitReactorRunner.getDisplayName(t));
                }

                public void onTaskCompleted(Task t) {
                    LOGGER.log(level, "Completed {0}", InitReactorRunner.getDisplayName(t));
                }

                public void onTaskFailed(Task t, Throwable err, boolean fatal) {
                    LOGGER.log(SEVERE, "Failed " + InitReactorRunner.getDisplayName(t), err);
                }

                public void onAttained(Milestone milestone) {
                    Level lv = level;
                    String s = "Attained " + milestone.toString();
                    if (milestone instanceof TermMilestone) {
                        lv = Level.INFO;
                        s = milestone.toString();
                    }
                    LOGGER.log(lv, s);
                }
            });
        } catch (InterruptedException | ReactorException | IOException e) {
            LOGGER.log(SEVERE, "Failed to execute termination", e);
            errors.add(e);
        } catch (OutOfMemoryError e) {
            throw e;
        } catch (LinkageError e) {
            LOGGER.log(SEVERE, "Failed to execute termination", e);
        } catch (Throwable e) {
            LOGGER.log(SEVERE, "Failed to execute termination", e);
            errors.add(e);
        }
    }

    private Set<Future<?>> _cleanUpDisconnectComputers(final List<Throwable> errors) {
        LOGGER.log(Level.INFO, "Starting node disconnection");
        final Set<Future<?>> pending = new HashSet<Future<?>>();
        Queue.withLock(new Runnable() {

            @Override
            public void run() {
                for (Computer c : computers.values()) {
                    try {
                        c.interrupt();
                        killComputer(c);
                        pending.add(c.disconnect(null));
                    } catch (OutOfMemoryError e) {
                        throw e;
                    } catch (LinkageError e) {
                        LOGGER.log(Level.WARNING, "Could not disconnect " + c + ": " + e.getMessage(), e);
                    } catch (Throwable e) {
                        LOGGER.log(Level.WARNING, "Could not disconnect " + c + ": " + e.getMessage(), e);
                        errors.add(e);
                    }
                }
            }
        });
        return pending;
    }

    private void _cleanUpShutdownUDPBroadcast(List<Throwable> errors) {
        if (udpBroadcastThread != null) {
            LOGGER.log(Level.FINE, "Shutting down {0}", udpBroadcastThread.getName());
            try {
                udpBroadcastThread.shutdown();
            } catch (OutOfMemoryError e) {
                throw e;
            } catch (LinkageError e) {
                LOGGER.log(SEVERE, "Failed to shutdown UDP Broadcast Thread", e);
            } catch (Throwable e) {
                LOGGER.log(SEVERE, "Failed to shutdown UDP Broadcast Thread", e);
                errors.add(e);
            }
        }
    }

    private void _cleanUpCloseDNSMulticast(List<Throwable> errors) {
        if (dnsMultiCast != null) {
            LOGGER.log(Level.FINE, "Closing DNS Multicast service");
            try {
                dnsMultiCast.close();
            } catch (OutOfMemoryError e) {
                throw e;
            } catch (LinkageError e) {
                LOGGER.log(SEVERE, "Failed to close DNS Multicast service", e);
            } catch (Throwable e) {
                LOGGER.log(SEVERE, "Failed to close DNS Multicast service", e);
                errors.add(e);
            }
        }
    }

    private void _cleanUpInterruptReloadThread(List<Throwable> errors) {
        LOGGER.log(Level.FINE, "Interrupting reload thread");
        try {
            interruptReloadThread();
        } catch (SecurityException e) {
            LOGGER.log(WARNING, "Not permitted to interrupt reload thread", e);
            errors.add(e);
        } catch (OutOfMemoryError e) {
            throw e;
        } catch (LinkageError e) {
            LOGGER.log(SEVERE, "Failed to interrupt reload thread", e);
        } catch (Throwable e) {
            LOGGER.log(SEVERE, "Failed to interrupt reload thread", e);
            errors.add(e);
        }
    }

    private void _cleanUpShutdownTriggers(List<Throwable> errors) {
        LOGGER.log(Level.FINE, "Shutting down triggers");
        try {
            final java.util.Timer timer = Trigger.timer;
            if (timer != null) {
                final CountDownLatch latch = new CountDownLatch(1);
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        timer.cancel();
                        latch.countDown();
                    }
                }, 0);
                if (latch.await(10, TimeUnit.SECONDS)) {
                    LOGGER.log(Level.FINE, "Triggers shut down successfully");
                } else {
                    timer.cancel();
                    LOGGER.log(Level.INFO, "Gave up waiting for triggers to finish running");
                }
            }
            Trigger.timer = null;
        } catch (OutOfMemoryError e) {
            throw e;
        } catch (LinkageError e) {
            LOGGER.log(SEVERE, "Failed to shut down triggers", e);
        } catch (Throwable e) {
            LOGGER.log(SEVERE, "Failed to shut down triggers", e);
            errors.add(e);
        }
    }

    private void _cleanUpShutdownTimer(List<Throwable> errors) {
        LOGGER.log(Level.FINE, "Shutting down timer");
        try {
            Timer.shutdown();
        } catch (SecurityException e) {
            LOGGER.log(WARNING, "Not permitted to shut down Timer", e);
            errors.add(e);
        } catch (OutOfMemoryError e) {
            throw e;
        } catch (LinkageError e) {
            LOGGER.log(SEVERE, "Failed to shut down Timer", e);
        } catch (Throwable e) {
            LOGGER.log(SEVERE, "Failed to shut down Timer", e);
            errors.add(e);
        }
    }

    private void _cleanUpShutdownTcpSlaveAgent(List<Throwable> errors) {
        if (tcpSlaveAgentListener != null) {
            LOGGER.log(FINE, "Shutting down TCP/IP slave agent listener");
            try {
                tcpSlaveAgentListener.shutdown();
            } catch (OutOfMemoryError e) {
                throw e;
            } catch (LinkageError e) {
                LOGGER.log(SEVERE, "Failed to shut down TCP/IP slave agent listener", e);
            } catch (Throwable e) {
                LOGGER.log(SEVERE, "Failed to shut down TCP/IP slave agent listener", e);
                errors.add(e);
            }
        }
    }

    private void _cleanUpShutdownPluginManager(List<Throwable> errors) {
        if (pluginManager != null) {
            LOGGER.log(Level.INFO, "Stopping plugin manager");
            try {
                pluginManager.stop();
            } catch (OutOfMemoryError e) {
                throw e;
            } catch (LinkageError e) {
                LOGGER.log(SEVERE, "Failed to stop plugin manager", e);
            } catch (Throwable e) {
                LOGGER.log(SEVERE, "Failed to stop plugin manager", e);
                errors.add(e);
            }
        }
    }

    private void _cleanUpPersistQueue(List<Throwable> errors) {
        if (getRootDir().exists()) {
            LOGGER.log(Level.INFO, "Persisting build queue");
            try {
                getQueue().save();
            } catch (OutOfMemoryError e) {
                throw e;
            } catch (LinkageError e) {
                LOGGER.log(SEVERE, "Failed to persist build queue", e);
            } catch (Throwable e) {
                LOGGER.log(SEVERE, "Failed to persist build queue", e);
                errors.add(e);
            }
        }
    }

    private void _cleanUpShutdownThreadPoolForLoad(List<Throwable> errors) {
        LOGGER.log(FINE, "Shuting down Jenkins load thread pool");
        try {
            threadPoolForLoad.shutdown();
        } catch (SecurityException e) {
            LOGGER.log(WARNING, "Not permitted to shut down Jenkins load thread pool", e);
            errors.add(e);
        } catch (OutOfMemoryError e) {
            throw e;
        } catch (LinkageError e) {
            LOGGER.log(SEVERE, "Failed to shut down Jenkins load thread pool", e);
        } catch (Throwable e) {
            LOGGER.log(SEVERE, "Failed to shut down Jenkins load thread pool", e);
            errors.add(e);
        }
    }

    private void _cleanUpAwaitDisconnects(List<Throwable> errors, Set<Future<?>> pending) {
        if (!pending.isEmpty()) {
            LOGGER.log(Level.INFO, "Waiting for node disconnection completion");
        }
        for (Future<?> f : pending) {
            try {
                f.get(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (ExecutionException e) {
                LOGGER.log(Level.WARNING, "Failed to shut down remote computer connection cleanly", e);
            } catch (TimeoutException e) {
                LOGGER.log(Level.WARNING, "Failed to shut down remote computer connection within 10 seconds", e);
            } catch (OutOfMemoryError e) {
                throw e;
            } catch (LinkageError e) {
                LOGGER.log(Level.WARNING, "Failed to shut down remote computer connection", e);
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, "Unexpected error while waiting for remote computer connection disconnect", e);
                errors.add(e);
            }
        }
    }

    private void _cleanUpPluginServletFilters(List<Throwable> errors) {
        LOGGER.log(Level.FINE, "Stopping filters");
        try {
            PluginServletFilter.cleanUp();
        } catch (OutOfMemoryError e) {
            throw e;
        } catch (LinkageError e) {
            LOGGER.log(SEVERE, "Failed to stop filters", e);
        } catch (Throwable e) {
            LOGGER.log(SEVERE, "Failed to stop filters", e);
            errors.add(e);
        }
    }

    private void _cleanUpReleaseAllLoggers(List<Throwable> errors) {
        LOGGER.log(Level.FINE, "Releasing all loggers");
        try {
            LogFactory.releaseAll();
        } catch (OutOfMemoryError e) {
            throw e;
        } catch (LinkageError e) {
            LOGGER.log(SEVERE, "Failed to release all loggers", e);
        } catch (Throwable e) {
            LOGGER.log(SEVERE, "Failed to release all loggers", e);
            errors.add(e);
        }
    }

    public Object getDynamic(String token) {
        for (Action a : getActions()) {
            String url = a.getUrlName();
            if (url == null)
                continue;
            if (url.equals(token) || url.equals('/' + token))
                return a;
        }
        for (Action a : getManagementLinks()) if (Objects.equals(a.getUrlName(), token))
            return a;
        return null;
    }

    public synchronized void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, FormException {
        BulkChange bc = new BulkChange(this);
        try {
            checkPermission(ADMINISTER);
            JSONObject json = req.getSubmittedForm();
            workspaceDir = json.getString("rawWorkspaceDir");
            buildsDir = json.getString("rawBuildsDir");
            systemMessage = Util.nullify(req.getParameter("system_message"));
            boolean result = true;
            for (Descriptor<?> d : Functions.getSortedDescriptorsForGlobalConfigUnclassified()) result &= configureDescriptor(req, json, d);
            version = VERSION;
            save();
            updateComputerList();
            if (result)
                FormApply.success(req.getContextPath() + '/').generateResponse(req, rsp, null);
            else
                FormApply.success("configure").generateResponse(req, rsp, null);
        } finally {
            bc.commit();
        }
    }

    public CrumbIssuer getCrumbIssuer() {
        return crumbIssuer;
    }

    public void setCrumbIssuer(CrumbIssuer issuer) {
        crumbIssuer = issuer;
    }

    public synchronized void doTestPost(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        rsp.sendRedirect("foo");
    }

    private boolean configureDescriptor(StaplerRequest req, JSONObject json, Descriptor<?> d) throws FormException {
        String name = d.getJsonSafeClassName();
        JSONObject js = json.has(name) ? json.getJSONObject(name) : new JSONObject();
        json.putAll(js);
        return d.configure(req, js);
    }

    @RequirePOST
    public synchronized void doConfigExecutorsSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, FormException {
        checkPermission(ADMINISTER);
        BulkChange bc = new BulkChange(this);
        try {
            JSONObject json = req.getSubmittedForm();
            MasterBuildConfiguration mbc = MasterBuildConfiguration.all().get(MasterBuildConfiguration.class);
            if (mbc != null)
                mbc.configure(req, json);
            getNodeProperties().rebuild(req, json.optJSONObject("nodeProperties"), NodeProperty.all());
        } finally {
            bc.commit();
        }
        updateComputerList();
        rsp.sendRedirect(req.getContextPath() + '/' + toComputer().getUrl());
    }

    @RequirePOST
    public synchronized void doSubmitDescription(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        getPrimaryView().doSubmitDescription(req, rsp);
    }

    @RequirePOST
    public synchronized HttpRedirect doQuietDown() throws IOException {
        try {
            return doQuietDown(false, 0);
        } catch (InterruptedException e) {
            throw new AssertionError();
        }
    }

    @RequirePOST
    public HttpRedirect doQuietDown(@QueryParameter boolean block, @QueryParameter int timeout) throws InterruptedException, IOException {
        synchronized (this) {
            checkPermission(ADMINISTER);
            isQuietingDown = true;
        }
        if (block) {
            long waitUntil = timeout;
            if (timeout > 0)
                waitUntil += System.currentTimeMillis();
            while (isQuietingDown && (timeout <= 0 || System.currentTimeMillis() < waitUntil) && !RestartListener.isAllReady()) {
                Thread.sleep(1000);
            }
        }
        return new HttpRedirect(".");
    }

    @RequirePOST
    public synchronized HttpRedirect doCancelQuietDown() {
        checkPermission(ADMINISTER);
        isQuietingDown = false;
        getQueue().scheduleMaintenance();
        return new HttpRedirect(".");
    }

    public HttpResponse doToggleCollapse() throws ServletException, IOException {
        final StaplerRequest request = Stapler.getCurrentRequest();
        final String paneId = request.getParameter("paneId");
        PaneStatusProperties.forCurrentUser().toggleCollapsed(paneId);
        return HttpResponses.forwardToPreviousPage();
    }

    public void doClassicThreadDump(StaplerResponse rsp) throws IOException, ServletException {
        rsp.sendRedirect2("threadDump");
    }

    public Map<String, Map<String, String>> getAllThreadDumps() throws IOException, InterruptedException {
        checkPermission(ADMINISTER);
        Map<String, Future<Map<String, String>>> future = new HashMap<String, Future<Map<String, String>>>();
        for (Computer c : getComputers()) {
            try {
                future.put(c.getName(), RemotingDiagnostics.getThreadDumpAsync(c.getChannel()));
            } catch (Exception e) {
                LOGGER.info("Failed to get thread dump for node " + c.getName() + ": " + e.getMessage());
            }
        }
        if (toComputer() == null) {
            future.put("master", RemotingDiagnostics.getThreadDumpAsync(FilePath.localChannel));
        }
        long endTime = System.currentTimeMillis() + 5000;
        Map<String, Map<String, String>> r = new HashMap<String, Map<String, String>>();
        for (Entry<String, Future<Map<String, String>>> e : future.entrySet()) {
            try {
                r.put(e.getKey(), e.getValue().get(endTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
            } catch (Exception x) {
                StringWriter sw = new StringWriter();
                x.printStackTrace(new PrintWriter(sw, true));
                r.put(e.getKey(), Collections.singletonMap("Failed to retrieve thread dump", sw.toString()));
            }
        }
        return Collections.unmodifiableSortedMap(new TreeMap<String, Map<String, String>>(r));
    }

    @RequirePOST
    public synchronized TopLevelItem doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        return itemGroupMixIn.createTopLevelItem(req, rsp);
    }

    public TopLevelItem createProjectFromXML(String name, InputStream xml) throws IOException {
        return itemGroupMixIn.createProjectFromXML(name, xml);
    }

    @SuppressWarnings({ "unchecked" })
    public <T extends TopLevelItem> T copy(T src, String name) throws IOException {
        return itemGroupMixIn.copy(src, name);
    }

    public <T extends AbstractProject<?, ?>> T copy(T src, String name) throws IOException {
        return (T) copy((TopLevelItem) src, name);
    }

    @RequirePOST
    public synchronized void doCreateView(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, FormException {
        checkPermission(View.CREATE);
        addView(View.create(req, rsp, this));
    }

    public static void checkGoodName(String name) throws Failure {
        if (name == null || name.length() == 0)
            throw new Failure(Messages.Hudson_NoName());
        if (".".equals(name.trim()))
            throw new Failure(Messages.Jenkins_NotAllowedName("."));
        if ("..".equals(name.trim()))
            throw new Failure(Messages.Jenkins_NotAllowedName(".."));
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Character.isISOControl(ch)) {
                throw new Failure(Messages.Hudson_ControlCodeNotAllowed(toPrintableName(name)));
            }
            if ("?*/\\%!@#$^&|<>[]:;".indexOf(ch) != -1)
                throw new Failure(Messages.Hudson_UnsafeChar(ch));
        }
    }

    private static String toPrintableName(String name) {
        StringBuilder printableName = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Character.isISOControl(ch))
                printableName.append("\\u").append((int) ch).append(';');
            else
                printableName.append(ch);
        }
        return printableName.toString();
    }

    public void doSecured(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        if (req.getUserPrincipal() == null) {
            rsp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String path = req.getContextPath() + req.getOriginalRestOfPath();
        String q = req.getQueryString();
        if (q != null)
            path += '?' + q;
        rsp.sendRedirect2(path);
    }

    public void doLoginEntry(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (req.getUserPrincipal() == null) {
            rsp.sendRedirect2("noPrincipal");
            return;
        }
        String from = req.getParameter("from");
        if (from != null && from.startsWith("/") && !from.equals("/loginError")) {
            rsp.sendRedirect2(from);
            return;
        }
        String url = AbstractProcessingFilter.obtainFullRequestUrl(req);
        if (url != null) {
            rsp.sendRedirect2(url);
            return;
        }
        rsp.sendRedirect2(".");
    }

    public void doLogout(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        String user = getAuthentication().getName();
        securityRealm.doLogout(req, rsp);
        SecurityListener.fireLoggedOut(user);
    }

    public Slave.JnlpJar getJnlpJars(String fileName) {
        return new Slave.JnlpJar(fileName);
    }

    public Slave.JnlpJar doJnlpJars(StaplerRequest req) {
        return new Slave.JnlpJar(req.getRestOfPath().substring(1));
    }

    @RequirePOST
    public synchronized HttpResponse doReload() throws IOException {
        checkPermission(ADMINISTER);
        LOGGER.log(Level.WARNING, "Reloading Jenkins as requested by {0}", getAuthentication().getName());
        servletContext.setAttribute("app", new HudsonIsLoading());
        new Thread("Jenkins config reload thread") {

            @Override
            public void run() {
                try {
                    ACL.impersonate(ACL.SYSTEM);
                    reload();
                } catch (Exception e) {
                    LOGGER.log(SEVERE, "Failed to reload Jenkins config", e);
                    new JenkinsReloadFailed(e).publish(servletContext, root);
                }
            }
        }.start();
        return HttpResponses.redirectViaContextPath("/");
    }

    public void reload() throws IOException, InterruptedException, ReactorException {
        queue.save();
        executeReactor(null, loadTasks());
        if (initLevel != InitMilestone.COMPLETED) {
            LOGGER.log(SEVERE, "Jenkins initialization has not reached the COMPLETED initialization milestone after the configuration reload. " + "Current state: {0}. " + "It may cause undefined incorrect behavior in Jenkins plugin relying on this state. " + "It is likely an issue with the Initialization task graph. " + "Example: usage of @Initializer(after = InitMilestone.COMPLETED) in a plugin (JENKINS-37759). " + "Please create a bug in Jenkins bugtracker.", initLevel);
        }
        User.reload();
        queue.load();
        servletContext.setAttribute("app", this);
    }

    public void doDoFingerprintCheck(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        MultipartFormDataParser p = new MultipartFormDataParser(req);
        if (isUseCrumbs() && !getCrumbIssuer().validateCrumb(req, p)) {
            rsp.sendError(HttpServletResponse.SC_FORBIDDEN, "No crumb found");
        }
        try {
            rsp.sendRedirect2(req.getContextPath() + "/fingerprint/" + Util.getDigestOf(p.getFileItem("name").getInputStream()) + '/');
        } finally {
            p.cleanUp();
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("DM_GC")
    @RequirePOST
    public void doGc(StaplerResponse rsp) throws IOException {
        checkPermission(Jenkins.ADMINISTER);
        System.gc();
        rsp.setStatus(HttpServletResponse.SC_OK);
        rsp.setContentType("text/plain");
        rsp.getWriter().println("GCed");
    }

    public void doException() {
        throw new RuntimeException();
    }

    public ContextMenu doContextMenu(StaplerRequest request, StaplerResponse response) throws IOException, JellyException {
        ContextMenu menu = new ContextMenu().from(this, request, response);
        for (MenuItem i : menu.items) {
            if (i.url.equals(request.getContextPath() + "/manage")) {
                i.subMenu = new ContextMenu().from(this, request, response, "manage");
            }
        }
        return menu;
    }

    public ContextMenu doChildrenContextMenu(StaplerRequest request, StaplerResponse response) throws Exception {
        ContextMenu menu = new ContextMenu();
        for (View view : getViews()) {
            menu.add(view.getViewUrl(), view.getDisplayName());
        }
        return menu;
    }

    public HeapDump getHeapDump() throws IOException {
        return new HeapDump(this, FilePath.localChannel);
    }

    @RequirePOST
    public void doSimulateOutOfMemory() throws IOException {
        checkPermission(ADMINISTER);
        System.out.println("Creating artificial OutOfMemoryError situation");
        List<Object> args = new ArrayList<Object>();
        while (true) args.add(new byte[1024 * 1024]);
    }

    public DirectoryBrowserSupport doUserContent() {
        return new DirectoryBrowserSupport(this, getRootPath().child("userContent"), "User content", "folder.png", true);
    }

    @CLIMethod(name = "restart")
    public void doRestart(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, RestartNotSupportedException {
        checkPermission(ADMINISTER);
        if (req != null && req.getMethod().equals("GET")) {
            req.getView(this, "_restart.jelly").forward(req, rsp);
            return;
        }
        restart();
        if (rsp != null)
            rsp.sendRedirect2(".");
    }

    @CLIMethod(name = "safe-restart")
    public HttpResponse doSafeRestart(StaplerRequest req) throws IOException, ServletException, RestartNotSupportedException {
        checkPermission(ADMINISTER);
        if (req != null && req.getMethod().equals("GET"))
            return HttpResponses.forwardToView(this, "_safeRestart.jelly");
        safeRestart();
        return HttpResponses.redirectToDot();
    }

    public void restart() throws RestartNotSupportedException {
        final Lifecycle lifecycle = Lifecycle.get();
        lifecycle.verifyRestartable();
        servletContext.setAttribute("app", new HudsonIsRestarting());
        new Thread("restart thread") {

            final String exitUser = getAuthentication().getName();

            @Override
            public void run() {
                try {
                    ACL.impersonate(ACL.SYSTEM);
                    Thread.sleep(5000);
                    LOGGER.severe(String.format("Restarting VM as requested by %s", exitUser));
                    for (RestartListener listener : RestartListener.all()) listener.onRestart();
                    lifecycle.restart();
                } catch (InterruptedException | IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to restart Jenkins", e);
                }
            }
        }.start();
    }

    public void safeRestart() throws RestartNotSupportedException {
        final Lifecycle lifecycle = Lifecycle.get();
        lifecycle.verifyRestartable();
        isQuietingDown = true;
        new Thread("safe-restart thread") {

            final String exitUser = getAuthentication().getName();

            @Override
            public void run() {
                try {
                    ACL.impersonate(ACL.SYSTEM);
                    doQuietDown(true, 0);
                    if (isQuietingDown) {
                        servletContext.setAttribute("app", new HudsonIsRestarting());
                        LOGGER.info("Restart in 10 seconds");
                        Thread.sleep(10000);
                        LOGGER.severe(String.format("Restarting VM as requested by %s", exitUser));
                        for (RestartListener listener : RestartListener.all()) listener.onRestart();
                        lifecycle.restart();
                    } else {
                        LOGGER.info("Safe-restart mode cancelled");
                    }
                } catch (Throwable e) {
                    LOGGER.log(Level.WARNING, "Failed to restart Jenkins", e);
                }
            }
        }.start();
    }

    @Extension
    @Restricted(NoExternalUse.class)
    public static class MasterRestartNotifyier extends RestartListener {

        @Override
        public void onRestart() {
            Computer computer = Jenkins.getInstance().toComputer();
            if (computer == null)
                return;
            RestartCause cause = new RestartCause();
            for (ComputerListener listener : ComputerListener.all()) {
                listener.onOffline(computer, cause);
            }
        }

        @Override
        public boolean isReadyToRestart() throws IOException, InterruptedException {
            return true;
        }

        private static class RestartCause extends OfflineCause.SimpleOfflineCause {

            protected RestartCause() {
                super(Messages._Jenkins_IsRestarting());
            }
        }
    }

    @CLIMethod(name = "shutdown")
    @RequirePOST
    public void doExit(StaplerRequest req, StaplerResponse rsp) throws IOException {
        checkPermission(ADMINISTER);
        LOGGER.severe(String.format("Shutting down VM as requested by %s from %s", getAuthentication().getName(), req != null ? req.getRemoteAddr() : "???"));
        if (rsp != null) {
            rsp.setStatus(HttpServletResponse.SC_OK);
            rsp.setContentType("text/plain");
            try (PrintWriter w = rsp.getWriter()) {
            w.println("Shutting down");
            }
        }
        System.exit(0);
    }

    @CLIMethod(name = "safe-shutdown")
    @RequirePOST
    public HttpResponse doSafeExit(StaplerRequest req) throws IOException {
        checkPermission(ADMINISTER);
        isQuietingDown = true;
        final String exitUser = getAuthentication().getName();
        final String exitAddr = req != null ? req.getRemoteAddr() : "unknown";
        new Thread("safe-exit thread") {

            @Override
            public void run() {
                try {
                    ACL.impersonate(ACL.SYSTEM);
                    LOGGER.severe(String.format("Shutting down VM as requested by %s from %s", exitUser, exitAddr));
                    doQuietDown(true, 0);
                    if (isQuietingDown) {
                        cleanUp();
                        System.exit(0);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to shut down Jenkins", e);
                }
            }
        }.start();
        return HttpResponses.plainText("Shutting down as soon as all jobs are complete");
    }

    @Nonnull
    public static Authentication getAuthentication() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null)
            a = ANONYMOUS;
        return a;
    }

    public void doScript(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        _doScript(req, rsp, req.getView(this, "_script.jelly"), FilePath.localChannel, getACL());
    }

    public void doScriptText(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        _doScript(req, rsp, req.getView(this, "_scriptText.jelly"), FilePath.localChannel, getACL());
    }

    public static void _doScript(StaplerRequest req, StaplerResponse rsp, RequestDispatcher view, VirtualChannel channel, ACL acl) throws IOException, ServletException {
        acl.checkPermission(RUN_SCRIPTS);
        String text = req.getParameter("script");
        if (text != null) {
            if (!"POST".equals(req.getMethod())) {
                throw HttpResponses.error(HttpURLConnection.HTTP_BAD_METHOD, "requires POST");
            }
            if (channel == null) {
                throw HttpResponses.error(HttpURLConnection.HTTP_NOT_FOUND, "Node is offline");
            }
            try {
                req.setAttribute("output", RemotingDiagnostics.executeGroovy(text, channel));
            } catch (InterruptedException e) {
                throw new ServletException(e);
            }
        }
        view.forward(req, rsp);
    }

    @RequirePOST
    public void doEval(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        checkPermission(RUN_SCRIPTS);
        try {
            MetaClass mc = WebApp.getCurrent().getMetaClass(getClass());
            Script script = mc.classLoader.loadTearOff(JellyClassLoaderTearOff.class).createContext().compileScript(new InputSource(req.getReader()));
            new JellyRequestDispatcher(this, script).forward(req, rsp);
        } catch (JellyException e) {
            throw new ServletException(e);
        }
    }

    public void doSignup(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        if (getSecurityRealm().allowsSignup()) {
            req.getView(getSecurityRealm(), "signup.jelly").forward(req, rsp);
            return;
        }
        req.getView(SecurityRealm.class, "signup.jelly").forward(req, rsp);
    }

    public void doIconSize(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        String qs = req.getQueryString();
        if (qs == null)
            throw new ServletException();
        Cookie cookie = new Cookie("iconSize", Functions.validateIconSize(qs));
        cookie.setMaxAge(9999999);
        rsp.addCookie(cookie);
        String ref = req.getHeader("Referer");
        if (ref == null)
            ref = ".";
        rsp.sendRedirect2(ref);
    }

    @RequirePOST
    public void doFingerprintCleanup(StaplerResponse rsp) throws IOException {
        FingerprintCleanupThread.invoke();
        rsp.setStatus(HttpServletResponse.SC_OK);
        rsp.setContentType("text/plain");
        rsp.getWriter().println("Invoked");
    }

    @RequirePOST
    public void doWorkspaceCleanup(StaplerResponse rsp) throws IOException {
        WorkspaceCleanupThread.invoke();
        rsp.setStatus(HttpServletResponse.SC_OK);
        rsp.setContentType("text/plain");
        rsp.getWriter().println("Invoked");
    }

    public FormValidation doDefaultJDKCheck(StaplerRequest request, @QueryParameter String value) {
        if (!JDK.isDefaultName(value))
            return FormValidation.ok();
        if (JDK.isDefaultJDKValid(Jenkins.this))
            return FormValidation.ok();
        else
            return FormValidation.errorWithMarkup(Messages.Hudson_NoJavaInPath(request.getContextPath()));
    }

    public FormValidation doCheckViewName(@QueryParameter String value) {
        checkPermission(View.CREATE);
        String name = fixEmpty(value);
        if (name == null)
            return FormValidation.ok();
        if (getView(name) != null)
            return FormValidation.error(Messages.Hudson_ViewAlreadyExists(name));
        try {
            checkGoodName(name);
        } catch (Failure e) {
            return FormValidation.error(e.getMessage());
        }
        return FormValidation.ok();
    }

    @Deprecated
    public FormValidation doViewExistsCheck(@QueryParameter String value) {
        checkPermission(View.CREATE);
        String view = fixEmpty(value);
        if (view == null)
            return FormValidation.ok();
        if (getView(view) == null)
            return FormValidation.ok();
        else
            return FormValidation.error(Messages.Hudson_ViewAlreadyExists(view));
    }

    public void doResources(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        String path = req.getRestOfPath();
        path = path.substring(path.indexOf('/', 1) + 1);
        int idx = path.lastIndexOf('.');
        String extension = path.substring(idx + 1);
        if (ALLOWED_RESOURCE_EXTENSIONS.contains(extension)) {
            URL url = pluginManager.uberClassLoader.getResource(path);
            if (url != null) {
                long expires = MetaClass.NO_CACHE ? 0 : 365L * 24 * 60 * 60 * 1000;
                rsp.serveFile(req, url, expires);
                return;
            }
        }
        rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    public static final Set<String> ALLOWED_RESOURCE_EXTENSIONS = new HashSet<String>(Arrays.asList("js|css|jpeg|jpg|png|gif|html|htm".split("\\|")));

    public FormValidation doCheckURIEncoding(StaplerRequest request) throws IOException {
        final String expected = "\u57f7\u4e8b";
        final String value = fixEmpty(request.getParameter("value"));
        if (!expected.equals(value))
            return FormValidation.warningWithMarkup(Messages.Hudson_NotUsesUTF8ToDecodeURL());
        return FormValidation.ok();
    }

    public static boolean isCheckURIEncodingEnabled() {
        return !"ISO-8859-1".equalsIgnoreCase(System.getProperty("file.encoding"));
    }

    public void rebuildDependencyGraph() {
        DependencyGraph graph = new DependencyGraph();
        graph.build();
        dependencyGraph = graph;
        dependencyGraphDirty.set(false);
    }

    public Future<DependencyGraph> rebuildDependencyGraphAsync() {
        dependencyGraphDirty.set(true);
        return Timer.get().schedule(new java.util.concurrent.Callable<DependencyGraph>() {

            @Override
            public DependencyGraph call() throws Exception {
                if (dependencyGraphDirty.get()) {
                    rebuildDependencyGraph();
                }
                return dependencyGraph;
            }
        }, 500, TimeUnit.MILLISECONDS);
    }

    public DependencyGraph getDependencyGraph() {
        return dependencyGraph;
    }

    public List<ManagementLink> getManagementLinks() {
        return ManagementLink.all();
    }

    @Restricted(NoExternalUse.class)
    public SetupWizard getSetupWizard() {
        return setupWizard;
    }

    public User getMe() {
        User u = User.current();
        if (u == null)
            throw new AccessDeniedException("/me is not available when not logged in");
        return u;
    }

    public List<Widget> getWidgets() {
        return widgets;
    }

    public Object getTarget() {
        try {
            checkPermission(READ);
        } catch (AccessDeniedException e) {
            String rest = Stapler.getCurrentRequest().getRestOfPath();
            for (String name : ALWAYS_READABLE_PATHS) {
                if (rest.startsWith(name)) {
                    return this;
                }
            }
            for (String name : getUnprotectedRootActions()) {
                if (rest.startsWith("/" + name + "/") || rest.equals("/" + name)) {
                    return this;
                }
            }
            if (rest.matches("/computer/[^/]+/slave-agent[.]jnlp") && "true".equals(Stapler.getCurrentRequest().getParameter("encrypt"))) {
                return this;
            }
            throw e;
        }
        return this;
    }

    public Collection<String> getUnprotectedRootActions() {
        Set<String> names = new TreeSet<String>();
        names.add("jnlpJars");
        for (Action a : getActions()) {
            if (a instanceof UnprotectedRootAction) {
                String url = a.getUrlName();
                if (url == null)
                    continue;
                names.add(url);
            }
        }
        return names;
    }

    public View getStaplerFallback() {
        return getPrimaryView();
    }

    boolean isDisplayNameUnique(String displayName, String currentJobName) {
        Collection<TopLevelItem> itemCollection = items.values();
        for (TopLevelItem item : itemCollection) {
            if (item.getName().equals(currentJobName)) {
                continue;
            } else if (displayName.equals(item.getDisplayName())) {
                return false;
            }
        }
        return true;
    }

    boolean isNameUnique(String name, String currentJobName) {
        Item item = getItem(name);
        if (null == item) {
            return true;
        } else if (item.getName().equals(currentJobName)) {
            return true;
        } else {
            return false;
        }
    }

    public FormValidation doCheckDisplayName(@QueryParameter String displayName, @QueryParameter String jobName) {
        displayName = displayName.trim();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Current job name is " + jobName);
        }
        if (!isNameUnique(displayName, jobName)) {
            return FormValidation.warning(Messages.Jenkins_CheckDisplayName_NameNotUniqueWarning(displayName));
        } else if (!isDisplayNameUnique(displayName, jobName)) {
            return FormValidation.warning(Messages.Jenkins_CheckDisplayName_DisplayNameNotUniqueWarning(displayName));
        } else {
            return FormValidation.ok();
        }
    }

    public static class MasterComputer extends Computer {

        protected MasterComputer() {
            super(Jenkins.getInstance());
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public boolean isConnecting() {
            return false;
        }

        @Override
        public String getDisplayName() {
            return Messages.Hudson_Computer_DisplayName();
        }

        @Override
        public String getCaption() {
            return Messages.Hudson_Computer_Caption();
        }

        @Override
        public String getUrl() {
            return "computer/(master)/";
        }

        public RetentionStrategy getRetentionStrategy() {
            return RetentionStrategy.NOOP;
        }

        @Override
        protected boolean isAlive() {
            return true;
        }

        @Override
        public Boolean isUnix() {
            return !Functions.isWindows();
        }

        @Override
        public HttpResponse doDoDelete() throws IOException {
            throw HttpResponses.status(SC_BAD_REQUEST);
        }

        @Override
        public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, FormException {
            Jenkins.getInstance().doConfigExecutorsSubmit(req, rsp);
        }

        @WebMethod(name = "config.xml")
        @Override
        public void doConfigDotXml(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            throw HttpResponses.status(SC_BAD_REQUEST);
        }

        @Override
        public boolean hasPermission(Permission permission) {
            if (permission == Computer.DELETE)
                return false;
            return super.hasPermission(permission == Computer.CONFIGURE ? Jenkins.ADMINISTER : permission);
        }

        @Override
        public VirtualChannel getChannel() {
            return FilePath.localChannel;
        }

        @Override
        public Charset getDefaultCharset() {
            return Charset.defaultCharset();
        }

        public List<LogRecord> getLogRecords() throws IOException, InterruptedException {
            return logRecords;
        }

        @RequirePOST
        public void doLaunchSlaveAgent(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            rsp.sendError(SC_NOT_FOUND);
        }

        protected Future<?> _connect(boolean forceReconnect) {
            return Futures.precomputed(null);
        }

        @Deprecated
        public static final LocalChannel localChannel = FilePath.localChannel;
    }

    @CheckForNull
    public static <T> T lookup(Class<T> type) {
        Jenkins j = Jenkins.getInstanceOrNull();
        return j != null ? j.lookup.get(type) : null;
    }

    public static List<LogRecord> logRecords = Collections.emptyList();

    public static final XStream XSTREAM;

    public static final XStream2 XSTREAM2;

    private static final int TWICE_CPU_NUM = Math.max(4, Runtime.getRuntime().availableProcessors() * 2);

    transient final ExecutorService threadPoolForLoad = new ThreadPoolExecutor(TWICE_CPU_NUM, TWICE_CPU_NUM, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamingThreadFactory(new DaemonThreadFactory(), "Jenkins load"));

    private static void computeVersion(ServletContext context) {
        Properties props = new Properties();
        InputStream is = null;
        try {
            is = Jenkins.class.getResourceAsStream("jenkins-version.properties");
            if (is != null)
                props.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(is);
        }
        String ver = props.getProperty("version");
        if (ver == null)
            ver = UNCOMPUTED_VERSION;
        if (Main.isDevelopmentMode && "${build.version}".equals(ver)) {
            try {
                File dir = new File(".").getAbsoluteFile();
                while (dir != null) {
                    File pom = new File(dir, "pom.xml");
                    if (pom.exists() && "pom".equals(XMLUtils.getValue("/project/artifactId", pom))) {
                        pom = pom.getCanonicalFile();
                        LOGGER.info("Reading version from: " + pom.getAbsolutePath());
                        ver = XMLUtils.getValue("/project/version", pom);
                        break;
                    }
                    dir = dir.getParentFile();
                }
                LOGGER.info("Jenkins is in dev mode, using version: " + ver);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Unable to read Jenkins version: " + e.getMessage(), e);
            }
        }
        VERSION = ver;
        context.setAttribute("version", ver);
        VERSION_HASH = Util.getDigestOf(ver).substring(0, 8);
        SESSION_HASH = Util.getDigestOf(ver + System.currentTimeMillis()).substring(0, 8);
        if (ver.equals(UNCOMPUTED_VERSION) || SystemProperties.getBoolean("hudson.script.noCache"))
            RESOURCE_PATH = "";
        else
            RESOURCE_PATH = "/static/" + SESSION_HASH;
        VIEW_RESOURCE_PATH = "/resources/" + SESSION_HASH;
    }

    @Restricted(NoExternalUse.class)
    public static final String UNCOMPUTED_VERSION = "?";

    public static String VERSION = UNCOMPUTED_VERSION;

    @CheckForNull
    public static VersionNumber getVersion() {
        return toVersion(VERSION);
    }

    @Restricted(NoExternalUse.class)
    @CheckForNull
    public static VersionNumber getStoredVersion() {
        return toVersion(Jenkins.getActiveInstance().version);
    }

    @CheckForNull
    private static VersionNumber toVersion(@CheckForNull String versionString) {
        if (versionString == null) {
            return null;
        }
        try {
            return new VersionNumber(versionString);
        } catch (NumberFormatException e) {
            try {
                int idx = versionString.indexOf(' ');
                if (idx > 0) {
                    return new VersionNumber(versionString.substring(0, idx));
                }
            } catch (NumberFormatException _) {
            }
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String VERSION_HASH;

    public static String SESSION_HASH;

    public static String RESOURCE_PATH = "";

    public static String VIEW_RESOURCE_PATH = "/resources/TBD";

    public static boolean PARALLEL_LOAD = Configuration.getBooleanConfigParameter("parallelLoad", true);

    public static boolean KILL_AFTER_LOAD = Configuration.getBooleanConfigParameter("killAfterLoad", false);

    @Deprecated
    public static boolean FLYWEIGHT_SUPPORT = true;

    @Restricted(NoExternalUse.class)
    @Deprecated
    public static boolean CONCURRENT_BUILD = true;

    private static final String WORKSPACE_DIRNAME = Configuration.getStringConfigParameter("workspaceDirName", "workspace");

    public static boolean AUTOMATIC_SLAVE_LAUNCH = true;

    private static final Logger LOGGER = Logger.getLogger(Jenkins.class.getName());

    public static final PermissionGroup PERMISSIONS = Permission.HUDSON_PERMISSIONS;

    public static final Permission ADMINISTER = Permission.HUDSON_ADMINISTER;

    public static final Permission READ = new Permission(PERMISSIONS, "Read", Messages._Hudson_ReadPermission_Description(), Permission.READ, PermissionScope.JENKINS);

    public static final Permission RUN_SCRIPTS = new Permission(PERMISSIONS, "RunScripts", Messages._Hudson_RunScriptsPermission_Description(), ADMINISTER, PermissionScope.JENKINS);

    private static final ImmutableSet<String> ALWAYS_READABLE_PATHS = ImmutableSet.of("/login", "/logout", "/accessDenied", "/adjuncts/", "/error", "/oops", "/signup", "/tcpSlaveAgentListener", "/federatedLoginService/", "/securityRealm", "/instance-identity");

    public static final Authentication ANONYMOUS;

    static {
        try {
            ANONYMOUS = new AnonymousAuthenticationToken("anonymous", "anonymous", new GrantedAuthority[] { new GrantedAuthorityImpl("anonymous") });
            XSTREAM = XSTREAM2 = new XStream2();
            XSTREAM.alias("jenkins", Jenkins.class);
            XSTREAM.alias("slave", DumbSlave.class);
            XSTREAM.alias("jdk", JDK.class);
            XSTREAM.alias("view", ListView.class);
            XSTREAM.alias("listView", ListView.class);
            XSTREAM.addImplicitArray(Jenkins.class, "_disabledAgentProtocols", "disabledAgentProtocol");
            XSTREAM.addImplicitArray(Jenkins.class, "_enabledAgentProtocols", "enabledAgentProtocol");
            XSTREAM2.addCriticalField(Jenkins.class, "securityRealm");
            XSTREAM2.addCriticalField(Jenkins.class, "authorizationStrategy");
            Mode.class.getEnumConstants();
            assert PERMISSIONS != null;
            assert ADMINISTER != null;
        } catch (RuntimeException | Error e) {
            LOGGER.log(SEVERE, "Failed to load Jenkins.class", e);
            throw e;
        }
    }

    private static final class JenkinsJVMAccess extends JenkinsJVM {

        private static void _setJenkinsJVM(boolean jenkinsJVM) {
            JenkinsJVM.setJenkinsJVM(jenkinsJVM);
        }
    }
}