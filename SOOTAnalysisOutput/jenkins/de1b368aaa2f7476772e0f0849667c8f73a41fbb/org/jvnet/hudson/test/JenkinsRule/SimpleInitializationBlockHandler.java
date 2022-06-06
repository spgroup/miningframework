package org.jvnet.hudson.test;

import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.DefaultCssErrorHandler;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.HtmlUnitContextFactory;
import com.gargoylesoftware.htmlunit.javascript.host.xml.XMLHttpRequest;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import hudson.ClassicPluginStrategy;
import hudson.CloseProofOutputStream;
import hudson.DNSMultiCast;
import hudson.DescriptorExtensionList;
import hudson.EnvVars;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher;
import hudson.Main;
import hudson.PluginManager;
import hudson.Util;
import hudson.WebAppMain;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixRun;
import hudson.maven.MavenBuild;
import hudson.maven.MavenEmbedder;
import hudson.maven.MavenEmbedderException;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.maven.MavenUtil;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.DownloadService;
import hudson.model.Executor;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.JDK;
import hudson.model.Job;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.Result;
import hudson.model.RootAction;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.UpdateSite;
import hudson.model.User;
import hudson.model.View;
import hudson.remoting.Which;
import hudson.security.ACL;
import hudson.security.AbstractPasswordBasedSecurityRealm;
import hudson.security.GroupDetails;
import hudson.security.csrf.CrumbIssuer;
import hudson.slaves.CommandLauncher;
import hudson.slaves.ComputerConnector;
import hudson.slaves.ComputerListener;
import hudson.slaves.DumbSlave;
import hudson.slaves.RetentionStrategy;
import hudson.tasks.Ant;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.Maven;
import hudson.tasks.Publisher;
import hudson.tools.ToolProperty;
import hudson.util.PersistedList;
import hudson.util.ReflectionUtils;
import hudson.util.StreamTaskListener;
import hudson.util.jna.GNUCLibrary;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsAdaptor;
import net.sf.json.JSONObject;
import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.ContextFactory;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.hamcrest.Matchers;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.recipes.Recipe;
import org.jvnet.hudson.test.rhino.JavaScriptDebugger;
import org.kohsuke.stapler.ClassDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Dispatcher;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.MetaClassLoader;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.webapp.Configuration;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.jetty.webapp.WebXmlConfiguration;
import org.mozilla.javascript.tools.debugger.Dim;
import org.mozilla.javascript.tools.shell.Global;
import org.springframework.dao.DataAccessException;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;
import org.xml.sax.SAXException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.management.ThreadInfo;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.jar.Manifest;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import jenkins.model.JenkinsLocationConfiguration;
import org.acegisecurity.GrantedAuthorityImpl;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import org.junit.internal.AssumptionViolatedException;
import static org.junit.matchers.JUnitMatchers.containsString;
import org.junit.rules.TemporaryFolder;

@SuppressWarnings({ "deprecation", "rawtypes" })
public class JenkinsRule implements TestRule, MethodRule, RootAction {

    protected TestEnvironment env;

    protected Description testDescription;

    @Deprecated
public Hudson hudson;

    public Jenkins jenkins;

    protected HudsonHomeLoader homeLoader = HudsonHomeLoader.NEW;

    protected int localPort;

    protected Server server;

<<<<<<< MINE
public String contextPath = "/jenkins";
=======
protected String contextPath = "/jenkins";
>>>>>>> YOURS


    protected List<LenientRunnable> tearDowns = new ArrayList<LenientRunnable>();

    protected List<JenkinsRecipe.Runner> recipes = new ArrayList<JenkinsRecipe.Runner>();

    private List<WebClient> clients = new ArrayList<WebClient>();

    protected JavaScriptDebugger jsDebugger = new JavaScriptDebugger();

    public boolean useLocalPluginManager;

    public int timeout = Integer.getInteger("jenkins.test.timeout", System.getProperty("maven.surefire.debug") == null ? 180 : 0);

    private volatile Timer timeoutTimer;

    private PluginManager pluginManager = TestPluginManager.INSTANCE;

    public JenkinsComputerConnectorTester computerConnectorTester = new JenkinsComputerConnectorTester(this);

    public Jenkins getInstance() {
        return jenkins;
    }

    public void before() throws Throwable {
        if (Boolean.getBoolean("ignore.random.failures")) {
            RandomlyFails rf = testDescription.getAnnotation(RandomlyFails.class);
            if (rf != null) {
                throw new AssumptionViolatedException("Known to randomly fail: " + rf.value());
            }
        }
        env = new TestEnvironment(testDescription);
        env.pin();
        recipe();
        AbstractProject.WORKSPACE.toString();
        User.clear();
        try {
            jenkins = hudson = newHudson();
        } catch (Exception e) {
            Field f = Jenkins.class.getDeclaredField("theInstance");
            f.setAccessible(true);
            f.set(null, null);
            throw e;
        }
        jenkins.setNoUsageStatistics(true);
        jenkins.setCrumbIssuer(new TestCrumbIssuer());
        jenkins.servletContext.setAttribute("app", jenkins);
        jenkins.servletContext.setAttribute("version", "?");
        WebAppMain.installExpressionFactory(new ServletContextEvent(jenkins.servletContext));
        jenkins.getJDKs().add(new JDK("default", System.getProperty("java.home")));
        configureUpdateCenter();
        jenkins.getActions().add(this);
<<<<<<< MINE
        JenkinsLocationConfiguration.get().setUrl(getURL().toString());
        setUpTimeout();
=======
        Mailer.DescriptorImpl desc = Mailer.descriptor();
        if (desc != null)
            Mailer.descriptor().setHudsonUrl(getURL().toString());
        JenkinsLocationConfiguration.get().setUrl(getURL().toString());
        for (Descriptor d : jenkins.getExtensionList(Descriptor.class)) d.load();
>>>>>>> YOURS
    }

    protected void configureUpdateCenter() throws Exception {
        final String updateCenterUrl = "http://localhost:" + JavaNetReverseProxy.getInstance().localPort + "/update-center.json";
        DownloadService.neverUpdate = true;
        UpdateSite.neverUpdate = true;
        PersistedList<UpdateSite> sites = jenkins.getUpdateCenter().getSites();
        sites.clear();
        sites.add(new UpdateSite("default", updateCenterUrl));
    }

    protected void setUpTimeout() {
        if (timeout <= 0) {
            System.out.println("Test timeout disabled.");
            return;
        }
        final Thread testThread = Thread.currentThread();
        timeoutTimer = new Timer();
        timeoutTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                if (timeoutTimer != null) {
                    LOGGER.warning(String.format("Test timed out (after %d seconds).", timeout));
                    testThread.interrupt();
            }
            }
        }, TimeUnit.SECONDS.toMillis(timeout));
    }

<<<<<<< MINE
public void after() throws Exception {
=======
protected void after() throws Exception {
>>>>>>> YOURS
        try {
<<<<<<< MINE
            if (jenkins != null) {
                for (EndOfTestListener tl : jenkins.getExtensionList(EndOfTestListener.class)) tl.onTearDown();
            }
=======
            for (EndOfTestListener tl : jenkins.getExtensionList(EndOfTestListener.class)) tl.onTearDown();
>>>>>>> YOURS
            if (timeoutTimer != null) {
                timeoutTimer.cancel();
                timeoutTimer = null;
            }
            for (WebClient client : clients) {
                try {
                    client.getPage("about:blank");
                } catch (IOException e) {
                }
                client.closeAllWindows();
            }
            clients.clear();
        } finally {
            try {
                server.stop();
            } catch (Exception e) {
            }
            for (LenientRunnable r : tearDowns) try {
                r.run();
            } catch (Exception e) {
            }
            if (jenkins != null)
            jenkins.cleanUp();
            ExtensionList.clearLegacyInstances();
            DescriptorExtensionList.clearLegacyInstances();
            try {
                env.dispose();
            } catch (Exception x) {
                x.printStackTrace();
            }
            System.gc();
        }
    }

    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        return apply(base, Description.createTestDescription(method.getMethod().getDeclaringClass(), method.getName(), method.getAnnotations()));
    }

    public Statement apply(final Statement base, final Description description) {
        if (description.getAnnotation(WithoutJenkins.class) != null) {
            return base;
        }
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                testDescription = description;
                Thread t = Thread.currentThread();
                String o = t.getName();
                t.setName("Executing " + testDescription.getDisplayName());
                before();
                try {
                    System.out.println("=== Starting " + testDescription.getDisplayName());
                    ACL.impersonate(ACL.SYSTEM);
                    try {
                        base.evaluate();
                    } catch (Throwable th) {
                        try {
                            throw new BreakException();
                        } catch (BreakException e) {
                        }
                        RandomlyFails rf = testDescription.getAnnotation(RandomlyFails.class);
                        if (rf != null) {
                            System.err.println("Note: known to randomly fail: " + rf.value());
                        }
                        ThreadInfo[] threadInfos = Functions.getThreadInfos();
                        Functions.ThreadGroupMap m = Functions.sortThreadsAndGetGroupMap(threadInfos);
                        for (ThreadInfo ti : threadInfos) {
                            System.err.println(Functions.dumpThreadInfo(ti, m));
                        }
                        throw th;
                    }
                } finally {
                    after();
                    testDescription = null;
                    t.setName(o);
                }
            }
        };
    }

    @SuppressWarnings("serial")
    public static class BreakException extends Exception {
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return "self";
    }

    protected Hudson newHudson() throws Exception {
        ServletContext webServer = createWebServer();
        File home = homeLoader.allocate();
        for (JenkinsRecipe.Runner r : recipes) r.decorateHome(this, home);
        return new Hudson(home, webServer, getPluginManager());
    }

    public PluginManager getPluginManager() {
        if (jenkins == null) {
            return useLocalPluginManager ? null : pluginManager;
        } else {
            return jenkins.getPluginManager();
        }
    }

    public void setPluginManager(PluginManager pluginManager) {
        this.useLocalPluginManager = false;
        this.pluginManager = pluginManager;
        if (jenkins != null)
            throw new IllegalStateException("Too late to override the plugin manager");
    }

    public JenkinsRule with(PluginManager pluginManager) {
        setPluginManager(pluginManager);
        return this;
    }

    public File getWebAppRoot() throws Exception {
        return WarExploder.getExplodedDir();
    }

    protected ServletContext createWebServer() throws Exception {
        server = new Server();
        WebAppContext context = new WebAppContext(WarExploder.getExplodedDir().getPath(), contextPath);
        context.setClassLoader(getClass().getClassLoader());
        context.setConfigurations(new Configuration[] { new WebXmlConfiguration(), new NoListenerConfiguration() });
        server.setHandler(context);
        context.setMimeTypes(MIME_TYPES);
        SocketConnector connector = new SocketConnector();
        connector.setHeaderBufferSize(12 * 1024);
        if (System.getProperty("port") != null)
            connector.setPort(Integer.parseInt(System.getProperty("port")));
        server.setThreadPool(new ThreadPoolImpl(new ThreadPoolExecutor(10, 10, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {

            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("Jetty Thread Pool");
                return t;
            }
        })));
        server.addConnector(connector);
        server.addUserRealm(configureUserRealm());
        server.start();
        localPort = connector.getLocalPort();
        LOGGER.log(Level.INFO, "Running on {0}", getURL());
        return context.getServletContext();
    }

    public UserRealm configureUserRealm() {
        HashUserRealm realm = new HashUserRealm();
        realm.setName("default");
        realm.put("alice", "alice");
        realm.put("bob", "bob");
        realm.put("charlie", "charlie");
        realm.addUserToRole("alice", "female");
        realm.addUserToRole("bob", "male");
        realm.addUserToRole("charlie", "male");
        return realm;
    }

    public Maven.MavenInstallation configureDefaultMaven() throws Exception {
        return configureDefaultMaven("apache-maven-2.2.1", Maven.MavenInstallation.MAVEN_20);
    }

    public Maven.MavenInstallation configureMaven3() throws Exception {
        Maven.MavenInstallation mvn = configureDefaultMaven("apache-maven-3.0.1", Maven.MavenInstallation.MAVEN_30);
        Maven.MavenInstallation m3 = new Maven.MavenInstallation("apache-maven-3.0.1", mvn.getHome(), NO_PROPERTIES);
        jenkins.getDescriptorByType(Maven.DescriptorImpl.class).setInstallations(m3);
        return m3;
    }

    public Maven.MavenInstallation configureDefaultMaven(String mavenVersion, int mavenReqVersion) throws Exception {
        File buildDirectory = new File(System.getProperty("buildDirectory", "target"));
        File mvnHome = new File(buildDirectory, mavenVersion);
        if (mvnHome.exists()) {
            Maven.MavenInstallation mavenInstallation = new Maven.MavenInstallation("default", mvnHome.getAbsolutePath(), NO_PROPERTIES);
            jenkins.getDescriptorByType(Maven.DescriptorImpl.class).setInstallations(mavenInstallation);
            return mavenInstallation;
        }
        String home = System.getProperty("maven.home");
        if (home != null) {
            Maven.MavenInstallation mavenInstallation = new Maven.MavenInstallation("default", home, NO_PROPERTIES);
            if (mavenInstallation.meetsMavenReqVersion(createLocalLauncher(), mavenReqVersion)) {
                jenkins.getDescriptorByType(Maven.DescriptorImpl.class).setInstallations(mavenInstallation);
                return mavenInstallation;
            }
        }
        LOGGER.warning("Extracting a copy of Maven bundled in the test harness into " + mvnHome + ". " + "To avoid a performance hit, set the system property 'maven.home' to point to a Maven2 installation.");
        FilePath mvn = jenkins.getRootPath().createTempFile("maven", "zip");
<<<<<<< MINE
        mvn.copyFrom(JenkinsRule.class.getClassLoader().getResource(mavenVersion + "-bin.zip"));
=======
        mvn.copyFrom(HudsonTestCase.class.getClassLoader().getResource(mavenVersion + "-bin.zip"));
>>>>>>> YOURS
        mvn.unzip(new FilePath(buildDirectory));
        if (!Functions.isWindows())
            GNUCLibrary.LIBC.chmod(new File(mvnHome, "bin/mvn").getPath(), 0755);
        Maven.MavenInstallation mavenInstallation = new Maven.MavenInstallation("default", mvnHome.getAbsolutePath(), NO_PROPERTIES);
        jenkins.getDescriptorByType(Maven.DescriptorImpl.class).setInstallations(mavenInstallation);
        return mavenInstallation;
    }

    public Ant.AntInstallation configureDefaultAnt() throws Exception {
        Ant.AntInstallation antInstallation;
        if (System.getenv("ANT_HOME") != null) {
            antInstallation = new Ant.AntInstallation("default", System.getenv("ANT_HOME"), NO_PROPERTIES);
        } else {
            LOGGER.warning("Extracting a copy of Ant bundled in the test harness. " + "To avoid a performance hit, set the environment variable ANT_HOME to point to an  Ant installation.");
            FilePath ant = jenkins.getRootPath().createTempFile("ant", "zip");
            ant.copyFrom(JenkinsRule.class.getClassLoader().getResource("apache-ant-1.8.1-bin.zip"));
            File antHome = createTmpDir();
            ant.unzip(new FilePath(antHome));
            if (!Functions.isWindows())
                GNUCLibrary.LIBC.chmod(new File(antHome, "apache-ant-1.8.1/bin/ant").getPath(), 0755);
            antInstallation = new Ant.AntInstallation("default", new File(antHome, "apache-ant-1.8.1").getAbsolutePath(), NO_PROPERTIES);
        }
        jenkins.getDescriptorByType(Ant.DescriptorImpl.class).setInstallations(antInstallation);
        return antInstallation;
    }

    public FreeStyleProject createFreeStyleProject() throws IOException {
        return createFreeStyleProject(createUniqueProjectName());
    }

    public FreeStyleProject createFreeStyleProject(String name) throws IOException {
        return jenkins.createProject(FreeStyleProject.class, name);
    }

    public MatrixProject createMatrixProject() throws IOException {
        return createMatrixProject(createUniqueProjectName());
    }

    public MatrixProject createMatrixProject(String name) throws IOException {
        return jenkins.createProject(MatrixProject.class, name);
    }

    public MavenModuleSet createMavenProject() throws IOException {
        return createMavenProject(createUniqueProjectName());
    }

    public MavenModuleSet createMavenProject(String name) throws IOException {
        MavenModuleSet mavenModuleSet = jenkins.createProject(MavenModuleSet.class, name);
        mavenModuleSet.setRunHeadless(true);
        return mavenModuleSet;
    }

    public MockFolder createFolder(String name) throws IOException {
        return jenkins.createProject(MockFolder.class, name);
    }

    protected String createUniqueProjectName() {
        return "test" + jenkins.getItems().size();
    }

    public Launcher.LocalLauncher createLocalLauncher() {
        return new Launcher.LocalLauncher(StreamTaskListener.fromStdout());
    }

    @Deprecated
    public File createTmpDir() throws IOException {
        return env.temporaryDirectoryAllocator.allocate();
    }

    public DumbSlave createSlave() throws Exception {
        return createSlave("", null);
    }

    public DumbSlave createSlave(Label l) throws Exception {
        return createSlave(l, null);
    }

    public DummySecurityRealm createDummySecurityRealm() {
        return new DummySecurityRealm();
    }

    public static class DummySecurityRealm extends AbstractPasswordBasedSecurityRealm {

        private final Map<String, Set<String>> groupsByUser = new HashMap<String, Set<String>>();

        DummySecurityRealm() {
        }

        @Override
        protected UserDetails authenticate(String username, String password) throws AuthenticationException {
            if (username.equals(password))
                return loadUserByUsername(username);
            throw new BadCredentialsException(username);
        }

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
            List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
            auths.add(AUTHENTICATED_AUTHORITY);
            Set<String> groups = groupsByUser.get(username);
            if (groups != null) {
                for (String g : groups) {
                    auths.add(new GrantedAuthorityImpl(g));
                }
            }
            return new org.acegisecurity.userdetails.User(username, "", true, true, true, true, auths.toArray(new GrantedAuthority[auths.size()]));
        }

        @Override
        public GroupDetails loadGroupByGroupname(final String groupname) throws UsernameNotFoundException, DataAccessException {
            for (Set<String> groups : groupsByUser.values()) {
                if (groups.contains(groupname)) {
                    return new GroupDetails() {

                        @Override
                        public String getName() {
                            return groupname;
                        }
                    };
                }
            }
            throw new UsernameNotFoundException(groupname);
        }

        public void addGroups(String username, String... groups) {
            Set<String> gs = groupsByUser.get(username);
            if (gs == null) {
                groupsByUser.put(username, gs = new TreeSet<String>());
            }
            gs.addAll(Arrays.asList(groups));
        }
    }

    public URL getURL() throws IOException {
        return new URL("http://localhost:" + localPort + contextPath + "/");
    }

    public DumbSlave createSlave(EnvVars env) throws Exception {
        return createSlave("", env);
    }

    public DumbSlave createSlave(Label l, EnvVars env) throws Exception {
        return createSlave(l == null ? null : l.getExpression(), env);
    }

    public DumbSlave createSlave(String labels, EnvVars env) throws Exception {
        synchronized (jenkins) {
            int sz = jenkins.getNodes().size();
            return createSlave("slave" + sz, labels, env);
        }
    }

    public DumbSlave createSlave(String nodeName, String labels, EnvVars env) throws Exception {
        synchronized (jenkins) {
            DumbSlave slave = new DumbSlave(nodeName, "dummy", createTmpDir().getPath(), "1", Node.Mode.NORMAL, labels == null ? "" : labels, createComputerLauncher(env), RetentionStrategy.NOOP, Collections.EMPTY_LIST);
            jenkins.addNode(slave);
            return slave;
        }
    }

    public PretendSlave createPretendSlave(FakeLauncher faker) throws Exception {
        synchronized (jenkins) {
            int sz = jenkins.getNodes().size();
            PretendSlave slave = new PretendSlave("slave" + sz, createTmpDir().getPath(), "", createComputerLauncher(null), faker);
            jenkins.addNode(slave);
            return slave;
        }
    }

    public CommandLauncher createComputerLauncher(EnvVars env) throws URISyntaxException, MalformedURLException {
        int sz = jenkins.getNodes().size();
        return new CommandLauncher(String.format("\"%s/bin/java\" %s -jar \"%s\"", System.getProperty("java.home"), SLAVE_DEBUG_PORT > 0 ? " -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=" + (SLAVE_DEBUG_PORT + sz) : "", new File(jenkins.getJnlpJars("slave.jar").getURL().toURI()).getAbsolutePath()), env);
    }

    public DumbSlave createOnlineSlave() throws Exception {
        return createOnlineSlave(null);
    }

    public DumbSlave createOnlineSlave(Label l) throws Exception {
        return createOnlineSlave(l, null);
    }

    @SuppressWarnings({ "deprecation" })
    public DumbSlave createOnlineSlave(Label l, EnvVars env) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        ComputerListener waiter = new ComputerListener() {

            @Override
            public void onOnline(Computer C, TaskListener t) {
                latch.countDown();
                unregister();
            }
        };
        waiter.register();
        DumbSlave s = createSlave(l, env);
        latch.await();
        return s;
    }

    public void interactiveBreak() throws Exception {
        System.out.println("Jenkins is running at http://localhost:" + localPort + "/");
        new BufferedReader(new InputStreamReader(System.in)).readLine();
    }

    public <T> T last(List<T> items) {
        return items.get(items.size() - 1);
    }

    public void pause() throws IOException {
        new BufferedReader(new InputStreamReader(System.in)).readLine();
    }

    public Page search(String q) throws Exception {
        return new WebClient().search(q);
    }

    public void configRoundtrip() throws Exception {
        submit(createWebClient().goTo("configure").getFormByName("config"));
    }

    public <P extends Job> P configRoundtrip(P job) throws Exception {
        submit(createWebClient().getPage(job, "configure").getFormByName("config"));
        return job;
    }

    public <P extends Item> P configRoundtrip(P job) throws Exception {
        submit(createWebClient().getPage(job, "configure").getFormByName("config"));
        return job;
    }

    public <B extends Builder> B configRoundtrip(B before) throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        p.getBuildersList().add(before);
        configRoundtrip((Item) p);
        return (B) p.getBuildersList().get(before.getClass());
    }

    public <P extends Publisher> P configRoundtrip(P before) throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        p.getPublishersList().add(before);
        configRoundtrip((Item) p);
        return (P) p.getPublishersList().get(before.getClass());
    }

    public <C extends ComputerConnector> C configRoundtrip(C before) throws Exception {
        computerConnectorTester.connector = before;
        submit(createWebClient().goTo("self/computerConnectorTester/configure").getFormByName("config"));
        return (C) computerConnectorTester.connector;
    }

    public User configRoundtrip(User u) throws Exception {
        submit(createWebClient().goTo(u.getUrl() + "/configure").getFormByName("config"));
        return u;
    }

    public <N extends Node> N configRoundtrip(N node) throws Exception {
        submit(createWebClient().goTo("computer/" + node.getNodeName() + "/configure").getFormByName("config"));
        return (N) jenkins.getNode(node.getNodeName());
    }

    public <V extends View> V configRoundtrip(V view) throws Exception {
        submit(createWebClient().getPage(view, "configure").getFormByName("viewConfig"));
        return view;
    }

    public <R extends Run> R assertBuildStatus(Result status, R r) throws Exception {
        if (status == r.getResult())
            return r;
        String msg = "unexpected build status; build log was:\n------\n" + getLog(r) + "\n------\n";
        if (r instanceof MatrixBuild) {
            MatrixBuild mb = (MatrixBuild) r;
            for (MatrixRun mr : mb.getRuns()) {
                msg += "--- " + mr.getParent().getCombination() + " ---\n" + getLog(mr) + "\n------\n";
            }
        }
        assertThat(msg, r.getResult(), is(status));
        return r;
    }

    public boolean isGoodHttpStatus(int status) {
        if ((400 <= status) && (status <= 417)) {
            return false;
        }
        if ((500 <= status) && (status <= 505)) {
            return false;
        }
        return true;
    }

    public void assertGoodStatus(Page page) {
        assertThat(isGoodHttpStatus(page.getWebResponse().getStatusCode()), is(true));
    }

    public <R extends Run> R assertBuildStatusSuccess(R r) throws Exception {
        assertBuildStatus(Result.SUCCESS, r);
        return r;
    }

    public <R extends Run> R assertBuildStatusSuccess(Future<? extends R> r) throws Exception {
        assertThat("build was actually scheduled", r, Matchers.notNullValue());
        return assertBuildStatusSuccess(r.get());
    }

    public <J extends AbstractProject<J, R>, R extends AbstractBuild<J, R>> R buildAndAssertSuccess(J job) throws Exception {
        return assertBuildStatusSuccess(job.scheduleBuild2(0));
    }

    public FreeStyleBuild buildAndAssertSuccess(FreeStyleProject job) throws Exception {
        return assertBuildStatusSuccess(job.scheduleBuild2(0));
    }

    public MavenModuleSetBuild buildAndAssertSuccess(MavenModuleSet job) throws Exception {
        return assertBuildStatusSuccess(job.scheduleBuild2(0));
    }

    public MavenBuild buildAndAssertSuccess(MavenModule job) throws Exception {
        return assertBuildStatusSuccess(job.scheduleBuild2(0));
    }

    public void assertLogContains(String substring, Run run) throws Exception {
        assertThat(getLog(run), containsString(substring));
    }

    public void assertLogNotContains(String substring, Run run) throws Exception {
        assertThat(getLog(run), not(containsString(substring)));
    }

    public static String getLog(Run run) throws IOException {
        return Util.loadFile(run.getLogFile(), run.getCharset());
    }

    public void assertXPath(HtmlPage page, String xpath) {
        assertNotNull("There should be an object that matches XPath:" + xpath, page.getDocumentElement().selectSingleNode(xpath));
    }

    public void assertXPath(DomNode page, String xpath) {
        List<? extends Object> nodes = page.getByXPath(xpath);
        assertThat("There should be an object that matches XPath:" + xpath, nodes.isEmpty(), is(false));
    }

    public void assertXPathValue(DomNode page, String xpath, String expectedValue) {
        Object node = page.getFirstByXPath(xpath);
        assertNotNull("no node found", node);
        assertTrue("the found object was not a Node " + xpath, node instanceof org.w3c.dom.Node);
        org.w3c.dom.Node n = (org.w3c.dom.Node) node;
        String textString = n.getTextContent();
        assertEquals("xpath value should match for " + xpath, expectedValue, textString);
    }

    public void assertXPathValueContains(DomNode page, String xpath, String needle) {
        Object node = page.getFirstByXPath(xpath);
        assertNotNull("no node found", node);
        assertTrue("the found object was not a Node " + xpath, node instanceof org.w3c.dom.Node);
        org.w3c.dom.Node n = (org.w3c.dom.Node) node;
        String textString = n.getTextContent();
        assertTrue("needle found in haystack", textString.contains(needle));
    }

    public void assertXPathResultsContainText(DomNode page, String xpath, String needle) {
        List<? extends Object> nodes = page.getByXPath(xpath);
        assertThat("no nodes matching xpath found", nodes.isEmpty(), is(false));
        boolean found = false;
        for (Object o : nodes) {
            if (o instanceof org.w3c.dom.Node) {
                org.w3c.dom.Node n = (org.w3c.dom.Node) o;
                String textString = n.getTextContent();
                if ((textString != null) && textString.contains(needle)) {
                    found = true;
                    break;
                }
            }
        }
        assertThat("needle found in haystack", found, is(true));
    }

    public void assertAllImageLoadSuccessfully(HtmlPage p) {
        for (HtmlImage img : p.<HtmlImage>selectNodes("//IMG")) {
            try {
                img.getHeight();
            } catch (IOException e) {
                throw new Error("Failed to load " + img.getSrcAttribute(), e);
            }
        }
    }

    public void assertStringContains(String message, String haystack, String needle) {
        assertThat(message, haystack, Matchers.containsString(needle));
    }

    public void assertStringContains(String haystack, String needle) {
        assertThat(haystack, Matchers.containsString(needle));
    }

    public void assertHelpExists(final Class<? extends Describable> type, final String properties) throws Exception {
        executeOnServer(new Callable<Object>() {

            public Object call() throws Exception {
                Descriptor d = jenkins.getDescriptor(type);
                WebClient wc = createWebClient();
                for (String property : listProperties(properties)) {
                    String url = d.getHelpFile(property);
                    assertThat("Help file for the property " + property + " is missing on " + type, url, Matchers.notNullValue());
                    wc.goTo(url);
                }
                return null;
            }
        });
    }

    private List<String> listProperties(String properties) {
        List<String> props = new ArrayList<String>(Arrays.asList(properties.split(",")));
        for (String p : props.toArray(new String[props.size()])) {
            if (p.startsWith("-")) {
                props.remove(p);
                props.remove(p.substring(1));
            }
        }
        return props;
    }

    public HtmlPage submit(HtmlForm form) throws Exception {
        return (HtmlPage) form.submit((HtmlButton) last(form.getHtmlElementsByTagName("button")));
    }

    public HtmlPage submit(HtmlForm form, String name) throws Exception {
        for (HtmlElement e : form.getHtmlElementsByTagName("button")) {
            HtmlElement p = (HtmlElement) e.getParentNode().getParentNode();
            if (p.getAttribute("name").equals(name)) {
                ((HtmlButton) e).click();
                return (HtmlPage) form.submit((HtmlButton) e);
            }
        }
        throw new AssertionError("No such submit button with the name " + name);
    }

    public HtmlInput findPreviousInputElement(HtmlElement current, String name) {
        return (HtmlInput) current.selectSingleNode("(preceding::input[@name='_." + name + "'])[last()]");
    }

    public HtmlButton getButtonByCaption(HtmlForm f, String s) {
        for (HtmlElement b : f.getHtmlElementsByTagName("button")) {
            if (b.getTextContent().trim().equals(s))
                return (HtmlButton) b;
        }
        return null;
    }

    public TaskListener createTaskListener() {
        return new StreamTaskListener(new CloseProofOutputStream(System.out));
    }

    public void assertEqualBeans(Object lhs, Object rhs, String properties) throws Exception {
        assertThat("LHS", lhs, notNullValue());
        assertThat("RHS", rhs, notNullValue());
        for (String p : properties.split(",")) {
            PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(lhs, p);
            Object lp, rp;
            if (pd == null) {
                try {
                    Field f = lhs.getClass().getField(p);
                    lp = f.get(lhs);
                    rp = f.get(rhs);
                } catch (NoSuchFieldException e) {
                    assertThat("No such property " + p + " on " + lhs.getClass(), pd, notNullValue());
                    return;
                }
            } else {
                lp = PropertyUtils.getProperty(lhs, p);
                rp = PropertyUtils.getProperty(rhs, p);
            }
            if (lp != null && rp != null && lp.getClass().isArray() && rp.getClass().isArray()) {
                int m = Array.getLength(lp);
                int n = Array.getLength(rp);
                assertThat("Array length is different for property " + p, n, is(m));
                for (int i = 0; i < m; i++) assertThat(p + "[" + i + "] is different", Array.get(rp, i), is(Array.get(lp, i)));
                return;
            }
            assertThat("Property " + p + " is different", rp, is(lp));
        }
    }

    public void setQuietPeriod(int qp) {
        JenkinsAdaptor.setQuietPeriod(jenkins, qp);
    }

    public void assertEqualDataBoundBeans(Object lhs, Object rhs) throws Exception {
        if (lhs == null && rhs == null)
            return;
        if (lhs == null)
            fail("lhs is null while rhs=" + rhs);
        if (rhs == null)
            fail("rhs is null while lhs=" + lhs);
        Constructor<?> lc = findDataBoundConstructor(lhs.getClass());
        Constructor<?> rc = findDataBoundConstructor(rhs.getClass());
        assertThat("Data bound constructor mismatch. Different type?", (Constructor) rc, is((Constructor) lc));
        List<String> primitiveProperties = new ArrayList<String>();
        String[] names = ClassDescriptor.loadParameterNames(lc);
        Class<?>[] types = lc.getParameterTypes();
        assertThat(types.length, is(names.length));
        for (int i = 0; i < types.length; i++) {
            Object lv = ReflectionUtils.getPublicProperty(lhs, names[i]);
            Object rv = ReflectionUtils.getPublicProperty(rhs, names[i]);
            if (Iterable.class.isAssignableFrom(types[i])) {
                Iterable lcol = (Iterable) lv;
                Iterable rcol = (Iterable) rv;
                Iterator ltr, rtr;
                for (ltr = lcol.iterator(), rtr = rcol.iterator(); ltr.hasNext() && rtr.hasNext(); ) {
                    Object litem = ltr.next();
                    Object ritem = rtr.next();
                    if (findDataBoundConstructor(litem.getClass()) != null) {
                        assertEqualDataBoundBeans(litem, ritem);
                    } else {
                        assertThat(ritem, is(litem));
                    }
                }
                assertThat("collection size mismatch between " + lhs + " and " + rhs, ltr.hasNext() ^ rtr.hasNext(), is(false));
            } else if (findDataBoundConstructor(types[i]) != null || (lv != null && findDataBoundConstructor(lv.getClass()) != null) || (rv != null && findDataBoundConstructor(rv.getClass()) != null)) {
                assertEqualDataBoundBeans(lv, rv);
            } else {
                primitiveProperties.add(names[i]);
            }
        }
        if (!primitiveProperties.isEmpty())
            assertEqualBeans(lhs, rhs, Util.join(primitiveProperties, ","));
    }

    public void assertEqualDataBoundBeans(List<?> lhs, List<?> rhs) throws Exception {
        assertThat(rhs.size(), is(lhs.size()));
        for (int i = 0; i < lhs.size(); i++) assertEqualDataBoundBeans(lhs.get(i), rhs.get(i));
    }

    public Constructor<?> findDataBoundConstructor(Class<?> c) {
        for (Constructor<?> m : c.getConstructors()) {
            if (m.getAnnotation(DataBoundConstructor.class) != null)
                return m;
        }
        return null;
    }

    public <T extends Descriptor<?>> T get(Class<T> d) {
        return jenkins.getDescriptorByType(d);
    }

    public boolean isSomethingHappening() {
        if (!jenkins.getQueue().isEmpty())
            return true;
        for (Computer n : jenkins.getComputers()) if (!n.isIdle())
            return true;
        return false;
    }

    public void waitUntilNoActivity() throws Exception {
        waitUntilNoActivityUpTo(60 * 1000);
    }

    public void waitUntilNoActivityUpTo(int timeout) throws Exception {
        long startTime = System.currentTimeMillis();
        int streak = 0;
        while (true) {
            Thread.sleep(10);
            if (isSomethingHappening())
                streak = 0;
            else
                streak++;
            if (streak > 5)
                return;
            if (System.currentTimeMillis() - startTime > timeout) {
                List<Queue.Executable> building = new ArrayList<Queue.Executable>();
                for (Computer c : jenkins.getComputers()) {
                    for (Executor e : c.getExecutors()) {
                        if (e.isBusy())
                            building.add(e.getCurrentExecutable());
                    }
                }
                throw new AssertionError(String.format("Hudson is still doing something after %dms: queue=%s building=%s", timeout, Arrays.asList(jenkins.getQueue().getItems()), building));
            }
        }
    }

    public void recipe() throws Exception {
        recipeLoadCurrentPlugin();
        try {
            for (final Annotation a : testDescription.getAnnotations()) {
                JenkinsRecipe r = a.annotationType().getAnnotation(JenkinsRecipe.class);
                if (r == null)
                    continue;
                final JenkinsRecipe.Runner runner = r.value().newInstance();
                recipes.add(runner);
                tearDowns.add(new LenientRunnable() {

                    public void run() throws Exception {
                        runner.tearDown(JenkinsRule.this, a);
                    }
                });
                runner.setup(this, a);
            }
        } catch (NoSuchMethodException e) {
        }
    }

    public void recipeLoadCurrentPlugin() throws Exception {
        final Enumeration<URL> jpls = getClass().getClassLoader().getResources("the.jpl");
        final Enumeration<URL> hpls = getClass().getClassLoader().getResources("the.hpl");
        final List<URL> all = Collections.list(jpls);
        all.addAll(Collections.list(hpls));
        if (all.isEmpty())
            return;
        recipes.add(new JenkinsRecipe.Runner() {

            private File home;

            private final List<Jpl> jpls = new ArrayList<Jpl>();

            @Override
            public void decorateHome(JenkinsRule testCase, File home) throws Exception {
                this.home = home;
                this.jpls.clear();
                for (URL hpl : all) {
                    Jpl jpl = new Jpl(hpl);
                    jpl.loadManifest();
                    jpls.add(jpl);
                }
                for (Jpl jpl : jpls) {
                    jpl.resolveDependencies();
                }
            }

            class Jpl {

                final URL jpl;

                Manifest m;

                private String shortName;

                Jpl(URL jpl) {
                    this.jpl = jpl;
                }

                void loadManifest() throws IOException {
                    m = new Manifest(jpl.openStream());
                    shortName = m.getMainAttributes().getValue("Short-Name");
                    if (shortName == null)
                        throw new Error(jpl + " doesn't have the Short-Name attribute");
                    FileUtils.copyURLToFile(jpl, new File(home, "plugins/" + shortName + ".jpl"));
                }

                void resolveDependencies() throws Exception {
                    String dependencies = m.getMainAttributes().getValue("Plugin-Dependencies");
                    if (dependencies != null) {
                        DEPENDENCY: for (String dep : dependencies.split(",")) {
                            String suffix = ";resolution:=optional";
                            boolean optional = dep.endsWith(suffix);
                            if (optional) {
                                dep = dep.substring(0, dep.length() - suffix.length());
                            }
                            String[] tokens = dep.split(":");
                            String artifactId = tokens[0];
                            String version = tokens[1];
                            for (Jpl other : jpls) {
                                if (other.shortName.equals(artifactId))
                                    continue DEPENDENCY;
                            }
                            File dependencyJar = resolveDependencyJar(artifactId, version);
                            if (dependencyJar == null) {
                                if (optional) {
                                    System.err.println("cannot resolve optional dependency " + dep + " of " + shortName + "; skipping");
                                    continue;
                                }
                                throw new IOException("Could not resolve " + dep + " in " + System.getProperty("java.class.path"));
                            }
                            File dst = new File(home, "plugins/" + artifactId + ".jpi");
                            if (!dst.exists() || dst.lastModified() != dependencyJar.lastModified()) {
                                FileUtils.copyFile(dependencyJar, dst);
                            }
                        }
                    }
                }
            }

            private MavenEmbedder embedder;

            private MavenEmbedder getMavenEmbedder() throws MavenEmbedderException, IOException {
                if (embedder == null)
                    embedder = MavenUtil.createEmbedder(new StreamTaskListener(System.out, Charset.defaultCharset()), (File) null, null);
                return embedder;
            }

            @CheckForNull
            private File resolveDependencyJar(String artifactId, String version) throws Exception {
                Enumeration<URL> manifests = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
                while (manifests.hasMoreElements()) {
                    URL manifest = manifests.nextElement();
                    InputStream is = manifest.openStream();
                    Manifest m = new Manifest(is);
                    is.close();
                    if (artifactId.equals(m.getMainAttributes().getValue("Short-Name")))
                        return Which.jarFile(manifest);
                }
                Enumeration<URL> jellies = getClass().getClassLoader().getResources("index.jelly");
                while (jellies.hasMoreElements()) {
                    URL jellyU = jellies.nextElement();
                    if (jellyU.getProtocol().equals("file")) {
                        File jellyF = new File(jellyU.toURI());
                        File classes = jellyF.getParentFile();
                        if (classes.getName().equals("classes")) {
                            File target = classes.getParentFile();
                            if (target.getName().equals("target")) {
                                File hpi = new File(target, artifactId + ".hpi");
                                if (hpi.isFile()) {
                                    return hpi;
                                }
                            }
                        }
                    }
                }
                Exception resolutionError = null;
                for (String groupId : PLUGIN_GROUPIDS) {
                    URL dependencyPomResource = getClass().getResource("/META-INF/maven/" + groupId + "/" + artifactId + "/pom.xml");
                    if (dependencyPomResource != null) {
                        return Which.jarFile(dependencyPomResource);
                    } else {
                        try {
                            return resolvePluginFile(artifactId, version, groupId, "hpi");
                        } catch (AbstractArtifactResolutionException x) {
                            try {
                                return resolvePluginFile(artifactId, version, groupId, "jpi");
                            } catch (AbstractArtifactResolutionException x2) {
                                resolutionError = x;
                            }
                        }
                    }
                }
                throw new Exception("Failed to resolve plugin: " + artifactId + " version " + version, resolutionError);
            }

<<<<<<< MINE
            @CheckForNull
            private File resolvePluginFile(String artifactId, String version, String groupId, String type) throws Exception {
                final Artifact jpi = getMavenEmbedder().createArtifact(groupId, artifactId, version, "compile", type);
                getMavenEmbedder().resolve(jpi, Arrays.asList(getMavenEmbedder().createRepository("http://maven.glassfish.org/content/groups/public/", "repo")), embedder.getLocalRepository());
=======
            private File resolvePluginFile(MavenEmbedder embedder, String artifactId, String version, String groupId, String type) throws MavenEmbedderException, ComponentLookupException, AbstractArtifactResolutionException {
                final Artifact jpi = embedder.createArtifact(groupId, artifactId, version, "compile", type);
                embedder.resolve(jpi, Arrays.asList(embedder.createRepository("http://maven.glassfish.org/content/groups/public/", "repo")), embedder.getLocalRepository());
                if (jpi.getFile() == null) {
                    throw new ArtifactNotFoundException("cannot find plugin dependency", jpi);
                }
>>>>>>> YOURS
                return jpi.getFile();
            }
        });
    }

    public JenkinsRule withNewHome() {
        return with(HudsonHomeLoader.NEW);
    }

    public JenkinsRule withExistingHome(File source) throws Exception {
        return with(new HudsonHomeLoader.CopyExisting(source));
    }

    public JenkinsRule withPresetData(String name) {
        name = "/" + name + ".zip";
        URL res = getClass().getResource(name);
        if (res == null)
            throw new IllegalArgumentException("No such data set found: " + name);
        return with(new HudsonHomeLoader.CopyExisting(res));
    }

    public JenkinsRule with(HudsonHomeLoader homeLoader) {
        this.homeLoader = homeLoader;
        return this;
    }

    public <V> V executeOnServer(Callable<V> c) throws Exception {
        return createWebClient().executeOnServer(c);
    }

    private Object writeReplace() {
        throw new AssertionError("JenkinsRule " + testDescription.getDisplayName() + " is not supposed to be serialized");
    }

    public WebClient createWebClient() {
        return new WebClient();
    }

    public class WebClient extends com.gargoylesoftware.htmlunit.WebClient {

        private static final long serialVersionUID = 5808915989048338267L;

        public WebClient() {
            super(BrowserVersion.FIREFOX_2);
            setPageCreator(HudsonPageCreator.INSTANCE);
            clients.add(this);
            setAjaxController(new AjaxController() {

                private static final long serialVersionUID = -5844060943564822678L;

                public boolean processSynchron(HtmlPage page, WebRequestSettings settings, boolean async) {
                    return false;
                }
            });
            setCssErrorHandler(new ErrorHandler() {

                final ErrorHandler defaultHandler = new DefaultCssErrorHandler();

                public void warning(CSSParseException exception) throws CSSException {
                    if (!ignore(exception))
                        defaultHandler.warning(exception);
                }

                public void error(CSSParseException exception) throws CSSException {
                    if (!ignore(exception))
                        defaultHandler.error(exception);
                }

                public void fatalError(CSSParseException exception) throws CSSException {
                    if (!ignore(exception))
                        defaultHandler.fatalError(exception);
                }

                private boolean ignore(CSSParseException e) {
                    String uri = e.getURI();
                    return uri.contains("/yui/") || uri.contains("/css/style.css") || uri.contains("/css/responsive-grid.css");
                }
            });
            getJavaScriptEngine().getContextFactory().addListener(new ContextFactory.Listener() {

                public void contextCreated(Context cx) {
                    if (cx.getDebugger() == null)
                        cx.setDebugger(jsDebugger, null);
                }

                public void contextReleased(Context cx) {
                }
            });
            setTimeout(60 * 1000);
        }

        public WebClient login(String username, String password) throws Exception {
<<<<<<< MINE
            return login(username, password, false);
=======
            HtmlPage page = goTo("login");
            HtmlForm form = page.getFormByName("login");
            form.getInputByName("j_username").setValueAttribute(username);
            form.getInputByName("j_password").setValueAttribute(password);
            form.submit(null);
            return this;
>>>>>>> YOURS
        }

        public WebClient login(String username, String password, boolean rememberMe) throws Exception {
            HtmlPage page = goTo("login");
            HtmlForm form = page.getFormByName("login");
            form.getInputByName("j_username").setValueAttribute(username);
            form.getInputByName("j_password").setValueAttribute(password);
            try {
                form.getInputByName("remember_me").setChecked(rememberMe);
            } catch (ElementNotFoundException e) {
                assert !rememberMe;
            }
            form.submit(null);
            return this;
        }

        public WebClient login(String username) throws Exception {
            login(username, username);
            return this;
        }

        public <V> V executeOnServer(final Callable<V> c) throws Exception {
            final Exception[] t = new Exception[1];
            final List<V> r = new ArrayList<V>(1);
            ClosureExecuterAction cea = jenkins.getExtensionList(RootAction.class).get(ClosureExecuterAction.class);
            UUID id = UUID.randomUUID();
            cea.add(id, new Runnable() {

                public void run() {
                    try {
                        StaplerResponse rsp = Stapler.getCurrentResponse();
                        rsp.setStatus(200);
                        rsp.setContentType("text/html");
                        r.add(c.call());
                    } catch (Exception e) {
                        t[0] = e;
                    }
                }
            });
            goTo("closures/?uuid=" + id);
            if (t[0] != null)
                throw t[0];
            return r.get(0);
        }

        public HtmlPage search(String q) throws IOException, SAXException {
            HtmlPage top = goTo("");
            HtmlForm search = top.getFormByName("search");
            search.getInputByName("q").setValueAttribute(q);
            return (HtmlPage) search.submit(null);
        }

        public HtmlPage getPage(Run r) throws IOException, SAXException {
            return getPage(r, "");
        }

        public HtmlPage getPage(Run r, String relative) throws IOException, SAXException {
            return goTo(r.getUrl() + relative);
        }

        public HtmlPage getPage(Item item) throws IOException, SAXException {
            return getPage(item, "");
        }

        public HtmlPage getPage(Item item, String relative) throws IOException, SAXException {
            return goTo(item.getUrl() + relative);
        }

        public HtmlPage getPage(Node item) throws IOException, SAXException {
            return getPage(item, "");
        }

        public HtmlPage getPage(Node item, String relative) throws IOException, SAXException {
            return goTo(item.toComputer().getUrl() + relative);
        }

        public HtmlPage getPage(View view) throws IOException, SAXException {
            return goTo(view.getUrl());
        }

        public HtmlPage getPage(View view, String relative) throws IOException, SAXException {
            return goTo(view.getUrl() + relative);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Page getPage(String url) throws IOException, FailingHttpStatusCodeException {
            return super.getPage(url);
        }

        public HtmlPage goTo(String relative) throws IOException, SAXException {
            Page p = goTo(relative, "text/html");
            if (p instanceof HtmlPage) {
                return (HtmlPage) p;
            } else {
                throw new AssertionError("Expected text/html but instead the content type was " + p.getWebResponse().getContentType());
            }
        }

<<<<<<< MINE
public Page goTo(String relative, @CheckForNull String expectedContentType) throws IOException, SAXException {
            assert !relative.startsWith("/");
            Page p;
            try {
                p = super.getPage(getContextPath() + relative);
            } catch (IOException x) {
                Throwable cause = x.getCause();
                if (cause instanceof SocketTimeoutException) {
                    throw new AssumptionViolatedException("failed to get " + relative + " due to read timeout", cause);
                } else if (cause != null) {
                    cause.printStackTrace();
                }
                throw x;
            }
            if (expectedContentType != null) {
=======
public Page goTo(String relative, String expectedContentType) throws IOException, SAXException {
            assert !relative.startsWith("/");
            Page p = super.getPage(getContextPath() + relative);
>>>>>>> YOURS
            assertThat(p.getWebResponse().getContentType(), is(expectedContentType));
            }
            return p;
        }

        public XmlPage goToXml(String path) throws IOException, SAXException {
            Page page = goTo(path, "application/xml");
            if (page instanceof XmlPage)
                return (XmlPage) page;
            else
                return null;
        }

        public void assertFails(String url, int statusCode) throws Exception {
            assert !url.startsWith("/");
            try {
                fail(url + " should have been rejected but produced: " + super.getPage(getContextPath() + url).getWebResponse().getContentAsString());
            } catch (FailingHttpStatusCodeException x) {
                assertEquals(statusCode, x.getStatusCode());
            }
        }

        public String getContextPath() throws IOException {
            return getURL().toExternalForm();
        }

        public WebRequestSettings addCrumb(WebRequestSettings req) {
            NameValuePair[] crumb = { new NameValuePair() };
            crumb[0].setName(jenkins.getCrumbIssuer().getDescriptor().getCrumbRequestField());
            crumb[0].setValue(jenkins.getCrumbIssuer().getCrumb(null));
            req.setRequestParameters(Arrays.asList(crumb));
            return req;
        }

        public URL createCrumbedUrl(String relativePath) throws IOException {
            CrumbIssuer issuer = jenkins.getCrumbIssuer();
            String crumbName = issuer.getDescriptor().getCrumbRequestField();
            String crumb = issuer.getCrumb(null);
            return new URL(getContextPath() + relativePath + "?" + crumbName + "=" + crumb);
        }

        public HtmlPage eval(final Runnable requestHandler) throws IOException, SAXException {
            ClosureExecuterAction cea = jenkins.getExtensionList(RootAction.class).get(ClosureExecuterAction.class);
            UUID id = UUID.randomUUID();
            cea.add(id, requestHandler);
            return goTo("closures/?uuid=" + id);
        }

        public Dim interactiveJavaScriptDebugger() {
            Global global = new Global();
            HtmlUnitContextFactory cf = getJavaScriptEngine().getContextFactory();
            global.init(cf);
            Dim dim = org.mozilla.javascript.tools.debugger.Main.mainEmbedded(cf, global, "Rhino debugger: " + testDescription.getDisplayName());
            dim.setBreakOnExceptions(true);
            return dim;
        }
    }

    private static final Logger XML_HTTP_REQUEST_LOGGER = Logger.getLogger(XMLHttpRequest.class.getName());

    private static final Logger SPRING_LOGGER = Logger.getLogger("org.springframework");

    private static final Logger JETTY_LOGGER = Logger.getLogger("org.mortbay.log");

    private static final Logger HTMLUNIT_DOCUMENT_LOGGER = Logger.getLogger("com.gargoylesoftware.htmlunit.javascript.host.Document");

    private static final Logger HTMLUNIT_JS_LOGGER = Logger.getLogger("com.gargoylesoftware.htmlunit.javascript.StrictErrorReporter");

    static {
        Locale.setDefault(Locale.ENGLISH);
        {
            Dispatcher.TRACE = true;
            MetaClass.NO_CACHE = true;
            File dir = new File("src/main/resources");
            if (dir.exists() && MetaClassLoader.debugLoader == null)
                try {
                    MetaClassLoader.debugLoader = new MetaClassLoader(new URLClassLoader(new URL[] { dir.toURI().toURL() }));
                } catch (MalformedURLException e) {
                    throw new AssertionError(e);
                }
        }
        SPRING_LOGGER.setLevel(Level.WARNING);
        JETTY_LOGGER.setLevel(Level.WARNING);
        Main.isUnitTest = true;
        XML_HTTP_REQUEST_LOGGER.setFilter(new Filter() {

            public boolean isLoggable(LogRecord record) {
                return !record.getMessage().contains("XMLHttpRequest.getResponseHeader() was called before the respon" + "se was available.");
            }
        });
        HTMLUNIT_DOCUMENT_LOGGER.setFilter(new Filter() {

            @Override
            public boolean isLoggable(LogRecord record) {
                return !record.getMessage().equals("Unexpected exception occurred while parsing HTML snippet");
            }
        });
        HTMLUNIT_JS_LOGGER.setFilter(new Filter() {

            @Override
            public boolean isLoggable(LogRecord record) {
                return !record.getMessage().contains("Unexpected exception occurred while parsing HTML snippet: input name=\"x\"");
            }
        });
        System.setProperty("org.mortbay.jetty.Request.maxFormContentSize", "-1");
    }

    static {
        Locale.setDefault(Locale.ENGLISH);
        {
            Dispatcher.TRACE = true;
            MetaClass.NO_CACHE = true;
            File dir = new File("src/main/resources");
            if (dir.exists() && MetaClassLoader.debugLoader == null)
                try {
                    MetaClassLoader.debugLoader = new MetaClassLoader(new URLClassLoader(new URL[] { dir.toURI().toURL() }));
                } catch (MalformedURLException e) {
                    throw new AssertionError(e);
                }
        }
        Logger.getLogger("org.springframework").setLevel(Level.WARNING);
        Main.isUnitTest = true;
        XML_HTTP_REQUEST_LOGGER.setFilter(new Filter() {

            public boolean isLoggable(LogRecord record) {
                return !record.getMessage().contains("XMLHttpRequest.getResponseHeader() was called before the respon" + "se was available.");
            }
        });
        System.setProperty("org.mortbay.jetty.Request.maxFormContentSize", "-1");
    }

    private static final Logger LOGGER = Logger.getLogger(HudsonTestCase.class.getName());

    public static final List<ToolProperty<?>> NO_PROPERTIES = Collections.<ToolProperty<?>>emptyList();

    public static final int SLAVE_DEBUG_PORT = Integer.getInteger(HudsonTestCase.class.getName() + ".slaveDebugPort", -1);

    public static final MimeTypes MIME_TYPES = new MimeTypes();

    static {
        MIME_TYPES.addMimeMapping("js", "application/javascript");
        Functions.DEBUG_YUI = true;
        ClassicPluginStrategy.useAntClassLoader = true;
        DNSMultiCast.disabled = true;
        if (!Functions.isWindows()) {
            try {
                GNUCLibrary.LIBC.unsetenv("MAVEN_OPTS");
                GNUCLibrary.LIBC.unsetenv("MAVEN_DEBUG_OPTS");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to cancel out MAVEN_OPTS", e);
            }
        }
    }

    public static class TestBuildWrapper extends BuildWrapper {

        public Result buildResultInTearDown;

        @Override
        public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
            return new BuildWrapper.Environment() {

                @Override
                public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
                    buildResultInTearDown = build.getResult();
                    return true;
                }
            };
        }

        @Extension
        public static class TestBuildWrapperDescriptor extends BuildWrapperDescriptor {

            @Override
            public boolean isApplicable(AbstractProject<?, ?> project) {
                return true;
            }

            @Override
            public BuildWrapper newInstance(StaplerRequest req, JSONObject formData) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getDisplayName() {
                return this.getClass().getName();
            }
        }
    }

    public Description getTestDescription() {
        return testDescription;
    }

    public static final List<String> PLUGIN_GROUPIDS = new ArrayList<String>(Arrays.asList("org.jvnet.hudson.plugins", "org.jvnet.hudson.main"));
}