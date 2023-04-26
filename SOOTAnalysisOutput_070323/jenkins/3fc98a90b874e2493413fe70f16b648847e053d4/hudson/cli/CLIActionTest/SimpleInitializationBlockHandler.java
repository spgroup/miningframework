package hudson.cli;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.google.common.collect.Lists;
import hudson.Functions;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.Item;
import hudson.model.User;
import hudson.remoting.Channel;
import hudson.remoting.ChannelBuilder;
import hudson.util.ProcessTree;
import hudson.util.StreamTaskListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import jenkins.model.Jenkins;
import jenkins.security.ApiTokenProperty;
import jenkins.util.FullDuplexHttpService;
import jenkins.util.Timer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.codehaus.groovy.runtime.Security218;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.LoggerRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.TestExtension;
import org.jvnet.hudson.test.recipes.PresetData;
import org.jvnet.hudson.test.recipes.PresetData.DataSet;

public class CLIActionTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    {
        j.timeout = System.getProperty("maven.surefire.debug") == null ? 300 : 0;
    }

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Rule
    public LoggerRule logging = new LoggerRule();

    private ExecutorService pool;

    @Test
    public void testDuplexHttp() throws Exception {
        pool = Executors.newCachedThreadPool();
        try {
            @SuppressWarnings("deprecation")
            FullDuplexHttpStream con = new FullDuplexHttpStream(new URL(j.getURL(), "cli"), null);
            Channel ch = new ChannelBuilder("test connection", pool).build(con.getInputStream(), con.getOutputStream());
            ch.close();
        } finally {
            pool.shutdown();
        }
    }

    @Test
    public void security218() throws Exception {
        pool = Executors.newCachedThreadPool();
        try {
            FullDuplexHttpStream con = new FullDuplexHttpStream(j.getURL(), "cli", null);
            Channel ch = new ChannelBuilder("test connection", pool).build(con.getInputStream(), con.getOutputStream());
            ch.call(new Security218());
            fail("Expected the call to be rejected");
        } catch (Exception e) {
            assertThat(Functions.printThrowable(e), containsString("Rejected: " + Security218.class.getName()));
        } finally {
            pool.shutdown();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
    @Test
    public void security218_take2() throws Exception {
        pool = Executors.newCachedThreadPool();
        try (CLI cli = new CLI(j.getURL())) {
            List commands = new ArrayList();
            commands.add(new Security218());
            cli.execute(commands);
            fail("Expected the call to be rejected");
        } catch (Exception e) {
            assertThat(Functions.printThrowable(e), containsString("Rejected: " + Security218.class.getName()));
        } finally {
            pool.shutdown();
        }
    }

    @Test
    @PresetData(DataSet.NO_ANONYMOUS_READACCESS)
    @Issue("SECURITY-192")
    public void serveCliActionToAnonymousUserWithoutPermissions() throws Exception {
        JenkinsRule.WebClient wc = j.createWebClient();
        wc.assertFails("cli", HttpURLConnection.HTTP_FORBIDDEN);
        WebRequest settings = new WebRequest(new URL(j.getURL(), "cli"));
        settings.setHttpMethod(HttpMethod.POST);
        settings.setAdditionalHeader("Session", UUID.randomUUID().toString());
        settings.setAdditionalHeader("Side", "download");
        Page page = wc.getPage(settings);
        WebResponse webResponse = page.getWebResponse();
        assertEquals("We expect that the proper POST request from CLI gets processed successfully", 200, webResponse.getStatusCode());
    }

    @Test
    public void serveCliActionToAnonymousUserWithAnonymousUserWithPermissions() throws Exception {
        JenkinsRule.WebClient wc = j.createWebClient();
        wc.goTo("cli");
    }

    @Issue({ "JENKINS-12543", "JENKINS-41745" })
    @Test
    public void authentication() throws Exception {
        logging.record(PlainCLIProtocol.class, Level.FINE);
        File jar = tmp.newFile("jenkins-cli.jar");
        FileUtils.copyURLToFile(j.jenkins.getJnlpJars("jenkins-cli.jar").getURL(), jar);
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy().grant(Jenkins.ADMINISTER).everywhere().to(ADMIN));
        j.createFreeStyleProject("p");
        assertExitCode(3, false, jar, "-remoting", "get-job", "p");
        assertExitCode(3, false, jar, "get-job", "p");
        assertExitCode(3, false, jar, "-remoting", "get-job", "--username", ADMIN, "--password", ADMIN, "p");
        assertExitCode(3, false, jar, "get-job", "--username", ADMIN, "--password", ADMIN, "p");
        assertExitCode(0, false, jar, "-remoting", "login", "--username", ADMIN, "--password", ADMIN);
        try {
            assertExitCode(3, false, jar, "-remoting", "get-job", "p");
        } finally {
            assertExitCode(0, false, jar, "-remoting", "logout");
        }
        assertExitCode(3, true, jar, "-remoting", "get-job", "p");
        assertExitCode(0, true, jar, "get-job", "p");
        assertExitCode(6, false, jar, "-remoting", "disable-job", "p");
        assertExitCode(6, false, jar, "disable-job", "p");
        assertExitCode(0, false, jar, "-remoting", "disable-job", "--username", ADMIN, "--password", ADMIN, "p");
        assertExitCode(0, false, jar, "disable-job", "--username", ADMIN, "--password", ADMIN, "p");
        assertExitCode(0, false, jar, "-remoting", "login", "--username", ADMIN, "--password", ADMIN);
        try {
            assertExitCode(0, false, jar, "-remoting", "disable-job", "p");
        } finally {
            assertExitCode(0, false, jar, "-remoting", "logout");
        }
        assertExitCode(6, true, jar, "-remoting", "disable-job", "p");
        assertExitCode(0, true, jar, "disable-job", "p");
        j.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy().grant(Jenkins.ADMINISTER).everywhere().to(ADMIN).grant(Jenkins.READ, Item.READ).everywhere().toEveryone());
        assertExitCode(6, false, jar, "-remoting", "get-job", "p");
        assertExitCode(6, false, jar, "get-job", "p");
        assertExitCode(0, false, jar, "-remoting", "get-job", "--username", ADMIN, "--password", ADMIN, "p");
        assertExitCode(0, false, jar, "get-job", "--username", ADMIN, "--password", ADMIN, "p");
        assertExitCode(0, false, jar, "-remoting", "login", "--username", ADMIN, "--password", ADMIN);
        try {
            assertExitCode(0, false, jar, "-remoting", "get-job", "p");
        } finally {
            assertExitCode(0, false, jar, "-remoting", "logout");
        }
        assertExitCode(6, true, jar, "-remoting", "get-job", "p");
        assertExitCode(0, true, jar, "get-job", "p");
        assertExitCode(6, false, jar, "-remoting", "disable-job", "p");
        assertExitCode(6, false, jar, "disable-job", "p");
        assertExitCode(0, false, jar, "-remoting", "disable-job", "--username", ADMIN, "--password", ADMIN, "p");
        assertExitCode(0, false, jar, "disable-job", "--username", ADMIN, "--password", ADMIN, "p");
        assertExitCode(0, false, jar, "-remoting", "login", "--username", ADMIN, "--password", ADMIN);
        try {
            assertExitCode(0, false, jar, "-remoting", "disable-job", "p");
        } finally {
            assertExitCode(0, false, jar, "-remoting", "logout");
        }
        assertExitCode(6, true, jar, "-remoting", "disable-job", "p");
        assertExitCode(0, true, jar, "disable-job", "p");
        j.jenkins.setSlaveAgentPort(-1);
        assertExitCode(0, true, jar, "-remoting", "get-job", "p");
        assertExitCode(0, true, jar, "-remoting", "disable-job", "p");
    }

    private static final String ADMIN = "admin@mycorp.com";

    private void assertExitCode(int code, boolean useApiToken, File jar, String... args) throws IOException, InterruptedException {
        List<String> commands = Lists.newArrayList("java", "-jar", jar.getAbsolutePath(), "-s", j.getURL().toString(), "-noKeyAuth");
        if (useApiToken) {
            commands.add("-auth");
            commands.add(ADMIN + ":" + User.get(ADMIN).getProperty(ApiTokenProperty.class).getApiToken());
        }
        commands.addAll(Arrays.asList(args));
        final Launcher.LocalLauncher launcher = new Launcher.LocalLauncher(StreamTaskListener.fromStderr());
        final Proc proc = launcher.launch().cmds(commands).stdout(System.out).stderr(System.err).start();
        if (!Functions.isWindows()) {
            Timer.get().schedule(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (proc.isAlive()) {
                            Field procF = Proc.LocalProc.class.getDeclaredField("proc");
                            procF.setAccessible(true);
                            ProcessTree.OSProcess osp = ProcessTree.get().get((Process) procF.get(proc));
                            if (osp != null) {
                                launcher.launch().cmds("kill", "-QUIT", Integer.toString(osp.getPid())).stdout(System.out).stderr(System.err).join();
                            }
                        }
                    } catch (Exception x) {
                        throw new AssertionError(x);
                    }
                }
            }, 1, TimeUnit.MINUTES);
        }
        assertEquals(code, proc.join());
    }

    @Issue("JENKINS-41745")
    @Test
    public void encodingAndLocale() throws Exception {
        File jar = tmp.newFile("jenkins-cli.jar");
        FileUtils.copyURLToFile(j.jenkins.getJnlpJars("jenkins-cli.jar").getURL(), jar);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertEquals(0, new Launcher.LocalLauncher(StreamTaskListener.fromStderr()).launch().cmds("java", "-Dfile.encoding=ISO-8859-2", "-Duser.language=cs", "-Duser.country=CZ", "-jar", jar.getAbsolutePath(), "-s", j.getURL().toString().replaceFirst("/$", ""), "-noKeyAuth", "test-diagnostic").stdout(baos).stderr(System.err).join());
        assertEquals("encoding=ISO-8859-2 locale=cs_CZ", baos.toString().trim());
    }

    @Issue("JENKINS-41745")
    @Test
    public void interleavedStdio() throws Exception {
<<<<<<< MINE
        logging.record(PlainCLIProtocol.class, Level.FINE);
=======
        logging.record(PlainCLIProtocol.class, Level.FINE).record(FullDuplexHttpService.class, Level.FINE);
>>>>>>> YOURS
        File jar = tmp.newFile("jenkins-cli.jar");
        FileUtils.copyURLToFile(j.jenkins.getJnlpJars("jenkins-cli.jar").getURL(), jar);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream(pis);
        PrintWriter pw = new PrintWriter(new TeeOutputStream(pos, System.err), true);
        Proc proc = new Launcher.LocalLauncher(StreamTaskListener.fromStderr()).launch().cmds("java", "-jar", jar.getAbsolutePath(), "-s", j.getURL().toString(), "-noKeyAuth", "groovysh").stdout(new TeeOutputStream(baos, System.out)).stderr(System.err).stdin(pis).start();
        while (!baos.toString().contains("000")) {
            Thread.sleep(100);
        }
        pw.println("11 * 11");
        while (!baos.toString().contains("121")) {
            Thread.sleep(100);
        }
        Thread.sleep(31_000);
        pw.println("11 * 11 * 11");
        while (!baos.toString().contains("1331")) {
            Thread.sleep(100);
        }
        pw.println(":q");
        assertEquals(0, proc.join());
    }

    @TestExtension("encodingAndLocale")
    public static class TestDiagnosticCommand extends CLICommand {

        @Override
        public String getShortDescription() {
            return "Print information about the command environment.";
        }

        @Override
        protected int run() throws Exception {
            stdout.println("encoding=" + getClientCharset() + " locale=" + locale);
            return 0;
        }
    }
}