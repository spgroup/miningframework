package hudson.tools;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.ProxyConfiguration;
import hudson.Util;
import hudson.model.DownloadService.Downloadable;
import hudson.model.JDK;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;
import hudson.util.HttpResponses;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import jenkins.security.MasterToSlaveCallable;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import javax.servlet.ServletException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static hudson.tools.JDKInstaller.Preference.*;

public class JDKInstaller extends ToolInstaller {

    static {
        Protocol.registerProtocol("http", new Protocol("http", new hudson.util.NoClientBindProtocolSocketFactory(), 80));
        Protocol.registerProtocol("https", new Protocol("https", new hudson.util.NoClientBindSSLProtocolSocketFactory(), 443));
    }

    public final String id;

    public final boolean acceptLicense;

    @DataBoundConstructor
    public JDKInstaller(String id, boolean acceptLicense) {
        super(null);
        this.id = id;
        this.acceptLicense = acceptLicense;
    }

    public FilePath performInstallation(ToolInstallation tool, Node node, TaskListener log) throws IOException, InterruptedException {
        FilePath expectedLocation = preferredLocation(tool, node);
        PrintStream out = log.getLogger();
        try {
            if (!acceptLicense) {
                out.println(Messages.JDKInstaller_UnableToInstallUntilLicenseAccepted());
                return expectedLocation;
            }
            FilePath marker = expectedLocation.child(".installedByHudson");
            if (marker.exists() && marker.readToString().equals(id)) {
                return expectedLocation;
            }
            expectedLocation.deleteRecursive();
            expectedLocation.mkdirs();
            Platform p = Platform.of(node);
            URL url = locate(log, p, CPU.of(node));
            FilePath file = expectedLocation.child(p.bundleFileName);
            file.copyFrom(url);
            install(node.createLauncher(log), p, new FilePathFileSystem(node), log, expectedLocation.absolutize().getRemote(), file.getRemote());
            file.delete();
            marker.write(id, null);
        } catch (DetectionFailedException e) {
            out.println("JDK installation skipped: " + e.getMessage());
        }
        return expectedLocation;
    }

    public void install(Launcher launcher, Platform p, FileSystem fs, TaskListener log, String expectedLocation, String jdkBundle) throws IOException, InterruptedException {
        PrintStream out = log.getLogger();
        out.println("Installing " + jdkBundle);
        switch(p) {
            case LINUX:
            case SOLARIS:
                byte[] header = new byte[2];
                {
                    DataInputStream in = new DataInputStream(fs.read(jdkBundle));
                    try {
                    in.readFully(header);
                    } finally {
                        IOUtils.closeQuietly(in);
                    }
                }
                ProcStarter starter;
                if (header[0] == 0x1F && header[1] == (byte) 0x8B) {
                    starter = launcher.launch().cmds("tar", "xvzf", jdkBundle);
                } else {
                    fs.chmod(jdkBundle, 0755);
                    starter = launcher.launch().cmds(jdkBundle, "-noregister");
                }
                int exit = starter.stdin(new ByteArrayInputStream("yes".getBytes())).stdout(out).pwd(new FilePath(launcher.getChannel(), expectedLocation)).join();
                if (exit != 0)
                    throw new AbortException(Messages.JDKInstaller_FailedToInstallJDK(exit));
                List<String> paths = fs.listSubDirectories(expectedLocation);
                for (Iterator<String> itr = paths.iterator(); itr.hasNext(); ) {
                    String s = itr.next();
                    if (!s.matches("j(2s)?dk.*"))
                        itr.remove();
                }
                if (paths.size() != 1)
                    throw new AbortException("Failed to find the extracted JDKs: " + paths);
                fs.pullUp(expectedLocation + '/' + paths.get(0), expectedLocation);
                break;
            case WINDOWS:
                expectedLocation = expectedLocation.trim();
                if (expectedLocation.endsWith("\\")) {
                    expectedLocation = expectedLocation.substring(0, expectedLocation.length() - 1);
                }
                String logFile = new FilePath(launcher.getChannel(), expectedLocation).getParent().createTempFile("install", "log").getRemote();
                ArgumentListBuilder args = new ArgumentListBuilder();
                assert (new File(expectedLocation).exists()) : expectedLocation + " must exist, otherwise /L will cause the installer to fail with error 1622";
                if (isJava15() || isJava14()) {
                    args.add("CMD.EXE", "/C");
                    args.add(jdkBundle + " /s /v\"/qn REBOOT=ReallySuppress INSTALLDIR=\\\"" + expectedLocation + "\\\" /L \\\"" + logFile + "\\\"\"");
                } else {
                    args.add(jdkBundle, "/s");
                    args.add("ADDLOCAL=\"ToolsFeature\"", "REBOOT=ReallySuppress", "INSTALLDIR=" + expectedLocation, "/L", logFile);
                }
                int r = launcher.launch().cmds(args).stdout(out).pwd(new FilePath(launcher.getChannel(), expectedLocation)).join();
                if (r != 0) {
                    out.println(Messages.JDKInstaller_FailedToInstallJDK(r));
                    InputStreamReader in = new InputStreamReader(fs.read(logFile), "UTF-16");
                    try {
                        IOUtils.copy(in, new OutputStreamWriter(out));
                    } finally {
                        in.close();
                    }
                    throw new AbortException();
                }
                fs.delete(logFile);
                break;
        }
    }

    private boolean isJava15() {
        return id.contains("-1.5");
    }

    private boolean isJava14() {
        return id.contains("-1.4");
    }

    public interface FileSystem {

        void delete(String file) throws IOException, InterruptedException;

        void chmod(String file, int mode) throws IOException, InterruptedException;

        InputStream read(String file) throws IOException, InterruptedException;

        List<String> listSubDirectories(String dir) throws IOException, InterruptedException;

        void pullUp(String from, String to) throws IOException, InterruptedException;
    }

    static final class FilePathFileSystem implements FileSystem {

        private final Node node;

        FilePathFileSystem(Node node) {
            this.node = node;
        }

        public void delete(String file) throws IOException, InterruptedException {
            $(file).delete();
        }

        public void chmod(String file, int mode) throws IOException, InterruptedException {
            $(file).chmod(mode);
        }

        public InputStream read(String file) throws IOException, InterruptedException {
            return $(file).read();
        }

        public List<String> listSubDirectories(String dir) throws IOException, InterruptedException {
            List<String> r = new ArrayList<String>();
            for (FilePath f : $(dir).listDirectories()) r.add(f.getName());
            return r;
        }

        public void pullUp(String from, String to) throws IOException, InterruptedException {
            $(from).moveAllChildrenTo($(to));
        }

        private FilePath $(String file) {
            return node.createPath(file);
        }
    }

    private File getLocalCacheFile(Platform platform, CPU cpu) {
        return new File(Jenkins.getInstance().getRootDir(), "cache/jdks/" + platform + "/" + cpu + "/" + id);
    }

    public URL locate(TaskListener log, Platform platform, CPU cpu) throws IOException {
        File cache = getLocalCacheFile(platform, cpu);
        if (cache.exists() && cache.length() > 1 * 1024 * 1024)
            return cache.toURL();
        log.getLogger().println("Installing JDK " + id);
        JDKFamilyList families = JDKList.all().get(JDKList.class).toList();
        if (families.isEmpty())
            throw new IOException("JDK data is empty.");
        JDKRelease release = families.getRelease(id);
        if (release == null)
            throw new IOException("Unable to find JDK with ID=" + id);
        JDKFile primary = null, secondary = null;
        for (JDKFile f : release.files) {
            String vcap = f.name.toUpperCase(Locale.ENGLISH);
            if (!platform.is(vcap))
                continue;
            switch(cpu.accept(vcap)) {
                case PRIMARY:
                    primary = f;
                    break;
                case SECONDARY:
                    secondary = f;
                    break;
                case UNACCEPTABLE:
                    break;
            }
        }
        if (primary == null)
            primary = secondary;
        if (primary == null)
            throw new AbortException("Couldn't find the right download for " + platform + " and " + cpu + " combination");
        LOGGER.fine("Platform choice:" + primary);
        log.getLogger().println("Downloading JDK from " + primary.filepath);
        HttpClient hc = new HttpClient();
        hc.getParams().setParameter("http.useragent", "Mozilla/5.0 (Windows; U; MSIE 9.0; Windows NT 9.0; en-US)");
        Jenkins j = Jenkins.getInstance();
        ProxyConfiguration jpc = j != null ? j.proxy : null;
        if (jpc != null) {
            hc.getHostConfiguration().setProxy(jpc.name, jpc.port);
            if (jpc.getUserName() != null)
                hc.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(jpc.getUserName(), jpc.getPassword()));
        }
        int authCount = 0, totalPageCount = 0;
        HttpMethodBase m = new GetMethod(primary.filepath);
        hc.getState().addCookie(new Cookie(".oracle.com", "gpw_e24", ".", "/", -1, false));
        hc.getState().addCookie(new Cookie(".oracle.com", "oraclelicense", "accept-securebackup-cookie", "/", -1, false));
        try {
            while (true) {
                if (totalPageCount++ > 16)
                    throw new IOException("Unable to find the login form");
                LOGGER.fine("Requesting " + m.getURI());
                int r = hc.executeMethod(m);
                if (r / 100 == 3) {
                    String loc = m.getResponseHeader("Location").getValue();
                    m.releaseConnection();
                    m = new GetMethod(loc);
                    continue;
                }
                if (r != 200)
                    throw new IOException("Failed to request " + m.getURI() + " exit code=" + r);
                if (m.getURI().getHost().equals("login.oracle.com")) {
                    LOGGER.fine("Appears to be a login page");
                    String resp = IOUtils.toString(m.getResponseBodyAsStream(), m.getResponseCharSet());
                    m.releaseConnection();
                    Matcher pm = Pattern.compile("<form .*?action=\"([^\"]*)\" .*?</form>", Pattern.DOTALL).matcher(resp);
                    if (!pm.find())
                        throw new IllegalStateException("Unable to find a form in the response:\n" + resp);
                    String form = pm.group();
                    PostMethod post = new PostMethod(new URL(new URL(m.getURI().getURI()), pm.group(1)).toExternalForm());
                    String u = getDescriptor().getUsername();
                    Secret p = getDescriptor().getPassword();
                    if (u == null || p == null) {
                        log.hyperlink(getCredentialPageUrl(), "Oracle now requires Oracle account to download previous versions of JDK. Please specify your Oracle account username/password.\n");
                        throw new AbortException("Unable to install JDK unless a valid Oracle account username/password is provided in the system configuration.");
                    }
                    for (String fragment : form.split("<input")) {
                        String n = extractAttribute(fragment, "name");
                        String v = extractAttribute(fragment, "value");
                        if (n == null || v == null)
                            continue;
                        if (n.equals("ssousername"))
                            v = u;
                        if (n.equals("password")) {
                            v = p.getPlainText();
                            if (authCount++ > 3) {
                                log.hyperlink(getCredentialPageUrl(), "Your Oracle account doesn't appear valid. Please specify a valid username/password\n");
                                throw new AbortException("Unable to install JDK unless a valid username/password is provided.");
                            }
                        }
                        post.addParameter(n, v);
                    }
                    m = post;
                } else {
                    log.getLogger().println("Downloading " + m.getResponseContentLength() + "bytes");
                    File tmp = new File(cache.getPath() + ".tmp");
                    try {
                        tmp.getParentFile().mkdirs();
                        FileOutputStream out = new FileOutputStream(tmp);
                        try {
                            IOUtils.copy(m.getResponseBodyAsStream(), out);
                        } finally {
                            out.close();
                        }
                        tmp.renameTo(cache);
                        return cache.toURL();
                    } finally {
                        tmp.delete();
                    }
                }
            }
        } finally {
            m.releaseConnection();
        }
    }

    private static String extractAttribute(String s, String name) {
        String h = name + "=\"";
        int si = s.indexOf(h);
        if (si < 0)
            return null;
        int ei = s.indexOf('\"', si + h.length());
        return s.substring(si + h.length(), ei);
    }

    private String getCredentialPageUrl() {
        return "/" + getDescriptor().getDescriptorUrl() + "/enterCredential";
    }

    public enum Preference {

        PRIMARY, SECONDARY, UNACCEPTABLE
    }

    public enum Platform {

        LINUX("jdk.sh"), SOLARIS("jdk.sh"), WINDOWS("jdk.exe");

        public final String bundleFileName;

        Platform(String bundleFileName) {
            this.bundleFileName = bundleFileName;
        }

        public boolean is(String line) {
            return line.contains(name());
        }

        public static Platform of(Node n) throws IOException, InterruptedException, DetectionFailedException {
            return n.getChannel().call(new GetCurrentPlatform());
        }

        public static Platform current() throws DetectionFailedException {
            String arch = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
            if (arch.contains("linux"))
                return LINUX;
            if (arch.contains("windows"))
                return WINDOWS;
            if (arch.contains("sun") || arch.contains("solaris"))
                return SOLARIS;
            throw new DetectionFailedException("Unknown CPU name: " + arch);
        }

        static class GetCurrentPlatform extends MasterToSlaveCallable<Platform, DetectionFailedException> {

            private static final long serialVersionUID = 1L;

            public Platform call() throws DetectionFailedException {
                return current();
            }
        }
    }

    public enum CPU {

        i386, amd64, Sparc, Itanium;

        public Preference accept(String line) {
            switch(this) {
                case Sparc:
                    return must(line.contains("SPARC"));
                case Itanium:
                    return must(line.contains("IA64"));
                case amd64:
                    if (line.contains("SPARC") || line.contains("IA64"))
                        return UNACCEPTABLE;
                    if (line.contains("64"))
                        return PRIMARY;
                    return SECONDARY;
                case i386:
                    if (line.contains("64") || line.contains("SPARC") || line.contains("IA64"))
                        return UNACCEPTABLE;
                    return PRIMARY;
            }
            return UNACCEPTABLE;
        }

        private static Preference must(boolean b) {
            return b ? PRIMARY : UNACCEPTABLE;
        }

        public static CPU of(Node n) throws IOException, InterruptedException, DetectionFailedException {
            return n.getChannel().call(new GetCurrentCPU());
        }

        public static CPU current() throws DetectionFailedException {
            String arch = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);
            if (arch.contains("sparc"))
                return Sparc;
            if (arch.contains("ia64"))
                return Itanium;
            if (arch.contains("amd64") || arch.contains("86_64"))
                return amd64;
            if (arch.contains("86"))
                return i386;
            throw new DetectionFailedException("Unknown CPU architecture: " + arch);
        }

        static class GetCurrentCPU extends MasterToSlaveCallable<CPU, DetectionFailedException> {

            private static final long serialVersionUID = 1L;

            public CPU call() throws DetectionFailedException {
                return current();
            }
        }
    }

    private static final class DetectionFailedException extends Exception {

        private DetectionFailedException(String message) {
            super(message);
        }
    }

    public static final class JDKFamilyList {

        public int version;

        public JDKFamily[] data = new JDKFamily[0];

        public boolean isEmpty() {
            for (JDKFamily f : data) {
                if (f.releases.length > 0)
                    return false;
            }
            return true;
        }

        public JDKRelease getRelease(String productCode) {
            for (JDKFamily f : data) {
                for (JDKRelease r : f.releases) {
                    if (r.matchesId(productCode))
                        return r;
                }
            }
            return null;
        }
    }

    public static final class JDKFamily {

        public String name;

        public JDKRelease[] releases;
    }

    public static final class JDKRelease {

        public String name;

        public String title;

        public JDKFile[] files;

        public boolean matchesId(String rhs) {
            return rhs != null && (rhs.equals(name) || rhs.startsWith(name + "@"));
        }
    }

    public static final class JDKFile {

        public String name;

        public String title;

        public String filepath;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends ToolInstallerDescriptor<JDKInstaller> {

        private String username;

        private Secret password;

        public DescriptorImpl() {
            load();
        }

        public String getDisplayName() {
            return Messages.JDKInstaller_DescriptorImpl_displayName();
        }

        @Override
        public boolean isApplicable(Class<? extends ToolInstallation> toolType) {
            return toolType == JDK.class;
        }

        public String getUsername() {
            return username;
        }

        public Secret getPassword() {
            return password;
        }

        public FormValidation doCheckId(@QueryParameter String value) {
            if (Util.fixEmpty(value) == null)
                return FormValidation.error(Messages.JDKInstaller_DescriptorImpl_doCheckId());
            return FormValidation.ok();
        }

        public List<JDKFamily> getInstallableJDKs() throws IOException {
            return Arrays.asList(JDKList.all().get(JDKList.class).toList().data);
        }

        public FormValidation doCheckAcceptLicense(@QueryParameter boolean value) {
            if (username == null || password == null)
                return FormValidation.errorWithMarkup(Messages.JDKInstaller_RequireOracleAccount(Stapler.getCurrentRequest().getContextPath() + '/' + getDescriptorUrl() + "/enterCredential"));
            if (value) {
                return FormValidation.ok();
            } else {
                return FormValidation.error(Messages.JDKInstaller_DescriptorImpl_doCheckAcceptLicense());
            }
        }

        public HttpResponse doPostCredential(@QueryParameter String username, @QueryParameter String password) throws IOException, ServletException {
            this.username = username;
            this.password = Secret.fromString(password);
            save();
            return HttpResponses.redirectTo("credentialOK");
        }
    }

    @Extension
    public static final class JDKList extends Downloadable {

        public JDKList() {
            super(JDKInstaller.class);
        }

        public JDKFamilyList toList() throws IOException {
            JSONObject d = getData();
            if (d == null)
                return new JDKFamilyList();
            return (JDKFamilyList) JSONObject.toBean(d, JDKFamilyList.class);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(JDKInstaller.class.getName());
}