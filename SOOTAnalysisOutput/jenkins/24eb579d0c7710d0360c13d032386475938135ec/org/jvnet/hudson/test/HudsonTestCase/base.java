package org.jvnet.hudson.test;

import com.gargoylesoftware.htmlunit.DefaultCssErrorHandler;
import com.gargoylesoftware.htmlunit.javascript.HtmlUnitContextFactory;
import com.gargoylesoftware.htmlunit.javascript.host.xml.XMLHttpRequest;
import hudson.*;
import hudson.Util;
import hudson.model.*;
import hudson.model.Queue.Executable;
import hudson.security.ACL;
import hudson.security.AbstractPasswordBasedSecurityRealm;
import hudson.security.GroupDetails;
import hudson.security.SecurityRealm;
import hudson.slaves.ComputerConnector;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.tools.ToolProperty;
import hudson.remoting.Which;
import hudson.Launcher.LocalLauncher;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenEmbedder;
import hudson.model.Node.Mode;
import hudson.security.csrf.CrumbIssuer;
import hudson.slaves.CommandLauncher;
import hudson.slaves.DumbSlave;
import hudson.slaves.RetentionStrategy;
import hudson.tasks.Mailer;
import hudson.tasks.Maven;
import hudson.tasks.Ant;
import hudson.tasks.Ant.AntInstallation;
import hudson.tasks.Maven.MavenInstallation;
import hudson.util.PersistedList;
import hudson.util.ReflectionUtils;
import hudson.util.StreamTaskListener;
import hudson.util.jna.GNUCLibrary;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.jar.Manifest;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.beans.PropertyDescriptor;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import junit.framework.TestCase;
import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.ContextFactory.Listener;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.jvnet.hudson.test.HudsonHomeLoader.CopyExisting;
import org.jvnet.hudson.test.recipes.Recipe;
import org.jvnet.hudson.test.recipes.Recipe.Runner;
import org.jvnet.hudson.test.recipes.WithPlugin;
import org.jvnet.hudson.test.rhino.JavaScriptDebugger;
import org.kohsuke.stapler.ClassDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Dispatcher;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.MetaClassLoader;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.Stapler;
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
import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import com.gargoylesoftware.htmlunit.html.*;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSetBuild;
import hudson.slaves.ComputerListener;
import java.util.concurrent.CountDownLatch;

public abstract class HudsonTestCase extends TestCase implements RootAction {

    public Hudson hudson;

    protected final TestEnvironment env = new TestEnvironment(this);

    protected HudsonHomeLoader homeLoader = HudsonHomeLoader.NEW;

    protected int localPort;

    protected Server server;

    protected String contextPath = "";

    protected List<LenientRunnable> tearDowns = new ArrayList<LenientRunnable>();

    protected List<Runner> recipes = new ArrayList<Runner>();

    private List<WeakReference<WebClient>> clients = new ArrayList<WeakReference<WebClient>>();

    protected JavaScriptDebugger jsDebugger = new JavaScriptDebugger();

    public boolean useLocalPluginManager;

    public ComputerConnectorTester computerConnectorTester = new ComputerConnectorTester(this);

    protected HudsonTestCase(String name) {
        super(name);
    }

    protected HudsonTestCase() {
    }

    @Override
    public void runBare() throws Throwable {
        Thread t = Thread.currentThread();
        String o = getClass().getName() + '.' + t.getName();
        t.setName("Executing " + getName());
        try {
            super.runBare();
        } finally {
            t.setName(o);
        }
    }

    @Override
    protected void setUp() throws Exception {
        env.pin();
        recipe();
        AbstractProject.WORKSPACE.toString();
        User.clear();
        try {
            hudson = newHudson();
        } catch (Exception e) {
            Field f = Hudson.class.getDeclaredField("theInstance");
            f.setAccessible(true);
            f.set(null, null);
            throw e;
        }
        hudson.setNoUsageStatistics(true);
        hudson.setCrumbIssuer(new TestCrumbIssuer());
        hudson.servletContext.setAttribute("app", hudson);
        hudson.servletContext.setAttribute("version", "?");
        WebAppMain.installExpressionFactory(new ServletContextEvent(hudson.servletContext));
        hudson.getJDKs().add(new JDK("default", System.getProperty("java.home")));
        configureUpdateCenter();
        hudson.getActions().add(this);
        Mailer.descriptor().setHudsonUrl(null);
        for (Descriptor d : hudson.getExtensionList(Descriptor.class)) d.load();
    }

    protected void configureUpdateCenter() throws Exception {
        final String updateCenterUrl = "http://localhost:" + JavaNetReverseProxy.getInstance().localPort + "/update-center.json";
        DownloadService.neverUpdate = true;
        UpdateSite.neverUpdate = true;
        PersistedList<UpdateSite> sites = hudson.getUpdateCenter().getSites();
        sites.clear();
        sites.add(new UpdateSite("default", updateCenterUrl));
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            for (WeakReference<WebClient> client : clients) {
                WebClient c = client.get();
                if (c == null)
                    continue;
                c.getPage("about:blank");
            }
            clients.clear();
        } finally {
            server.stop();
            for (LenientRunnable r : tearDowns) r.run();
            hudson.cleanUp();
            env.dispose();
            ExtensionList.clearLegacyInstances();
            DescriptorExtensionList.clearLegacyInstances();
            System.gc();
        }
    }

    @Override
    protected void runTest() throws Throwable {
        System.out.println("=== Starting " + getClass().getSimpleName() + "." + getName());
        SecurityContextHolder.getContext().setAuthentication(ACL.SYSTEM);
        super.runTest();
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
        File home = homeLoader.allocate();
        for (Runner r : recipes) r.decorateHome(this, home);
        return new Hudson(home, createWebServer(), useLocalPluginManager ? null : TestPluginManager.INSTANCE);
    }

    protected ServletContext createWebServer() throws Exception {
        server = new Server();
        WebAppContext context = new WebAppContext(WarExploder.getExplodedDir().getPath(), contextPath);
        context.setClassLoader(getClass().getClassLoader());
        context.setConfigurations(new Configuration[] { new WebXmlConfiguration(), new NoListenerConfiguration() });
        server.setHandler(context);
        context.setMimeTypes(MIME_TYPES);
        SocketConnector connector = new SocketConnector();
        server.addConnector(connector);
        server.addUserRealm(configureUserRealm());
        server.start();
        localPort = connector.getLocalPort();
        return context.getServletContext();
    }

    protected UserRealm configureUserRealm() {
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

    protected MavenInstallation configureDefaultMaven() throws Exception {
        return configureDefaultMaven("apache-maven-2.2.1", MavenInstallation.MAVEN_20);
    }

    protected MavenInstallation configureMaven3() throws Exception {
        MavenInstallation mvn = configureDefaultMaven("apache-maven-3.0.1", MavenInstallation.MAVEN_30);
        MavenInstallation m3 = new MavenInstallation("apache-maven-3.0.1", mvn.getHome(), NO_PROPERTIES);
        hudson.getDescriptorByType(Maven.DescriptorImpl.class).setInstallations(m3);
        return m3;
    }

    protected MavenInstallation configureDefaultMaven(String mavenVersion, int mavenReqVersion) throws Exception {
        String buildDirectory = System.getProperty("buildDirectory", "./target/classes/");
        File mavenAlreadyInstalled = new File(buildDirectory, mavenVersion);
        if (mavenAlreadyInstalled.exists()) {
            MavenInstallation mavenInstallation = new MavenInstallation("default", mavenAlreadyInstalled.getAbsolutePath(), NO_PROPERTIES);
            hudson.getDescriptorByType(Maven.DescriptorImpl.class).setInstallations(mavenInstallation);
            return mavenInstallation;
        }
        String home = System.getProperty("maven.home");
        if (home != null) {
            MavenInstallation mavenInstallation = new MavenInstallation("default", home, NO_PROPERTIES);
            if (mavenInstallation.meetsMavenReqVersion(createLocalLauncher(), mavenReqVersion)) {
                hudson.getDescriptorByType(Maven.DescriptorImpl.class).setInstallations(mavenInstallation);
                return mavenInstallation;
            }
        }
        LOGGER.warning("Extracting a copy of Maven bundled in the test harness. " + "To avoid a performance hit, set the system property 'maven.home' to point to a Maven2 installation.");
        FilePath mvn = hudson.getRootPath().createTempFile("maven", "zip");
        mvn.copyFrom(HudsonTestCase.class.getClassLoader().getResource(mavenVersion + "-bin.zip"));
        File mvnHome = new File(buildDirectory);
        mvn.unzip(new FilePath(mvnHome));
        if (!Functions.isWindows())
            GNUCLibrary.LIBC.chmod(new File(mvnHome, mavenVersion + "/bin/mvn").getPath(), 0755);
        MavenInstallation mavenInstallation = new MavenInstallation("default", new File(mvnHome, mavenVersion).getAbsolutePath(), NO_PROPERTIES);
        hudson.getDescriptorByType(Maven.DescriptorImpl.class).setInstallations(mavenInstallation);
        return mavenInstallation;
    }

    protected Ant.AntInstallation configureDefaultAnt() throws Exception {
        Ant.AntInstallation antInstallation;
        if (System.getenv("ANT_HOME") != null) {
            antInstallation = new AntInstallation("default", System.getenv("ANT_HOME"), NO_PROPERTIES);
        } else {
            LOGGER.warning("Extracting a copy of Ant bundled in the test harness. " + "To avoid a performance hit, set the environment variable ANT_HOME to point to an  Ant installation.");
            FilePath ant = hudson.getRootPath().createTempFile("ant", "zip");
            ant.copyFrom(HudsonTestCase.class.getClassLoader().getResource("apache-ant-1.8.1-bin.zip"));
            File antHome = createTmpDir();
            ant.unzip(new FilePath(antHome));
            if (!Functions.isWindows())
                GNUCLibrary.LIBC.chmod(new File(antHome, "apache-ant-1.8.1/bin/ant").getPath(), 0755);
            antInstallation = new AntInstallation("default", new File(antHome, "apache-ant-1.8.1").getAbsolutePath(), NO_PROPERTIES);
        }
        hudson.getDescriptorByType(Ant.DescriptorImpl.class).setInstallations(antInstallation);
        return antInstallation;
    }

    protected FreeStyleProject createFreeStyleProject() throws IOException {
        return createFreeStyleProject(createUniqueProjectName());
    }

    protected FreeStyleProject createFreeStyleProject(String name) throws IOException {
        return hudson.createProject(FreeStyleProject.class, name);
    }

    protected MatrixProject createMatrixProject() throws IOException {
        return createMatrixProject(createUniqueProjectName());
    }

    protected MatrixProject createMatrixProject(String name) throws IOException {
        return hudson.createProject(MatrixProject.class, name);
    }

    protected MavenModuleSet createMavenProject() throws IOException {
        return createMavenProject(createUniqueProjectName());
    }

    protected MavenModuleSet createMavenProject(String name) throws IOException {
        return hudson.createProject(MavenModuleSet.class, name);
    }

    private String createUniqueProjectName() {
        return "test" + hudson.getItems().size();
    }

    protected LocalLauncher createLocalLauncher() {
        return new LocalLauncher(StreamTaskListener.fromStdout());
    }

    public File createTmpDir() throws IOException {
        return env.temporaryDirectoryAllocator.allocate();
    }

    public DumbSlave createSlave() throws Exception {
        return createSlave("", null);
    }

    public DumbSlave createSlave(Label l) throws Exception {
        return createSlave(l, null);
    }

    public SecurityRealm createDummySecurityRealm() {
        return new AbstractPasswordBasedSecurityRealm() {

            @Override
            protected UserDetails authenticate(String username, String password) throws AuthenticationException {
                if (username.equals(password))
                    return loadUserByUsername(username);
                throw new BadCredentialsException(username);
            }

            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
                return new org.acegisecurity.userdetails.User(username, "", true, true, true, true, new GrantedAuthority[] { AUTHENTICATED_AUTHORITY });
            }

            @Override
            public GroupDetails loadGroupByGroupname(String groupname) throws UsernameNotFoundException, DataAccessException {
                throw new UsernameNotFoundException(groupname);
            }
        };
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
        synchronized (hudson) {
            int sz = hudson.getNodes().size();
            DumbSlave slave = new DumbSlave("slave" + sz, "dummy", createTmpDir().getPath(), "1", Mode.NORMAL, labels == null ? "" : labels, createComputerLauncher(env), RetentionStrategy.NOOP, Collections.EMPTY_LIST);
            hudson.addNode(slave);
            return slave;
        }
    }

    public PretendSlave createPretendSlave(FakeLauncher faker) throws Exception {
        synchronized (hudson) {
            int sz = hudson.getNodes().size();
            PretendSlave slave = new PretendSlave("slave" + sz, createTmpDir().getPath(), "", createComputerLauncher(null), faker);
            hudson.addNode(slave);
            return slave;
        }
    }

    public CommandLauncher createComputerLauncher(EnvVars env) throws URISyntaxException, MalformedURLException {
        int sz = hudson.getNodes().size();
        return new CommandLauncher(String.format("\"%s/bin/java\" %s -jar \"%s\"", System.getProperty("java.home"), SLAVE_DEBUG_PORT > 0 ? " -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=" + (SLAVE_DEBUG_PORT + sz) : "", new File(hudson.getJnlpJars("slave.jar").getURL().toURI()).getAbsolutePath()), env);
    }

    public DumbSlave createOnlineSlave() throws Exception {
        return createOnlineSlave(null);
    }

    public DumbSlave createOnlineSlave(Label l) throws Exception {
        return createOnlineSlave(l, null);
    }

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
        System.out.println("Hudson is running at http://localhost:" + localPort + "/");
        new BufferedReader(new InputStreamReader(System.in)).readLine();
    }

    protected <T> T last(List<T> items) {
        return items.get(items.size() - 1);
    }

    protected void pause() throws IOException {
        new BufferedReader(new InputStreamReader(System.in)).readLine();
    }

    protected Page search(String q) throws Exception {
        return new WebClient().search(q);
    }

    protected void configRoundtrip() throws Exception {
        submit(createWebClient().goTo("configure").getFormByName("config"));
    }

    protected <P extends Job> P configRoundtrip(P job) throws Exception {
        submit(createWebClient().getPage(job, "configure").getFormByName("config"));
        return job;
    }

    protected <P extends Item> P configRoundtrip(P job) throws Exception {
        submit(createWebClient().getPage(job, "configure").getFormByName("config"));
        return job;
    }

    protected <B extends Builder> B configRoundtrip(B before) throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        p.getBuildersList().add(before);
        configRoundtrip(p);
        return (B) p.getBuildersList().get(before.getClass());
    }

    protected <P extends Publisher> P configRoundtrip(P before) throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        p.getPublishersList().add(before);
        configRoundtrip(p);
        return (P) p.getPublishersList().get(before.getClass());
    }

    protected <C extends ComputerConnector> C configRoundtrip(C before) throws Exception {
        computerConnectorTester.connector = before;
        submit(createWebClient().goTo("self/computerConnectorTester/configure").getFormByName("config"));
        return (C) computerConnectorTester.connector;
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
        assertEquals(msg, status, r.getResult());
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
        assertTrue(isGoodHttpStatus(page.getWebResponse().getStatusCode()));
    }

    public <R extends Run> R assertBuildStatusSuccess(R r) throws Exception {
        assertBuildStatus(Result.SUCCESS, r);
        return r;
    }

    public <R extends Run> R assertBuildStatusSuccess(Future<? extends R> r) throws Exception {
        assertNotNull("build was actually scheduled", r);
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
        String log = getLog(run);
        if (log.contains(substring))
            return;
        System.out.println(log);
        fail("Console output of " + run + " didn't contain " + substring);
    }

    protected static String getLog(Run run) throws IOException {
        return Util.loadFile(run.getLogFile(), run.getCharset());
    }

    public void assertXPath(HtmlPage page, String xpath) {
        assertNotNull("There should be an object that matches XPath:" + xpath, page.getDocumentElement().selectSingleNode(xpath));
    }

    public void assertXPath(DomNode page, String xpath) {
        List<? extends Object> nodes = page.getByXPath(xpath);
        assertFalse("There should be an object that matches XPath:" + xpath, nodes.isEmpty());
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
        assertFalse("no nodes matching xpath found", nodes.isEmpty());
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
        assertTrue("needle found in haystack", found);
    }

    public void assertStringContains(String message, String haystack, String needle) {
        if (haystack.contains(needle)) {
            return;
        } else {
            fail(message + " (seeking '" + needle + "')");
        }
    }

    public void assertStringContains(String haystack, String needle) {
        if (haystack.contains(needle)) {
            return;
        } else {
            fail("Could not find '" + needle + "'.");
        }
    }

    public void assertHelpExists(final Class<? extends Describable> type, final String properties) throws Exception {
        executeOnServer(new Callable<Object>() {

            public Object call() throws Exception {
                Descriptor d = hudson.getDescriptor(type);
                WebClient wc = createWebClient();
                for (String property : listProperties(properties)) {
                    String url = d.getHelpFile(property);
                    assertNotNull("Help file for the property " + property + " is missing on " + type, url);
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

    protected HtmlInput findPreviousInputElement(HtmlElement current, String name) {
        return (HtmlInput) current.selectSingleNode("(preceding::input[@name='_." + name + "'])[last()]");
    }

    protected HtmlButton getButtonByCaption(HtmlForm f, String s) {
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
        assertNotNull("lhs is null", lhs);
        assertNotNull("rhs is null", rhs);
        for (String p : properties.split(",")) {
            PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(lhs, p);
            Object lp, rp;
            if (pd == null) {
                try {
                    Field f = lhs.getClass().getField(p);
                    lp = f.get(lhs);
                    rp = f.get(rhs);
                } catch (NoSuchFieldException e) {
                    assertNotNull("No such property " + p + " on " + lhs.getClass(), pd);
                    return;
                }
            } else {
                lp = PropertyUtils.getProperty(lhs, p);
                rp = PropertyUtils.getProperty(rhs, p);
            }
            if (lp != null && rp != null && lp.getClass().isArray() && rp.getClass().isArray()) {
                int m = Array.getLength(lp);
                int n = Array.getLength(rp);
                assertEquals("Array length is different for property " + p, m, n);
                for (int i = 0; i < m; i++) assertEquals(p + "[" + i + "] is different", Array.get(lp, i), Array.get(rp, i));
                return;
            }
            assertEquals("Property " + p + " is different", lp, rp);
        }
    }

    public void assertEqualDataBoundBeans(Object lhs, Object rhs) throws Exception {
        if (lhs == null && rhs == null)
            return;
        if (lhs == null)
            fail("lhs is null while rhs=" + rhs);
        if (rhs == null)
            fail("rhs is null while lhs=" + rhs);
        Constructor<?> lc = findDataBoundConstructor(lhs.getClass());
        Constructor<?> rc = findDataBoundConstructor(rhs.getClass());
        assertEquals("Data bound constructor mismatch. Different type?", lc, rc);
        List<String> primitiveProperties = new ArrayList<String>();
        String[] names = ClassDescriptor.loadParameterNames(lc);
        Class<?>[] types = lc.getParameterTypes();
        assertEquals(names.length, types.length);
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
                        assertEquals(litem, ritem);
                    }
                }
                assertFalse("collection size mismatch between " + lhs + " and " + rhs, ltr.hasNext() ^ rtr.hasNext());
            } else if (findDataBoundConstructor(types[i]) != null) {
                assertEqualDataBoundBeans(lv, rv);
            } else {
                primitiveProperties.add(names[i]);
            }
        }
        if (!primitiveProperties.isEmpty())
            assertEqualBeans(lhs, rhs, Util.join(primitiveProperties, ","));
    }

    private Constructor<?> findDataBoundConstructor(Class<?> c) {
        for (Constructor<?> m : c.getConstructors()) {
            if (m.getAnnotation(DataBoundConstructor.class) != null)
                return m;
        }
        return null;
    }

    protected <T extends Descriptor<?>> T get(Class<T> d) {
        return hudson.getDescriptorByType(d);
    }

    protected boolean isSomethingHappening() {
        if (!hudson.getQueue().isEmpty())
            return true;
        for (Computer n : hudson.getComputers()) if (!n.isIdle())
            return true;
        return false;
    }

    protected void waitUntilNoActivity() throws Exception {
        waitUntilNoActivityUpTo(60 * 1000);
    }

    protected void waitUntilNoActivityUpTo(int timeout) throws Exception {
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
                List<Executable> building = new ArrayList<Executable>();
                for (Computer c : hudson.getComputers()) {
                    for (Executor e : c.getExecutors()) {
                        if (e.isBusy())
                            building.add(e.getCurrentExecutable());
                    }
                }
                throw new AssertionError(String.format("Hudson is still doing something after %dms: queue=%s building=%s", timeout, Arrays.asList(hudson.getQueue().getItems()), building));
            }
        }
    }

    protected void recipe() throws Exception {
        recipeLoadCurrentPlugin();
        try {
            Method runMethod = getClass().getMethod(getName());
            for (final Annotation a : runMethod.getAnnotations()) {
                Recipe r = a.annotationType().getAnnotation(Recipe.class);
                if (r == null)
                    continue;
                final Runner runner = r.value().newInstance();
                recipes.add(runner);
                tearDowns.add(new LenientRunnable() {

                    public void run() throws Exception {
                        runner.tearDown(HudsonTestCase.this, a);
                    }
                });
                runner.setup(this, a);
            }
        } catch (NoSuchMethodException e) {
        }
    }

    protected void recipeLoadCurrentPlugin() throws Exception {
        final Enumeration<URL> e = getClass().getClassLoader().getResources("the.hpl");
        if (!e.hasMoreElements())
            return;
        final URL hpl = e.nextElement();
        recipes.add(new Runner() {

            @Override
            public void decorateHome(HudsonTestCase testCase, File home) throws Exception {
                while (e.hasMoreElements()) {
                    final URL hpl = e.nextElement();
                    Manifest m = new Manifest(hpl.openStream());
                    String shortName = m.getMainAttributes().getValue("Short-Name");
                    if (shortName == null)
                        throw new Error(hpl + " doesn't have the Short-Name attribute");
                    FileUtils.copyURLToFile(hpl, new File(home, "plugins/" + shortName + ".hpl"));
                    String dependencies = m.getMainAttributes().getValue("Plugin-Dependencies");
                    if (dependencies != null) {
                        MavenEmbedder embedder = new MavenEmbedder(getClass().getClassLoader(), null);
                        for (String dep : dependencies.split(",")) {
                            String[] tokens = dep.split(":");
                            String artifactId = tokens[0];
                            String version = tokens[1];
                            File dependencyJar = null;
                            Exception resolutionError = null;
                            for (String groupId : new String[] { "org.jvnet.hudson.plugins", "org.jvnet.hudson.main" }) {
                                URL dependencyPomResource = getClass().getResource("/META-INF/maven/" + groupId + "/" + artifactId + "/pom.xml");
                                if (dependencyPomResource != null) {
                                    dependencyJar = Which.jarFile(dependencyPomResource);
                                    break;
                                } else {
                                    Artifact a;
                                    a = embedder.createArtifact(groupId, artifactId, version, "compile", "hpi");
                                    try {
                                        embedder.resolve(a, Arrays.asList(embedder.createRepository("http://maven.glassfish.org/content/groups/public/", "repo")), embedder.getLocalRepository());
                                        dependencyJar = a.getFile();
                                    } catch (AbstractArtifactResolutionException x) {
                                        resolutionError = x;
                                    }
                                }
                            }
                            if (dependencyJar == null)
                                throw new Exception("Failed to resolve plugin: " + dep, resolutionError);
                            File dst = new File(home, "plugins/" + artifactId + ".hpi");
                            if (!dst.exists() || dst.lastModified() != dependencyJar.lastModified()) {
                                FileUtils.copyFile(dependencyJar, dst);
                            }
                        }
                    }
                }
            }
        });
    }

    public HudsonTestCase withNewHome() {
        return with(HudsonHomeLoader.NEW);
    }

    public HudsonTestCase withExistingHome(File source) throws Exception {
        return with(new CopyExisting(source));
    }

    public HudsonTestCase withPresetData(String name) {
        name = "/" + name + ".zip";
        URL res = getClass().getResource(name);
        if (res == null)
            throw new IllegalArgumentException("No such data set found: " + name);
        return with(new CopyExisting(res));
    }

    public HudsonTestCase with(HudsonHomeLoader homeLoader) {
        this.homeLoader = homeLoader;
        return this;
    }

    public <V> V executeOnServer(final Callable<V> c) throws Exception {
        final Exception[] t = new Exception[1];
        final List<V> r = new ArrayList<V>(1);
        ClosureExecuterAction cea = hudson.getExtensionList(RootAction.class).get(ClosureExecuterAction.class);
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
        createWebClient().goTo("closures/?uuid=" + id);
        if (t[0] != null)
            throw t[0];
        return r.get(0);
    }

    private Object writeReplace() {
        throw new AssertionError("HudsonTestCase " + getName() + " is not supposed to be serialized");
    }

    public WebClient createWebClient() {
        return new WebClient();
    }

    public class WebClient extends com.gargoylesoftware.htmlunit.WebClient {

        public WebClient() {
            super(BrowserVersion.FIREFOX_2);
            setPageCreator(HudsonPageCreator.INSTANCE);
            clients.add(new WeakReference<WebClient>(this));
            setAjaxController(new AjaxController() {

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
                    return e.getURI().contains("/yui/");
                }
            });
            getJavaScriptEngine().getContextFactory().addListener(new Listener() {

                public void contextCreated(Context cx) {
                    if (cx.getDebugger() == null)
                        cx.setDebugger(jsDebugger, null);
                }

                public void contextReleased(Context cx) {
                }
            });
        }

        public WebClient login(String username, String password) throws Exception {
            HtmlPage page = goTo("/login");
            HtmlForm form = page.getFormByName("login");
            form.getInputByName("j_username").setValueAttribute(username);
            form.getInputByName("j_password").setValueAttribute(password);
            form.submit(null);
            return this;
        }

        public WebClient login(String username) throws Exception {
            login(username, username);
            return this;
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

        public Page goTo(String relative, String expectedContentType) throws IOException, SAXException {
            Page p = super.getPage(getContextPath() + relative);
            assertEquals(expectedContentType, p.getWebResponse().getContentType());
            return p;
        }

        public XmlPage goToXml(String path) throws IOException, SAXException {
            Page page = goTo(path, "application/xml");
            if (page instanceof XmlPage)
                return (XmlPage) page;
            else
                return null;
        }

        public String getContextPath() throws IOException {
            return getURL().toExternalForm();
        }

        public WebRequestSettings addCrumb(WebRequestSettings req) {
            NameValuePair[] crumb = { new NameValuePair() };
            crumb[0].setName(hudson.getCrumbIssuer().getDescriptor().getCrumbRequestField());
            crumb[0].setValue(hudson.getCrumbIssuer().getCrumb(null));
            req.setRequestParameters(Arrays.asList(crumb));
            return req;
        }

        public URL createCrumbedUrl(String relativePath) throws IOException {
            CrumbIssuer issuer = hudson.getCrumbIssuer();
            String crumbName = issuer.getDescriptor().getCrumbRequestField();
            String crumb = issuer.getCrumb(null);
            return new URL(getContextPath() + relativePath + "?" + crumbName + "=" + crumb);
        }

        public HtmlPage eval(final Runnable requestHandler) throws IOException, SAXException {
            ClosureExecuterAction cea = hudson.getExtensionList(RootAction.class).get(ClosureExecuterAction.class);
            UUID id = UUID.randomUUID();
            cea.add(id, requestHandler);
            return goTo("closures/?uuid=" + id);
        }

        public Dim interactiveJavaScriptDebugger() {
            Global global = new Global();
            HtmlUnitContextFactory cf = getJavaScriptEngine().getContextFactory();
            global.init(cf);
            Dim dim = org.mozilla.javascript.tools.debugger.Main.mainEmbedded(cf, global, "Rhino debugger: " + getName());
            dim.setBreakOnExceptions(true);
            return dim;
        }
    }

    private static final Logger XML_HTTP_REQUEST_LOGGER = Logger.getLogger(XMLHttpRequest.class.getName());

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
                return !record.getMessage().contains("XMLHttpRequest.getResponseHeader() was called before the response was available.");
            }
        });
        System.setProperty("org.mortbay.jetty.Request.maxFormContentSize", "-1");
    }

    private static final Logger LOGGER = Logger.getLogger(HudsonTestCase.class.getName());

    protected static final List<ToolProperty<?>> NO_PROPERTIES = Collections.<ToolProperty<?>>emptyList();

    public static int SLAVE_DEBUG_PORT = Integer.getInteger(HudsonTestCase.class.getName() + ".slaveDebugPort", -1);

    public static final MimeTypes MIME_TYPES = new MimeTypes();

    static {
        MIME_TYPES.addMimeMapping("js", "application/javascript");
        Functions.DEBUG_YUI = true;
        ClassicPluginStrategy.useAntClassLoader = true;
        DNSMultiCast.disabled = true;
    }
}
