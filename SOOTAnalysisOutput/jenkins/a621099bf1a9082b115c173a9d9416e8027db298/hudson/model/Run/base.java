package hudson.model;

import hudson.console.ConsoleLogFilter;
import hudson.Functions;
import hudson.AbortException;
import hudson.BulkChange;
import hudson.EnvVars;
import hudson.ExtensionPoint;
import hudson.FeedAdapter;
import hudson.FilePath;
import hudson.Util;
import hudson.XmlFile;
import hudson.cli.declarative.CLIMethod;
import hudson.console.AnnotatedLargeText;
import hudson.console.ConsoleNote;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.Descriptor.FormException;
import hudson.model.listeners.RunListener;
import hudson.model.listeners.SaveableListener;
import hudson.security.PermissionScope;
import hudson.search.SearchIndexBuilder;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.tasks.BuildWrapper;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.util.FlushProofOutputStream;
import hudson.util.FormApply;
import hudson.util.IOException2;
import hudson.util.LogTaskListener;
import hudson.util.XStream2;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.jcraft.jzlib.GZIPInputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import jenkins.model.BuildDiscarder;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;
import jenkins.util.io.OnMaster;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import com.thoughtworks.xstream.XStream;
import hudson.model.Run.RunExecution;
import java.io.ByteArrayInputStream;
import org.kohsuke.stapler.interceptor.RequirePOST;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import static java.util.logging.Level.*;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jenkins.model.ArtifactManager;
import jenkins.model.ArtifactManagerConfiguration;
import jenkins.model.ArtifactManagerFactory;
import jenkins.model.PeepholePermalink;
import jenkins.model.StandardArtifactManager;
import jenkins.model.RunAction2;
import jenkins.util.VirtualFile;

@ExportedBean
public abstract class Run<JobT extends Job<JobT, RunT>, RunT extends Run<JobT, RunT>> extends Actionable implements ExtensionPoint, Comparable<RunT>, AccessControlled, PersistenceRoot, DescriptorByNameOwner, OnMaster {

    protected transient final JobT project;

    public int number;

    @Restricted(NoExternalUse.class)
    protected volatile transient RunT previousBuild;

    @Restricted(NoExternalUse.class)
    protected volatile transient RunT nextBuild;

    volatile transient RunT previousBuildInProgress;

    protected transient final long timestamp;

    private long startTime;

    protected volatile Result result;

    protected volatile String description;

    private volatile String displayName;

    protected volatile transient State state;

    private static enum State {

        NOT_STARTED, BUILDING, POST_PRODUCTION, COMPLETED
    }

    protected long duration;

    protected String charset;

    private boolean keepLog;

    private volatile transient RunExecution runner;

    @CheckForNull
    private ArtifactManager artifactManager;

    private static final SimpleDateFormat CANONICAL_ID_FORMATTER = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    public static final ThreadLocal<SimpleDateFormat> ID_FORMATTER = new IDFormatterProvider();

    private static final class IDFormatterProvider extends ThreadLocal<SimpleDateFormat> {

        @Override
        protected SimpleDateFormat initialValue() {
            synchronized (CANONICAL_ID_FORMATTER) {
                return (SimpleDateFormat) CANONICAL_ID_FORMATTER.clone();
            }
        }
    }

    protected Run(JobT job) throws IOException {
        this(job, new GregorianCalendar());
        this.number = project.assignBuildNumber();
    }

    protected Run(JobT job, Calendar timestamp) {
        this(job, timestamp.getTimeInMillis());
    }

    protected Run(JobT job, long timestamp) {
        this.project = job;
        this.timestamp = timestamp;
        this.state = State.NOT_STARTED;
        getRootDir().mkdirs();
    }

    protected Run(JobT project, File buildDir) throws IOException {
        this(project, parseTimestampFromBuildDir(buildDir));
        this.previousBuildInProgress = _this();
        reload();
    }

    public void reload() throws IOException {
        this.state = State.COMPLETED;
        this.result = Result.FAILURE;
        getDataFile().unmarshal(this);
    }

    @SuppressWarnings("deprecation")
    protected void onLoad() {
        for (Action a : getActions()) {
            if (a instanceof RunAction2) {
                ((RunAction2) a).onLoad(this);
            } else if (a instanceof RunAction) {
                ((RunAction) a).onLoad();
            }
        }
        if (artifactManager != null) {
            artifactManager.onLoad(this);
        }
    }

    public List<Action> getTransientActions() {
        List<Action> actions = new ArrayList<Action>();
        for (TransientBuildActionFactory factory : TransientBuildActionFactory.all()) {
            actions.addAll(factory.createFor(this));
            assert !actions.contains(null) : "null action added by " + factory;
        }
        return Collections.unmodifiableList(actions);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addAction(Action a) {
        super.addAction(a);
        if (a instanceof RunAction2) {
            ((RunAction2) a).onAttached(this);
        } else if (a instanceof RunAction) {
            ((RunAction) a).onAttached(this);
        }
    }

    static class InvalidDirectoryNameException extends IOException {

        InvalidDirectoryNameException(File buildDir) {
            super("Invalid directory name " + buildDir);
        }
    }

    static long parseTimestampFromBuildDir(File buildDir) throws IOException, InvalidDirectoryNameException {
        try {
            if (Util.isSymlink(buildDir)) {
                File target = Util.resolveSymlinkToFile(buildDir);
                if (target != null)
                    buildDir = target;
            }
            buildDir = buildDir.getCanonicalFile();
            return ID_FORMATTER.get().parse(buildDir.getName()).getTime();
        } catch (ParseException e) {
            throw new InvalidDirectoryNameException(buildDir);
        } catch (InterruptedException e) {
            throw new IOException2("Interrupted while resolving symlink directory " + buildDir, e);
        }
    }

    @SuppressWarnings({ "unchecked" })
    protected RunT _this() {
        return (RunT) this;
    }

    public int compareTo(RunT that) {
        return this.number - that.number;
    }

    @Exported
    @CheckForNull
    public Result getResult() {
        return result;
    }

    public void setResult(Result r) {
        assert state == State.BUILDING;
        if (result == null) {
            result = r;
            LOGGER.log(FINE, toString() + " : result is set to " + r, new Exception());
        } else {
            if (r.isWorseThan(result)) {
                LOGGER.log(FINE, toString() + " : result is set to " + r, new Exception());
                result = r;
            }
        }
    }

    public List<BuildBadgeAction> getBadgeActions() {
        List<BuildBadgeAction> r = null;
        for (Action a : getActions()) {
            if (a instanceof BuildBadgeAction) {
                if (r == null)
                    r = new ArrayList<BuildBadgeAction>();
                r.add((BuildBadgeAction) a);
            }
        }
        if (isKeepLog()) {
            if (r == null)
                r = new ArrayList<BuildBadgeAction>();
            r.add(new KeepLogBuildBadge());
        }
        if (r == null)
            return Collections.emptyList();
        else
            return r;
    }

    @Exported
    public boolean isBuilding() {
        return state.compareTo(State.POST_PRODUCTION) < 0;
    }

    public boolean isLogUpdated() {
        return state.compareTo(State.COMPLETED) < 0;
    }

    @Exported
    @CheckForNull
    public Executor getExecutor() {
        Jenkins j = Jenkins.getInstance();
        if (j == null) {
            return null;
        }
        for (Computer c : j.getComputers()) {
            for (Executor e : c.getExecutors()) {
                if (e.getCurrentExecutable() == this)
                    return e;
            }
        }
        return null;
    }

    public Executor getOneOffExecutor() {
        for (Computer c : Jenkins.getInstance().getComputers()) {
            for (Executor e : c.getOneOffExecutors()) {
                if (e.getCurrentExecutable() == this)
                    return e;
            }
        }
        return null;
    }

    public final Charset getCharset() {
        if (charset == null)
            return Charset.defaultCharset();
        return Charset.forName(charset);
    }

    public List<Cause> getCauses() {
        CauseAction a = getAction(CauseAction.class);
        if (a == null)
            return Collections.emptyList();
        return Collections.unmodifiableList(a.getCauses());
    }

    public <T extends Cause> T getCause(Class<T> type) {
        for (Cause c : getCauses()) if (type.isInstance(c))
            return type.cast(c);
        return null;
    }

    @Exported
    public final boolean isKeepLog() {
        return getWhyKeepLog() != null;
    }

    public String getWhyKeepLog() {
        if (keepLog)
            return Messages.Run_MarkedExplicitly();
        return null;
    }

    public JobT getParent() {
        return project;
    }

    @Exported
    public Calendar getTimestamp() {
        GregorianCalendar c = new GregorianCalendar();
        c.setTimeInMillis(timestamp);
        return c;
    }

    public final Date getTime() {
        return new Date(timestamp);
    }

    public final long getTimeInMillis() {
        return timestamp;
    }

    public final long getStartTimeInMillis() {
        if (startTime == 0)
            return timestamp;
        return startTime;
    }

    @Exported
    public String getDescription() {
        return description;
    }

    public String getTruncatedDescription() {
        final int maxDescrLength = 100;
        if (description == null || description.length() < maxDescrLength) {
            return description;
        }
        final String ending = "...";
        final int sz = description.length(), maxTruncLength = maxDescrLength - ending.length();
        boolean inTag = false;
        int displayChars = 0;
        int lastTruncatablePoint = -1;
        for (int i = 0; i < sz; i++) {
            char ch = description.charAt(i);
            if (ch == '<') {
                inTag = true;
            } else if (ch == '>') {
                inTag = false;
                if (displayChars <= maxTruncLength) {
                    lastTruncatablePoint = i + 1;
                }
            }
            if (!inTag) {
                displayChars++;
                if (displayChars <= maxTruncLength && ch == ' ') {
                    lastTruncatablePoint = i;
                }
            }
        }
        String truncDesc = description;
        if (lastTruncatablePoint == -1)
            lastTruncatablePoint = maxTruncLength;
        if (displayChars >= maxDescrLength) {
            truncDesc = truncDesc.substring(0, lastTruncatablePoint) + ending;
        }
        return truncDesc;
    }

    public String getTimestampString() {
        long duration = new GregorianCalendar().getTimeInMillis() - timestamp;
        return Util.getPastTimeString(duration);
    }

    public String getTimestampString2() {
        return Util.XS_DATETIME_FORMATTER.format(new Date(timestamp));
    }

    public String getDurationString() {
        if (isBuilding())
            return Messages.Run_InProgressDuration(Util.getTimeSpanString(System.currentTimeMillis() - timestamp));
        return Util.getTimeSpanString(duration);
    }

    @Exported
    public long getDuration() {
        return duration;
    }

    public BallColor getIconColor() {
        if (!isBuilding()) {
            return getResult().color;
        }
        BallColor baseColor;
        RunT pb = getPreviousBuild();
        if (pb == null)
            baseColor = BallColor.NOTBUILT;
        else
            baseColor = pb.getIconColor();
        return baseColor.anime();
    }

    public boolean hasntStartedYet() {
        return state == State.NOT_STARTED;
    }

    @Override
    public String toString() {
        return getFullDisplayName();
    }

    @Exported
    public String getFullDisplayName() {
        return project.getFullDisplayName() + ' ' + getDisplayName();
    }

    public String getDisplayName() {
        return displayName != null ? displayName : "#" + number;
    }

    public boolean hasCustomDisplayName() {
        return displayName != null;
    }

    public void setDisplayName(String value) throws IOException {
        checkPermission(UPDATE);
        this.displayName = value;
        save();
    }

    @Exported(visibility = 2)
    public int getNumber() {
        return number;
    }

    void dropLinks() {
        if (nextBuild != null)
            nextBuild.previousBuild = previousBuild;
        if (previousBuild != null)
            previousBuild.nextBuild = nextBuild;
    }

    public RunT getPreviousBuild() {
        return previousBuild;
    }

    public final RunT getPreviousCompletedBuild() {
        RunT r = getPreviousBuild();
        while (r != null && r.isBuilding()) r = r.getPreviousBuild();
        return r;
    }

    public final RunT getPreviousBuildInProgress() {
        if (previousBuildInProgress == this)
            return null;
        List<RunT> fixUp = new ArrayList<RunT>();
        RunT r = _this();
        RunT answer;
        while (true) {
            RunT n = r.previousBuildInProgress;
            if (n == null) {
                n = r.getPreviousBuild();
                fixUp.add(r);
            }
            if (r == n || n == null) {
                answer = null;
                break;
            }
            if (n.isBuilding()) {
                answer = n;
                break;
            }
            fixUp.add(r);
            r = n;
        }
        for (RunT f : fixUp) f.previousBuildInProgress = answer == null ? f : answer;
        return answer;
    }

    public RunT getPreviousBuiltBuild() {
        RunT r = getPreviousBuild();
        while (r != null && (r.getResult() == null || r.getResult() == Result.NOT_BUILT)) r = r.getPreviousBuild();
        return r;
    }

    public RunT getPreviousNotFailedBuild() {
        RunT r = getPreviousBuild();
        while (r != null && r.getResult() == Result.FAILURE) r = r.getPreviousBuild();
        return r;
    }

    public RunT getPreviousFailedBuild() {
        RunT r = getPreviousBuild();
        while (r != null && r.getResult() != Result.FAILURE) r = r.getPreviousBuild();
        return r;
    }

    public RunT getPreviousSuccessfulBuild() {
        RunT r = getPreviousBuild();
        while (r != null && r.getResult() != Result.SUCCESS) r = r.getPreviousBuild();
        return r;
    }

    public List<RunT> getPreviousBuildsOverThreshold(int numberOfBuilds, Result threshold) {
        List<RunT> builds = new ArrayList<RunT>(numberOfBuilds);
        RunT r = getPreviousBuild();
        while (r != null && builds.size() < numberOfBuilds) {
            if (!r.isBuilding() && (r.getResult() != null && r.getResult().isBetterOrEqualTo(threshold))) {
                builds.add(r);
            }
            r = r.getPreviousBuild();
        }
        return builds;
    }

    public RunT getNextBuild() {
        return nextBuild;
    }

    public String getUrl() {
        StaplerRequest req = Stapler.getCurrentRequest();
        if (req != null) {
            String seed = Functions.getNearestAncestorUrl(req, this);
            if (seed != null) {
                return seed.substring(req.getContextPath().length() + 1) + '/';
            }
        }
        return project.getUrl() + getNumber() + '/';
    }

    @Exported(visibility = 2, name = "url")
    public final String getAbsoluteUrl() {
        return project.getAbsoluteUrl() + getNumber() + '/';
    }

    public final String getSearchUrl() {
        return getNumber() + "/";
    }

    @Exported
    public String getId() {
        return ID_FORMATTER.get().format(new Date(timestamp));
    }

    public static DateFormat getIDFormatter() {
        return ID_FORMATTER.get();
    }

    public Descriptor getDescriptorByName(String className) {
        return Jenkins.getInstance().getDescriptorByName(className);
    }

    public File getRootDir() {
        return new File(project.getBuildDir(), getId());
    }

    @Nonnull
    public final ArtifactManager getArtifactManager() {
        return artifactManager != null ? artifactManager : new StandardArtifactManager(this);
    }

    @Nonnull
    public final synchronized ArtifactManager pickArtifactManager() throws IOException {
        if (artifactManager != null) {
            return artifactManager;
        } else {
            for (ArtifactManagerFactory f : ArtifactManagerConfiguration.get().getArtifactManagerFactories()) {
                ArtifactManager mgr = f.managerFor(this);
                if (mgr != null) {
                    artifactManager = mgr;
                    save();
                    return mgr;
                }
            }
            return new StandardArtifactManager(this);
        }
    }

    @Deprecated
    public File getArtifactsDir() {
        return new File(getRootDir(), "archive");
    }

    @Exported
    public List<Artifact> getArtifacts() {
        return getArtifactsUpTo(Integer.MAX_VALUE);
    }

    public List<Artifact> getArtifactsUpTo(int n) {
        ArtifactList r = new ArtifactList();
        try {
            addArtifacts(getArtifactManager().root(), "", "", r, null, n);
        } catch (IOException x) {
            LOGGER.log(Level.WARNING, null, x);
        }
        r.computeDisplayName();
        return r;
    }

    public boolean getHasArtifacts() {
        return !getArtifactsUpTo(1).isEmpty();
    }

    private int addArtifacts(VirtualFile dir, String path, String pathHref, ArtifactList r, Artifact parent, int upTo) throws IOException {
        VirtualFile[] kids = dir.list();
        Arrays.sort(kids);
        int n = 0;
        for (VirtualFile sub : kids) {
            String child = sub.getName();
            String childPath = path + child;
            String childHref = pathHref + Util.rawEncode(child);
            String length = sub.isFile() ? String.valueOf(sub.length()) : "";
            boolean collapsed = (kids.length == 1 && parent != null);
            Artifact a;
            if (collapsed) {
                a = new Artifact(parent.getFileName() + '/' + child, childPath, sub.isDirectory() ? null : childHref, length, parent.getTreeNodeId());
                r.tree.put(a, r.tree.remove(parent));
            } else {
                a = new Artifact(child, childPath, sub.isDirectory() ? null : childHref, length, "n" + ++r.idSeq);
                r.tree.put(a, parent != null ? parent.getTreeNodeId() : null);
            }
            if (sub.isDirectory()) {
                n += addArtifacts(sub, childPath + '/', childHref + '/', r, a, upTo - n);
                if (n >= upTo)
                    break;
            } else {
                r.add(collapsed ? new Artifact(child, a.relativePath, a.href, length, a.treeNodeId) : a);
                if (++n >= upTo)
                    break;
            }
        }
        return n;
    }

    public static final int LIST_CUTOFF = Integer.parseInt(System.getProperty("hudson.model.Run.ArtifactList.listCutoff", "16"));

    public static final int TREE_CUTOFF = Integer.parseInt(System.getProperty("hudson.model.Run.ArtifactList.treeCutoff", "40"));

    public final class ArtifactList extends ArrayList<Artifact> {

        private static final long serialVersionUID = 1L;

        private LinkedHashMap<Artifact, String> tree = new LinkedHashMap<Artifact, String>();

        private int idSeq = 0;

        public Map<Artifact, String> getTree() {
            return tree;
        }

        public void computeDisplayName() {
            if (size() > LIST_CUTOFF)
                return;
            int maxDepth = 0;
            int[] len = new int[size()];
            String[][] tokens = new String[size()][];
            for (int i = 0; i < tokens.length; i++) {
                tokens[i] = get(i).relativePath.split("[\\\\/]+");
                maxDepth = Math.max(maxDepth, tokens[i].length);
                len[i] = 1;
            }
            boolean collision;
            int depth = 0;
            do {
                collision = false;
                Map<String, Integer> names = new HashMap<String, Integer>();
                for (int i = 0; i < tokens.length; i++) {
                    String[] token = tokens[i];
                    String displayName = combineLast(token, len[i]);
                    Integer j = names.put(displayName, i);
                    if (j != null) {
                        collision = true;
                        if (j >= 0)
                            len[j]++;
                        len[i]++;
                        names.put(displayName, -1);
                    }
                }
            } while (collision && depth++ < maxDepth);
            for (int i = 0; i < tokens.length; i++) get(i).displayPath = combineLast(tokens[i], len[i]);
        }

        private String combineLast(String[] token, int n) {
            StringBuilder buf = new StringBuilder();
            for (int i = Math.max(0, token.length - n); i < token.length; i++) {
                if (buf.length() > 0)
                    buf.append('/');
                buf.append(token[i]);
            }
            return buf.toString();
        }
    }

    @ExportedBean
    public class Artifact {

        @Exported(visibility = 3)
        public final String relativePath;

        String displayPath;

        private String name;

        private String href;

        private String treeNodeId;

        private String length;

        Artifact(String name, String relativePath, String href, String len, String treeNodeId) {
            this.name = name;
            this.relativePath = relativePath;
            this.href = href;
            this.treeNodeId = treeNodeId;
            this.length = len;
        }

        @Deprecated
        public File getFile() {
            return new File(getArtifactsDir(), relativePath);
        }

        @Exported(visibility = 3)
        public String getFileName() {
            return name;
        }

        @Exported(visibility = 3)
        public String getDisplayPath() {
            return displayPath;
        }

        public String getHref() {
            return href;
        }

        public String getLength() {
            return length;
        }

        public long getFileSize() {
            return Long.decode(length);
        }

        public String getTreeNodeId() {
            return treeNodeId;
        }

        @Override
        public String toString() {
            return relativePath;
        }
    }

    public File getLogFile() {
        File rawF = new File(getRootDir(), "log");
        if (rawF.isFile()) {
            return rawF;
        }
        File gzF = new File(getRootDir(), "log.gz");
        if (gzF.isFile()) {
            return gzF;
        }
        return rawF;
    }

    public InputStream getLogInputStream() throws IOException {
        File logFile = getLogFile();
        if (logFile != null && logFile.exists()) {
            FileInputStream fis = new FileInputStream(logFile);
            if (logFile.getName().endsWith(".gz")) {
                return new GZIPInputStream(fis);
            } else {
                return fis;
            }
        }
        String message = "No such file: " + logFile;
        return new ByteArrayInputStream(charset != null ? message.getBytes(charset) : message.getBytes());
    }

    public Reader getLogReader() throws IOException {
        if (charset == null)
            return new InputStreamReader(getLogInputStream());
        else
            return new InputStreamReader(getLogInputStream(), charset);
    }

    public void writeLogTo(long offset, XMLOutput out) throws IOException {
        try {
            getLogText().writeHtmlTo(offset, out.asWriter());
        } catch (IOException e) {
            InputStream input = getLogInputStream();
            try {
                IOUtils.copy(input, out.asWriter());
            } finally {
                IOUtils.closeQuietly(input);
            }
        }
    }

    public void writeWholeLogTo(OutputStream out) throws IOException, InterruptedException {
        long pos = 0;
        AnnotatedLargeText logText;
        do {
            logText = getLogText();
            pos = logText.writeLogTo(pos, out);
        } while (!logText.isComplete());
    }

    public AnnotatedLargeText getLogText() {
        return new AnnotatedLargeText(getLogFile(), getCharset(), !isLogUpdated(), this);
    }

    @Override
    protected SearchIndexBuilder makeSearchIndex() {
        SearchIndexBuilder builder = super.makeSearchIndex().add("console").add("changes");
        for (Action a : getActions()) {
            if (a.getIconFileName() != null)
                builder.add(a.getUrlName());
        }
        return builder;
    }

    public Api getApi() {
        return new Api(this);
    }

    public void checkPermission(Permission p) {
        getACL().checkPermission(p);
    }

    public boolean hasPermission(Permission p) {
        return getACL().hasPermission(p);
    }

    public ACL getACL() {
        return getParent().getACL();
    }

    public synchronized void deleteArtifacts() throws IOException {
        File artifactsDir = getArtifactsDir();
        Util.deleteContentsRecursive(artifactsDir);
    }

    public synchronized void delete() throws IOException {
        RunListener.fireDeleted(this);
        File link = new File(project.getBuildDir(), String.valueOf(getNumber()));
        link.delete();
        File rootDir = getRootDir();
        if (!rootDir.isDirectory()) {
            throw new IOException(rootDir + " looks to have already been deleted");
        }
        File tmp = new File(rootDir.getParentFile(), '.' + rootDir.getName());
        if (tmp.exists()) {
            Util.deleteRecursive(tmp);
        }
        boolean renamingSucceeded = rootDir.renameTo(tmp);
        Util.deleteRecursive(tmp);
        if (tmp.exists())
            tmp.deleteOnExit();
        if (!renamingSucceeded)
            throw new IOException(rootDir + " is in use");
        removeRunFromParent();
    }

    @SuppressWarnings("unchecked")
    private void removeRunFromParent() {
        getParent().removeRun((RunT) this);
    }

    static void reportCheckpoint(CheckPoint id) {
        RunnerStack.INSTANCE.peek().checkpoints.report(id);
    }

    static void waitForCheckpoint(CheckPoint id, @CheckForNull BuildListener listener, @CheckForNull String waiter) throws InterruptedException {
        while (true) {
            Run b = RunnerStack.INSTANCE.peek().getBuild().getPreviousBuildInProgress();
            if (b == null)
                return;
            Run.RunExecution runner = b.runner;
            if (runner == null) {
                Thread.sleep(0);
                continue;
            }
            if (runner.checkpoints.waitForCheckPoint(id, listener, waiter))
                return;
        }
    }

    protected abstract class Runner extends RunExecution {
    }

    public abstract class RunExecution {

        private final class CheckpointSet {

            private final Set<CheckPoint> checkpoints = new HashSet<CheckPoint>();

            private boolean allDone;

            protected synchronized void report(CheckPoint identifier) {
                checkpoints.add(identifier);
                notifyAll();
            }

            protected synchronized boolean waitForCheckPoint(CheckPoint identifier, @CheckForNull BuildListener listener, @CheckForNull String waiter) throws InterruptedException {
                final Thread t = Thread.currentThread();
                final String oldName = t.getName();
                t.setName(oldName + " : waiting for " + identifier + " on " + getFullDisplayName() + " from " + waiter);
                try {
                    boolean first = true;
                    while (!allDone && !checkpoints.contains(identifier)) {
                        if (first && listener != null && waiter != null) {
                            listener.getLogger().println(Messages.Run__is_waiting_for_a_checkpoint_on_(waiter, getFullDisplayName()));
                        }
                        wait();
                        first = false;
                    }
                    return checkpoints.contains(identifier);
                } finally {
                    t.setName(oldName);
                }
            }

            private synchronized void allDone() {
                allDone = true;
                notifyAll();
            }
        }

        private final CheckpointSet checkpoints = new CheckpointSet();

        private final Map<Object, Object> attributes = new HashMap<Object, Object>();

        public abstract Result run(BuildListener listener) throws Exception, RunnerAbortedException;

        public abstract void post(BuildListener listener) throws Exception;

        public abstract void cleanUp(@Nonnull BuildListener listener) throws Exception;

        public RunT getBuild() {
            return _this();
        }

        public JobT getProject() {
            return _this().getParent();
        }

        public Map<Object, Object> getAttributes() {
            return attributes;
        }
    }

    public static final class RunnerAbortedException extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }

    protected final void run(Runner job) {
        execute(job);
    }

    protected final void execute(RunExecution job) {
        if (result != null)
            return;
        StreamBuildListener listener = null;
        runner = job;
        onStartBuilding();
        try {
            long start = System.currentTimeMillis();
            try {
                try {
                    Computer computer = Computer.currentComputer();
                    Charset charset = null;
                    if (computer != null) {
                        charset = computer.getDefaultCharset();
                        this.charset = charset.name();
                    }
                    OutputStream logger = new FileOutputStream(getLogFile());
                    RunT build = job.getBuild();
                    for (ConsoleLogFilter filter : ConsoleLogFilter.all()) {
                        logger = filter.decorateLogger((AbstractBuild) build, logger);
                    }
                    if (project instanceof BuildableItemWithBuildWrappers && build instanceof AbstractBuild) {
                        BuildableItemWithBuildWrappers biwbw = (BuildableItemWithBuildWrappers) project;
                        for (BuildWrapper bw : biwbw.getBuildWrappersList()) {
                            logger = bw.decorateLogger((AbstractBuild) build, logger);
                        }
                    }
                    listener = new StreamBuildListener(logger, charset);
                    listener.started(getCauses());
                    RunListener.fireStarted(this, listener);
                    updateSymlinks(listener);
                    setResult(job.run(listener));
                    LOGGER.info(toString() + " main build action completed: " + result);
                    CheckPoint.MAIN_COMPLETED.report();
                } catch (ThreadDeath t) {
                    throw t;
                } catch (AbortException e) {
                    result = Result.FAILURE;
                    listener.error(e.getMessage());
                    LOGGER.log(FINE, "Build " + this + " aborted", e);
                } catch (RunnerAbortedException e) {
                    result = Result.FAILURE;
                    LOGGER.log(FINE, "Build " + this + " aborted", e);
                } catch (InterruptedException e) {
                    result = Executor.currentExecutor().abortResult();
                    listener.getLogger().println(Messages.Run_BuildAborted());
                    Executor.currentExecutor().recordCauseOfInterruption(Run.this, listener);
                    LOGGER.log(Level.INFO, toString() + " aborted", e);
                } catch (Throwable e) {
                    handleFatalBuildProblem(listener, e);
                    result = Result.FAILURE;
                }
                job.post(listener);
            } catch (ThreadDeath t) {
                throw t;
            } catch (Throwable e) {
                handleFatalBuildProblem(listener, e);
                result = Result.FAILURE;
            } finally {
                long end = System.currentTimeMillis();
                duration = Math.max(end - start, 0);
                state = State.POST_PRODUCTION;
                if (listener != null) {
                    try {
                        job.cleanUp(listener);
                    } catch (Exception e) {
                        handleFatalBuildProblem(listener, e);
                    }
                    RunListener.fireCompleted(this, listener);
                    listener.finished(result);
                    listener.closeQuietly();
                }
                try {
                    save();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Failed to save build record", e);
                }
            }
            try {
                getParent().logRotate();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to rotate log", e);
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Failed to rotate log", e);
            }
        } finally {
            onEndBuilding();
        }
    }

    public final void updateSymlinks(TaskListener listener) throws InterruptedException {
        Util.createSymlink(getParent().getBuildDir(), getId(), String.valueOf(getNumber()), listener);
        createSymlink(listener, "lastSuccessful", PermalinkProjectAction.Permalink.LAST_SUCCESSFUL_BUILD);
        createSymlink(listener, "lastStable", PermalinkProjectAction.Permalink.LAST_STABLE_BUILD);
    }

    private void createSymlink(TaskListener listener, String name, PermalinkProjectAction.Permalink target) throws InterruptedException {
        File buildDir = getParent().getBuildDir();
        File rootDir = getParent().getRootDir();
        String targetDir;
        if (buildDir.equals(new File(rootDir, "builds"))) {
            targetDir = "builds" + File.separator + target.getId();
        } else {
            targetDir = buildDir + File.separator + target.getId();
        }
        Util.createSymlink(rootDir, targetDir, name, listener);
    }

    private void handleFatalBuildProblem(BuildListener listener, Throwable e) {
        if (listener != null) {
            LOGGER.log(FINE, getDisplayName() + " failed to build", e);
            if (e instanceof IOException)
                Util.displayIOException((IOException) e, listener);
            Writer w = listener.fatalError(e.getMessage());
            if (w != null) {
                try {
                    e.printStackTrace(new PrintWriter(w));
                    w.close();
                } catch (IOException e1) {
                }
            }
        } else {
            LOGGER.log(SEVERE, getDisplayName() + " failed to build and we don't even have a listener", e);
        }
    }

    protected void onStartBuilding() {
        state = State.BUILDING;
        startTime = System.currentTimeMillis();
        if (runner != null)
            RunnerStack.INSTANCE.push(runner);
    }

    protected void onEndBuilding() {
        if (runner != null) {
            state = State.COMPLETED;
            runner.checkpoints.allDone();
            runner = null;
            RunnerStack.INSTANCE.pop();
        } else {
            state = State.COMPLETED;
        }
        if (result == null) {
            result = Result.FAILURE;
            LOGGER.warning(toString() + ": No build result is set, so marking as failure. This shouldn't happen.");
        }
        RunListener.fireFinalized(this);
    }

    public synchronized void save() throws IOException {
        if (BulkChange.contains(this))
            return;
        getDataFile().write(this);
        SaveableListener.fireOnChange(this, getDataFile());
    }

    private XmlFile getDataFile() {
        return new XmlFile(XSTREAM, new File(getRootDir(), "build.xml"));
    }

    @Deprecated
    public String getLog() throws IOException {
        return Util.loadFile(getLogFile(), getCharset());
    }

    public List<String> getLog(int maxLines) throws IOException {
        int lineCount = 0;
        List<String> logLines = new LinkedList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(getLogFile()), getCharset()));
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                logLines.add(line);
                ++lineCount;
                if (lineCount > maxLines)
                    logLines.remove(0);
            }
        } finally {
            reader.close();
        }
        if (lineCount > maxLines)
            logLines.set(0, "[...truncated " + (lineCount - (maxLines - 1)) + " lines...]");
        return ConsoleNote.removeNotes(logLines);
    }

    public void doBuildStatus(StaplerRequest req, StaplerResponse rsp) throws IOException {
        rsp.sendRedirect2(req.getContextPath() + "/images/48x48/" + getBuildStatusUrl());
    }

    public String getBuildStatusUrl() {
        return getIconColor().getImage();
    }

    public static class Summary {

        public boolean isWorse;

        public String message;

        public Summary(boolean worse, String message) {
            this.isWorse = worse;
            this.message = message;
        }
    }

    public Summary getBuildStatusSummary() {
        if (isBuilding()) {
            return new Summary(false, Messages.Run_Summary_Unknown());
        }
        ResultTrend trend = ResultTrend.getResultTrend(this);
        switch(trend) {
            case ABORTED:
                return new Summary(false, Messages.Run_Summary_Aborted());
            case NOT_BUILT:
                return new Summary(false, Messages.Run_Summary_NotBuilt());
            case FAILURE:
                return new Summary(true, Messages.Run_Summary_BrokenSinceThisBuild());
            case STILL_FAILING:
                RunT since = getPreviousNotFailedBuild();
                if (since == null)
                    return new Summary(false, Messages.Run_Summary_BrokenForALongTime());
                RunT failedBuild = since.getNextBuild();
                return new Summary(false, Messages.Run_Summary_BrokenSince(failedBuild.getDisplayName()));
            case NOW_UNSTABLE:
                return determineDetailedUnstableSummary(Boolean.FALSE);
            case UNSTABLE:
                return determineDetailedUnstableSummary(Boolean.TRUE);
            case STILL_UNSTABLE:
                return determineDetailedUnstableSummary(null);
            case SUCCESS:
                return new Summary(false, Messages.Run_Summary_Stable());
            case FIXED:
                return new Summary(false, Messages.Run_Summary_BackToNormal());
        }
        return new Summary(false, Messages.Run_Summary_Unknown());
    }

    private Summary determineDetailedUnstableSummary(Boolean worseOverride) {
        if (((Run) this) instanceof AbstractBuild) {
            AbstractTestResultAction trN = ((AbstractBuild) (Run) this).getTestResultAction();
            Run prev = getPreviousBuild();
            AbstractTestResultAction trP = prev == null ? null : ((AbstractBuild) prev).getTestResultAction();
            if (trP == null) {
                if (trN != null && trN.getFailCount() > 0)
                    return new Summary(worseOverride != null ? worseOverride : true, Messages.Run_Summary_TestFailures(trN.getFailCount()));
            } else {
                if (trN != null && trN.getFailCount() != 0) {
                    if (trP.getFailCount() == 0)
                        return new Summary(worseOverride != null ? worseOverride : true, Messages.Run_Summary_TestsStartedToFail(trN.getFailCount()));
                    if (trP.getFailCount() < trN.getFailCount())
                        return new Summary(worseOverride != null ? worseOverride : true, Messages.Run_Summary_MoreTestsFailing(trN.getFailCount() - trP.getFailCount(), trN.getFailCount()));
                    if (trP.getFailCount() > trN.getFailCount())
                        return new Summary(worseOverride != null ? worseOverride : false, Messages.Run_Summary_LessTestsFailing(trP.getFailCount() - trN.getFailCount(), trN.getFailCount()));
                    return new Summary(worseOverride != null ? worseOverride : false, Messages.Run_Summary_TestsStillFailing(trN.getFailCount()));
                }
            }
        }
        return new Summary(worseOverride != null ? worseOverride : false, Messages.Run_Summary_Unstable());
    }

    public DirectoryBrowserSupport doArtifact() {
        if (Functions.isArtifactsPermissionEnabled()) {
            checkPermission(ARTIFACTS);
        }
        return new DirectoryBrowserSupport(this, getArtifactManager().root(), project.getDisplayName() + ' ' + getDisplayName(), "package.png", true);
    }

    public void doBuildNumber(StaplerResponse rsp) throws IOException {
        rsp.setContentType("text/plain");
        rsp.setCharacterEncoding("US-ASCII");
        rsp.setStatus(HttpServletResponse.SC_OK);
        rsp.getWriter().print(number);
    }

    public void doBuildTimestamp(StaplerRequest req, StaplerResponse rsp, @QueryParameter String format) throws IOException {
        rsp.setContentType("text/plain");
        rsp.setCharacterEncoding("US-ASCII");
        rsp.setStatus(HttpServletResponse.SC_OK);
        DateFormat df = format == null ? DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.ENGLISH) : new SimpleDateFormat(format, req.getLocale());
        rsp.getWriter().print(df.format(getTime()));
    }

    public void doConsoleText(StaplerRequest req, StaplerResponse rsp) throws IOException {
        rsp.setContentType("text/plain;charset=UTF-8");
        FlushProofOutputStream out = new FlushProofOutputStream(rsp.getCompressedOutputStream(req));
        try {
            getLogText().writeLogTo(0, out);
        } catch (IOException e) {
            InputStream input = getLogInputStream();
            try {
                IOUtils.copy(input, out);
            } finally {
                IOUtils.closeQuietly(input);
            }
        }
        out.close();
    }

    public void doProgressiveLog(StaplerRequest req, StaplerResponse rsp) throws IOException {
        getLogText().doProgressText(req, rsp);
    }

    public boolean canToggleLogKeep() {
        if (!keepLog && isKeepLog()) {
            return false;
        }
        return true;
    }

    public void doToggleLogKeep(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        keepLog(!keepLog);
        rsp.forwardToPreviousPage(req);
    }

    @CLIMethod(name = "keep-build")
    public final void keepLog() throws IOException {
        keepLog(true);
    }

    public void keepLog(boolean newValue) throws IOException {
        checkPermission(UPDATE);
        keepLog = newValue;
        save();
    }

    @RequirePOST
    public void doDoDelete(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        checkPermission(DELETE);
        String why = getWhyKeepLog();
        if (why != null) {
            sendError(Messages.Run_UnableToDelete(toString(), why), req, rsp);
            return;
        }
        try {
            delete();
        } catch (IOException ex) {
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            req.setAttribute("stackTraces", writer);
            req.getView(this, "delete-retry.jelly").forward(req, rsp);
            return;
        }
        rsp.sendRedirect2(req.getContextPath() + '/' + getParent().getUrl());
    }

    public void setDescription(String description) throws IOException {
        checkPermission(UPDATE);
        this.description = description;
        save();
    }

    public synchronized void doSubmitDescription(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        setDescription(req.getParameter("description"));
        rsp.sendRedirect(".");
    }

    public Map<String, String> getEnvVars() {
        try {
            return getEnvironment();
        } catch (IOException e) {
            return new EnvVars();
        } catch (InterruptedException e) {
            return new EnvVars();
        }
    }

    public EnvVars getEnvironment() throws IOException, InterruptedException {
        return getEnvironment(new LogTaskListener(LOGGER, Level.INFO));
    }

    public EnvVars getEnvironment(TaskListener listener) throws IOException, InterruptedException {
        Computer c = Computer.currentComputer();
        Node n = c == null ? null : c.getNode();
        EnvVars env = getParent().getEnvironment(n, listener);
        env.putAll(getCharacteristicEnvVars());
        for (EnvironmentContributor ec : EnvironmentContributor.all().reverseView()) ec.buildEnvironmentFor(this, env, listener);
        return env;
    }

    public final EnvVars getCharacteristicEnvVars() {
        EnvVars env = getParent().getCharacteristicEnvVars();
        env.put("BUILD_NUMBER", String.valueOf(number));
        env.put("BUILD_ID", getId());
        env.put("BUILD_TAG", "jenkins-" + getParent().getFullName().replace('/', '-') + "-" + number);
        return env;
    }

    public String getExternalizableId() {
        return project.getFullName() + "#" + getNumber();
    }

    public static Run<?, ?> fromExternalizableId(String id) {
        int hash = id.lastIndexOf('#');
        if (hash <= 0) {
            throw new IllegalArgumentException("Invalid id");
        }
        String jobName = id.substring(0, hash);
        int number = Integer.parseInt(id.substring(hash + 1));
        Job<?, ?> job = Jenkins.getInstance().getItemByFullName(jobName, Job.class);
        if (job == null) {
            throw new IllegalArgumentException("no such job " + jobName);
        }
        return job.getBuildByNumber(number);
    }

    @Exported
    public long getEstimatedDuration() {
        return project.getEstimatedDuration();
    }

    @RequirePOST
    public HttpResponse doConfigSubmit(StaplerRequest req) throws IOException, ServletException, FormException {
        checkPermission(UPDATE);
        BulkChange bc = new BulkChange(this);
        try {
            JSONObject json = req.getSubmittedForm();
            submit(json);
            bc.commit();
        } finally {
            bc.abort();
        }
        return FormApply.success(".");
    }

    protected void submit(JSONObject json) throws IOException {
        setDisplayName(Util.fixEmptyAndTrim(json.getString("displayName")));
        setDescription(json.getString("description"));
    }

    public static final XStream XSTREAM = new XStream2();

    public static final XStream2 XSTREAM2 = (XStream2) XSTREAM;

    static {
        XSTREAM.alias("build", FreeStyleBuild.class);
        XSTREAM.alias("matrix-build", MatrixBuild.class);
        XSTREAM.alias("matrix-run", MatrixRun.class);
        XSTREAM.registerConverter(Result.conv);
    }

    private static final Logger LOGGER = Logger.getLogger(Run.class.getName());

    public static final Comparator<Run> ORDER_BY_DATE = new Comparator<Run>() {

        public int compare(Run lhs, Run rhs) {
            long lt = lhs.getTimeInMillis();
            long rt = rhs.getTimeInMillis();
            if (lt > rt)
                return -1;
            if (lt < rt)
                return 1;
            return 0;
        }
    };

    public static final FeedAdapter<Run> FEED_ADAPTER = new DefaultFeedAdapter();

    public static final FeedAdapter<Run> FEED_ADAPTER_LATEST = new DefaultFeedAdapter() {

        @Override
        public String getEntryID(Run e) {
            return "tag:hudson.dev.java.net,2008:" + e.getParent().getAbsoluteUrl();
        }
    };

    public final class KeepLogBuildBadge implements BuildBadgeAction {

        public String getIconFileName() {
            return null;
        }

        public String getDisplayName() {
            return null;
        }

        public String getUrlName() {
            return null;
        }

        public String getWhyKeepLog() {
            return Run.this.getWhyKeepLog();
        }
    }

    public static final PermissionGroup PERMISSIONS = new PermissionGroup(Run.class, Messages._Run_Permissions_Title());

    public static final Permission DELETE = new Permission(PERMISSIONS, "Delete", Messages._Run_DeletePermission_Description(), Permission.DELETE, PermissionScope.RUN);

    public static final Permission UPDATE = new Permission(PERMISSIONS, "Update", Messages._Run_UpdatePermission_Description(), Permission.UPDATE, PermissionScope.RUN);

    public static final Permission ARTIFACTS = new Permission(PERMISSIONS, "Artifacts", Messages._Run_ArtifactsPermission_Description(), null, Functions.isArtifactsPermissionEnabled(), new PermissionScope[] { PermissionScope.RUN });

    private static class DefaultFeedAdapter implements FeedAdapter<Run> {

        public String getEntryTitle(Run entry) {
            return entry + " (" + entry.getBuildStatusSummary().message + ")";
        }

        public String getEntryUrl(Run entry) {
            return entry.getUrl();
        }

        public String getEntryID(Run entry) {
            return "tag:" + "hudson.dev.java.net," + entry.getTimestamp().get(Calendar.YEAR) + ":" + entry.getParent().getName() + ':' + entry.getId();
        }

        public String getEntryDescription(Run entry) {
            return entry.getDescription();
        }

        public Calendar getEntryTimestamp(Run entry) {
            return entry.getTimestamp();
        }

        public String getEntryAuthor(Run entry) {
            return JenkinsLocationConfiguration.get().getAdminAddress();
        }
    }

    @Override
    public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
        Object result = super.getDynamic(token, req, rsp);
        if (result == null) {
            for (Action action : getTransientActions()) {
                String urlName = action.getUrlName();
                if (urlName == null) {
                    continue;
                }
                if (urlName.equals(token)) {
                    return action;
                }
            }
            result = new RedirectUp();
        }
        return result;
    }

    public static class RedirectUp {

        public void doDynamic(StaplerResponse rsp) throws IOException {
            rsp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            rsp.setContentType("text/html;charset=UTF-8");
            PrintWriter out = rsp.getWriter();
            out.println("<html><head>" + "<meta http-equiv='refresh' content='1;url=..'/>" + "<script>window.location.replace('..');</script>" + "</head>" + "<body style='background-color:white; color:white;'>" + "Not found</body></html>");
            out.flush();
        }
    }
}
