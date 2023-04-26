package hudson.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher.ProcStarter;
import hudson.slaves.Cloud;
import jenkins.security.stapler.StaplerDispatchable;
import hudson.Util;
import hudson.cli.declarative.CLIResolver;
import hudson.console.AnnotatedLargeText;
import hudson.init.Initializer;
import hudson.model.Descriptor.FormException;
import hudson.model.Queue.FlyweightTask;
import hudson.model.labels.LabelAtom;
import hudson.model.queue.WorkUnit;
import hudson.node_monitors.NodeMonitor;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.PermissionScope;
import hudson.slaves.AbstractCloudSlave;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.ComputerListener;
import hudson.slaves.NodeProperty;
import hudson.slaves.RetentionStrategy;
import hudson.slaves.WorkspaceList;
import hudson.slaves.OfflineCause;
import hudson.slaves.OfflineCause.ByCLI;
import hudson.util.DaemonThreadFactory;
import hudson.util.EditDistance;
import hudson.util.ExceptionCatchingThreadFactory;
import hudson.util.RemotingDiagnostics;
import hudson.util.RemotingDiagnostics.HeapDump;
import hudson.util.RunList;
import hudson.util.Futures;
import hudson.util.IOUtils;
import hudson.util.NamingThreadFactory;
import jenkins.model.Jenkins;
import jenkins.util.ContextResettingExecutorService;
import jenkins.util.SystemProperties;
import jenkins.security.MasterToSlaveCallable;
import jenkins.security.ImpersonatingExecutorService;
import org.apache.commons.lang.StringUtils;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.GuardedBy;
import javax.servlet.ServletException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.logging.LogRecord;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.charset.Charset;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Inet4Address;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import static javax.servlet.http.HttpServletResponse.*;

@ExportedBean
public abstract class Computer extends Actionable implements AccessControlled, ExecutorListener, DescriptorByNameOwner {

    private final CopyOnWriteArrayList<Executor> executors = new CopyOnWriteArrayList<>();

    private final CopyOnWriteArrayList<OneOffExecutor> oneOffExecutors = new CopyOnWriteArrayList<>();

    private int numExecutors;

    protected volatile OfflineCause offlineCause;

    private long connectTime = 0;

    private boolean temporarilyOffline;

    protected String nodeName;

    private volatile String cachedHostName;

    private volatile boolean hostNameCached;

    private volatile EnvVars cachedEnvironment;

    private final WorkspaceList workspaceList = new WorkspaceList();

    protected transient List<Action> transientActions;

    protected final Object statusChangeLock = new Object();

    private transient final List<TerminationRequest> terminatedBy = Collections.synchronizedList(new ArrayList<>());

    public void recordTermination() {
        StaplerRequest request = Stapler.getCurrentRequest();
        if (request != null) {
            terminatedBy.add(new TerminationRequest(String.format("Termination requested at %s by %s [id=%d] from HTTP request for %s", new Date(), Thread.currentThread(), Thread.currentThread().getId(), request.getRequestURL())));
        } else {
            terminatedBy.add(new TerminationRequest(String.format("Termination requested at %s by %s [id=%d]", new Date(), Thread.currentThread(), Thread.currentThread().getId())));
        }
    }

    public List<TerminationRequest> getTerminatedBy() {
        return new ArrayList<>(terminatedBy);
    }

    public Computer(Node node) {
        setNode(node);
    }

    public List<ComputerPanelBox> getComputerPanelBoxs() {
        return ComputerPanelBox.all(this);
    }

    @SuppressWarnings("deprecation")
    public List<Action> getActions() {
        List<Action> result = new ArrayList<>(super.getActions());
        synchronized (this) {
            if (transientActions == null) {
                transientActions = TransientComputerActionFactory.createAllFor(this);
            }
            result.addAll(transientActions);
        }
        return Collections.unmodifiableList(result);
    }

    @SuppressWarnings({ "ConstantConditions", "deprecation" })
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    @Override
    public void addAction(@Nonnull Action a) {
        if (a == null) {
            throw new IllegalArgumentException("Action must be non-null");
        }
        super.getActions().add(a);
    }

    @Nonnull
    public File getLogFile() {
        return new File(getLogDir(), "slave.log");
    }

    @Nonnull
    protected File getLogDir() {
        File dir = new File(Jenkins.get().getRootDir(), "logs/slaves/" + nodeName);
        try {
            IOUtils.mkdirs(dir);
        } catch (IOException x) {
            LOGGER.log(Level.SEVERE, "Failed to create agent log directory " + dir, x);
        }
        return dir;
    }

    public WorkspaceList getWorkspaceList() {
        return workspaceList;
    }

    public String getLog() throws IOException {
        return Util.loadFile(getLogFile());
    }

    public AnnotatedLargeText<Computer> getLogText() {
        checkPermission(CONNECT);
        return new AnnotatedLargeText<>(getLogFile(), Charset.defaultCharset(), false, this);
    }

    public ACL getACL() {
        return Jenkins.get().getAuthorizationStrategy().getACL(this);
    }

    @Exported
    public OfflineCause getOfflineCause() {
        return offlineCause;
    }

    @Exported
    public String getOfflineCauseReason() {
        if (offlineCause == null) {
            return "";
        }
        String gsub_base = hudson.slaves.Messages.SlaveComputer_DisconnectedBy("", "");
        String gsub1 = "^" + gsub_base + "[\\w\\W]* \\: ";
        String gsub2 = "^" + gsub_base + "[\\w\\W]*";
        String newString = offlineCause.toString().replaceAll(gsub1, "");
        return newString.replaceAll(gsub2, "");
    }

    @Nullable
    public abstract VirtualChannel getChannel();

    public abstract Charset getDefaultCharset();

    public abstract List<LogRecord> getLogRecords() throws IOException, InterruptedException;

    public abstract void doLaunchSlaveAgent(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException;

    @Deprecated
    public final void launch() {
        connect(true);
    }

    public final Future<?> connect(boolean forceReconnect) {
        connectTime = System.currentTimeMillis();
        return _connect(forceReconnect);
    }

    protected abstract Future<?> _connect(boolean forceReconnect);

    @Deprecated
    public void cliConnect(boolean force) throws ExecutionException, InterruptedException {
        checkPermission(CONNECT);
        connect(force).get();
    }

    public final long getConnectTime() {
        return connectTime;
    }

    public Future<?> disconnect(OfflineCause cause) {
        recordTermination();
        offlineCause = cause;
        if (Util.isOverridden(Computer.class, getClass(), "disconnect"))
            return disconnect();
        connectTime = 0;
        return Futures.precomputed(null);
    }

    @Deprecated
    public Future<?> disconnect() {
        recordTermination();
        if (Util.isOverridden(Computer.class, getClass(), "disconnect", OfflineCause.class))
            return disconnect(null);
        connectTime = 0;
        return Futures.precomputed(null);
    }

    @Deprecated
    public void cliDisconnect(String cause) throws ExecutionException, InterruptedException {
        checkPermission(DISCONNECT);
        disconnect(new ByCLI(cause)).get();
    }

    @Deprecated
    public void cliOffline(String cause) throws ExecutionException, InterruptedException {
        checkPermission(DISCONNECT);
        setTemporarilyOffline(true, new ByCLI(cause));
    }

    @Deprecated
    public void cliOnline() throws ExecutionException, InterruptedException {
        checkPermission(CONNECT);
        setTemporarilyOffline(false, null);
    }

    @Exported
    public int getNumExecutors() {
        return numExecutors;
    }

    @Nonnull
    public String getName() {
        return nodeName != null ? nodeName : "";
    }

    @CheckForNull
    public abstract Boolean isUnix();

    @CheckForNull
    public Node getNode() {
        Jenkins j = Jenkins.getInstanceOrNull();
        if (j == null) {
            return null;
        }
        if (nodeName == null) {
            return j;
        }
        return j.getNode(nodeName);
    }

    @Exported
    public LoadStatistics getLoadStatistics() {
        return LabelAtom.get(nodeName != null ? nodeName : Jenkins.get().getSelfLabel().toString()).loadStatistics;
    }

    public BuildTimelineWidget getTimeline() {
        return new BuildTimelineWidget(getBuilds());
    }

    @Override
    public void taskAccepted(Executor executor, Queue.Task task) {
    }

    @Override
    public void taskCompleted(Executor executor, Queue.Task task, long durationMS) {
    }

    @Override
    public void taskCompletedWithProblems(Executor executor, Queue.Task task, long durationMS, Throwable problems) {
    }

    @Exported
    public boolean isOffline() {
        return temporarilyOffline || getChannel() == null;
    }

    public final boolean isOnline() {
        return !isOffline();
    }

    @Exported
    public boolean isManualLaunchAllowed() {
        return getRetentionStrategy().isManualLaunchAllowed(this);
    }

    public abstract boolean isConnecting();

    @Exported
    @Deprecated
    public boolean isJnlpAgent() {
        return false;
    }

    @Exported
    public boolean isLaunchSupported() {
        return true;
    }

    @Exported
    @Deprecated
    public boolean isTemporarilyOffline() {
        return temporarilyOffline;
    }

    @Deprecated
    public void setTemporarilyOffline(boolean temporarilyOffline) {
        setTemporarilyOffline(temporarilyOffline, null);
    }

    public void setTemporarilyOffline(boolean temporarilyOffline, OfflineCause cause) {
        offlineCause = temporarilyOffline ? cause : null;
        this.temporarilyOffline = temporarilyOffline;
        Node node = getNode();
        if (node != null) {
            node.setTemporaryOfflineCause(offlineCause);
        }
        synchronized (statusChangeLock) {
            statusChangeLock.notifyAll();
        }
        for (ComputerListener cl : ComputerListener.all()) {
            if (temporarilyOffline)
                cl.onTemporarilyOffline(this, cause);
            else
                cl.onTemporarilyOnline(this);
        }
    }

    @Exported
    public String getIcon() {
        if (isTemporarilyOffline() && getOfflineCause() instanceof OfflineCause.UserCause)
            return "computer-user-offline.png";
        if (isOffline() || !isAcceptingTasks())
            return "computer-x.png";
            return "computer.png";
    }

    @Exported
    public String getIconClassName() {
        if (isTemporarilyOffline() && getOfflineCause() instanceof OfflineCause.UserCause)
            return "icon-computer-user-offline";
        if (isOffline() || !isAcceptingTasks())
            return "icon-computer-x";
            return "icon-computer";
    }

    public String getIconAltText() {
        if (isTemporarilyOffline() && getOfflineCause() instanceof OfflineCause.UserCause)
            return "[temporarily offline by user]";
        if (isOffline() || !isAcceptingTasks())
            return "[offline]";
            return "[online]";
    }

    @Exported
    @Override
    @Nonnull
    public String getDisplayName() {
        return nodeName;
    }

    public String getCaption() {
        return Messages.Computer_Caption(nodeName);
    }

    public String getUrl() {
        return "computer/" + Util.rawEncode(getName()) + "/";
    }

    @Exported
    public Set<LabelAtom> getAssignedLabels() {
        Node node = getNode();
        return (node != null) ? node.getAssignedLabels() : Collections.EMPTY_SET;
    }

    public List<AbstractProject> getTiedJobs() {
        Node node = getNode();
        return (node != null) ? node.getSelfLabel().getTiedJobs() : Collections.EMPTY_LIST;
    }

    public RunList getBuilds() {
        return RunList.fromJobs((Iterable) Jenkins.get().allItems(Job.class)).node(getNode());
    }

    protected void setNode(Node node) {
        assert node != null;
        if (node instanceof Slave)
            this.nodeName = node.getNodeName();
        else
            this.nodeName = null;
        setNumExecutors(node.getNumExecutors());
        if (this.temporarilyOffline) {
            node.setTemporaryOfflineCause(this.offlineCause);
        }
    }

    protected void kill() {
        setNumExecutors(0);
    }

    @Restricted(NoExternalUse.class)
    @GuardedBy("hudson.model.Queue.lock")
    void inflictMortalWound() {
        setNumExecutors(0);
    }

    protected void onRemoved() {
    }

    @GuardedBy("hudson.model.Queue.lock")
    private void setNumExecutors(int n) {
        this.numExecutors = n;
        final int diff = executors.size() - n;
        if (diff > 0) {
            Queue.withLock(new Runnable() {

                @Override
                public void run() {
                    for (Executor e : executors) if (e.isIdle())
                        e.interrupt();
                }
            });
        }
        if (diff < 0) {
            addNewExecutorIfNecessary();
        }
    }

    private void addNewExecutorIfNecessary() {
        if (Jenkins.getInstanceOrNull() == null) {
            return;
        }
        Set<Integer> availableNumbers = new HashSet<>();
        for (int i = 0; i < numExecutors; i++) availableNumbers.add(i);
        for (Executor executor : executors) availableNumbers.remove(executor.getNumber());
        for (Integer number : availableNumbers) {
            if (executors.size() < numExecutors) {
                Executor e = new Executor(this, number);
                executors.add(e);
            }
        }
    }

    public int countIdle() {
        int n = 0;
        for (Executor e : executors) {
            if (e.isIdle())
                n++;
        }
        return n;
    }

    public final int countBusy() {
        return countExecutors() - countIdle();
    }

    public final int countExecutors() {
        return executors.size();
    }

    @Exported
    @StaplerDispatchable
    public List<Executor> getExecutors() {
        return new ArrayList<>(executors);
    }

    @Exported
    @StaplerDispatchable
    public List<OneOffExecutor> getOneOffExecutors() {
        return new ArrayList<>(oneOffExecutors);
    }

    public List<Executor> getAllExecutors() {
        List<Executor> result = new ArrayList<>(executors.size() + oneOffExecutors.size());
        result.addAll(executors);
        result.addAll(oneOffExecutors);
        return result;
    }

    @Restricted(NoExternalUse.class)
    public List<DisplayExecutor> getDisplayExecutors() {
        List<DisplayExecutor> result = new ArrayList<>(executors.size() + oneOffExecutors.size());
        int index = 0;
        for (Executor e : executors) {
            if (e.isDisplayCell()) {
                result.add(new DisplayExecutor(Integer.toString(index + 1), String.format("executors/%d", index), e));
            }
            index++;
        }
        index = 0;
        for (OneOffExecutor e : oneOffExecutors) {
            if (e.isDisplayCell()) {
                result.add(new DisplayExecutor("", String.format("oneOffExecutors/%d", index), e));
            }
            index++;
        }
        return result;
    }

    @Exported
    public final boolean isIdle() {
        if (!oneOffExecutors.isEmpty())
            return false;
        for (Executor e : executors) if (!e.isIdle())
            return false;
        return true;
    }

    public final boolean isPartiallyIdle() {
        for (Executor e : executors) if (e.isIdle())
            return true;
        return false;
    }

    public final long getIdleStartMilliseconds() {
        long firstIdle = Long.MIN_VALUE;
        for (Executor e : oneOffExecutors) {
            firstIdle = Math.max(firstIdle, e.getIdleStartMilliseconds());
        }
        for (Executor e : executors) {
            firstIdle = Math.max(firstIdle, e.getIdleStartMilliseconds());
        }
        return firstIdle;
    }

    public final long getDemandStartMilliseconds() {
        long firstDemand = Long.MAX_VALUE;
        for (Queue.BuildableItem item : Jenkins.get().getQueue().getBuildableItems(this)) {
            firstDemand = Math.min(item.buildableStartMilliseconds, firstDemand);
        }
        return firstDemand;
    }

    @Restricted(DoNotUse.class)
    @Exported
    @Nonnull
    public String getDescription() {
        Node node = getNode();
        return (node != null) ? node.getNodeDescription() : null;
    }

    protected void removeExecutor(final Executor e) {
        final Runnable task = new Runnable() {

            @Override
            public void run() {
                synchronized (Computer.this) {
                    executors.remove(e);
                    oneOffExecutors.remove(e);
                    addNewExecutorIfNecessary();
                    if (!isAlive()) {
                        AbstractCIBase ciBase = Jenkins.getInstanceOrNull();
                        if (ciBase != null) {
                            ciBase.removeComputer(Computer.this);
                        }
                    }
                }
            }
        };
        if (!Queue.tryWithLock(task)) {
            threadPoolForRemoting.submit(Queue.wrapWithLock(task));
        }
    }

    protected boolean isAlive() {
        for (Executor e : executors) if (e.isActive())
            return true;
        return false;
    }

    public void interrupt() {
        Queue.withLock(new Runnable() {

            @Override
            public void run() {
                for (Executor e : executors) {
                    e.interruptForShutdown();
                }
            }
        });
    }

    public String getSearchUrl() {
        return getUrl();
    }

    public abstract RetentionStrategy getRetentionStrategy();

    @Exported(inline = true)
    public Map<String, Object> getMonitorData() {
        Map<String, Object> r = new HashMap<>();
        if (hasPermission(CONNECT)) {
            for (NodeMonitor monitor : NodeMonitor.getAll()) r.put(monitor.getClass().getName(), monitor.data(this));
        }
        return r;
    }

    public Map<Object, Object> getSystemProperties() throws IOException, InterruptedException {
        return RemotingDiagnostics.getSystemProperties(getChannel());
    }

    @Deprecated
    public Map<String, String> getEnvVars() throws IOException, InterruptedException {
        return getEnvironment();
    }

    public EnvVars getEnvironment() throws IOException, InterruptedException {
        EnvVars cachedEnvironment = this.cachedEnvironment;
        if (cachedEnvironment != null) {
            return new EnvVars(cachedEnvironment);
        }
        cachedEnvironment = EnvVars.getRemote(getChannel());
        this.cachedEnvironment = cachedEnvironment;
        return new EnvVars(cachedEnvironment);
    }

    @Nonnull
    public EnvVars buildEnvironment(@Nonnull TaskListener listener) throws IOException, InterruptedException {
        EnvVars env = new EnvVars();
        Node node = getNode();
        if (node == null)
            return env;
        for (NodeProperty nodeProperty : Jenkins.get().getGlobalNodeProperties()) {
            nodeProperty.buildEnvVars(env, listener);
        }
        for (NodeProperty nodeProperty : node.getNodeProperties()) {
            nodeProperty.buildEnvVars(env, listener);
        }
        String rootUrl = Jenkins.get().getRootUrl();
        if (rootUrl != null) {
            env.put("HUDSON_URL", rootUrl);
            env.put("JENKINS_URL", rootUrl);
        }
        return env;
    }

    public Map<String, String> getThreadDump() throws IOException, InterruptedException {
        return RemotingDiagnostics.getThreadDump(getChannel());
    }

    public HeapDump getHeapDump() throws IOException {
        return new HeapDump(this, getChannel());
    }

    public String getHostName() throws IOException, InterruptedException {
        if (hostNameCached)
            return cachedHostName;
        VirtualChannel channel = getChannel();
        if (channel == null)
            return null;
        for (String address : channel.call(new ListPossibleNames())) {
            try {
                InetAddress ia = InetAddress.getByName(address);
                if (!(ia instanceof Inet4Address)) {
                    LOGGER.log(Level.FINE, "{0} is not an IPv4 address", address);
                    continue;
                }
                if (!ComputerPinger.checkIsReachable(ia, 3)) {
                    LOGGER.log(Level.FINE, "{0} didn't respond to ping", address);
                    continue;
                }
                cachedHostName = ia.getCanonicalHostName();
                hostNameCached = true;
                return cachedHostName;
            } catch (IOException e) {
                LogRecord lr = new LogRecord(Level.FINE, "Failed to parse {0}");
                lr.setThrown(e);
                lr.setParameters(new Object[] { address });
                LOGGER.log(lr);
            }
        }
        cachedHostName = channel.call(new GetFallbackName());
        hostNameCached = true;
        return cachedHostName;
    }

    final void startFlyWeightTask(WorkUnit p) {
        OneOffExecutor e = new OneOffExecutor(this);
        e.start(p);
        oneOffExecutors.add(e);
    }

    final void remove(OneOffExecutor e) {
        oneOffExecutors.remove(e);
    }

    private static class ListPossibleNames extends MasterToSlaveCallable<List<String>, IOException> {

        private static final Logger LOGGER = Logger.getLogger(ListPossibleNames.class.getName());

        public List<String> call() throws IOException {
            List<String> names = new ArrayList<>();
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                LOGGER.log(Level.FINE, "Listing up IP addresses for {0}", ni.getDisplayName());
                Enumeration<InetAddress> e = ni.getInetAddresses();
                while (e.hasMoreElements()) {
                    InetAddress ia = e.nextElement();
                    if (ia.isLoopbackAddress()) {
                        LOGGER.log(Level.FINE, "{0} is a loopback address", ia);
                        continue;
                    }
                    if (!(ia instanceof Inet4Address)) {
                        LOGGER.log(Level.FINE, "{0} is not an IPv4 address", ia);
                        continue;
                    }
                    LOGGER.log(Level.FINE, "{0} is a viable candidate", ia);
                    names.add(ia.getHostAddress());
                }
            }
            return names;
        }

        private static final long serialVersionUID = 1L;
    }

    private static class GetFallbackName extends MasterToSlaveCallable<String, IOException> {

        public String call() throws IOException {
            return SystemProperties.getString("host.name");
        }

        private static final long serialVersionUID = 1L;
    }

    public static final ExecutorService threadPoolForRemoting = new ContextResettingExecutorService(new ImpersonatingExecutorService(Executors.newCachedThreadPool(new ExceptionCatchingThreadFactory(new NamingThreadFactory(new DaemonThreadFactory(), "Computer.threadPoolForRemoting"))), ACL.SYSTEM));

    public void doRssAll(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        rss(req, rsp, " all builds", getBuilds());
    }

    public void doRssFailed(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        rss(req, rsp, " failed builds", getBuilds().failureOnly());
    }

    private void rss(StaplerRequest req, StaplerResponse rsp, String suffix, RunList runs) throws IOException, ServletException {
        RSS.forwardToRss(getDisplayName() + suffix, getUrl(), runs.newBuilds(), Run.FEED_ADAPTER, req, rsp);
    }

    @RequirePOST
    public HttpResponse doToggleOffline(@QueryParameter String offlineMessage) throws IOException, ServletException {
        if (!temporarilyOffline) {
            checkPermission(DISCONNECT);
            offlineMessage = Util.fixEmptyAndTrim(offlineMessage);
            setTemporarilyOffline(!temporarilyOffline, new OfflineCause.UserCause(User.current(), offlineMessage));
        } else {
            checkPermission(CONNECT);
            setTemporarilyOffline(!temporarilyOffline, null);
        }
        return HttpResponses.redirectToDot();
    }

    @RequirePOST
    public HttpResponse doChangeOfflineCause(@QueryParameter String offlineMessage) throws IOException, ServletException {
        checkPermission(DISCONNECT);
        offlineMessage = Util.fixEmptyAndTrim(offlineMessage);
        setTemporarilyOffline(true, new OfflineCause.UserCause(User.current(), offlineMessage));
        return HttpResponses.redirectToDot();
    }

    public Api getApi() {
        return new Api(this);
    }

    public void doDumpExportTable(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
        checkPermission(Jenkins.ADMINISTER);
        rsp.setContentType("text/plain");
        try (PrintWriter w = new PrintWriter(rsp.getCompressedWriter(req))) {
            VirtualChannel vc = getChannel();
            if (vc instanceof Channel) {
                w.println("Master to slave");
                ((Channel) vc).dumpExportTable(w);
                w.flush();
                w.println("\n\n\nSlave to master");
                w.print(vc.call(new DumpExportTableTask()));
            } else {
                w.println(Messages.Computer_BadChannel());
            }
        }
    }

    private static final class DumpExportTableTask extends MasterToSlaveCallable<String, IOException> {

        public String call() throws IOException {
            final Channel ch = getChannelOrFail();
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                ch.dumpExportTable(pw);
            }
            return sw.toString();
        }
    }

    public void doScript(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        _doScript(req, rsp, "_script.jelly");
    }

    public void doScriptText(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        _doScript(req, rsp, "_scriptText.jelly");
    }

    protected void _doScript(StaplerRequest req, StaplerResponse rsp, String view) throws IOException, ServletException {
        Jenkins._doScript(req, rsp, req.getView(this, view), getChannel(), getACL());
    }

    @POST
    public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, FormException {
        checkPermission(CONFIGURE);
        String proposedName = Util.fixEmptyAndTrim(req.getSubmittedForm().getString("name"));
        Jenkins.checkGoodName(proposedName);
        Node node = getNode();
        if (node == null) {
            throw new ServletException("No such node " + nodeName);
        }
        if ((!proposedName.equals(nodeName)) && Jenkins.getActiveInstance().getNode(proposedName) != null) {
            throw new FormException(Messages.ComputerSet_SlaveAlreadyExists(proposedName), "name");
        }
        String nExecutors = req.getSubmittedForm().getString("numExecutors");
        if (StringUtils.isBlank(nExecutors) || Integer.parseInt(nExecutors) <= 0) {
            throw new FormException(Messages.Slave_InvalidConfig_Executors(nodeName), "numExecutors");
        }
        Node result = node.reconfigure(req, req.getSubmittedForm());
        Jenkins.get().getNodesObject().replaceNode(this.getNode(), result);
        rsp.sendRedirect2("../" + result.getNodeName() + '/');
    }

    @WebMethod(name = "config.xml")
    public void doConfigDotXml(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        if (req.getMethod().equals("GET")) {
            checkPermission(EXTENDED_READ);
            rsp.setContentType("application/xml");
            Node node = getNode();
            if (node == null) {
                throw HttpResponses.notFound();
            }
            Jenkins.XSTREAM2.toXMLUTF8(node, rsp.getOutputStream());
            return;
        }
        if (req.getMethod().equals("POST")) {
            updateByXml(req.getInputStream());
            return;
        }
        rsp.sendError(SC_BAD_REQUEST);
    }

    public void updateByXml(final InputStream source) throws IOException, ServletException {
        checkPermission(CONFIGURE);
        Node result = (Node) Jenkins.XSTREAM2.fromXML(source);
        Jenkins.get().getNodesObject().replaceNode(this.getNode(), result);
    }

    @RequirePOST
    public HttpResponse doDoDelete() throws IOException {
        checkPermission(DELETE);
        Node node = getNode();
        if (node != null) {
            Jenkins.get().removeNode(node);
        } else {
            AbstractCIBase app = Jenkins.get();
            app.removeComputer(this);
        }
        return new HttpRedirect("..");
    }

    public void waitUntilOnline() throws InterruptedException {
        synchronized (statusChangeLock) {
            while (!isOnline()) statusChangeLock.wait(1000);
        }
    }

    public void waitUntilOffline() throws InterruptedException {
        synchronized (statusChangeLock) {
            while (!isOffline()) statusChangeLock.wait(1000);
        }
    }

    public void doProgressiveLog(StaplerRequest req, StaplerResponse rsp) throws IOException {
        getLogText().doProgressText(req, rsp);
    }

    @Nullable
    public static Computer currentComputer() {
        Executor e = Executor.currentExecutor();
        return e != null ? e.getOwner() : null;
    }

    @OverridingMethodsMustInvokeSuper
    public boolean isAcceptingTasks() {
        final Node node = getNode();
        return getRetentionStrategy().isAcceptingTasks(this) && (node == null || node.isAcceptingTasks());
    }

    @CLIResolver
    public static Computer resolveForCLI(@Argument(required = true, metaVar = "NAME", usage = "Agent name, or empty string for master") String name) throws CmdLineException {
        Jenkins h = Jenkins.get();
        Computer item = h.getComputer(name);
        if (item == null) {
            List<String> names = ComputerSet.getComputerNames();
            String adv = EditDistance.findNearest(name, names);
            throw new IllegalArgumentException(adv == null ? hudson.model.Messages.Computer_NoSuchSlaveExistsWithoutAdvice(name) : hudson.model.Messages.Computer_NoSuchSlaveExists(name, adv));
        }
        return item;
    }

    @Initializer
    public static void relocateOldLogs() {
        relocateOldLogs(Jenkins.get().getRootDir());
    }

    static void relocateOldLogs(File dir) {
        final Pattern logfile = Pattern.compile("slave-(.*)\\.log(\\.[0-9]+)?");
        File[] logfiles = dir.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return logfile.matcher(name).matches();
            }
        });
        if (logfiles == null)
            return;
        for (File f : logfiles) {
            Matcher m = logfile.matcher(f.getName());
            if (m.matches()) {
                File newLocation = new File(dir, "logs/slaves/" + m.group(1) + "/slave.log" + Util.fixNull(m.group(2)));
                newLocation.getParentFile().mkdirs();
                boolean relocationSuccessful = f.renameTo(newLocation);
                if (relocationSuccessful) {
                    LOGGER.log(Level.INFO, "Relocated log file {0} to {1}", new Object[] { f.getPath(), newLocation.getPath() });
                } else {
                    LOGGER.log(Level.WARNING, "Cannot relocate log file {0} to {1}", new Object[] { f.getPath(), newLocation.getPath() });
                }
            } else {
                assert false;
            }
        }
    }

    @Restricted(NoExternalUse.class)
    public static class DisplayExecutor implements ModelObject {

        @Nonnull
        private final String displayName;

        @Nonnull
        private final String url;

        @Nonnull
        private final Executor executor;

        public DisplayExecutor(@Nonnull String displayName, @Nonnull String url, @Nonnull Executor executor) {
            this.displayName = displayName;
            this.url = url;
            this.executor = executor;
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return displayName;
        }

        @Nonnull
        public String getUrl() {
            return url;
        }

        @Nonnull
        public Executor getExecutor() {
            return executor;
        }

        @Override
        public String toString() {
            String sb = "DisplayExecutor{" + "displayName='" + displayName + '\'' + ", url='" + url + '\'' + ", executor=" + executor + '}';
            return sb;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            DisplayExecutor that = (DisplayExecutor) o;
            if (!executor.equals(that.executor)) {
                return false;
            }
            return true;
        }

        @Extension(ordinal = Double.MAX_VALUE)
        @Restricted(DoNotUse.class)
        public static class InternalComputerListener extends ComputerListener {

            @Override
            public void onOnline(Computer c, TaskListener listener) throws IOException, InterruptedException {
                c.cachedEnvironment = null;
            }
        }

        @Override
        public int hashCode() {
            return executor.hashCode();
        }
    }

    public static class TerminationRequest extends RuntimeException {

        private final long when;

        public TerminationRequest(String message) {
            super(message);
            this.when = System.currentTimeMillis();
        }

        public long getWhen() {
            return when;
        }
    }

    public static final PermissionGroup PERMISSIONS = new PermissionGroup(Computer.class, Messages._Computer_Permissions_Title());

    public static final Permission CONFIGURE = new Permission(PERMISSIONS, "Configure", Messages._Computer_ConfigurePermission_Description(), Permission.CONFIGURE, PermissionScope.COMPUTER);

    public static final Permission EXTENDED_READ = new Permission(PERMISSIONS, "ExtendedRead", Messages._Computer_ExtendedReadPermission_Description(), CONFIGURE, SystemProperties.getBoolean("hudson.security.ExtendedReadPermission"), new PermissionScope[] { PermissionScope.COMPUTER });

    public static final Permission DELETE = new Permission(PERMISSIONS, "Delete", Messages._Computer_DeletePermission_Description(), Permission.DELETE, PermissionScope.COMPUTER);

    public static final Permission CREATE = new Permission(PERMISSIONS, "Create", Messages._Computer_CreatePermission_Description(), Permission.CREATE, PermissionScope.JENKINS);

    public static final Permission DISCONNECT = new Permission(PERMISSIONS, "Disconnect", Messages._Computer_DisconnectPermission_Description(), Jenkins.ADMINISTER, PermissionScope.COMPUTER);

    public static final Permission CONNECT = new Permission(PERMISSIONS, "Connect", Messages._Computer_ConnectPermission_Description(), DISCONNECT, PermissionScope.COMPUTER);

    public static final Permission BUILD = new Permission(PERMISSIONS, "Build", Messages._Computer_BuildPermission_Description(), Permission.WRITE, PermissionScope.COMPUTER);

    @Deprecated
    private static final Permission CLOUD_PROVISION = Cloud.PROVISION;

    private static final Logger LOGGER = Logger.getLogger(Computer.class.getName());

    static {
        IconSet.icons.addIcon(new Icon("icon-computer-user-offline icon-sm", "16x16/computer-user-offline.png", Icon.ICON_SMALL_STYLE));
        IconSet.icons.addIcon(new Icon("icon-computer-user-offline icon-md", "24x24/computer-user-offline.png", Icon.ICON_MEDIUM_STYLE));
        IconSet.icons.addIcon(new Icon("icon-computer-user-offline icon-lg", "32x32/computer-user-offline.png", Icon.ICON_LARGE_STYLE));
        IconSet.icons.addIcon(new Icon("icon-computer-user-offline icon-xlg", "48x48/computer-user-offline.png", Icon.ICON_XLARGE_STYLE));
    }
}