package hudson.cli;

import hudson.AbortException;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.cli.declarative.CLIMethod;
import hudson.ExtensionPoint.LegacyInstancesAreScopedToHudson;
import hudson.Functions;
import hudson.cli.declarative.OptionHandlerExtension;
import jenkins.model.Jenkins;
import hudson.remoting.Channel;
import hudson.security.SecurityRealm;
import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.Authentication;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.discovery.ResourceClassIterator;
import org.apache.commons.discovery.ResourceNameIterator;
import org.apache.commons.discovery.resource.ClassLoaders;
import org.apache.commons.discovery.resource.classes.DiscoverClasses;
import org.apache.commons.discovery.resource.names.DiscoverServiceNames;
import org.jvnet.hudson.annotation_indexer.Index;
import org.jvnet.tiger_types.Types;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.spi.OptionHandler;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

@LegacyInstancesAreScopedToHudson
public abstract class CLICommand implements ExtensionPoint, Cloneable {

    public transient PrintStream stdout, stderr;

    static final String CLI_LISTPARAM_SUMMARY_ERROR_TEXT = "Error occurred while performing this command, see previous stderr output.";

    public transient InputStream stdin;

    @Deprecated
    public transient Channel channel;

    public transient Locale locale;

    @CheckForNull
    private transient Charset encoding;

    private transient Authentication transportAuth;

    public String getName() {
        String name = getClass().getName();
        name = name.substring(name.lastIndexOf('.') + 1);
        name = name.substring(name.lastIndexOf('$') + 1);
        if (name.endsWith("Command"))
            name = name.substring(0, name.length() - 7);
        return name.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase(Locale.ENGLISH);
    }

    public abstract String getShortDescription();

    public int main(List<String> args, Locale locale, InputStream stdin, PrintStream stdout, PrintStream stderr) {
        this.stdin = new BufferedInputStream(stdin);
        this.stdout = stdout;
        this.stderr = stderr;
        this.locale = locale;
        registerOptionHandlers();
        CmdLineParser p = getCmdLineParser();
        SecurityContext sc = null;
        Authentication old = null;
        Authentication auth = null;
        try {
            sc = SecurityContextHolder.getContext();
            old = sc.getAuthentication();
            sc.setAuthentication(auth = getTransportAuthentication());
            if (!(this instanceof HelpCommand || this instanceof WhoAmICommand))
                Jenkins.getActiveInstance().checkPermission(Jenkins.READ);
            p.parseArgument(args.toArray(new String[args.size()]));
            if (!(this instanceof HelpCommand || this instanceof WhoAmICommand))
                Jenkins.getActiveInstance().checkPermission(Jenkins.READ);
            LOGGER.log(Level.FINE, "Invoking CLI command {0}, with {1} arguments, as user {2}.", new Object[] { getName(), args.size(), auth.getName() });
            int res = run();
            LOGGER.log(Level.FINE, "Executed CLI command {0}, with {1} arguments, as user {2}, return code {3}", new Object[] { getName(), args.size(), auth.getName(), res });
            return res;
        } catch (CmdLineException e) {
            LOGGER.log(Level.FINE, String.format("Failed call to CLI command %s, with %d arguments, as user %s.", getName(), args.size(), auth != null ? auth.getName() : "<unknown>"), e);
            stderr.println("");
            stderr.println("ERROR: " + e.getMessage());
            printUsage(stderr, p);
            return 2;
        } catch (IllegalStateException e) {
            LOGGER.log(Level.FINE, String.format("Failed call to CLI command %s, with %d arguments, as user %s.", getName(), args.size(), auth != null ? auth.getName() : "<unknown>"), e);
            stderr.println("");
            stderr.println("ERROR: " + e.getMessage());
            return 4;
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.FINE, String.format("Failed call to CLI command %s, with %d arguments, as user %s.", getName(), args.size(), auth != null ? auth.getName() : "<unknown>"), e);
            stderr.println("");
            stderr.println("ERROR: " + e.getMessage());
            return 3;
        } catch (AbortException e) {
            LOGGER.log(Level.FINE, String.format("Failed call to CLI command %s, with %d arguments, as user %s.", getName(), args.size(), auth != null ? auth.getName() : "<unknown>"), e);
            stderr.println("");
            stderr.println("ERROR: " + e.getMessage());
            return 5;
        } catch (AccessDeniedException e) {
            LOGGER.log(Level.FINE, String.format("Failed call to CLI command %s, with %d arguments, as user %s.", getName(), args.size(), auth != null ? auth.getName() : "<unknown>"), e);
            stderr.println("");
            stderr.println("ERROR: " + e.getMessage());
            return 6;
        } catch (BadCredentialsException e) {
            String id = UUID.randomUUID().toString();
            LOGGER.log(Level.INFO, "CLI login attempt failed: " + id, e);
            stderr.println("");
            stderr.println("ERROR: Bad Credentials. Search the server log for " + id + " for more details.");
            return 7;
        } catch (Throwable e) {
            final String errorMsg = String.format("Unexpected exception occurred while performing %s command.", getName());
            stderr.println("");
            stderr.println("ERROR: " + errorMsg);
            LOGGER.log(Level.WARNING, errorMsg, e);
            Functions.printStackTrace(e, stderr);
            return 1;
        } finally {
            if (sc != null)
                sc.setAuthentication(old);
        }
    }

    protected CmdLineParser getCmdLineParser() {
        return new CmdLineParser(this);
    }

    @Deprecated
    public Channel checkChannel() throws AbortException {
        throw new AbortException("This command is requesting the -remoting mode which is no longer supported. See https://jenkins.io/redirect/cli-command-requires-channel");
    }

    @Deprecated
    protected boolean shouldPerformAuthentication(Authentication auth) {
        return auth == Jenkins.ANONYMOUS;
    }

    public Authentication getTransportAuthentication() {
        Authentication a = transportAuth;
        if (a == null)
            a = Jenkins.ANONYMOUS;
        return a;
    }

    public void setTransportAuth(Authentication transportAuth) {
        this.transportAuth = transportAuth;
    }

    protected abstract int run() throws Exception;

    protected void printUsage(PrintStream stderr, CmdLineParser p) {
        stderr.print("java -jar jenkins-cli.jar " + getName());
        p.printSingleLineUsage(stderr);
        stderr.println();
        printUsageSummary(stderr);
        p.printUsage(stderr);
    }

    @Restricted(NoExternalUse.class)
    public final String getSingleLineSummary() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        getCmdLineParser().printSingleLineUsage(out);
        return out.toString();
    }

    @Restricted(NoExternalUse.class)
    public final String getUsage() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        getCmdLineParser().printUsage(out);
        return out.toString();
    }

    @Restricted(NoExternalUse.class)
    public final String getLongDescription() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);
        printUsageSummary(ps);
        ps.close();
        return out.toString();
    }

    protected void printUsageSummary(PrintStream stderr) {
        stderr.println(getShortDescription());
    }

    @Deprecated
    protected String getClientSystemProperty(String name) throws IOException, InterruptedException {
        checkChannel();
        return null;
    }

    public void setClientCharset(@Nonnull Charset encoding) {
        this.encoding = encoding;
    }

    @Nonnull
    protected Charset getClientCharset() throws IOException, InterruptedException {
        if (encoding != null) {
            return encoding;
        }
        return Charset.defaultCharset();
    }

    @Deprecated
    protected String getClientEnvironmentVariable(String name) throws IOException, InterruptedException {
        checkChannel();
        return null;
    }

    protected CLICommand createClone() {
        try {
            return getClass().newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new AssertionError(e);
        }
    }

    protected void registerOptionHandlers() {
        try {
            for (Class c : Index.list(OptionHandlerExtension.class, Jenkins.getActiveInstance().pluginManager.uberClassLoader, Class.class)) {
                Type t = Types.getBaseClass(c, OptionHandler.class);
                CmdLineParser.registerHandler(Types.erasure(Types.getTypeArgument(t, 0)), c);
            }
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    public static ExtensionList<CLICommand> all() {
        return ExtensionList.lookup(CLICommand.class);
    }

    public static CLICommand clone(String name) {
        for (CLICommand cmd : all()) if (name.equals(cmd.getName()))
            return cmd.createClone();
        return null;
    }

    private static final Logger LOGGER = Logger.getLogger(CLICommand.class.getName());

<<<<<<< MINE
    private static final ThreadLocal<CLICommand> CURRENT_COMMAND = new ThreadLocal<CLICommand>();
=======
    public static final ChannelProperty<Authentication> TRANSPORT_AUTHENTICATION = new ChannelProperty<>(Authentication.class, "transportAuthentication");

    private static final ThreadLocal<CLICommand> CURRENT_COMMAND = new ThreadLocal<>();
>>>>>>> YOURS

    static CLICommand setCurrent(CLICommand cmd) {
        CLICommand old = getCurrent();
        CURRENT_COMMAND.set(cmd);
        return old;
    }

    public static CLICommand getCurrent() {
        return CURRENT_COMMAND.get();
    }

    static {
        ClassLoaders cls = new ClassLoaders();
        Jenkins j = Jenkins.getInstanceOrNull();
        if (j != null) {
            cls.put(j.getPluginManager().uberClassLoader);
            ResourceNameIterator servicesIter = new DiscoverServiceNames(cls).findResourceNames(OptionHandler.class.getName());
            final ResourceClassIterator itr = new DiscoverClasses(cls).findResourceClasses(servicesIter);
            while (itr.hasNext()) {
                Class h = itr.nextResourceClass().loadClass();
                Class c = Types.erasure(Types.getTypeArgument(Types.getBaseClass(h, OptionHandler.class), 0));
                CmdLineParser.registerHandler(c, h);
            }
        }
    }
}
