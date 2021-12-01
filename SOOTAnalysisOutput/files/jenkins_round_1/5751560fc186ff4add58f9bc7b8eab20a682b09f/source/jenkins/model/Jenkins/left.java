/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Sun Microsystems, Inc., Kohsuke Kawaguchi,
 * Erik Ramfelt, Koichi Fujikawa, Red Hat, Inc., Seiji Sogabe,
 * Stephen Connolly, Tom Huybrechts, Yahoo! Inc., Alan Harder, CloudBees, Inc.,
 * Yahoo!, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.model;

import antlr.ANTLRException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
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
import hudson.LocalPluginManager;
import hudson.Lookup;
import hudson.Plugin;
import hudson.PluginManager;
import hudson.PluginWrapper;
import hudson.ProxyConfiguration;
import hudson.TcpSlaveAgentListener;
import hudson.UDPBroadcastThread;
import hudson.Util;
import hudson.WebAppMain;
import hudson.XmlFile;
import hudson.cli.declarative.CLIMethod;
import hudson.cli.declarative.CLIResolver;
import hudson.init.InitMilestone;
import hudson.init.InitStrategy;
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
import hudson.slaves.EphemeralNode;
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
import jenkins.ExtensionComponentSet;
import jenkins.ExtensionRefreshException;
import jenkins.InitReactorRunner;
import jenkins.model.ProjectNamingStrategy.DefaultProjectNamingStrategy;
import jenkins.security.ConfidentialKey;
import jenkins.security.ConfidentialStore;
import jenkins.security.SecurityListener;
import jenkins.slaves.WorkspaceLocator;
import jenkins.util.Timer;
import jenkins.util.io.FileBoolean;
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
import org.jvnet.hudson.reactor.Reactor;
import org.jvnet.hudson.reactor.ReactorException;
import org.jvnet.hudson.reactor.Task;
import org.jvnet.hudson.reactor.TaskBuilder;
import org.jvnet.hudson.reactor.TaskGraphBuilder;
import org.jvnet.hudson.reactor.TaskGraphBuilder.Handle;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
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
import java.io.FileFilter;
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
import hudson.util.LogTaskListener;
import static java.util.logging.Level.*;
import static javax.servlet.http.HttpServletResponse.*;

/**
 * Root object of the system.
 *
 * @author Kohsuke Kawaguchi
 */
@ExportedBean
public class Jenkins extends AbstractCIBase implements DirectlyModifiableTopLevelItemGroup, StaplerProxy, StaplerFallback,
        ModifiableViewGroup, AccessControlled, DescriptorByNameOwner,
        ModelObjectWithContextMenu, ModelObjectWithChildren {
    private transient final Queue queue;

    /**
     * Stores various objects scoped to {@link Jenkins}.
     */
    public transient final Lookup lookup = new Lookup();

    /**
     * We update this field to the current version of Hudson whenever we save {@code config.xml}.
     * This can be used to detect when an upgrade happens from one version to next.
     *
     * <p>
     * Since this field is introduced starting 1.301, "1.0" is used to represent every version
     * up to 1.300. This value may also include non-standard versions like "1.301-SNAPSHOT" or
     * "?", etc., so parsing needs to be done with a care.
     *
     * @since 1.301
     */
    // this field needs to be at the very top so that other components can look at this value even during unmarshalling
    private String version = "1.0";

    /**
     * Number of executors of the master node.
     */
    private int numExecutors = 2;

    /**
     * Job allocation strategy.
     */
    private Mode mode = Mode.NORMAL;

    /**
     * False to enable anyone to do anything.
     * Left as a field so that we can still read old data that uses this flag.
     *
     * @see #authorizationStrategy
     * @see #securityRealm
     */
    private Boolean useSecurity;

    /**
     * Controls how the
     * <a href="http://en.wikipedia.org/wiki/Authorization">authorization</a>
     * is handled in Hudson.
     * <p>
     * This ultimately controls who has access to what.
     *
     * Never null.
     */
    private volatile AuthorizationStrategy authorizationStrategy = AuthorizationStrategy.UNSECURED;

    /**
     * Controls a part of the
     * <a href="http://en.wikipedia.org/wiki/Authentication">authentication</a>
     * handling in Hudson.
     * <p>
     * Intuitively, this corresponds to the user database.
     *
     * See {@link HudsonFilter} for the concrete authentication protocol.
     *
     * Never null. Always use {@link #setSecurityRealm(SecurityRealm)} to
     * update this field.
     *
     * @see #getSecurity()
     * @see #setSecurityRealm(SecurityRealm)
     */
    private volatile SecurityRealm securityRealm = SecurityRealm.NO_AUTHENTICATION;

    /**
     * Disables the remember me on this computer option in the standard login screen.
     *
     * @since 1.534
     */
    private volatile boolean disableRememberMe;
    
    /**
     * The project naming strategy defines/restricts the names which can be given to a project/job. e.g. does the name have to follow a naming convention?
     */
    private ProjectNamingStrategy projectNamingStrategy = DefaultProjectNamingStrategy.DEFAULT_NAMING_STRATEGY;

    /**
     * Root directory for the workspaces.
     * This value will be variable-expanded as per {@link #expandVariablesForDirectory}.
     * @see #getWorkspaceFor(TopLevelItem)
     */
    private String workspaceDir = "${ITEM_ROOTDIR}/"+WORKSPACE_DIRNAME;

    /**
     * Root directory for the builds.
     * This value will be variable-expanded as per {@link #expandVariablesForDirectory}.
     * @see #getBuildDirFor(Job)
     */
    private String buildsDir = "${ITEM_ROOTDIR}/builds";

    /**
     * Message displayed in the top page.
     */
    private String systemMessage;

    private MarkupFormatter markupFormatter;

    /**
     * Root directory of the system.
     */
    public transient final File root;

    /**
     * Where are we in the initialization?
     */
    private transient volatile InitMilestone initLevel = InitMilestone.STARTED;

    /**
     * All {@link Item}s keyed by their {@link Item#getName() name}s.
     */
    /*package*/ transient final Map<String,TopLevelItem> items = new CopyOnWriteMap.Tree<String,TopLevelItem>(CaseInsensitiveComparator.INSTANCE);

    /**
     * The sole instance.
     */
    private static Jenkins theInstance;

    private transient volatile boolean isQuietingDown;
    private transient volatile boolean terminating;

    private List<JDK> jdks = new ArrayList<JDK>();

    private transient volatile DependencyGraph dependencyGraph;
    private final transient AtomicBoolean dependencyGraphDirty = new AtomicBoolean();

    /**
     * Currently active Views tab bar.
     */
    private volatile ViewsTabBar viewsTabBar = new DefaultViewsTabBar();

    /**
     * Currently active My Views tab bar.
     */
    private volatile MyViewsTabBar myViewsTabBar = new DefaultMyViewsTabBar();

    /**
     * All {@link ExtensionList} keyed by their {@link ExtensionList#extensionType}.
     */
    private transient final Memoizer<Class,ExtensionList> extensionLists = new Memoizer<Class,ExtensionList>() {
        public ExtensionList compute(Class key) {
            return ExtensionList.create(Jenkins.this,key);
        }
    };

    /**
     * All {@link DescriptorExtensionList} keyed by their {@link DescriptorExtensionList#describableType}.
     */
    private transient final Memoizer<Class,DescriptorExtensionList> descriptorLists = new Memoizer<Class,DescriptorExtensionList>() {
        public DescriptorExtensionList compute(Class key) {
            return DescriptorExtensionList.createDescriptorList(Jenkins.this,key);
        }
    };

    /**
     * {@link Computer}s in this Hudson system. Read-only.
     */
    protected transient final Map<Node,Computer> computers = new CopyOnWriteMap.Hash<Node,Computer>();

    /**
     * Active {@link Cloud}s.
     */
    public final Hudson.CloudList clouds = new Hudson.CloudList(this);
    
    public static class CloudList extends DescribableList<Cloud,Descriptor<Cloud>> {
        public CloudList(Jenkins h) {
            super(h);
        }

        public CloudList() {// needed for XStream deserialization
        }

        public Cloud getByName(String name) {
            for (Cloud c : this)
                if (c.name.equals(name))
                    return c;
            return null;
        }

        @Override
        protected void onModified() throws IOException {
            super.onModified();
            Jenkins.getInstance().trimLabels();
        }
    }

    /**
     * Set of installed cluster nodes.
     * <p>
     * We use this field with copy-on-write semantics.
     * This field has mutable list (to keep the serialization look clean),
     * but it shall never be modified. Only new completely populated slave
     * list can be set here.
     * <p>
     * The field name should be really {@code nodes}, but again the backward compatibility
     * prevents us from renaming.
     */
    protected volatile NodeList slaves;

    /**
     * Quiet period.
     *
     * This is {@link Integer} so that we can initialize it to '5' for upgrading users.
     */
    /*package*/ Integer quietPeriod;

    /**
     * Global default for {@link AbstractProject#getScmCheckoutRetryCount()}
     */
    /*package*/ int scmCheckoutRetryCount;

    /**
     * {@link View}s.
     */
    private final CopyOnWriteArrayList<View> views = new CopyOnWriteArrayList<View>();

    /**
     * Name of the primary view.
     * <p>
     * Start with null, so that we can upgrade pre-1.269 data well.
     * @since 1.269
     */
    private volatile String primaryView;

    private transient final ViewGroupMixIn viewGroupMixIn = new ViewGroupMixIn(this) {
        protected List<View> views() { return views; }
        protected String primaryView() { return primaryView; }
        protected void primaryView(String name) { primaryView=name; }
    };


    private transient final FingerprintMap fingerprintMap = new FingerprintMap();

    /**
     * Loaded plugins.
     */
    public transient final PluginManager pluginManager;

    public transient volatile TcpSlaveAgentListener tcpSlaveAgentListener;

    private transient UDPBroadcastThread udpBroadcastThread;

    private transient DNSMultiCast dnsMultiCast;

    /**
     * List of registered {@link SCMListener}s.
     */
    private transient final CopyOnWriteList<SCMListener> scmListeners = new CopyOnWriteList<SCMListener>();

    /**
     * TCP slave agent port.
     * 0 for random, -1 to disable.
     */
    private int slaveAgentPort =0;

    /**
     * Whitespace-separated labels assigned to the master as a {@link Node}.
     */
    private String label="";

    /**
     * {@link hudson.security.csrf.CrumbIssuer}
     */
    private volatile CrumbIssuer crumbIssuer;

    /**
     * All labels known to Jenkins. This allows us to reuse the same label instances
     * as much as possible, even though that's not a strict requirement.
     */
    private transient final ConcurrentHashMap<String,Label> labels = new ConcurrentHashMap<String,Label>();

    /**
     * Load statistics of the entire system.
     *
     * This includes every executor and every job in the system.
     */
    @Exported
    public transient final OverallLoadStatistics overallLoad = new OverallLoadStatistics();

    /**
     * Load statistics of the free roaming jobs and slaves.
     * 
     * This includes all executors on {@link hudson.model.Node.Mode#NORMAL} nodes and jobs that do not have any assigned nodes.
     *
     * @since 1.467
     */
    @Exported
    public transient final LoadStatistics unlabeledLoad = new UnlabeledLoadStatistics();

    /**
     * {@link NodeProvisioner} that reacts to {@link #unlabeledLoad}.
     * @since 1.467
     */
    public transient final NodeProvisioner unlabeledNodeProvisioner = new NodeProvisioner(null,unlabeledLoad);

    /**
     * @deprecated as of 1.467
     *      Use {@link #unlabeledNodeProvisioner}.
     *      This was broken because it was tracking all the executors in the system, but it was only tracking
     *      free-roaming jobs in the queue. So {@link Cloud} fails to launch nodes when you have some exclusive
     *      slaves and free-roaming jobs in the queue.
     */
    @Restricted(NoExternalUse.class)
    public transient final NodeProvisioner overallNodeProvisioner = unlabeledNodeProvisioner;


    public transient final ServletContext servletContext;

    /**
     * Transient action list. Useful for adding navigation items to the navigation bar
     * on the left.
     */
    private transient final List<Action> actions = new CopyOnWriteArrayList<Action>();

    /**
     * List of master node properties
     */
    private DescribableList<NodeProperty<?>,NodePropertyDescriptor> nodeProperties = new DescribableList<NodeProperty<?>,NodePropertyDescriptor>(this);

    /**
     * List of global properties
     */
    private DescribableList<NodeProperty<?>,NodePropertyDescriptor> globalNodeProperties = new DescribableList<NodeProperty<?>,NodePropertyDescriptor>(this);

    /**
     * {@link AdministrativeMonitor}s installed on this system.
     *
     * @see AdministrativeMonitor
     */
    public transient final List<AdministrativeMonitor> administrativeMonitors = getExtensionList(AdministrativeMonitor.class);

    /**
     * Widgets on Hudson.
     */
    private transient final List<Widget> widgets = getExtensionList(Widget.class);

    /**
     * {@link AdjunctManager}
     */
    private transient final AdjunctManager adjuncts;

    /**
     * Code that handles {@link ItemGroup} work.
     */
    private transient final ItemGroupMixIn itemGroupMixIn = new ItemGroupMixIn(this,this) {
        @Override
        protected void add(TopLevelItem item) {
            items.put(item.getName(),item);
        }

        @Override
        protected File getRootDirFor(String name) {
            return Jenkins.this.getRootDirFor(name);
        }
    };


    /**
     * Hook for a test harness to intercept Jenkins.getInstance()
     *
     * Do not use in the production code as the signature may change.
     */
    public interface JenkinsHolder {
        Jenkins getInstance();
    }

    static JenkinsHolder HOLDER = new JenkinsHolder() {
        public Jenkins getInstance() {
            return theInstance;
        }
    };

    /**
     * Gets the Jenkins singleton.
     * @return the instance, or null if Jenkins has not been started, or was already shut down
     */
    @CLIResolver
    @CheckForNull
    public static Jenkins getInstance() {
        return HOLDER.getInstance();
    }

    /**
     * Secret key generated once and used for a long time, beyond
     * container start/stop. Persisted outside <tt>config.xml</tt> to avoid
     * accidental exposure.
     */
    private transient final String secretKey;

    private transient final UpdateCenter updateCenter = new UpdateCenter();

    /**
     * True if the user opted out from the statistics tracking. We'll never send anything if this is true.
     */
    private Boolean noUsageStatistics;

    /**
     * HTTP proxy configuration.
     */
    public transient volatile ProxyConfiguration proxy;

    /**
     * Bound to "/log".
     */
    private transient final LogRecorderManager log = new LogRecorderManager();

    protected Jenkins(File root, ServletContext context) throws IOException, InterruptedException, ReactorException {
        this(root,context,null);
    }

    /**
     * @param pluginManager
     *      If non-null, use existing plugin manager.  create a new one.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings({
        "SC_START_IN_CTOR", // bug in FindBugs. It flags UDPBroadcastThread.start() call but that's for another class
        "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD" // Trigger.timer
    })
    protected Jenkins(File root, ServletContext context, PluginManager pluginManager) throws IOException, InterruptedException, ReactorException {
        long start = System.currentTimeMillis();
        
    	// As Jenkins is starting, grant this process full control
        ACL.impersonate(ACL.SYSTEM);
        try {
            this.root = root;
            this.servletContext = context;
            computeVersion(context);
            if(theInstance!=null)
                throw new IllegalStateException("second instance");
            theInstance = this;

            if (!new File(root,"jobs").exists()) {
                // if this is a fresh install, use more modern default layout that's consistent with slaves
                workspaceDir = "${JENKINS_HOME}/workspace/${ITEM_FULLNAME}";
            }

            // doing this early allows InitStrategy to set environment upfront
            final InitStrategy is = InitStrategy.get(Thread.currentThread().getContextClassLoader());

            Trigger.timer = new java.util.Timer("Jenkins cron thread");
            queue = new Queue(LoadBalancer.CONSISTENT_HASH);

            try {
                dependencyGraph = DependencyGraph.EMPTY;
            } catch (InternalError e) {
                if(e.getMessage().contains("window server")) {
                    throw new Error("Looks like the server runs without X. Please specify -Djava.awt.headless=true as JVM option",e);
                }
                throw e;
            }

            // get or create the secret
            TextFile secretFile = new TextFile(new File(getRootDir(),"secret.key"));
            if(secretFile.exists()) {
                secretKey = secretFile.readTrim();
            } else {
                SecureRandom sr = new SecureRandom();
                byte[] random = new byte[32];
                sr.nextBytes(random);
                secretKey = Util.toHexString(random);
                secretFile.write(secretKey);

                // this marker indicates that the secret.key is generated by the version of Jenkins post SECURITY-49.
                // this indicates that there's no need to rewrite secrets on disk
                new FileBoolean(new File(root,"secret.key.not-so-secret")).on();
            }

            try {
                proxy = ProxyConfiguration.load();
            } catch (IOException e) {
                LOGGER.log(SEVERE, "Failed to load proxy configuration", e);
            }

            if (pluginManager==null)
                pluginManager = new LocalPluginManager(this);
            this.pluginManager = pluginManager;
            // JSON binding needs to be able to see all the classes from all the plugins
            WebApp.get(servletContext).setClassLoader(pluginManager.uberClassLoader);

            adjuncts = new AdjunctManager(servletContext, pluginManager.uberClassLoader,"adjuncts/"+SESSION_HASH, TimeUnit2.DAYS.toMillis(365));

            // initialization consists of ...
            executeReactor( is,
                    pluginManager.initTasks(is),    // loading and preparing plugins
                    loadTasks(),                    // load jobs
                    InitMilestone.ordering()        // forced ordering among key milestones
            );

            if(KILL_AFTER_LOAD)
                System.exit(0);

            if(slaveAgentPort!=-1) {
                try {
                    tcpSlaveAgentListener = new TcpSlaveAgentListener(slaveAgentPort);
                } catch (BindException e) {
                    new AdministrativeError(getClass().getName()+".tcpBind",
                            "Failed to listen to incoming slave connection",
                            "Failed to listen to incoming slave connection. <a href='configure'>Change the port number</a> to solve the problem.",e);
                }
            } else
                tcpSlaveAgentListener = null;

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

            {// master is online now
                Computer c = toComputer();
                if(c!=null)
                    for (ComputerListener cl : ComputerListener.all())
                        cl.onOnline(c, new LogTaskListener(LOGGER, INFO));
            }

            for (ItemListener l : ItemListener.all()) {
                long itemListenerStart = System.currentTimeMillis();
                try {
                    l.onLoaded();
                } catch (RuntimeException x) {
                    LOGGER.log(Level.WARNING, null, x);
                }
                if (LOG_STARTUP_PERFORMANCE)
                    LOGGER.info(String.format("Took %dms for item listener %s startup",
                            System.currentTimeMillis()-itemListenerStart,l.getClass().getName()));
            }
            
            if (LOG_STARTUP_PERFORMANCE)
                LOGGER.info(String.format("Took %dms for complete Jenkins startup",
                        System.currentTimeMillis()-start));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Executes a reactor.
     *
     * @param is
     *      If non-null, this can be consulted for ignoring some tasks. Only used during the initialization of Hudson.
     */
    private void executeReactor(final InitStrategy is, TaskBuilder... builders) throws IOException, InterruptedException, ReactorException {
        Reactor reactor = new Reactor(builders) {
            /**
             * Sets the thread name to the task for better diagnostics.
             */
            @Override
            protected void runTask(Task task) throws Exception {
                if (is!=null && is.skipInitTask(task))  return;

                ACL.impersonate(ACL.SYSTEM); // full access in the initialization thread
                String taskName = task.getDisplayName();

                Thread t = Thread.currentThread();
                String name = t.getName();
                if (taskName !=null)
                    t.setName(taskName);
                try {
                    long start = System.currentTimeMillis();
                    super.runTask(task);
                    if(LOG_STARTUP_PERFORMANCE)
                        LOGGER.info(String.format("Took %dms for %s by %s",
                                System.currentTimeMillis()-start, taskName, name));
                } finally {
                    t.setName(name);
                    SecurityContextHolder.clearContext();
                }
            }
        };

        new InitReactorRunner() {
            @Override
            protected void onInitMilestoneAttained(InitMilestone milestone) {
                initLevel = milestone;
            }
        }.run(reactor);
    }


    public TcpSlaveAgentListener getTcpSlaveAgentListener() {
        return tcpSlaveAgentListener;
    }

    /**
     * Makes {@link AdjunctManager} URL-bound.
     * The dummy parameter allows us to use different URLs for the same adjunct,
     * for proper cache handling.
     */
    public AdjunctManager getAdjuncts(String dummy) {
        return adjuncts;
    }

    @Exported
    public int getSlaveAgentPort() {
        return slaveAgentPort;
    }

    /**
     * @param port
     *      0 to indicate random available TCP port. -1 to disable this service.
     */
    public void setSlaveAgentPort(int port) throws IOException {
        this.slaveAgentPort = port;

        // relaunch the agent
        if(tcpSlaveAgentListener==null) {
            if(slaveAgentPort!=-1)
                tcpSlaveAgentListener = new TcpSlaveAgentListener(slaveAgentPort);
        } else {
            if(tcpSlaveAgentListener.configuredPort!=slaveAgentPort) {
                tcpSlaveAgentListener.shutdown();
                tcpSlaveAgentListener = null;
                if(slaveAgentPort!=-1)
                    tcpSlaveAgentListener = new TcpSlaveAgentListener(slaveAgentPort);
            }
        }
    }

    public void setNodeName(String name) {
        throw new UnsupportedOperationException(); // not allowed
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
        return noUsageStatistics==null || !noUsageStatistics;
    }

    public void setNoUsageStatistics(Boolean noUsageStatistics) throws IOException {
        this.noUsageStatistics = noUsageStatistics;
        save();
    }

    public View.People getPeople() {
        return new View.People(this);
    }

    /**
     * @since 1.484
     */
    public View.AsynchPeople getAsynchPeople() {
        return new View.AsynchPeople(this);
    }

    /**
     * Does this {@link View} has any associated user information recorded?
     * @deprecated Potentially very expensive call; do not use from Jelly views.
     */
    public boolean hasPeople() {
        return View.People.isApplicable(items.values());
    }

    public Api getApi() {
        return new Api(this);
    }

    /**
     * Returns a secret key that survives across container start/stop.
     * <p>
     * This value is useful for implementing some of the security features.
     *
     * @deprecated
     *      Due to the past security advisory, this value should not be used any more to protect sensitive information.
     *      See {@link ConfidentialStore} and {@link ConfidentialKey} for how to store secrets.
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * Gets {@linkplain #getSecretKey() the secret key} as a key for AES-128.
     * @since 1.308
     * @deprecated
     *       See {@link #getSecretKey()}.
     */
    public SecretKey getSecretKeyAsAES128() {
        return Util.toAes128Key(secretKey);
    }

    /**
     * Returns the unique identifier of this Jenkins that has been historically used to identify
     * this Jenkins to the outside world.
     *
     * <p>
     * This form of identifier is weak in that it can be impersonated by others. See
     * https://wiki.jenkins-ci.org/display/JENKINS/Instance+Identity for more modern form of instance ID
     * that can be challenged and verified.
     *
     * @since 1.498
     */
    @SuppressWarnings("deprecation")
    public String getLegacyInstanceId() {
        return Util.getDigestOf(getSecretKey());
    }

    /**
     * Gets the SCM descriptor by name. Primarily used for making them web-visible.
     */
    public Descriptor<SCM> getScm(String shortClassName) {
        return findDescriptor(shortClassName,SCM.all());
    }

    /**
     * Gets the repository browser descriptor by name. Primarily used for making them web-visible.
     */
    public Descriptor<RepositoryBrowser<?>> getRepositoryBrowser(String shortClassName) {
        return findDescriptor(shortClassName,RepositoryBrowser.all());
    }

    /**
     * Gets the builder descriptor by name. Primarily used for making them web-visible.
     */
    public Descriptor<Builder> getBuilder(String shortClassName) {
        return findDescriptor(shortClassName, Builder.all());
    }

    /**
     * Gets the build wrapper descriptor by name. Primarily used for making them web-visible.
     */
    public Descriptor<BuildWrapper> getBuildWrapper(String shortClassName) {
        return findDescriptor(shortClassName, BuildWrapper.all());
    }

    /**
     * Gets the publisher descriptor by name. Primarily used for making them web-visible.
     */
    public Descriptor<Publisher> getPublisher(String shortClassName) {
        return findDescriptor(shortClassName, Publisher.all());
    }

    /**
     * Gets the trigger descriptor by name. Primarily used for making them web-visible.
     */
    public TriggerDescriptor getTrigger(String shortClassName) {
        return (TriggerDescriptor) findDescriptor(shortClassName, Trigger.all());
    }

    /**
     * Gets the retention strategy descriptor by name. Primarily used for making them web-visible.
     */
    public Descriptor<RetentionStrategy<?>> getRetentionStrategy(String shortClassName) {
        return findDescriptor(shortClassName, RetentionStrategy.all());
    }

    /**
     * Gets the {@link JobPropertyDescriptor} by name. Primarily used for making them web-visible.
     */
    public JobPropertyDescriptor getJobProperty(String shortClassName) {
        // combining these two lines triggers javac bug. See issue #610.
        Descriptor d = findDescriptor(shortClassName, JobPropertyDescriptor.all());
        return (JobPropertyDescriptor) d;
    }

    /**
     * @deprecated
     *      UI method. Not meant to be used programatically.
     */
    public ComputerSet getComputer() {
        return new ComputerSet();
    }

    /**
     * Exposes {@link Descriptor} by its name to URL.
     *
     * After doing all the {@code getXXX(shortClassName)} methods, I finally realized that
     * this just doesn't scale.
     *
     * @param id
     *      Either {@link Descriptor#getId()} (recommended) or the short name of a {@link Describable} subtype (for compatibility)
     * @throws IllegalArgumentException if a short name was passed which matches multiple IDs (fail fast)
     */
    @SuppressWarnings({"unchecked", "rawtypes"}) // too late to fix
    public Descriptor getDescriptor(String id) {
        // legacy descriptors that are reigstered manually doesn't show up in getExtensionList, so check them explicitly.
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

    /**
     * Alias for {@link #getDescriptor(String)}.
     */
    public Descriptor getDescriptorByName(String id) {
        return getDescriptor(id);
    }

    /**
     * Gets the {@link Descriptor} that corresponds to the given {@link Describable} type.
     * <p>
     * If you have an instance of {@code type} and call {@link Describable#getDescriptor()},
     * you'll get the same instance that this method returns.
     */
    public Descriptor getDescriptor(Class<? extends Describable> type) {
        for( Descriptor d : getExtensionList(Descriptor.class) )
            if(d.clazz==type)
                return d;
        return null;
    }

    /**
     * Works just like {@link #getDescriptor(Class)} but don't take no for an answer.
     *
     * @throws AssertionError
     *      If the descriptor is missing.
     * @since 1.326
     */
    public Descriptor getDescriptorOrDie(Class<? extends Describable> type) {
        Descriptor d = getDescriptor(type);
        if (d==null)
            throw new AssertionError(type+" is missing its descriptor");
        return d;
    }

    /**
     * Gets the {@link Descriptor} instance in the current Hudson by its type.
     */
    public <T extends Descriptor> T getDescriptorByType(Class<T> type) {
        for( Descriptor d : getExtensionList(Descriptor.class) )
            if(d.getClass()==type)
                return type.cast(d);
        return null;
    }

    /**
     * Gets the {@link SecurityRealm} descriptors by name. Primarily used for making them web-visible.
     */
    public Descriptor<SecurityRealm> getSecurityRealms(String shortClassName) {
        return findDescriptor(shortClassName,SecurityRealm.all());
    }

    /**
     * Finds a descriptor that has the specified name.
     */
    private <T extends Describable<T>>
    Descriptor<T> findDescriptor(String shortClassName, Collection<? extends Descriptor<T>> descriptors) {
        String name = '.'+shortClassName;
        for (Descriptor<T> d : descriptors) {
            if(d.clazz.getName().endsWith(name))
                return d;
        }
        return null;
    }

    protected void updateComputerList() throws IOException {
        updateComputerList(AUTOMATIC_SLAVE_LAUNCH);
    }

    /** @deprecated Use {@link SCMListener#all} instead. */
    @Deprecated
    public CopyOnWriteList<SCMListener> getSCMListeners() {
        return scmListeners;
    }

    /**
     * Gets the plugin object from its short name.
     *
     * <p>
     * This allows URL <tt>hudson/plugin/ID</tt> to be served by the views
     * of the plugin class.
     */
    public Plugin getPlugin(String shortName) {
        PluginWrapper p = pluginManager.getPlugin(shortName);
        if(p==null)     return null;
        return p.getPlugin();
    }

    /**
     * Gets the plugin object from its class.
     *
     * <p>
     * This allows easy storage of plugin information in the plugin singleton without
     * every plugin reimplementing the singleton pattern.
     *
     * @param clazz The plugin class (beware class-loader fun, this will probably only work
     * from within the jpi that defines the plugin class, it may or may not work in other cases)
     *
     * @return The plugin instance.
     */
    @SuppressWarnings("unchecked")
    public <P extends Plugin> P getPlugin(Class<P> clazz) {
        PluginWrapper p = pluginManager.getPlugin(clazz);
        if(p==null)     return null;
        return (P) p.getPlugin();
    }

    /**
     * Gets the plugin objects from their super-class.
     *
     * @param clazz The plugin class (beware class-loader fun)
     *
     * @return The plugin instances.
     */
    public <P extends Plugin> List<P> getPlugins(Class<P> clazz) {
        List<P> result = new ArrayList<P>();
        for (PluginWrapper w: pluginManager.getPlugins(clazz)) {
            result.add((P)w.getPlugin());
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Synonym for {@link #getDescription}.
     */
    public String getSystemMessage() {
        return systemMessage;
    }

    /**
     * Gets the markup formatter used in the system.
     *
     * @return
     *      never null.
     * @since 1.391
     */
    public @Nonnull MarkupFormatter getMarkupFormatter() {
        MarkupFormatter f = markupFormatter;
        return f != null ? f : new EscapedMarkupFormatter();
    }

    /**
     * Sets the markup formatter used in the system globally.
     *
     * @since 1.391
     */
    public void setMarkupFormatter(MarkupFormatter f) {
        this.markupFormatter = f;
    }

    /**
     * Sets the system message.
     */
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

    /**
     * Returns the transient {@link Action}s associated with the top page.
     *
     * <p>
     * Adding {@link Action} is primarily useful for plugins to contribute
     * an item to the navigation bar of the top page. See existing {@link Action}
     * implementation for it affects the GUI.
     *
     * <p>
     * To register an {@link Action}, implement {@link RootAction} extension point, or write code like
     * {@code Hudson.getInstance().getActions().add(...)}.
     *
     * @return
     *      Live list where the changes can be made. Can be empty but never null.
     * @since 1.172
     */
    public List<Action> getActions() {
        return actions;
    }

    /**
     * Gets just the immediate children of {@link Jenkins}.
     *
     * @see #getAllItems(Class)
     */
    @Exported(name="jobs")
    public List<TopLevelItem> getItems() {
		if (authorizationStrategy instanceof AuthorizationStrategy.Unsecured ||
			authorizationStrategy instanceof FullControlOnceLoggedInAuthorizationStrategy) {
			return new ArrayList(items.values());
		}

        List<TopLevelItem> viewableItems = new ArrayList<TopLevelItem>();
        for (TopLevelItem item : items.values()) {
            if (item.hasPermission(Item.READ))
                viewableItems.add(item);
        }
		
        return viewableItems;
    }

    /**
     * Returns the read-only view of all the {@link TopLevelItem}s keyed by their names.
     * <p>
     * This method is efficient, as it doesn't involve any copying.
     *
     * @since 1.296
     */
    public Map<String,TopLevelItem> getItemMap() {
        return Collections.unmodifiableMap(items);
    }

    /**
     * Gets just the immediate children of {@link Jenkins} but of the given type.
     */
    public <T> List<T> getItems(Class<T> type) {
        List<T> r = new ArrayList<T>();
        for (TopLevelItem i : getItems())
            if (type.isInstance(i))
                 r.add(type.cast(i));
        return r;
    }

    /**
     * Gets all the {@link Item}s recursively in the {@link ItemGroup} tree
     * and filter them by the given type.
     */
    public <T extends Item> List<T> getAllItems(Class<T> type) {
        return Items.getAllItems(this, type);
    }

    /**
     * Gets all the items recursively.
     *
     * @since 1.402
     */
    public List<Item> getAllItems() {
        return getAllItems(Item.class);
    }

    /**
     * Gets a list of simple top-level projects.
     * @deprecated This method will ignore Maven and matrix projects, as well as projects inside containers such as folders.
     * You may prefer to call {@link #getAllItems(Class)} on {@link AbstractProject},
     * perhaps also using {@link Util#createSubList} to consider only {@link TopLevelItem}s.
     * (That will also consider the caller's permissions.)
     * If you really want to get just {@link Project}s at top level, ignoring permissions,
     * you can filter the values from {@link #getItemMap} using {@link Util#createSubList}.
     */
    @Deprecated
    public List<Project> getProjects() {
        return Util.createSubList(items.values(),Project.class);
    }

    /**
     * Gets the names of all the {@link Job}s.
     */
    public Collection<String> getJobNames() {
        List<String> names = new ArrayList<String>();
        for (Job j : getAllItems(Job.class))
            names.add(j.getFullName());
        return names;
    }

    public List<Action> getViewActions() {
        return getActions();
    }

    /**
     * Gets the names of all the {@link TopLevelItem}s.
     */
    public Collection<String> getTopLevelItemNames() {
        List<String> names = new ArrayList<String>();
        for (TopLevelItem j : items.values())
            names.add(j.getName());
        return names;
    }

    public View getView(String name) {
        return viewGroupMixIn.getView(name);
    }

    /**
     * Gets the read-only list of all {@link View}s.
     */
    @Exported
    public Collection<View> getViews() {
        return viewGroupMixIn.getViews();
    }

    @Override
    public void addView(View v) throws IOException {
        viewGroupMixIn.addView(v);
    }

    public boolean canDelete(View view) {
        return viewGroupMixIn.canDelete(view);
    }

    public synchronized void deleteView(View view) throws IOException {
        viewGroupMixIn.deleteView(view);
    }

    public void onViewRenamed(View view, String oldName, String newName) {
        viewGroupMixIn.onViewRenamed(view,oldName,newName);
    }

    /**
     * Returns the primary {@link View} that renders the top-page of Hudson.
     */
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

    /**
     * Returns true if the current running Jenkins is upgraded from a version earlier than the specified version.
     *
     * <p>
     * This method continues to return true until the system configuration is saved, at which point
     * {@link #version} will be overwritten and Hudson forgets the upgrade history.
     *
     * <p>
     * To handle SNAPSHOTS correctly, pass in "1.N.*" to test if it's upgrading from the version
     * equal or younger than N. So say if you implement a feature in 1.301 and you want to check
     * if the installation upgraded from pre-1.301, pass in "1.300.*"
     *
     * @since 1.301
     */
    public boolean isUpgradedFromBefore(VersionNumber v) {
        try {
            return new VersionNumber(version).isOlderThan(v);
        } catch (IllegalArgumentException e) {
            // fail to parse this version number
            return false;
        }
    }

    /**
     * Gets the read-only list of all {@link Computer}s.
     */
    public Computer[] getComputers() {
        Computer[] r = computers.values().toArray(new Computer[computers.size()]);
        Arrays.sort(r,new Comparator<Computer>() {
            @Override public int compare(Computer lhs, Computer rhs) {
                if(lhs.getNode()==Jenkins.this)  return -1;
                if(rhs.getNode()==Jenkins.this)  return 1;
                return lhs.getName().compareTo(rhs.getName());
            }
        });
        return r;
    }

    @CLIResolver
    public @CheckForNull Computer getComputer(@Argument(required=true,metaVar="NAME",usage="Node name") @Nonnull String name) {
        if(name.equals("(master)"))
            name = "";

        for (Computer c : computers.values()) {
            if(c.getName().equals(name))
                return c;
        }
        return null;
    }

    /**
     * Gets the label that exists on this system by the name.
     *
     * @return null if name is null.
     * @see Label#parseExpression(String) (String)
     */
    public Label getLabel(String expr) {
        if(expr==null)  return null;
        expr = hudson.util.QuotedStringTokenizer.unquote(expr);
        while(true) {
            Label l = labels.get(expr);
            if(l!=null)
                return l;

            // non-existent
            try {
                labels.putIfAbsent(expr,Label.parseExpression(expr));
            } catch (ANTLRException e) {
                // laxly accept it as a single label atom for backward compatibility
                return getLabelAtom(expr);
            }
        }
    }

    /**
     * Returns the label atom of the given name.
     * @return non-null iff name is non-null
     */
    public @Nullable LabelAtom getLabelAtom(@CheckForNull String name) {
        if (name==null)  return null;

        while(true) {
            Label l = labels.get(name);
            if(l!=null)
                return (LabelAtom)l;

            // non-existent
            LabelAtom la = new LabelAtom(name);
            if (labels.putIfAbsent(name, la)==null)
                la.load();
        }
    }

    /**
     * Gets all the active labels in the current system.
     */
    public Set<Label> getLabels() {
        Set<Label> r = new TreeSet<Label>();
        for (Label l : labels.values()) {
            if(!l.isEmpty())
                r.add(l);
        }
        return r;
    }

    public Set<LabelAtom> getLabelAtoms() {
        Set<LabelAtom> r = new TreeSet<LabelAtom>();
        for (Label l : labels.values()) {
            if(!l.isEmpty() && l instanceof LabelAtom)
                r.add((LabelAtom)l);
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
        if(jdks==null)
            jdks = new ArrayList<JDK>();
        return jdks;
    }

    /**
     * Gets the JDK installation of the given name, or returns null.
     */
    public JDK getJDK(String name) {
        if(name==null) {
            // if only one JDK is configured, "default JDK" should mean that JDK.
            List<JDK> jdks = getJDKs();
            if(jdks.size()==1)  return jdks.get(0);
            return null;
        }
        for (JDK j : getJDKs()) {
            if(j.getName().equals(name))
                return j;
        }
        return null;
    }



    /**
     * Gets the slave node of the give name, hooked under this Hudson.
     */
    public @CheckForNull Node getNode(String name) {
        return slaves.getNode(name);
    }

    /**
     * Gets a {@link Cloud} by {@link Cloud#name its name}, or null.
     */
    public Cloud getCloud(String name) {
        return clouds.getByName(name);
    }

    protected Map<Node,Computer> getComputerMap() {
        return computers;
    }

    /**
     * Returns all {@link Node}s in the system, excluding {@link Jenkins} instance itself which
     * represents the master.
     */
    public List<Node> getNodes() {
        return slaves;
    }

    /**
     * Adds one more {@link Node} to Hudson.
     */
    public synchronized void addNode(Node n) throws IOException {
        if(n==null)     throw new IllegalArgumentException();
        ArrayList<Node> nl = new ArrayList<Node>(this.slaves);
        if(!nl.contains(n)) // defensive check
            nl.add(n);
        setNodes(nl);
    }

    /**
     * Removes a {@link Node} from Hudson.
     */
    public synchronized void removeNode(@Nonnull Node n) throws IOException {
        Computer c = n.toComputer();
        if (c!=null)
            c.disconnect(OfflineCause.create(Messages._Hudson_NodeBeingRemoved()));

        ArrayList<Node> nl = new ArrayList<Node>(this.slaves);
        nl.remove(n);
        setNodes(nl);
    }

    public void setNodes(List<? extends Node> nodes) throws IOException {
        this.slaves = new NodeList(nodes);
        updateComputerList();
        trimLabels();
        save();
    }

    public DescribableList<NodeProperty<?>, NodePropertyDescriptor> getNodeProperties() {
    	return nodeProperties;
    }

    public DescribableList<NodeProperty<?>, NodePropertyDescriptor> getGlobalNodeProperties() {
    	return globalNodeProperties;
    }

    /**
     * Resets all labels and remove invalid ones.
     *
     * This should be called when the assumptions behind label cache computation changes,
     * but we also call this periodically to self-heal any data out-of-sync issue.
     */
    private void trimLabels() {
        for (Iterator<Label> itr = labels.values().iterator(); itr.hasNext();) {
            Label l = itr.next();
            resetLabel(l);
            if(l.isEmpty())
                itr.remove();
        }
    }

    /**
     * Binds {@link AdministrativeMonitor}s to URL.
     */
    public AdministrativeMonitor getAdministrativeMonitor(String id) {
        for (AdministrativeMonitor m : administrativeMonitors)
            if(m.id.equals(id))
                return m;
        return null;
    }

    public NodeDescriptor getDescriptor() {
        return DescriptorImpl.INSTANCE;
    }

    public static final class DescriptorImpl extends NodeDescriptor {
        @Extension
        public static final DescriptorImpl INSTANCE = new DescriptorImpl();

        public String getDisplayName() {
            return "";
        }

        @Override
        public boolean isInstantiable() {
            return false;
        }

        public FormValidation doCheckNumExecutors(@QueryParameter String value) {
            return FormValidation.validateNonNegativeInteger(value);
        }

        public FormValidation doCheckRawBuildsDir(@QueryParameter String value) {
            if (!value.contains("${")) {
                File d = new File(value);
                if (!d.isDirectory() && (d.getParentFile() == null || !d.getParentFile().canWrite())) {
                    return FormValidation.error(value + " does not exist and probably cannot be created");
                }
                // TODO failure to use either ITEM_* variable might be an error too?
            }
            return FormValidation.ok(); // TODO assumes it will be OK after substitution, but can we be sure?
        }

        // to route /descriptor/FQCN/xxx to getDescriptor(FQCN).xxx
        public Object getDynamic(String token) {
            return Jenkins.getInstance().getDescriptor(token);
        }
    }

    /**
     * Gets the system default quiet period.
     */
    public int getQuietPeriod() {
        return quietPeriod!=null ? quietPeriod : 5;
    }

    /**
     * Sets the global quiet period.
     *
     * @param quietPeriod
     *      null to the default value.
     */
    public void setQuietPeriod(Integer quietPeriod) throws IOException {
        this.quietPeriod = quietPeriod;
        save();
    }

    /**
     * Gets the global SCM check out retry count.
     */
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
        return super.makeSearchIndex()
            .add("configure", "config","configure")
            .add("manage")
            .add("log")
            .add(new CollectionSearchIndex<TopLevelItem>() {
                protected SearchItem get(String key) { return getItemByFullName(key, TopLevelItem.class); }
                protected Collection<TopLevelItem> all() { return getAllItems(TopLevelItem.class); }
            })
            .add(getPrimaryView().makeSearchIndex())
            .add(new CollectionSearchIndex() {// for computers
                protected Computer get(String key) { return getComputer(key); }
                protected Collection<Computer> all() { return computers.values(); }
            })
            .add(new CollectionSearchIndex() {// for users
                protected User get(String key) { return User.get(key,false); }
                protected Collection<User> all() { return User.getAll(); }
            })
            .add(new CollectionSearchIndex() {// for views
                protected View get(String key) { return getView(key); }
                protected Collection<View> all() { return views; }
            });
    }

    public String getUrlChildPrefix() {
        return "job";
    }

    /**
     * Gets the absolute URL of Jenkins, such as {@code http://localhost/jenkins/}.
     *
     * <p>
     * This method first tries to use the manually configured value, then
     * fall back to {@link #getRootUrlFromRequest}.
     * It is done in this order so that it can work correctly even in the face
     * of a reverse proxy.
     *
     * @return null if this parameter is not configured by the user and the calling thread is not in an HTTP request; otherwise the returned URL will always have the trailing {@code /}
     * @since 1.66
     * @see <a href="https://wiki.jenkins-ci.org/display/JENKINS/Hyperlinks+in+HTML">Hyperlinks in HTML</a>
     */
    public @Nullable String getRootUrl() {
        String url = JenkinsLocationConfiguration.get().getUrl();
        if(url!=null) {
            return Util.ensureEndsWith(url,"/");
        }
        StaplerRequest req = Stapler.getCurrentRequest();
        if(req!=null)
            return getRootUrlFromRequest();
        return null;
    }

    /**
     * Is Jenkins running in HTTPS?
     *
     * Note that we can't really trust {@link StaplerRequest#isSecure()} because HTTPS might be terminated
     * in the reverse proxy.
     */
    public boolean isRootUrlSecure() {
        String url = getRootUrl();
        return url!=null && url.startsWith("https");
    }

    /**
     * Gets the absolute URL of Hudson top page, such as {@code http://localhost/hudson/}.
     *
     * <p>
     * Unlike {@link #getRootUrl()}, which uses the manually configured value,
     * this one uses the current request to reconstruct the URL. The benefit is
     * that this is immune to the configuration mistake (users often fail to set the root URL
     * correctly, especially when a migration is involved), but the downside
     * is that unless you are processing a request, this method doesn't work.
     *
     * <p>Please note that this will not work in all cases if Jenkins is running behind a
     * reverse proxy which has not been fully configured.
     * Specifically the {@code Host} and {@code X-Forwarded-Proto} headers must be set.
     * <a href="https://wiki.jenkins-ci.org/display/JENKINS/Running+Jenkins+behind+Apache">Running Jenkins behind Apache</a>
     * shows some examples of configuration.
     * @since 1.263
     */
    public @Nonnull String getRootUrlFromRequest() {
        StaplerRequest req = Stapler.getCurrentRequest();
        if (req == null) {
            throw new IllegalStateException("cannot call getRootUrlFromRequest from outside a request handling thread");
        }
        StringBuilder buf = new StringBuilder();
        String scheme = getXForwardedHeader(req, "X-Forwarded-Proto", req.getScheme());
        buf.append(scheme).append("://");
        String host = getXForwardedHeader(req, "X-Forwarded-Host", req.getServerName());
        buf.append(host);
        int port = req.getServerPort();
        String forwardedPort = getXForwardedHeader(req, "X-Forwarded-Port", null);
        if (forwardedPort != null) {
            try {
                port = Integer.parseInt(forwardedPort);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        if (port != ("https".equals(scheme) ? 443 : 80)) {
            buf.append(':').append(port);
        }
        buf.append(req.getContextPath()).append('/');
        return buf.toString();
    }

    /**
     * Gets the originating "X-Forwarded-..." header from the request. If there are multiple headers the originating
     * header is the first header. If the originating header contains a comma separated list, the originating entry
     * is the first one.
     * @param req the request
     * @param header the header name
     * @param defaultValue the value to return if the header is absent.
     * @return the originating entry of the header or the default value if the header was not present.
     */
    private static String getXForwardedHeader(StaplerRequest req, String header, String defaultValue) {
        String value = req.getHeader(header);
        if (value != null) {
            int index = value.indexOf(',');
            return index == -1 ? value.trim() : value.substring(0,index).trim();
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
        return new File(Util.replaceMacro(base, ImmutableMap.of(
                "JENKINS_HOME", getRootDir().getPath(),
                "ITEM_ROOTDIR", item.getRootDir().getPath(),
                "ITEM_FULLNAME", item.getFullName(),   // legacy, deprecated
                "ITEM_FULL_NAME", item.getFullName().replace(':','$')))); // safe, see JENKINS-12251
    }
    
    public String getRawWorkspaceDir() {
        return workspaceDir;
    }

    public String getRawBuildsDir() {
        return buildsDir;
    }

    public FilePath getRootPath() {
        return new FilePath(getRootDir());
    }

    @Override
    public FilePath createPath(String absolutePath) {
        return new FilePath((VirtualChannel)null,absolutePath);
    }

    public ClockDifference getClockDifference() {
        return ClockDifference.ZERO;
    }

    @Override
    public Callable<ClockDifference, IOException> getClockDifferenceCallable() {
        return new Callable<ClockDifference, IOException>() {
            public ClockDifference call() throws IOException {
                return new ClockDifference(0);
            }
        };
    }

    /**
     * For binding {@link LogRecorderManager} to "/log".
     * Everything below here is admin-only, so do the check here.
     */
    public LogRecorderManager getLog() {
        checkPermission(ADMINISTER);
        return log;
    }

    /**
     * A convenience method to check if there's some security
     * restrictions in place.
     */
    @Exported
    public boolean isUseSecurity() {
        return securityRealm!=SecurityRealm.NO_AUTHENTICATION || authorizationStrategy!=AuthorizationStrategy.UNSECURED;
    }
    
    public boolean isUseProjectNamingStrategy(){
        return projectNamingStrategy != DefaultProjectNamingStrategy.DEFAULT_NAMING_STRATEGY;
    }

    /**
     * If true, all the POST requests to Hudson would have to have crumb in it to protect
     * Hudson from CSRF vulnerabilities.
     */
    @Exported
    public boolean isUseCrumbs() {
        return crumbIssuer!=null;
    }

    /**
     * Returns the constant that captures the three basic security modes
     * in Hudson.
     */
    public SecurityMode getSecurity() {
        // fix the variable so that this code works under concurrent modification to securityRealm.
        SecurityRealm realm = securityRealm;

        if(realm==SecurityRealm.NO_AUTHENTICATION)
            return SecurityMode.UNSECURED;
        if(realm instanceof LegacySecurityRealm)
            return SecurityMode.LEGACY;
        return SecurityMode.SECURED;
    }

    /**
     * @return
     *      never null.
     */
    public SecurityRealm getSecurityRealm() {
        return securityRealm;
    }

    public void setSecurityRealm(SecurityRealm securityRealm) {
        if(securityRealm==null)
            securityRealm= SecurityRealm.NO_AUTHENTICATION;
        this.useSecurity = true;
        IdStrategy oldUserIdStrategy = this.securityRealm == null
                ? securityRealm.getUserIdStrategy() // don't trigger rekey on Jenkins load
                : this.securityRealm.getUserIdStrategy();
        this.securityRealm = securityRealm;
        // reset the filters and proxies for the new SecurityRealm
        try {
            HudsonFilter filter = HudsonFilter.get(servletContext);
            if (filter == null) {
                // Fix for #3069: This filter is not necessarily initialized before the servlets.
                // when HudsonFilter does come back, it'll initialize itself.
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
            // for binary compatibility, this method cannot throw a checked exception
            throw new AcegiSecurityException("Failed to configure filter",e) {};
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
        if(ns == null){
            ns = DefaultProjectNamingStrategy.DEFAULT_NAMING_STRATEGY;
        }
        projectNamingStrategy = ns;
    }
    
    public Lifecycle getLifecycle() {
        return Lifecycle.get();
    }

    /**
     * Gets the dependency injection container that hosts all the extension implementations and other
     * components in Jenkins.
     *
     * @since 1.433
     */
    public Injector getInjector() {
        return lookup(Injector.class);
    }

    /**
     * Returns {@link ExtensionList} that retains the discovered instances for the given extension type.
     *
     * @param extensionType
     *      The base type that represents the extension point. Normally {@link ExtensionPoint} subtype
     *      but that's not a hard requirement.
     * @return
     *      Can be an empty list but never null.
     * @see ExtensionList#lookup
     */
    @SuppressWarnings({"unchecked"})
    public <T> ExtensionList<T> getExtensionList(Class<T> extensionType) {
        return extensionLists.get(extensionType);
    }

    /**
     * Used to bind {@link ExtensionList}s to URLs.
     *
     * @since 1.349
     */
    public ExtensionList getExtensionList(String extensionType) throws ClassNotFoundException {
        return getExtensionList(pluginManager.uberClassLoader.loadClass(extensionType));
    }

    /**
     * Returns {@link ExtensionList} that retains the discovered {@link Descriptor} instances for the given
     * kind of {@link Describable}.
     *
     * @return
     *      Can be an empty list but never null.
     */
    @SuppressWarnings({"unchecked"})
    public <T extends Describable<T>,D extends Descriptor<T>> DescriptorExtensionList<T,D> getDescriptorList(Class<T> type) {
        return descriptorLists.get(type);
    }

    /**
     * Refresh {@link ExtensionList}s by adding all the newly discovered extensions.
     *
     * Exposed only for {@link PluginManager#dynamicLoad(File)}.
     */
    public void refreshExtensions() throws ExtensionRefreshException {
        ExtensionList<ExtensionFinder> finders = getExtensionList(ExtensionFinder.class);
        for (ExtensionFinder ef : finders) {
            if (!ef.isRefreshable())
                throw new ExtensionRefreshException(ef+" doesn't support refresh");
        }

        List<ExtensionComponentSet> fragments = Lists.newArrayList();
        for (ExtensionFinder ef : finders) {
            fragments.add(ef.refresh());
        }
        ExtensionComponentSet delta = ExtensionComponentSet.union(fragments).filtered();

        // if we find a new ExtensionFinder, we need it to list up all the extension points as well
        List<ExtensionComponent<ExtensionFinder>> newFinders = Lists.newArrayList(delta.find(ExtensionFinder.class));
        while (!newFinders.isEmpty()) {
            ExtensionFinder f = newFinders.remove(newFinders.size()-1).getInstance();

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

        // TODO: we need some generalization here so that extension points can be notified when a refresh happens?
        for (ExtensionComponent<RootAction> ea : delta.find(RootAction.class)) {
            Action a = ea.getInstance();
            if (!actions.contains(a)) actions.add(a);
        }
    }

    /**
     * Returns the root {@link ACL}.
     *
     * @see AuthorizationStrategy#getRootACL()
     */
    @Override
    public ACL getACL() {
        return authorizationStrategy.getRootACL();
    }

    /**
     * @return
     *      never null.
     */
    public AuthorizationStrategy getAuthorizationStrategy() {
        return authorizationStrategy;
    }
    
    /**
     * The strategy used to check the project names.
     * @return never <code>null</code>
     */
    public ProjectNamingStrategy getProjectNamingStrategy() {
        return projectNamingStrategy == null ? ProjectNamingStrategy.DEFAULT_NAMING_STRATEGY : projectNamingStrategy;
    }

    /**
     * Returns true if Hudson is quieting down.
     * <p>
     * No further jobs will be executed unless it
     * can be finished while other current pending builds
     * are still in progress.
     */
    @Exported
    public boolean isQuietingDown() {
        return isQuietingDown;
    }

    /**
     * Returns true if the container initiated the termination of the web application.
     */
    public boolean isTerminating() {
        return terminating;
    }

    /**
     * Gets the initialization milestone that we've already reached.
     *
     * @return
     *      {@link InitMilestone#STARTED} even if the initialization hasn't been started, so that this method
     *      never returns null.
     */
    public InitMilestone getInitLevel() {
        return initLevel;
    }

    public void setNumExecutors(int n) throws IOException {
        this.numExecutors = n;
        save();
    }



    /**
     * {@inheritDoc}.
     *
     * Note that the look up is case-insensitive.
     */
    public TopLevelItem getItem(String name) {
        if (name==null)    return null;
    	TopLevelItem item = items.get(name);
        if (item==null)
            return null;
        if (!item.hasPermission(Item.READ)) {
            if (item.hasPermission(Item.DISCOVER)) {
                throw new AccessDeniedException("Please login to access job " + name);
            }
            return null;
        }
        return item;
    }

    /**
     * Gets the item by its path name from the given context
     *
     * <h2>Path Names</h2>
     * <p>
     * If the name starts from '/', like "/foo/bar/zot", then it's interpreted as absolute.
     * Otherwise, the name should be something like "foo/bar" and it's interpreted like
     * relative path name in the file system is, against the given context.
     * <p>For compatibility, as a fallback when nothing else matches, a simple path
     * like {@code foo/bar} can also be treated with {@link #getItemByFullName}.
     * @param context
     *      null is interpreted as {@link Jenkins}. Base 'directory' of the interpretation.
     * @since 1.406
     */
    public Item getItem(String pathName, ItemGroup context) {
        if (context==null)  context = this;
        if (pathName==null) return null;

        if (pathName.startsWith("/"))   // absolute
            return getItemByFullName(pathName);

        Object/*Item|ItemGroup*/ ctx = context;

        StringTokenizer tokens = new StringTokenizer(pathName,"/");
        while (tokens.hasMoreTokens()) {
            String s = tokens.nextToken();
            if (s.equals("..")) {
                if (ctx instanceof Item) {
                    ctx = ((Item)ctx).getParent();
                    continue;
                }

                ctx=null;    // can't go up further
                break;
            }
            if (s.equals(".")) {
                continue;
            }

            if (ctx instanceof ItemGroup) {
                ItemGroup g = (ItemGroup) ctx;
                Item i = g.getItem(s);
                if (i==null || !i.hasPermission(Item.READ)) { // TODO consider DISCOVER
                    ctx=null;    // can't go up further
                    break;
                }
                ctx=i;
            } else {
                return null;
            }
        }

        if (ctx instanceof Item)
            return (Item)ctx;

        // fall back to the classic interpretation
        return getItemByFullName(pathName);
    }

    public final Item getItem(String pathName, Item context) {
        return getItem(pathName,context!=null?context.getParent():null);
    }

    public final <T extends Item> T getItem(String pathName, ItemGroup context, Class<T> type) {
        Item r = getItem(pathName, context);
        if (type.isInstance(r))
            return type.cast(r);
        return null;
    }

    public final <T extends Item> T getItem(String pathName, Item context, Class<T> type) {
        return getItem(pathName,context!=null?context.getParent():null,type);
    }

    public File getRootDirFor(TopLevelItem child) {
        return getRootDirFor(child.getName());
    }

    private File getRootDirFor(String name) {
        return new File(new File(getRootDir(),"jobs"), name);
    }

    /**
     * Gets the {@link Item} object by its full name.
     * Full names are like path names, where each name of {@link Item} is
     * combined by '/'.
     *
     * @return
     *      null if either such {@link Item} doesn't exist under the given full name,
     *      or it exists but it's no an instance of the given type.
     */
    public @CheckForNull <T extends Item> T getItemByFullName(String fullName, Class<T> type) {
        StringTokenizer tokens = new StringTokenizer(fullName,"/");
        ItemGroup parent = this;

        if(!tokens.hasMoreTokens()) return null;    // for example, empty full name.

        while(true) {
            Item item = parent.getItem(tokens.nextToken());
            if(!tokens.hasMoreTokens()) {
                if(type.isInstance(item))
                    return type.cast(item);
                else
                    return null;
            }

            if(!(item instanceof ItemGroup))
                return null;    // this item can't have any children

            if (!item.hasPermission(Item.READ))
                return null; // TODO consider DISCOVER

            parent = (ItemGroup) item;
        }
    }

    public @CheckForNull Item getItemByFullName(String fullName) {
        return getItemByFullName(fullName,Item.class);
    }

    /**
     * Gets the user of the given name.
     *
     * @return the user of the given name, if that person exists or the invoker {@link #hasPermission} on {@link #ADMINISTER}; else null
     * @see User#get(String,boolean)
     */
    public @CheckForNull User getUser(String name) {
        return User.get(name,hasPermission(ADMINISTER));
    }

    public synchronized TopLevelItem createProject( TopLevelItemDescriptor type, String name ) throws IOException {
        return createProject(type, name, true);
    }

    public synchronized TopLevelItem createProject( TopLevelItemDescriptor type, String name, boolean notify ) throws IOException {
        return itemGroupMixIn.createProject(type,name,notify);
    }

    /**
     * Overwrites the existing item by new one.
     *
     * <p>
     * This is a short cut for deleting an existing job and adding a new one.
     */
    public synchronized void putItem(TopLevelItem item) throws IOException, InterruptedException {
        String name = item.getName();
        TopLevelItem old = items.get(name);
        if (old ==item)  return; // noop

        checkPermission(Item.CREATE);
        if (old!=null)
            old.delete();
        items.put(name,item);
        ItemListener.fireOnCreated(item);
    }

    /**
     * Creates a new job.
     *
     * <p>
     * This version infers the descriptor from the type of the top-level item.
     *
     * @throws IllegalArgumentException
     *      if the project of the given name already exists.
     */
    public synchronized <T extends TopLevelItem> T createProject( Class<T> type, String name ) throws IOException {
        return type.cast(createProject((TopLevelItemDescriptor)getDescriptor(type),name));
    }

    /**
     * Called by {@link Job#renameTo(String)} to update relevant data structure.
     * assumed to be synchronized on Hudson by the caller.
     */
    public void onRenamed(TopLevelItem job, String oldName, String newName) throws IOException {
        items.remove(oldName);
        items.put(newName,job);

        // For compatibility with old views:
        for (View v : views)
            v.onJobRenamed(job, oldName, newName);
    }

    /**
     * Called in response to {@link Job#doDoDelete(StaplerRequest, StaplerResponse)}
     */
    public void onDeleted(TopLevelItem item) throws IOException {
        ItemListener.fireOnDeleted(item);

        items.remove(item.getName());
        // For compatibility with old views:
        for (View v : views)
            v.onJobRenamed(item, item.getName(), null);
    }

    @Override public boolean canAdd(TopLevelItem item) {
        return true;
    }

    @Override synchronized public <I extends TopLevelItem> I add(I item, String name) throws IOException, IllegalArgumentException {
        if (items.containsKey(name)) {
            throw new IllegalArgumentException("already an item '" + name + "'");
        }
        items.put(name, item);
        return item;
    }

    @Override public void remove(TopLevelItem item) throws IOException, IllegalArgumentException {
        items.remove(item.getName());
    }

    public FingerprintMap getFingerprintMap() {
        return fingerprintMap;
    }

    // if no finger print matches, display "not found page".
    public Object getFingerprint( String md5sum ) throws IOException {
        Fingerprint r = fingerprintMap.get(md5sum);
        if(r==null)     return new NoFingerprintMatch(md5sum);
        else            return r;
    }

    /**
     * Gets a {@link Fingerprint} object if it exists.
     * Otherwise null.
     */
    public Fingerprint _getFingerprint( String md5sum ) throws IOException {
        return fingerprintMap.get(md5sum);
    }

    /**
     * The file we save our configuration.
     */
    private XmlFile getConfigFile() {
        return new XmlFile(XSTREAM, new File(root,"config.xml"));
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

    private synchronized TaskBuilder loadTasks() throws IOException {
        File projectsDir = new File(root,"jobs");
        if(!projectsDir.getCanonicalFile().isDirectory() && !projectsDir.mkdirs()) {
            if(projectsDir.exists())
                throw new IOException(projectsDir+" is not a directory");
            throw new IOException("Unable to create "+projectsDir+"\nPermission issue? Please create this directory manually.");
        }
        File[] subdirs = projectsDir.listFiles(new FileFilter() {
            public boolean accept(File child) {
                return child.isDirectory() && Items.getConfigFile(child).exists();
            }
        });

        final Set<String> loadedNames = Collections.synchronizedSet(new HashSet<String>());

        TaskGraphBuilder g = new TaskGraphBuilder();
        Handle loadHudson = g.requires(EXTENSIONS_AUGMENTED).attains(JOB_LOADED).add("Loading global config", new Executable() {
            public void run(Reactor session) throws Exception {
                // JENKINS-8043: some slaves (eg. swarm slaves) are not saved into the config file
                // and will get overwritten when reloading. Make a backup copy now, and re-add them later
                NodeList oldSlaves = slaves;
                
                XmlFile cfg = getConfigFile();
                if (cfg.exists()) {
                    // reset some data that may not exist in the disk file
                    // so that we can take a proper compensation action later.
                    primaryView = null;
                    views.clear();

                    // load from disk
                    cfg.unmarshal(Jenkins.this);
                }

                // if we are loading old data that doesn't have this field
                if (slaves == null) slaves = new NodeList();

                clouds.setOwner(Jenkins.this);

                // JENKINS-8043: re-add the slaves which were not saved into the config file
                // and are now missing, but still connected.
                if (oldSlaves != null) {
                    ArrayList<Node> newSlaves = new ArrayList<Node>(slaves);
                    for (Node n: oldSlaves) {
                        if (n instanceof EphemeralNode) {
                            if(!newSlaves.contains(n)) {
                                newSlaves.add(n);
                            }
                        }
                    }
                    setNodes(newSlaves);
                }
            }
        });

        for (final File subdir : subdirs) {
            g.requires(loadHudson).attains(JOB_LOADED).notFatal().add("Loading job "+subdir.getName(),new Executable() {
                public void run(Reactor session) throws Exception {
                    TopLevelItem item = (TopLevelItem) Items.load(Jenkins.this, subdir);
                    items.put(item.getName(), item);
                    loadedNames.add(item.getName());
                }
            });
        }

        g.requires(JOB_LOADED).add("Cleaning up old builds",new Executable() {
            public void run(Reactor reactor) throws Exception {
                // anything we didn't load from disk, throw them away.
                // doing this after loading from disk allows newly loaded items
                // to inspect what already existed in memory (in case of reloading)

                // retainAll doesn't work well because of CopyOnWriteMap implementation, so remove one by one
                // hopefully there shouldn't be too many of them.
                for (String name : items.keySet()) {
                    if (!loadedNames.contains(name))
                        items.remove(name);
                }
            }
        });

        g.requires(JOB_LOADED).add("Finalizing set up",new Executable() {
            public void run(Reactor session) throws Exception {
                rebuildDependencyGraph();

                {// recompute label objects - populates the labels mapping.
                    for (Node slave : slaves)
                        // Note that not all labels are visible until the slaves have connected.
                        slave.getAssignedLabels();
                    getAssignedLabels();
                }

                // initialize views by inserting the default view if necessary
                // this is both for clean Hudson and for backward compatibility.
                if(views.size()==0 || primaryView==null) {
                    View v = new AllView(Messages.Hudson_ViewName());
                    setViewOwner(v);
                    views.add(0,v);
                    primaryView = v.getViewName();
                }

                if (useSecurity!=null && !useSecurity) {
                    // forced reset to the unsecure mode.
                    // this works as an escape hatch for people who locked themselves out.
                    authorizationStrategy = AuthorizationStrategy.UNSECURED;
                    setSecurityRealm(SecurityRealm.NO_AUTHENTICATION);
                } else {
                    // read in old data that doesn't have the security field set
                    if(authorizationStrategy==null) {
                        if(useSecurity==null)
                            authorizationStrategy = AuthorizationStrategy.UNSECURED;
                        else
                            authorizationStrategy = new LegacyAuthorizationStrategy();
                    }
                    if(securityRealm==null) {
                        if(useSecurity==null)
                            setSecurityRealm(SecurityRealm.NO_AUTHENTICATION);
                        else
                            setSecurityRealm(new LegacySecurityRealm());
                    } else {
                        // force the set to proxy
                        setSecurityRealm(securityRealm);
                    }
                }


                // Initialize the filter with the crumb issuer
                setCrumbIssuer(crumbIssuer);

                // auto register root actions
                for (Action a : getExtensionList(RootAction.class))
                    if (!actions.contains(a)) actions.add(a);
            }
        });

        return g;
    }

    /**
     * Save the settings to a file.
     */
    public synchronized void save() throws IOException {
        if(BulkChange.contains(this))   return;
        getConfigFile().write(this);
        SaveableListener.fireOnChange(this, getConfigFile());
    }


    /**
     * Called to shut down the system.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public void cleanUp() {
        for (ItemListener l : ItemListener.all())
            l.onBeforeShutdown();

        try {
            final TerminatorFinder tf = new TerminatorFinder(
                    pluginManager != null ? pluginManager.uberClassLoader : Thread.currentThread().getContextClassLoader());
            new Reactor(tf).execute(new Executor() {
                @Override
                public void execute(Runnable command) {
                    command.run();
                }
            });
        } catch (InterruptedException e) {
            LOGGER.log(SEVERE, "Failed to execute termination",e);
            e.printStackTrace();
        } catch (ReactorException e) {
            LOGGER.log(SEVERE, "Failed to execute termination",e);
        } catch (IOException e) {
            LOGGER.log(SEVERE, "Failed to execute termination",e);
        }

        Set<Future<?>> pending = new HashSet<Future<?>>();
        terminating = true;
        for( Computer c : computers.values() ) {
            c.interrupt();
            killComputer(c);
            pending.add(c.disconnect(null));
        }
        if(udpBroadcastThread!=null)
            udpBroadcastThread.shutdown();
        if(dnsMultiCast!=null)
            dnsMultiCast.close();
        interruptReloadThread();

        java.util.Timer timer = Trigger.timer;
        if (timer != null) {
            timer.cancel();
        }
        // TODO: how to wait for the completion of the last job?
        Trigger.timer = null;

        Timer.shutdown();

        if(tcpSlaveAgentListener!=null)
            tcpSlaveAgentListener.shutdown();

        if(pluginManager!=null) // be defensive. there could be some ugly timing related issues
            pluginManager.stop();

        if(getRootDir().exists())
            // if we are aborting because we failed to create JENKINS_HOME,
            // don't try to save. Issue #536
            getQueue().save();

        threadPoolForLoad.shutdown();
        for (Future<?> f : pending)
            try {
                f.get(10, TimeUnit.SECONDS);    // if clean up operation didn't complete in time, we fail the test
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;  // someone wants us to die now. quick!
            } catch (ExecutionException e) {
                LOGGER.log(Level.WARNING, "Failed to shut down properly",e);
            } catch (TimeoutException e) {
                LOGGER.log(Level.WARNING, "Failed to shut down properly",e);
            }

        LogFactory.releaseAll();

        theInstance = null;
    }

    public Object getDynamic(String token) {
        for (Action a : getActions()) {
            String url = a.getUrlName();
            if (url==null)  continue;
            if (url.equals(token) || url.equals('/' + token))
                return a;
        }
        for (Action a : getManagementLinks())
            if(a.getUrlName().equals(token))
                return a;
        return null;
    }


//
//
// actions
//
//
    /**
     * Accepts submission from the configuration page.
     */
    public synchronized void doConfigSubmit( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException, FormException {
        BulkChange bc = new BulkChange(this);
        try {
            checkPermission(ADMINISTER);

            JSONObject json = req.getSubmittedForm();

            workspaceDir = json.getString("rawWorkspaceDir");
            buildsDir = json.getString("rawBuildsDir");

            systemMessage = Util.nullify(req.getParameter("system_message"));

            jdks.clear();
            jdks.addAll(req.bindJSONToList(JDK.class,json.get("jdks")));

            boolean result = true;
            for (Descriptor<?> d : Functions.getSortedDescriptorsForGlobalConfigUnclassified())
                result &= configureDescriptor(req,json,d);

            version = VERSION;

            save();
            updateComputerList();
            if(result)
                FormApply.success(req.getContextPath()+'/').generateResponse(req, rsp, null);
            else
                FormApply.success("configure").generateResponse(req, rsp, null);    // back to config
        } finally {
            bc.commit();
        }
    }

    /**
     * Gets the {@link CrumbIssuer} currently in use.
     *
     * @return null if none is in use.
     */
    public CrumbIssuer getCrumbIssuer() {
        return crumbIssuer;
    }

    public void setCrumbIssuer(CrumbIssuer issuer) {
        crumbIssuer = issuer;
    }

    public synchronized void doTestPost( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        rsp.sendRedirect("foo");
    }

    private boolean configureDescriptor(StaplerRequest req, JSONObject json, Descriptor<?> d) throws FormException {
        // collapse the structure to remain backward compatible with the JSON structure before 1.
        String name = d.getJsonSafeClassName();
        JSONObject js = json.has(name) ? json.getJSONObject(name) : new JSONObject(); // if it doesn't have the property, the method returns invalid null object.
        json.putAll(js);
        return d.configure(req, js);
    }

    /**
     * Accepts submission from the node configuration page.
     */
    @RequirePOST
    public synchronized void doConfigExecutorsSubmit( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException, FormException {
        checkPermission(ADMINISTER);

        BulkChange bc = new BulkChange(this);
        try {
            JSONObject json = req.getSubmittedForm();

            MasterBuildConfiguration mbc = MasterBuildConfiguration.all().get(MasterBuildConfiguration.class);
            if (mbc!=null)
                mbc.configure(req,json);

            getNodeProperties().rebuild(req, json.optJSONObject("nodeProperties"), NodeProperty.all());
        } finally {
            bc.commit();
        }

        updateComputerList();

        rsp.sendRedirect(req.getContextPath()+'/'+toComputer().getUrl());  // back to the computer page
    }

    /**
     * Accepts the new description.
     */
    @RequirePOST
    public synchronized void doSubmitDescription( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        getPrimaryView().doSubmitDescription(req, rsp);
    }

    @RequirePOST // TODO does not seem to work on _either_ overload!
    public synchronized HttpRedirect doQuietDown() throws IOException {
        try {
            return doQuietDown(false,0);
        } catch (InterruptedException e) {
            throw new AssertionError(); // impossible
        }
    }

    @CLIMethod(name="quiet-down")
    @RequirePOST
    public HttpRedirect doQuietDown(
            @Option(name="-block",usage="Block until the system really quiets down and no builds are running") @QueryParameter boolean block,
            @Option(name="-timeout",usage="If non-zero, only block up to the specified number of milliseconds") @QueryParameter int timeout) throws InterruptedException, IOException {
        synchronized (this) {
            checkPermission(ADMINISTER);
            isQuietingDown = true;
        }
        if (block) {
            if (timeout > 0) timeout += System.currentTimeMillis();
            while (isQuietingDown
                   && (timeout <= 0 || System.currentTimeMillis() < timeout)
                   && !RestartListener.isAllReady()) {
                Thread.sleep(1000);
            }
        }
        return new HttpRedirect(".");
    }

    @CLIMethod(name="cancel-quiet-down")
    @RequirePOST // TODO the cancel link needs to be updated accordingly
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

    /**
     * Backward compatibility. Redirect to the thread dump.
     */
    public void doClassicThreadDump(StaplerResponse rsp) throws IOException, ServletException {
        rsp.sendRedirect2("threadDump");
    }

    /**
     * Obtains the thread dump of all slaves (including the master.)
     *
     * <p>
     * Since this is for diagnostics, it has a built-in precautionary measure against hang slaves.
     */
    public Map<String,Map<String,String>> getAllThreadDumps() throws IOException, InterruptedException {
        checkPermission(ADMINISTER);

        // issue the requests all at once
        Map<String,Future<Map<String,String>>> future = new HashMap<String, Future<Map<String, String>>>();

        for (Computer c : getComputers()) {
            try {
                future.put(c.getName(), RemotingDiagnostics.getThreadDumpAsync(c.getChannel()));
            } catch(Exception e) {
                LOGGER.info("Failed to get thread dump for node " + c.getName() + ": " + e.getMessage());
            }
        }
		if (toComputer() == null) {
			future.put("master", RemotingDiagnostics.getThreadDumpAsync(FilePath.localChannel));
		}

        // if the result isn't available in 5 sec, ignore that.
        // this is a precaution against hang nodes
        long endTime = System.currentTimeMillis() + 5000;

        Map<String,Map<String,String>> r = new HashMap<String, Map<String, String>>();
        for (Entry<String, Future<Map<String, String>>> e : future.entrySet()) {
            try {
                r.put(e.getKey(), e.getValue().get(endTime-System.currentTimeMillis(), TimeUnit.MILLISECONDS));
            } catch (Exception x) {
                StringWriter sw = new StringWriter();
                x.printStackTrace(new PrintWriter(sw,true));
                r.put(e.getKey(), Collections.singletonMap("Failed to retrieve thread dump",sw.toString()));
            }
        }
        return Collections.unmodifiableSortedMap(new TreeMap<String, Map<String, String>>(r));
    }

    @RequirePOST
    public synchronized TopLevelItem doCreateItem( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        return itemGroupMixIn.createTopLevelItem(req, rsp);
    }

    /**
     * @since 1.319
     */
    public TopLevelItem createProjectFromXML(String name, InputStream xml) throws IOException {
        return itemGroupMixIn.createProjectFromXML(name, xml);
    }


    @SuppressWarnings({"unchecked"})
    public <T extends TopLevelItem> T copy(T src, String name) throws IOException {
        return itemGroupMixIn.copy(src, name);
    }

    // a little more convenient overloading that assumes the caller gives us the right type
    // (or else it will fail with ClassCastException)
    public <T extends AbstractProject<?,?>> T copy(T src, String name) throws IOException {
        return (T)copy((TopLevelItem)src,name);
    }

    @RequirePOST
    public synchronized void doCreateView( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException, FormException {
        checkPermission(View.CREATE);
        addView(View.create(req,rsp, this));
    }

    /**
     * Check if the given name is suitable as a name
     * for job, view, etc.
     *
     * @throws Failure
     *      if the given name is not good
     */
    public static void checkGoodName(String name) throws Failure {
        if(name==null || name.length()==0)
            throw new Failure(Messages.Hudson_NoName());

        if(".".equals(name.trim())) 
            throw new Failure(Messages.Jenkins_NotAllowedName("."));
        if("..".equals(name.trim())) 
            throw new Failure(Messages.Jenkins_NotAllowedName(".."));
        for( int i=0; i<name.length(); i++ ) {
            char ch = name.charAt(i);
            if(Character.isISOControl(ch)) {
                throw new Failure(Messages.Hudson_ControlCodeNotAllowed(toPrintableName(name)));
            }
            if("?*/\\%!@#$^&|<>[]:;".indexOf(ch)!=-1)
                throw new Failure(Messages.Hudson_UnsafeChar(ch));
        }

        // looks good
    }

    /**
     * Makes sure that the given name is good as a job name.
     * @return trimmed name if valid; throws Failure if not
     */
    private String checkJobName(String name) throws Failure {
        checkGoodName(name);
        name = name.trim();
        projectNamingStrategy.checkName(name);
        if(getItem(name)!=null)
            throw new Failure(Messages.Hudson_JobAlreadyExists(name));
        // looks good
        return name;
    }

    private static String toPrintableName(String name) {
        StringBuilder printableName = new StringBuilder();
        for( int i=0; i<name.length(); i++ ) {
            char ch = name.charAt(i);
            if(Character.isISOControl(ch))
                printableName.append("\\u").append((int)ch).append(';');
            else
                printableName.append(ch);
        }
        return printableName.toString();
    }

    /**
     * Checks if the user was successfully authenticated.
     *
     * @see BasicAuthenticationFilter
     */
    public void doSecured( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        // TODO fire something in SecurityListener? (seems to be used only for REST calls when LegacySecurityRealm is active)

        if(req.getUserPrincipal()==null) {
            // authentication must have failed
            rsp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // the user is now authenticated, so send him back to the target
        String path = req.getContextPath()+req.getOriginalRestOfPath();
        String q = req.getQueryString();
        if(q!=null)
            path += '?'+q;

        rsp.sendRedirect2(path);
    }

    /**
     * Called once the user logs in. Just forward to the top page.
     * Used only by {@link LegacySecurityRealm}.
     */
    public void doLoginEntry( StaplerRequest req, StaplerResponse rsp ) throws IOException {
        if(req.getUserPrincipal()==null) {
            rsp.sendRedirect2("noPrincipal");
            return;
        }

        // TODO fire something in SecurityListener?

        String from = req.getParameter("from");
        if(from!=null && from.startsWith("/") && !from.equals("/loginError")) {
            rsp.sendRedirect2(from);    // I'm bit uncomfortable letting users redircted to other sites, make sure the URL falls into this domain
            return;
        }

        String url = AbstractProcessingFilter.obtainFullRequestUrl(req);
        if(url!=null) {
            // if the login redirect is initiated by Acegi
            // this should send the user back to where s/he was from.
            rsp.sendRedirect2(url);
            return;
        }

        rsp.sendRedirect2(".");
    }

    /**
     * Logs out the user.
     */
    public void doLogout( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        String user = getAuthentication().getName();
        securityRealm.doLogout(req, rsp);
        SecurityListener.fireLoggedOut(user);
    }

    /**
     * Serves jar files for JNLP slave agents.
     */
    public Slave.JnlpJar getJnlpJars(String fileName) {
        return new Slave.JnlpJar(fileName);
    }

    public Slave.JnlpJar doJnlpJars(StaplerRequest req) {
        return new Slave.JnlpJar(req.getRestOfPath().substring(1));
    }

    /**
     * Reloads the configuration.
     */
    @CLIMethod(name="reload-configuration")
    @RequirePOST
    public synchronized HttpResponse doReload() throws IOException {
        checkPermission(ADMINISTER);

        // engage "loading ..." UI and then run the actual task in a separate thread
        servletContext.setAttribute("app", new HudsonIsLoading());

        new Thread("Jenkins config reload thread") {
            @Override
            public void run() {
                try {
                    ACL.impersonate(ACL.SYSTEM);
                    reload();
                } catch (Exception e) {
                    LOGGER.log(SEVERE,"Failed to reload Jenkins config",e);
                    new JenkinsReloadFailed(e).publish(servletContext,root);
                }
            }
        }.start();

        return HttpResponses.redirectViaContextPath("/");
    }

    /**
     * Reloads the configuration synchronously.
     */
    public void reload() throws IOException, InterruptedException, ReactorException {
        executeReactor(null, loadTasks());
        User.reload();
        servletContext.setAttribute("app", this);
    }

    /**
     * Do a finger-print check.
     */
    public void doDoFingerprintCheck( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        // Parse the request
        MultipartFormDataParser p = new MultipartFormDataParser(req);
        if(isUseCrumbs() && !getCrumbIssuer().validateCrumb(req, p)) {
            rsp.sendError(HttpServletResponse.SC_FORBIDDEN,"No crumb found");
        }
        try {
            rsp.sendRedirect2(req.getContextPath()+"/fingerprint/"+
                Util.getDigestOf(p.getFileItem("name").getInputStream())+'/');
        } finally {
            p.cleanUp();
        }
    }

    /**
     * For debugging. Expose URL to perform GC.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("DM_GC")
    @RequirePOST
    public void doGc(StaplerResponse rsp) throws IOException {
        checkPermission(Jenkins.ADMINISTER);
        System.gc();
        rsp.setStatus(HttpServletResponse.SC_OK);
        rsp.setContentType("text/plain");
        rsp.getWriter().println("GCed");
    }

    /**
     * End point that intentionally throws an exception to test the error behaviour.
     * @since 1.467
     */
    public void doException() {
        throw new RuntimeException();
    }

    public ContextMenu doContextMenu(StaplerRequest request, StaplerResponse response) throws IOException, JellyException {
        ContextMenu menu = new ContextMenu().from(this, request, response);
        for (MenuItem i : menu.items) {
            if (i.url.equals(request.getContextPath() + "/manage")) {
                // add "Manage Jenkins" subitems
                i.subMenu = new ContextMenu().from(this, request, response, "manage");
            }
        }
        return menu;
    }

    public ContextMenu doChildrenContextMenu(StaplerRequest request, StaplerResponse response) throws Exception {
        ContextMenu menu = new ContextMenu();
        for (View view : getViews()) {
            menu.add(view.getViewUrl(),view.getDisplayName());
        }
        return menu;
    }

    /**
     * Obtains the heap dump.
     */
    public HeapDump getHeapDump() throws IOException {
        return new HeapDump(this,FilePath.localChannel);
    }

    /**
     * Simulates OutOfMemoryError.
     * Useful to make sure OutOfMemoryHeapDump setting.
     */
    @RequirePOST
    public void doSimulateOutOfMemory() throws IOException {
        checkPermission(ADMINISTER);

        System.out.println("Creating artificial OutOfMemoryError situation");
        List<Object> args = new ArrayList<Object>();
        while (true)
            args.add(new byte[1024*1024]);
    }

    /**
     * Binds /userContent/... to $JENKINS_HOME/userContent.
     */
    public DirectoryBrowserSupport doUserContent() {
        return new DirectoryBrowserSupport(this,getRootPath().child("userContent"),"User content","folder.png",true);
    }

    /**
     * Perform a restart of Hudson, if we can.
     *
     * This first replaces "app" to {@link HudsonIsRestarting}
     */
    @CLIMethod(name="restart")
    public void doRestart(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, RestartNotSupportedException {
        checkPermission(ADMINISTER);
        if (req != null && req.getMethod().equals("GET")) {
            req.getView(this,"_restart.jelly").forward(req,rsp);
            return;
        }

        restart();

        if (rsp != null) // null for CLI
            rsp.sendRedirect2(".");
    }

    /**
     * Queues up a restart of Hudson for when there are no builds running, if we can.
     *
     * This first replaces "app" to {@link HudsonIsRestarting}
     *
     * @since 1.332
     */
    @CLIMethod(name="safe-restart")
    public HttpResponse doSafeRestart(StaplerRequest req) throws IOException, ServletException, RestartNotSupportedException {
        checkPermission(ADMINISTER);
        if (req != null && req.getMethod().equals("GET"))
            return HttpResponses.forwardToView(this,"_safeRestart.jelly");

        safeRestart();

        return HttpResponses.redirectToDot();
    }

    /**
     * Performs a restart.
     */
    public void restart() throws RestartNotSupportedException {
        final Lifecycle lifecycle = Lifecycle.get();
        lifecycle.verifyRestartable(); // verify that Hudson is restartable
        servletContext.setAttribute("app", new HudsonIsRestarting());

        new Thread("restart thread") {
            final String exitUser = getAuthentication().getName();
            @Override
            public void run() {
                try {
                    ACL.impersonate(ACL.SYSTEM);

                    // give some time for the browser to load the "reloading" page
                    Thread.sleep(5000);
                    LOGGER.severe(String.format("Restarting VM as requested by %s",exitUser));
                    for (RestartListener listener : RestartListener.all())
                        listener.onRestart();
                    lifecycle.restart();
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Failed to restart Hudson",e);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to restart Hudson",e);
                }
            }
        }.start();
    }

    /**
     * Queues up a restart to be performed once there are no builds currently running.
     * @since 1.332
     */
    public void safeRestart() throws RestartNotSupportedException {
        final Lifecycle lifecycle = Lifecycle.get();
        lifecycle.verifyRestartable(); // verify that Hudson is restartable
        // Quiet down so that we won't launch new builds.
        isQuietingDown = true;

        new Thread("safe-restart thread") {
            final String exitUser = getAuthentication().getName();
            @Override
            public void run() {
                try {
                    ACL.impersonate(ACL.SYSTEM);

                    // Wait 'til we have no active executors.
                    doQuietDown(true, 0);

                    // Make sure isQuietingDown is still true.
                    if (isQuietingDown) {
                        servletContext.setAttribute("app",new HudsonIsRestarting());
                        // give some time for the browser to load the "reloading" page
                        LOGGER.info("Restart in 10 seconds");
                        Thread.sleep(10000);
                        LOGGER.severe(String.format("Restarting VM as requested by %s",exitUser));
                        for (RestartListener listener : RestartListener.all())
                            listener.onRestart();
                        lifecycle.restart();
                    } else {
                        LOGGER.info("Safe-restart mode cancelled");
                    }
                } catch (Throwable e) {
                    LOGGER.log(Level.WARNING, "Failed to restart Jenkins",e);
                }
            }
        }.start();
    }

    @Extension @Restricted(NoExternalUse.class)
    public static class MasterRestartNotifyier extends RestartListener {

        @Override
        public void onRestart() {
            Computer computer = Jenkins.getInstance().toComputer();
            if (computer == null) return;
            RestartCause cause = new RestartCause();
            for (ComputerListener listener: ComputerListener.all()) {
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

    /**
     * Shutdown the system.
     * @since 1.161
     */
    @CLIMethod(name="shutdown")
    @RequirePOST
    public void doExit( StaplerRequest req, StaplerResponse rsp ) throws IOException {
        checkPermission(ADMINISTER);
        LOGGER.severe(String.format("Shutting down VM as requested by %s from %s",
                getAuthentication().getName(), req!=null?req.getRemoteAddr():"???"));
        if (rsp!=null) {
            rsp.setStatus(HttpServletResponse.SC_OK);
            rsp.setContentType("text/plain");
            PrintWriter w = rsp.getWriter();
            w.println("Shutting down");
            w.close();
        }

        System.exit(0);
    }


    /**
     * Shutdown the system safely.
     * @since 1.332
     */
    @CLIMethod(name="safe-shutdown")
    @RequirePOST
    public HttpResponse doSafeExit(StaplerRequest req) throws IOException {
        checkPermission(ADMINISTER);
        isQuietingDown = true;
        final String exitUser = getAuthentication().getName();
        final String exitAddr = req!=null ? req.getRemoteAddr() : "unknown";
        new Thread("safe-exit thread") {
            @Override
            public void run() {
                try {
                    ACL.impersonate(ACL.SYSTEM);
                    LOGGER.severe(String.format("Shutting down VM as requested by %s from %s",
                                                exitUser, exitAddr));
                    // Wait 'til we have no active executors.
                    while (isQuietingDown
                           && (overallLoad.computeTotalExecutors() > overallLoad.computeIdleExecutors())) {
                        Thread.sleep(5000);
                    }
                    // Make sure isQuietingDown is still true.
                    if (isQuietingDown) {
                        cleanUp();
                        System.exit(0);
                    }
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Failed to shutdown Hudson",e);
                }
            }
        }.start();

        return HttpResponses.plainText("Shutting down as soon as all jobs are complete");
    }

    /**
     * Gets the {@link Authentication} object that represents the user
     * associated with the current request.
     */
    public static @Nonnull Authentication getAuthentication() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        // on Tomcat while serving the login page, this is null despite the fact
        // that we have filters. Looking at the stack trace, Tomcat doesn't seem to
        // run the request through filters when this is the login request.
        // see http://www.nabble.com/Matrix-authorization-problem-tp14602081p14886312.html
        if(a==null)
            a = ANONYMOUS;
        return a;
    }

    /**
     * For system diagnostics.
     * Run arbitrary Groovy script.
     */
    public void doScript(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        _doScript(req, rsp, req.getView(this, "_script.jelly"), FilePath.localChannel, getACL());
    }

    /**
     * Run arbitrary Groovy script and return result as plain text.
     */
    public void doScriptText(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        _doScript(req, rsp, req.getView(this, "_scriptText.jelly"), FilePath.localChannel, getACL());
    }

    /**
     * @since 1.509.1
     */
    public static void _doScript(StaplerRequest req, StaplerResponse rsp, RequestDispatcher view, VirtualChannel channel, ACL acl) throws IOException, ServletException {
        // ability to run arbitrary script is dangerous
        acl.checkPermission(RUN_SCRIPTS);

        String text = req.getParameter("script");
        if (text != null) {
            if (!"POST".equals(req.getMethod())) {
                throw HttpResponses.error(HttpURLConnection.HTTP_BAD_METHOD, "requires POST");
            }
            try {
                req.setAttribute("output",
                        RemotingDiagnostics.executeGroovy(text, channel));
            } catch (InterruptedException e) {
                throw new ServletException(e);
            }
        }

        view.forward(req, rsp);
    }

    /**
     * Evaluates the Jelly script submitted by the client.
     *
     * This is useful for system administration as well as unit testing.
     */
    @RequirePOST
    public void doEval(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        checkPermission(RUN_SCRIPTS);

        try {
            MetaClass mc = WebApp.getCurrent().getMetaClass(getClass());
            Script script = mc.classLoader.loadTearOff(JellyClassLoaderTearOff.class).createContext().compileScript(new InputSource(req.getReader()));
            new JellyRequestDispatcher(this,script).forward(req,rsp);
        } catch (JellyException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Sign up for the user account.
     */
    public void doSignup( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        req.getView(getSecurityRealm(), "signup.jelly").forward(req, rsp);
    }

    /**
     * Changes the icon size by changing the cookie
     */
    public void doIconSize( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
        String qs = req.getQueryString();
        if(qs==null)
            throw new ServletException();
        Cookie cookie = new Cookie("iconSize", Functions.validateIconSize(qs));
        cookie.setMaxAge(/* ~4 mo. */9999999); // #762
        rsp.addCookie(cookie);
        String ref = req.getHeader("Referer");
        if(ref==null)   ref=".";
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

    /**
     * If the user chose the default JDK, make sure we got 'java' in PATH.
     */
    public FormValidation doDefaultJDKCheck(StaplerRequest request, @QueryParameter String value) {
        if(!value.equals("(Default)"))
            // assume the user configured named ones properly in system config ---
            // or else system config should have reported form field validation errors.
            return FormValidation.ok();

        // default JDK selected. Does such java really exist?
        if(JDK.isDefaultJDKValid(Jenkins.this))
            return FormValidation.ok();
        else
            return FormValidation.errorWithMarkup(Messages.Hudson_NoJavaInPath(request.getContextPath()));
    }

    /**
     * Makes sure that the given name is good as a job name.
     */
    public FormValidation doCheckJobName(@QueryParameter String value) {
        // this method can be used to check if a file exists anywhere in the file system,
        // so it should be protected.
        checkPermission(Item.CREATE);

        if(fixEmpty(value)==null)
            return FormValidation.ok();

        try {
            checkJobName(value);
            return FormValidation.ok();
        } catch (Failure e) {
            return FormValidation.error(e.getMessage());
        }
    }

    /**
     * Checks if a top-level view with the given name exists and 
     * make sure that the name is good as a view name.
     */
    public FormValidation doCheckViewName(@QueryParameter String value) {
        checkPermission(View.CREATE);
        
        String name = fixEmpty(value);
        if (name == null) 
            return FormValidation.ok();
        
        // already exists?
        if (getView(name) != null) 
            return FormValidation.error(Messages.Hudson_ViewAlreadyExists(name));
        
        // good view name?
        try {
            checkGoodName(name);
        } catch (Failure e) {
            return FormValidation.error(e.getMessage());
        }

        return FormValidation.ok();
    }
    
    /**
     * Checks if a top-level view with the given name exists.
     * @deprecated 1.512
     */
    public FormValidation doViewExistsCheck(@QueryParameter String value) {
        checkPermission(View.CREATE);

        String view = fixEmpty(value);
        if(view==null) return FormValidation.ok();

        if(getView(view)==null)
            return FormValidation.ok();
        else
            return FormValidation.error(Messages.Hudson_ViewAlreadyExists(view));
    }

    /**
     * Serves static resources placed along with Jelly view files.
     * <p>
     * This method can serve a lot of files, so care needs to be taken
     * to make this method secure. It's not clear to me what's the best
     * strategy here, though the current implementation is based on
     * file extensions.
     */
    public void doResources(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        String path = req.getRestOfPath();
        // cut off the "..." portion of /resources/.../path/to/file
        // as this is only used to make path unique (which in turn
        // allows us to set a long expiration date
        path = path.substring(path.indexOf('/',1)+1);

        int idx = path.lastIndexOf('.');
        String extension = path.substring(idx+1);
        if(ALLOWED_RESOURCE_EXTENSIONS.contains(extension)) {
            URL url = pluginManager.uberClassLoader.getResource(path);
            if(url!=null) {
                long expires = MetaClass.NO_CACHE ? 0 : 365L * 24 * 60 * 60 * 1000; /*1 year*/
                rsp.serveFile(req,url,expires);
                return;
            }
        }
        rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Extension list that {@link #doResources(StaplerRequest, StaplerResponse)} can serve.
     * This set is mutable to allow plugins to add additional extensions.
     */
    public static final Set<String> ALLOWED_RESOURCE_EXTENSIONS = new HashSet<String>(Arrays.asList(
        "js|css|jpeg|jpg|png|gif|html|htm".split("\\|")
    ));

    /**
     * Checks if container uses UTF-8 to decode URLs. See
     * http://wiki.jenkins-ci.org/display/JENKINS/Tomcat#Tomcat-i18n
     */
    public FormValidation doCheckURIEncoding(StaplerRequest request) throws IOException {
        // expected is non-ASCII String
        final String expected = "\u57f7\u4e8b";
        final String value = fixEmpty(request.getParameter("value"));
        if (!expected.equals(value))
            return FormValidation.warningWithMarkup(Messages.Hudson_NotUsesUTF8ToDecodeURL());
        return FormValidation.ok();
    }

    /**
     * Does not check when system default encoding is "ISO-8859-1".
     */
    public static boolean isCheckURIEncodingEnabled() {
        return !"ISO-8859-1".equalsIgnoreCase(System.getProperty("file.encoding"));
    }

    /**
     * Rebuilds the dependency map.
     */
    public void rebuildDependencyGraph() {
        DependencyGraph graph = new DependencyGraph();
        graph.build();
        // volatile acts a as a memory barrier here and therefore guarantees 
        // that graph is fully build, before it's visible to other threads
        dependencyGraph = graph;
        dependencyGraphDirty.set(false);
    }

    /**
     * Rebuilds the dependency map asynchronously.
     *
     * <p>
     * This would keep the UI thread more responsive and helps avoid the deadlocks,
     * as dependency graph recomputation tends to touch a lot of other things.
     *
     * @since 1.522
     */
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

    // for Jelly
    public List<ManagementLink> getManagementLinks() {
        return ManagementLink.all();
    }

    /**
     * Exposes the current user to <tt>/me</tt> URL.
     */
    public User getMe() {
        User u = User.current();
        if (u == null)
            throw new AccessDeniedException("/me is not available when not logged in");
        return u;
    }

    /**
     * Gets the {@link Widget}s registered on this object.
     *
     * <p>
     * Plugins who wish to contribute boxes on the side panel can add widgets
     * by {@code getWidgets().add(new MyWidget())} from {@link Plugin#start()}.
     */
    public List<Widget> getWidgets() {
        return widgets;
    }

    public Object getTarget() {
        try {
            checkPermission(READ);
        } catch (AccessDeniedException e) {
            String rest = Stapler.getCurrentRequest().getRestOfPath();
            if(rest.startsWith("/login")
            || rest.startsWith("/logout")
            || rest.startsWith("/accessDenied")
            || rest.startsWith("/adjuncts/")
            || rest.startsWith("/error")
            || rest.startsWith("/oops")
            || rest.startsWith("/signup")
            || rest.startsWith("/tcpSlaveAgentListener")
            // TODO SlaveComputer.doSlaveAgentJnlp; there should be an annotation to request unprotected access
            || rest.matches("/computer/[^/]+/slave-agent[.]jnlp") && "true".equals(Stapler.getCurrentRequest().getParameter("encrypt"))
            || rest.startsWith("/federatedLoginService/")
            || rest.startsWith("/securityRealm"))
                return this;    // URLs that are always visible without READ permission

            for (String name : getUnprotectedRootActions()) {
                if (rest.startsWith("/" + name + "/") || rest.equals("/" + name)) {
                    return this;
                }
            }

            throw e;
        }
        return this;
    }

    /**
     * Gets a list of unprotected root actions.
     * These URL prefixes should be exempted from access control checks by container-managed security.
     * Ideally would be synchronized with {@link #getTarget}.
     * @return a list of {@linkplain Action#getUrlName URL names}
     * @since 1.495
     */
    public Collection<String> getUnprotectedRootActions() {
        Set<String> names = new TreeSet<String>();
        names.add("jnlpJars"); // TODO cleaner to refactor doJnlpJars into a URA
        // TODO consider caching (expiring cache when actions changes)
        for (Action a : getActions()) {
            if (a instanceof UnprotectedRootAction) {
                names.add(a.getUrlName());
            }
        }
        return names;
    }

    /**
     * Fallback to the primary view.
     */
    public View getStaplerFallback() {
        return getPrimaryView();
    }

    /**
     * This method checks all existing jobs to see if displayName is 
     * unique. It does not check the displayName against the displayName of the
     * job that the user is configuring though to prevent a validation warning 
     * if the user sets the displayName to what it currently is.
     * @param displayName
     * @param currentJobName
     */
    boolean isDisplayNameUnique(String displayName, String currentJobName) {
        Collection<TopLevelItem> itemCollection = items.values();
        
        // if there are a lot of projects, we'll have to store their 
        // display names in a HashSet or something for a quick check
        for(TopLevelItem item : itemCollection) {
            if(item.getName().equals(currentJobName)) {
                // we won't compare the candidate displayName against the current
                // item. This is to prevent an validation warning if the user 
                // sets the displayName to what the existing display name is
                continue;
            }
            else if(displayName.equals(item.getDisplayName())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * True if there is no item in Jenkins that has this name
     * @param name The name to test
     * @param currentJobName The name of the job that the user is configuring
     */
    boolean isNameUnique(String name, String currentJobName) {
        Item item = getItem(name);
        
        if(null==item) {
            // the candidate name didn't return any items so the name is unique
            return true;
        }
        else if(item.getName().equals(currentJobName)) {
            // the candidate name returned an item, but the item is the item
            // that the user is configuring so this is ok
            return true;
        } 
        else {
            // the candidate name returned an item, so it is not unique
            return false;
        }
    }
    
    /**
     * Checks to see if the candidate displayName collides with any 
     * existing display names or project names
     * @param displayName The display name to test
     * @param jobName The name of the job the user is configuring
     */
    public FormValidation doCheckDisplayName(@QueryParameter String displayName, 
            @QueryParameter String jobName) {
        displayName = displayName.trim();
        
        if(LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Current job name is " + jobName);
        }
        
        if(!isNameUnique(displayName, jobName)) {
            return FormValidation.warning(Messages.Jenkins_CheckDisplayName_NameNotUniqueWarning(displayName));
        }
        else if(!isDisplayNameUnique(displayName, jobName)){
            return FormValidation.warning(Messages.Jenkins_CheckDisplayName_DisplayNameNotUniqueWarning(displayName));
        }
        else {
            return FormValidation.ok();
        }
    }
    
    public static class MasterComputer extends Computer {
        protected MasterComputer() {
            super(Jenkins.getInstance());
        }

        /**
         * Returns "" to match with {@link Jenkins#getNodeName()}.
         */
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

        /**
         * Will always keep this guy alive so that it can function as a fallback to
         * execute {@link FlyweightTask}s. See JENKINS-7291.
         */
        @Override
        protected boolean isAlive() {
            return true;
        }

        /**
         * Report an error.
         */
        @Override
        public HttpResponse doDoDelete() throws IOException {
            throw HttpResponses.status(SC_BAD_REQUEST);
        }

        @Override
        public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, FormException {
            Jenkins.getInstance().doConfigExecutorsSubmit(req, rsp);
        }

        @Override
        public boolean hasPermission(Permission permission) {
            // no one should be allowed to delete the master.
            // this hides the "delete" link from the /computer/(master) page.
            if(permission==Computer.DELETE)
                return false;
            // Configuration of master node requires ADMINISTER permission
            return super.hasPermission(permission==Computer.CONFIGURE ? Jenkins.ADMINISTER : permission);
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
            // this computer never returns null from channel, so
            // this method shall never be invoked.
            rsp.sendError(SC_NOT_FOUND);
        }

        protected Future<?> _connect(boolean forceReconnect) {
            return Futures.precomputed(null);
        }

        /**
         * {@link LocalChannel} instance that can be used to execute programs locally.
         *
         * @deprecated as of 1.558
         *      Use {@link FilePath#localChannel}
         */
        public static final LocalChannel localChannel = FilePath.localChannel;
    }

    /**
     * Shortcut for {@code Jenkins.getInstance().lookup.get(type)}
     */
    public static @CheckForNull <T> T lookup(Class<T> type) {
        Jenkins j = Jenkins.getInstance();
        return j != null ? j.lookup.get(type) : null;
    }

    /**
     * Live view of recent {@link LogRecord}s produced by Hudson.
     */
    public static List<LogRecord> logRecords = Collections.emptyList(); // initialized to dummy value to avoid NPE

    /**
     * Thread-safe reusable {@link XStream}.
     */
    public static final XStream XSTREAM = new XStream2();

    /**
     * Alias to {@link #XSTREAM} so that one can access additional methods on {@link XStream2} more easily.
     */
    public static final XStream2 XSTREAM2 = (XStream2)XSTREAM;

    private static final int TWICE_CPU_NUM = Math.max(4, Runtime.getRuntime().availableProcessors() * 2);

    /**
     * Thread pool used to load configuration in parallel, to improve the start up time.
     * <p>
     * The idea here is to overlap the CPU and I/O, so we want more threads than CPU numbers.
     */
    /*package*/ transient final ExecutorService threadPoolForLoad = new ThreadPoolExecutor(
        TWICE_CPU_NUM, TWICE_CPU_NUM,
        5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamingThreadFactory(new DaemonThreadFactory(), "Jenkins load"));


    private static void computeVersion(ServletContext context) {
        // set the version
        Properties props = new Properties();
        InputStream is = null;
        try {
            is = Jenkins.class.getResourceAsStream("jenkins-version.properties");
            if(is!=null)
                props.load(is);
        } catch (IOException e) {
            e.printStackTrace(); // if the version properties is missing, that's OK.
        } finally {
            IOUtils.closeQuietly(is);
        }
        String ver = props.getProperty("version");
        if(ver==null)   ver="?";
        VERSION = ver;
        context.setAttribute("version",ver);

        VERSION_HASH = Util.getDigestOf(ver).substring(0, 8);
        SESSION_HASH = Util.getDigestOf(ver+System.currentTimeMillis()).substring(0, 8);

        if(ver.equals("?") || Boolean.getBoolean("hudson.script.noCache"))
            RESOURCE_PATH = "";
        else
            RESOURCE_PATH = "/static/"+SESSION_HASH;

        VIEW_RESOURCE_PATH = "/resources/"+ SESSION_HASH;
    }

    /**
     * Version number of this Hudson.
     */
    public static String VERSION="?";

    /**
     * Parses {@link #VERSION} into {@link VersionNumber}, or null if it's not parseable as a version number
     * (such as when Hudson is run with "mvn hudson-dev:run")
     */
    public static VersionNumber getVersion() {
        try {
            return new VersionNumber(VERSION);
        } catch (NumberFormatException e) {
            try {
                // for non-released version of Hudson, this looks like "1.345 (private-foobar), so try to approximate.
                int idx = VERSION.indexOf(' ');
                if (idx>0)
                    return new VersionNumber(VERSION.substring(0,idx));
            } catch (NumberFormatException _) {
                // fall through
            }

            // totally unparseable
            return null;
        } catch (IllegalArgumentException e) {
            // totally unparseable
            return null;
        }
    }

    /**
     * Hash of {@link #VERSION}.
     */
    public static String VERSION_HASH;

    /**
     * Unique random token that identifies the current session.
     * Used to make {@link #RESOURCE_PATH} unique so that we can set long "Expires" header.
     * 
     * We used to use {@link #VERSION_HASH}, but making this session local allows us to
     * reuse the same {@link #RESOURCE_PATH} for static resources in plugins.
     */
    public static String SESSION_HASH;

    /**
     * Prefix to static resources like images and javascripts in the war file.
     * Either "" or strings like "/static/VERSION", which avoids Jenkins to pick up
     * stale cache when the user upgrades to a different version.
     * <p>
     * Value computed in {@link WebAppMain}.
     */
    public static String RESOURCE_PATH = "";

    /**
     * Prefix to resources alongside view scripts.
     * Strings like "/resources/VERSION", which avoids Hudson to pick up
     * stale cache when the user upgrades to a different version.
     * <p>
     * Value computed in {@link WebAppMain}.
     */
    public static String VIEW_RESOURCE_PATH = "/resources/TBD";

    public static boolean PARALLEL_LOAD = Configuration.getBooleanConfigParameter("parallelLoad", true);
    public static boolean KILL_AFTER_LOAD = Configuration.getBooleanConfigParameter("killAfterLoad", false);
    /**
     * Enabled by default as of 1.337. Will keep it for a while just in case we have some serious problems.
     */
    public static boolean FLYWEIGHT_SUPPORT = Configuration.getBooleanConfigParameter("flyweightSupport", true);

    /**
     * Tentative switch to activate the concurrent build behavior.
     * When we merge this back to the trunk, this allows us to keep
     * this feature hidden for a while until we iron out the kinks.
     * @see AbstractProject#isConcurrentBuild()
     * @deprecated as of 1.464
     *      This flag will have no effect.
     */
    @Restricted(NoExternalUse.class)
    public static boolean CONCURRENT_BUILD = true;

    /**
     * Switch to enable people to use a shorter workspace name.
     */
    private static final String WORKSPACE_DIRNAME = Configuration.getStringConfigParameter("workspaceDirName", "workspace");

    /**
     * Automatically try to launch a slave when Jenkins is initialized or a new slave is created.
     */
    public static boolean AUTOMATIC_SLAVE_LAUNCH = true;

    private static final Logger LOGGER = Logger.getLogger(Jenkins.class.getName());

    public static final PermissionGroup PERMISSIONS = Permission.HUDSON_PERMISSIONS;
    public static final Permission ADMINISTER = Permission.HUDSON_ADMINISTER;
    public static final Permission READ = new Permission(PERMISSIONS,"Read",Messages._Hudson_ReadPermission_Description(),Permission.READ,PermissionScope.JENKINS);
    public static final Permission RUN_SCRIPTS = new Permission(PERMISSIONS, "RunScripts", Messages._Hudson_RunScriptsPermission_Description(),ADMINISTER,PermissionScope.JENKINS);

    /**
     * {@link Authentication} object that represents the anonymous user.
     * Because Acegi creates its own {@link AnonymousAuthenticationToken} instances, the code must not
     * expect the singleton semantics. This is just a convenient instance.
     *
     * @since 1.343
     */
    public static final Authentication ANONYMOUS = new AnonymousAuthenticationToken(
            "anonymous","anonymous",new GrantedAuthority[]{new GrantedAuthorityImpl("anonymous")});

    static {
        XSTREAM.alias("jenkins",Jenkins.class);
        XSTREAM.alias("slave", DumbSlave.class);
        XSTREAM.alias("jdk",JDK.class);
        // for backward compatibility with <1.75, recognize the tag name "view" as well.
        XSTREAM.alias("view", ListView.class);
        XSTREAM.alias("listView", ListView.class);
        XSTREAM2.addCriticalField(Jenkins.class, "securityRealm");
        XSTREAM2.addCriticalField(Jenkins.class, "authorizationStrategy");
        // this seems to be necessary to force registration of converter early enough
        Mode.class.getEnumConstants();

        // double check that initialization order didn't do any harm
        assert PERMISSIONS!=null;
        assert ADMINISTER!=null;
    }

}
