/*
 * The MIT License
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc.
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

/**
 * Base class for Hudson CLI.
 *
 * <h2>How does a CLI command work</h2>
 * <p>
 * The users starts {@linkplain CLI the "CLI agent"} on a remote system, by specifying arguments, like
 * {@code "java -jar jenkins-cli.jar command arg1 arg2 arg3"}. The CLI agent creates
 * a connection to the server, and it sends the entire arguments to the server, along with
 * the remoted stdin/out/err.
 *
 * <p>
 * The Hudson master then picks the right {@link CLICommand} to execute, clone it, and
 * calls {@link #main(List, Locale, InputStream, PrintStream, PrintStream)} method.
 *
 * <h2>Note for CLI command implementor</h2>
 * Start with <a href="http://wiki.jenkins-ci.org/display/JENKINS/Writing+CLI+commands">this document</a>
 * to get the general idea of CLI.
 *
 * <ul>
 * <li>
 * Put {@link Extension} on your implementation to have it discovered by Hudson.
 *
 * <li>
 * Use <a href="https://github.com/kohsuke/args4j">args4j</a> annotation on your implementation to define
 * options and arguments (however, if you don't like that, you could override
 * the {@link #main(List, Locale, InputStream, PrintStream, PrintStream)} method directly.
 *
 * <li>
 * stdin, stdout, stderr are remoted, so proper buffering is necessary for good user experience.
 *
 * </ul>
 *
 * @author Kohsuke Kawaguchi
 * @since 1.302
 * @see CLIMethod
 */
@LegacyInstancesAreScopedToHudson
public abstract class CLICommand implements ExtensionPoint, Cloneable {
    /**
     * Connected to stdout and stderr of the CLI agent that initiated the session.
     * IOW, if you write to these streams, the person who launched the CLI command
     * will see the messages in his terminal.
     *
     * <p>
     * (In contrast, calling {@code System.out.println(...)} would print out
     * the message to the server log file, which is probably not what you want.
     */
    public transient PrintStream stdout,stderr;

    /**
     * Shared text, which is reported back to CLI if an error happens in commands 
     * taking lists of parameters.
     * @since 2.26
     */
    static final String CLI_LISTPARAM_SUMMARY_ERROR_TEXT = "Error occurred while performing this command, see previous stderr output.";
    
    /**
     * Connected to stdin of the CLI agent.
     *
     * <p>
     * This input stream is buffered to hide the latency in the remoting.
     */
    public transient InputStream stdin;

    /**
     * @deprecated No longer used.
     */
    @Deprecated
    public transient Channel channel;

    /**
     * The locale of the client. Messages should be formatted with this resource.
     */
    public transient Locale locale;

    /**
     * The encoding of the client, if defined.
     */
    private transient @CheckForNull Charset encoding;

    /**
     * Set by the caller of the CLI system if the transport already provides
     * authentication.
     */
    private transient Authentication transportAuth;

    /**
     * Gets the command name.
     *
     * <p>
     * For example, if the CLI is invoked as {@code java -jar cli.jar foo arg1 arg2 arg4},
     * on the server side {@link CLICommand} that returns "foo" from {@link #getName()}
     * will be invoked.
     *
     * <p>
     * By default, this method creates "foo-bar-zot" from "FooBarZotCommand".
     */
    public String getName() {
        String name = getClass().getName();
        name = name.substring(name.lastIndexOf('.') + 1); // short name
        name = name.substring(name.lastIndexOf('$')+1);
        if(name.endsWith("Command"))
            name = name.substring(0,name.length()-7); // trim off the command

        // convert "FooBarZot" into "foo-bar-zot"
        // Locale is fixed so that "CreateInstance" always become "create-instance" no matter where this is run.
        return name.replaceAll("([a-z0-9])([A-Z])","$1-$2").toLowerCase(Locale.ENGLISH);
    }

    /**
     * Gets the quick summary of what this command does.
     * Used by the help command to generate the list of commands.
     */
    public abstract String getShortDescription();

    /**
     * Entry point to the CLI command.
     * 
     * <p>
     * The default implementation uses args4j to parse command line arguments and call {@link #run()},
     * but if that processing is undesirable, subtypes can directly override this method and leave {@link #run()}
     * to an empty method.
     * You would however then have to consider {@link #getTransportAuthentication},
     * so this is not really recommended.
     *
     * @param args
     *      Arguments to the sub command. For example, if the CLI is invoked like "java -jar cli.jar foo bar zot",
     *      then "foo" is the sub-command and the argument list is ["bar","zot"].
     * @param locale
     *      Locale of the client (which can be different from that of the server.) Good behaving command implementation
     *      would use this locale for formatting messages.
     * @param stdin
     *      Connected to the stdin of the CLI client.
     * @param stdout
     *      Connected to the stdout of the CLI client.
     * @param stderr
     *      Connected to the stderr of the CLI client.
     * @return
     *      Exit code from the CLI command execution
     *
     *      <p>
     *      Jenkins standard exit codes from CLI:
     *      0 means everything went well.
     *      1 means further unspecified exception is thrown while performing the command.
     *      2 means CmdLineException is thrown while performing the command.
     *      3 means IllegalArgumentException is thrown while performing the command.
     *      4 mean IllegalStateException is thrown while performing the command.
     *      5 means AbortException is thrown while performing the command.
     *      6 means AccessDeniedException is thrown while performing the command.
     *      7 means BadCredentialsException is thrown while performing the command.
     *      8-15 are reserved for future usage
     *      16+ mean a custom CLI exit error code (meaning defined by the CLI command itself)
     *
     *      <p>
     *      Note: For details - see JENKINS-32273
     */
    public int main(List<String> args, Locale locale, InputStream stdin, PrintStream stdout, PrintStream stderr) {
        this.stdin = new BufferedInputStream(stdin);
        this.stdout = stdout;
        this.stderr = stderr;
        this.locale = locale;
        registerOptionHandlers();
        CmdLineParser p = getCmdLineParser();

        // add options from the authenticator
        SecurityContext sc = null;
        Authentication old = null;
        Authentication auth = null;
        try {
            sc = SecurityContextHolder.getContext();
            old = sc.getAuthentication();

            sc.setAuthentication(auth = getTransportAuthentication());

            if (!(this instanceof HelpCommand || this instanceof WhoAmICommand))
                Jenkins.get().checkPermission(Jenkins.READ);
            p.parseArgument(args.toArray(new String[0]));
            LOGGER.log(Level.FINE, "Invoking CLI command {0}, with {1} arguments, as user {2}.",
                    new Object[] {getName(), args.size(), auth.getName()});
            int res = run();
            LOGGER.log(Level.FINE, "Executed CLI command {0}, with {1} arguments, as user {2}, return code {3}",
                    new Object[] {getName(), args.size(), auth.getName(), res});
            return res;
        } catch (CmdLineException e) {
            LOGGER.log(Level.FINE, String.format("Failed call to CLI command %s, with %d arguments, as user %s.",
                    getName(), args.size(), auth != null ? auth.getName() : "<unknown>"), e);
            stderr.println();
            stderr.println("ERROR: " + e.getMessage());
            printUsage(stderr, p);
            return 2;
        } catch (IllegalStateException e) {
            LOGGER.log(Level.FINE, String.format("Failed call to CLI command %s, with %d arguments, as user %s.",
                    getName(), args.size(), auth != null ? auth.getName() : "<unknown>"), e);
            stderr.println();
            stderr.println("ERROR: " + e.getMessage());
            return 4;
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.FINE, String.format("Failed call to CLI command %s, with %d arguments, as user %s.",
                    getName(), args.size(), auth != null ? auth.getName() : "<unknown>"), e);
            stderr.println();
            stderr.println("ERROR: " + e.getMessage());
            return 3;
        } catch (AbortException e) {
            LOGGER.log(Level.FINE, String.format("Failed call to CLI command %s, with %d arguments, as user %s.",
                    getName(), args.size(), auth != null ? auth.getName() : "<unknown>"), e);
            // signals an error without stack trace
            stderr.println();
            stderr.println("ERROR: " + e.getMessage());
            return 5;
        } catch (AccessDeniedException e) {
            LOGGER.log(Level.FINE, String.format("Failed call to CLI command %s, with %d arguments, as user %s.",
                    getName(), args.size(), auth != null ? auth.getName() : "<unknown>"), e);
            stderr.println();
            stderr.println("ERROR: " + e.getMessage());
            return 6;
        } catch (BadCredentialsException e) {
            // to the caller, we can't reveal whether the user didn't exist or the password didn't match.
            // do that to the server log instead
            String id = UUID.randomUUID().toString();
            LOGGER.log(Level.INFO, "CLI login attempt failed: " + id, e);
            stderr.println();
            stderr.println("ERROR: Bad Credentials. Search the server log for " + id + " for more details.");
            return 7;
        } catch (Throwable e) {
            final String errorMsg = String.format("Unexpected exception occurred while performing %s command.",
                    getName());
            stderr.println();
            stderr.println("ERROR: " + errorMsg);
            LOGGER.log(Level.WARNING, errorMsg, e);
            Functions.printStackTrace(e, stderr);
            return 1;
        } finally {
            if(sc != null)
                sc.setAuthentication(old); // restore
        }
    }

    /**
     * Get parser for this command.
     *
     * Exposed to be overridden by {@link hudson.cli.declarative.CLIRegisterer}.
     * @since 1.538
     */
    protected CmdLineParser getCmdLineParser() {
        return new CmdLineParser(this);
    }

    /**
     * @deprecated Specific to Remoting-based protocol.
     */
    @Deprecated
    public Channel checkChannel() throws AbortException {
        throw new AbortException("This command is requesting the -remoting mode which is no longer supported. See https://jenkins.io/redirect/cli-command-requires-channel");
    }

    /**
     * Determines if the user authentication is attempted through CLI before running this command.
     *
     * <p>
     * If your command doesn't require any authentication whatsoever, and if you don't even want to let the user
     * authenticate, then override this method to always return false &mdash; doing so will result in all the commands
     * running as anonymous user credential.
     *
     * <p>
     * Note that even if this method returns true, the user can still skip aut 
     *
     * @param auth
     *      Always non-null.
     *      If the underlying transport had already performed authentication, this object is something other than
     *      {@link jenkins.model.Jenkins#ANONYMOUS}.
     * @deprecated Unused.
     */
    @Deprecated
    protected boolean shouldPerformAuthentication(Authentication auth) {
        return auth== Jenkins.ANONYMOUS;
    }

    /**
     * Returns the identity of the client as determined at the CLI transport level.
     *
     * <p>
     * When the CLI connection to the server is tunneled over HTTP, that HTTP connection
     * can authenticate the client, just like any other HTTP connections to the server
     * can authenticate the client. This method returns that information, if one is available.
     * By generalizing it, this method returns the identity obtained at the transport-level authentication.
     *
     * <p>
     * For example, imagine if the current {@link SecurityRealm} is doing Kerberos authentication,
     * then this method can return a valid identity of the client.
     *
     * <p>
     * If the transport doesn't do authentication, this method returns {@link jenkins.model.Jenkins#ANONYMOUS}.
     */
    public Authentication getTransportAuthentication() {
        Authentication a = transportAuth; 
        if (a==null)    a = Jenkins.ANONYMOUS;
        return a;
    }

    public void setTransportAuth(Authentication transportAuth) {
        this.transportAuth = transportAuth;
    }

    /**
     * Executes the command, and return the exit code.
     * 
     * <p>
     * This is an internal contract between {@link CLICommand} and its subtype.
     * To execute CLI method from outside, use {@link #main(List, Locale, InputStream, PrintStream, PrintStream)}
     *
     * @return
     *      0 to indicate a success, otherwise a custom error code.
     *      Error codes 1-15 shouldn;t be used in {@link #run()} as a custom error code.
     * @throws Exception
     *      If a further unspecified exception is thrown; means: Unknown and/or unexpected issue occurred
     * @throws CmdLineException
     *      If a wrong parameter specified, input value can't be decoded etc.
     * @throws IllegalArgumentException
     *      If the execution can't continue due to wrong input parameter (job doesn't exist etc.)
     * @throws IllegalStateException
     *      If the execution can't continue due to an incorrect state of Jenkins, job, build etc.
     * @throws AbortException
     *      If the execution can't continue due to an other (rare, but foreseeable) issue
     * @throws AccessDeniedException
     *      If the caller doesn't have sufficient rights for requested action
     * @throws BadCredentialsException
     *      If bad credentials were provided to CLI
     */
    protected abstract int run() throws Exception;

    protected void printUsage(PrintStream stderr, CmdLineParser p) {
        stderr.print("java -jar jenkins-cli.jar " + getName());
        p.printSingleLineUsage(stderr);
        stderr.println();
        printUsageSummary(stderr);
        p.printUsage(stderr);
    }

    /**
     * Get single line summary as a string.
     */
    @Restricted(NoExternalUse.class)
    public final String getSingleLineSummary() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        getCmdLineParser().printSingleLineUsage(out);
        return out.toString();
    }

    /**
     * Get usage as a string.
     */
    @Restricted(NoExternalUse.class)
    public final String getUsage() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        getCmdLineParser().printUsage(out);
        return out.toString();
    }

    /**
     * Get long description as a string.
     */
    @Restricted(NoExternalUse.class)
    public final String getLongDescription() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);

        printUsageSummary(ps);
        ps.close();
        return out.toString();
    }

    /**
     * Called while producing usage. This is a good method to override
     * to render the general description of the command that goes beyond
     * a single-line summary. 
     */
    protected void printUsageSummary(PrintStream stderr) {
        stderr.println(getShortDescription());
    }

    /**
     * Convenience method for subtypes to obtain the system property of the client.
     * @deprecated Specific to Remoting-based protocol.
     */
    @Deprecated
    protected String getClientSystemProperty(String name) throws IOException, InterruptedException {
        checkChannel();
        return null; // never run
    }

    /**
     * Define the encoding for the command.
     * @since 2.54
     */
    public void setClientCharset(@Nonnull Charset encoding) {
        this.encoding = encoding;
    }

    protected @Nonnull Charset getClientCharset() throws IOException, InterruptedException {
        if (encoding != null) {
            return encoding;
        }
        // for SSH, assume the platform default encoding
        // this is in-line with the standard SSH behavior
        return Charset.defaultCharset();
    }

    /**
     * Convenience method for subtypes to obtain environment variables of the client.
     * @deprecated Specific to Remoting-based protocol.
     */
    @Deprecated
    protected String getClientEnvironmentVariable(String name) throws IOException, InterruptedException {
        checkChannel();
        return null; // never run
    }

    /**
     * Creates a clone to be used to execute a command.
     */
    protected CLICommand createClone() {
        try {
            return getClass().newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Auto-discovers {@link OptionHandler}s and add them to the given command line parser.
     */
    protected void registerOptionHandlers() {
        try {
            for (Class c : Index.list(OptionHandlerExtension.class, Jenkins.get().pluginManager.uberClassLoader,Class.class)) {
                Type t = Types.getBaseClass(c, OptionHandler.class);
                CmdLineParser.registerHandler(Types.erasure(Types.getTypeArgument(t,0)), c);
            }
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    /**
     * Returns all the registered {@link CLICommand}s.
     */
    public static ExtensionList<CLICommand> all() {
        return ExtensionList.lookup(CLICommand.class);
    }

    /**
     * Obtains a copy of the command for invocation.
     */
    public static CLICommand clone(String name) {
        for (CLICommand cmd : all())
            if(name.equals(cmd.getName()))
                return cmd.createClone();
        return null;
    }

    private static final Logger LOGGER = Logger.getLogger(CLICommand.class.getName());

    private static final ThreadLocal<CLICommand> CURRENT_COMMAND = new ThreadLocal<>();

    /*package*/ static CLICommand setCurrent(CLICommand cmd) {
        CLICommand old = getCurrent();
        CURRENT_COMMAND.set(cmd);
        return old;
    }

    /**
     * If the calling thread is in the middle of executing a CLI command, return it. Otherwise null.
     */
    public static CLICommand getCurrent() {
        return CURRENT_COMMAND.get();
    }

    static {
        // register option handlers that are defined
        ClassLoaders cls = new ClassLoaders();
        Jenkins j = Jenkins.getInstanceOrNull();
        if (j!=null) {// only when running on the master
            cls.put(j.getPluginManager().uberClassLoader);

            ResourceNameIterator servicesIter =
                new DiscoverServiceNames(cls).findResourceNames(OptionHandler.class.getName());
            final ResourceClassIterator itr =
                new DiscoverClasses(cls).findResourceClasses(servicesIter);

            while(itr.hasNext()) {
                Class h = itr.nextResourceClass().loadClass();
                Class c = Types.erasure(Types.getTypeArgument(Types.getBaseClass(h, OptionHandler.class), 0));
                CmdLineParser.registerHandler(c,h);
            }
        }
    }

}
