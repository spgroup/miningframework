package org.graalvm.component.installer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import java.util.stream.Collectors;
import static org.graalvm.component.installer.CommonConstants.PATH_COMPONENT_STORAGE;
import org.graalvm.component.installer.commands.AvailableCommand;
import org.graalvm.component.installer.commands.InfoCommand;
import org.graalvm.component.installer.commands.InstallCommand;
import org.graalvm.component.installer.commands.ListInstalledCommand;
import org.graalvm.component.installer.commands.PostInstCommand;
import org.graalvm.component.installer.commands.PreRemoveCommand;
import org.graalvm.component.installer.commands.RebuildImageCommand;
import org.graalvm.component.installer.commands.UninstallCommand;
import org.graalvm.component.installer.commands.UpgradeCommand;
import org.graalvm.component.installer.model.ComponentRegistry;
import org.graalvm.component.installer.os.WindowsJVMWrapper;
import org.graalvm.component.installer.persist.DirectoryStorage;
import org.graalvm.component.installer.remote.CatalogIterable;
import org.graalvm.component.installer.remote.GraalEditionList;
import org.graalvm.launcher.Launcher;
import org.graalvm.options.OptionCategory;
import org.graalvm.options.OptionDescriptor;

public class ComponentInstaller extends Launcher {

    private static final Logger LOG = Logger.getLogger(ComponentInstaller.class.getName());

    public static final String GRAAL_DEFAULT_RELATIVE_PATH = "../..";

    private static final Environment SIMPLE_ENV = new Environment("help", Collections.emptyList(), Collections.emptyMap()).enableStacktraces();

    private String command;

    private InstallerCommand cmdHandler;

    private LinkedList<String> cmdlineParams;

    private List<String> parameters = Collections.emptyList();

    private Path graalHomePath;

    private Path storagePath;

    private SimpleGetopt options;

    static final Map<String, InstallerCommand> commands = new HashMap<>();

    public static final Map<String, String> globalOptions = new HashMap<>();

    public static final Map<String, String> componentOptions = new HashMap<>();

    @SuppressWarnings("deprecation")
    static void initCommands() {
        commands.clear();
        globalOptions.clear();
        componentOptions.put(Commands.OPTION_CATALOG, "");
        componentOptions.put(Commands.OPTION_FILES, "");
        componentOptions.put(Commands.OPTION_URLS, "");
        componentOptions.put(Commands.OPTION_FOREIGN_CATALOG, "s");
        componentOptions.put(Commands.OPTION_FILES_OLD, "=L");
        componentOptions.put(Commands.LONG_OPTION_FILES, Commands.OPTION_FILES);
        componentOptions.put(Commands.LONG_OPTION_CATALOG, Commands.OPTION_CATALOG);
        componentOptions.put(Commands.LONG_OPTION_URLS, Commands.OPTION_URLS);
        componentOptions.put(Commands.LONG_OPTION_FOREIGN_CATALOG, Commands.OPTION_FOREIGN_CATALOG);
        componentOptions.put(Commands.LONG_OPTION_FILES_OLD, Commands.OPTION_FILES);
        commands.put("install", new InstallCommand());
        commands.put("remove", new UninstallCommand());
        commands.put("list", new ListInstalledCommand());
        commands.put("available", new AvailableCommand());
        commands.put("info", new InfoCommand());
        commands.put("rebuild-images", new RebuildImageCommand());
        commands.put("update", new UpgradeCommand());
        commands.put("upgrade", new UpgradeCommand());
        commands.put("#postinstall", new PostInstCommand());
        commands.put("#preremove", new PreRemoveCommand());
        globalOptions.put(Commands.OPTION_VERBOSE, "");
        globalOptions.put(Commands.OPTION_DEBUG, "");
        globalOptions.put(Commands.OPTION_HELP, "");
        globalOptions.put(Commands.LONG_OPTION_VERBOSE, Commands.OPTION_VERBOSE);
        globalOptions.put(Commands.LONG_OPTION_DEBUG, Commands.OPTION_DEBUG);
        globalOptions.put(Commands.LONG_OPTION_HELP, Commands.OPTION_HELP);
        globalOptions.put(Commands.OPTION_AUTO_YES, "");
        globalOptions.put(Commands.LONG_OPTION_AUTO_YES, Commands.OPTION_AUTO_YES);
        globalOptions.put(Commands.OPTION_NON_INTERACTIVE, "");
        globalOptions.put(Commands.LONG_OPTION_NON_INTERACTIVE, Commands.OPTION_NON_INTERACTIVE);
        globalOptions.put(Commands.OPTION_PRINT_VERSION, "");
        globalOptions.put(Commands.OPTION_SHOW_VERSION, "");
        globalOptions.put(Commands.LONG_OPTION_PRINT_VERSION, Commands.OPTION_PRINT_VERSION);
        globalOptions.put(Commands.LONG_OPTION_SHOW_VERSION, Commands.OPTION_SHOW_VERSION);
        globalOptions.put(Commands.OPTION_IGNORE_CATALOG_ERRORS, "");
        globalOptions.put(Commands.LONG_OPTION_IGNORE_CATALOG_ERRORS, Commands.OPTION_IGNORE_CATALOG_ERRORS);
        globalOptions.putAll(componentOptions);
    }

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org.graalvm.component.installer.Bundle");

    public static void forSoftwareChannels(boolean report, Consumer<SoftwareChannel.Factory> callback) {
        ServiceLoader<SoftwareChannel.Factory> channels = ServiceLoader.load(SoftwareChannel.Factory.class);
        for (Iterator<SoftwareChannel.Factory> it = channels.iterator(); it.hasNext(); ) {
            try {
                SoftwareChannel.Factory ch = it.next();
                callback.accept(ch);
            } catch (ServiceConfigurationError | Exception ex) {
                if (report) {
                    LOG.log(Level.SEVERE, MessageFormat.format(BUNDLE.getString("ERROR_SoftwareChannelBroken"), ex.getLocalizedMessage()));
                }
            }
        }
    }

    static {
        initCommands();
    }

    public ComponentInstaller(String[] args) {
        cmdlineParams = new LinkedList<>(Arrays.asList(args));
    }

    protected void printUsage(Feedback output) {
        output.output("INFO_InstallerVersion", CommonConstants.INSTALLER_VERSION);
        printHelp(output);
    }

    private static void printHelp(Feedback output) {
        StringBuilder extra = new StringBuilder();
        forSoftwareChannels(false, (ch) -> {
            ch.init(SIMPLE_ENV, output);
            String s = ch.globalOptionsHelp();
            if (s != null) {
                extra.append(s);
            }
        });
        String extraS;
        if (extra.length() != 0) {
            extraS = output.l10n("INFO_UsageExtensions", extra.toString());
        } else {
            extraS = "";
        }
        output.output("INFO_Usage", extraS);
    }

    static void printErr(String messageKey, Object... args) {
        SIMPLE_ENV.message(messageKey, args);
    }

    static RuntimeException err(String messageKey, Object... args) {
        printErr(messageKey, args);
        printHelp(SIMPLE_ENV);
        System.exit(1);
        throw new RuntimeException("should not reach here");
    }

    protected RuntimeException error(String messageKey, Object... args) {
        return err(messageKey, args);
    }

    private Environment env;

    private CommandInput input;

    private Feedback feedback;

    CommandInput getInput() {
        return input;
    }

    protected void setInput(CommandInput input) {
        this.input = input;
    }

    Feedback getFeedback() {
        return feedback;
    }

    protected void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }

    Path getGraalHomePath() {
        return graalHomePath;
    }

    protected Environment setupEnvironment(SimpleGetopt go) {
        Environment e = new Environment(command, parameters, go.getOptValues());
        setInput(e);
        setFeedback(e);
        findGraalHome();
        e.setGraalHome(graalHomePath);
        Path trustStorePath = SystemUtils.resolveRelative(SystemUtils.getRuntimeBaseDir(e.getGraalHomePath()), "lib/security/cacerts");
        System.setProperty("javax.net.ssl.trustStore", trustStorePath.normalize().toString());
        DirectoryStorage storage = new DirectoryStorage(e, storagePath, graalHomePath);
        storage.setConfig(env);
        storage.setJavaVersion("" + SystemUtils.getJavaMajorVersion(e));
        e.setLocalRegistry(new ComponentRegistry(e, storage));
        FileOperations fops = FileOperations.createPlatformInstance(e, e.getGraalHomePath());
        e.setFileOperations(fops);
        return e;
    }

    protected SimpleGetopt createOptionsObject(Map<String, String> opts) {
        return new SimpleGetopt(opts);
    }

    SimpleGetopt createOptions(LinkedList<String> cmdline) {
        SimpleGetopt go = createOptionsObject(globalOptions).ignoreUnknownOptions(true);
        go.setParameters(new LinkedList<>(cmdline));
        for (String s : commands.keySet()) {
            go.addCommandOptions(s, commands.get(s).supportedOptions());
        }
        go.process();
        options = go;
        command = go.getCommand();
        cmdHandler = commands.get(command);
        parameters = go.getPositionalParameters();
        env = setupEnvironment(go);
        forSoftwareChannels(true, (ch) -> {
            ch.init(input, feedback);
        });
        return go;
    }

    SimpleGetopt interpretOptions(SimpleGetopt go) {
        List<String> unknownOptions = go.getUnknownOptions();
        if (env.hasOption(Commands.OPTION_HELP) && go.getCommand() == null) {
            unknownOptions.add("help");
        }
        parseUnknownOptions(unknownOptions);
        if (runLauncher()) {
            return null;
        }
        return go;
    }

    public String getCommand() {
        return command;
    }

    public List<String> getParameters() {
        return parameters;
    }

    int processOptions(LinkedList<String> cmdline) {
        if (cmdline.size() < 1) {
            env = SIMPLE_ENV;
            printDefaultHelp(OptionCategory.USER);
            return 1;
        }
        SimpleGetopt go = createOptions(cmdline);
        launch(cmdline);
        go = interpretOptions(go);
        if (go == null) {
            return 0;
        }
        if (env.hasOption(Commands.OPTION_PRINT_VERSION)) {
            printVersion();
            return 0;
        } else if (env.hasOption(Commands.OPTION_SHOW_VERSION)) {
            printVersion();
        }
        if (cmdHandler == null) {
            error("ERROR_MissingCommand");
        }
        int srcCount = 0;
        if (input.hasOption(Commands.OPTION_FILES)) {
            srcCount++;
        }
        if (input.hasOption(Commands.OPTION_URLS)) {
            srcCount++;
        }
        if (srcCount > 1) {
            error("ERROR_MultipleSourcesUnsupported");
        }
        if (input.hasOption(Commands.OPTION_AUTO_YES)) {
            env.setAutoYesEnabled(true);
        }
        if (input.hasOption(Commands.OPTION_NON_INTERACTIVE)) {
            env.setNonInteractive(true);
        }
        String catalogURL = getExplicitCatalogURL();
        String builtinCatLocation = getReleaseCatalogURL();
        if (builtinCatLocation == null) {
            builtinCatLocation = feedback.l10n("Installer_BuiltingCatalogURL");
        }
        GraalEditionList editionList = new GraalEditionList(feedback, input, input.getLocalRegistry());
        editionList.setDefaultCatalogSpec(builtinCatLocation);
        editionList.setOverrideCatalogSpec(catalogURL);
        env.setCatalogFactory(editionList);
        if (input.hasOption(Commands.OPTION_USE_EDITION)) {
            input.getLocalRegistry().setOverrideEdition(input.optValue(Commands.OPTION_USE_EDITION));
        }
        boolean builtinsImplied = true;
        boolean setIterable = true;
        if (input.hasOption(Commands.OPTION_FILES)) {
            FileIterable fi = new FileIterable(env, env);
            fi.setCatalogFactory(editionList);
            env.setFileIterable(fi);
            builtinsImplied = false;
            if (input.hasOption(Commands.OPTION_LOCAL_DEPENDENCIES)) {
                while (env.hasParameter()) {
                    String s = env.nextParameter();
                    Path p = SystemUtils.fromUserString(s);
                    if (p != null) {
                        Path parent = p.getParent();
                        if (parent != null && Files.isDirectory(parent)) {
                            SoftwareChannelSource localSource = new SoftwareChannelSource(parent.toUri().toString(), null);
                            localSource.setPriority(10000);
                            editionList.addLocalChannelSource(localSource);
                        }
                    }
                }
                env.resetParameters();
            }
            setIterable = false;
        } else if (input.hasOption(Commands.OPTION_URLS)) {
            DownloadURLIterable dit = new DownloadURLIterable(env, env);
            dit.setCatalogFactory(editionList);
            env.setFileIterable(dit);
            setIterable = false;
            builtinsImplied = false;
        }
        if (setIterable) {
            env.setFileIterable(new CatalogIterable(env, env));
        }
        editionList.setRemoteSourcesAllowed(builtinsImplied || env.hasOption(Commands.OPTION_CATALOG) || env.hasOption(Commands.OPTION_FOREIGN_CATALOG));
        return -1;
    }

    int doProcessCommand() throws IOException {
        cmdHandler.init(input, feedback.withBundle(cmdHandler.getClass()));
        return cmdHandler.execute();
    }

    private int processCommand(LinkedList<String> cmds) {
        int retcode = 0;
        try {
            retcode = processOptions(cmds);
            if (retcode >= 0) {
                return retcode;
            }
            LOG.log(Level.INFO, "Installer starting");
            retcode = doProcessCommand();
        } catch (FileAlreadyExistsException ex) {
            feedback.error("INSTALLER_FileExists", ex, ex.getLocalizedMessage());
            return 2;
        } catch (NoSuchFileException ex) {
            feedback.error("INSTALLER_FileDoesNotExist", ex, ex.getLocalizedMessage());
            return 2;
        } catch (AccessDeniedException ex) {
            feedback.error("INSTALLER_AccessDenied", ex, ex.getLocalizedMessage());
            return 2;
        } catch (DirectoryNotEmptyException ex) {
            feedback.error("INSTALLER_DirectoryNotEmpty", ex, ex.getLocalizedMessage());
            return 2;
        } catch (IOError | IOException ex) {
            feedback.error("INSTALLER_IOException", ex, ex.getLocalizedMessage());
            return 2;
        } catch (MetadataException ex) {
            feedback.error("INSTALLER_InvalidMetadata", ex, ex.getLocalizedMessage());
            return 3;
        } catch (UserAbortException ex) {
            feedback.error("ERROR_Aborted", ex, ex.getLocalizedMessage());
            return 4;
        } catch (InstallerStopException ex) {
            feedback.error("INSTALLER_Error", ex, ex.getLocalizedMessage());
            return 3;
        } catch (AbortException ex) {
            feedback.error(null, ex.getCause(), ex.getLocalizedMessage());
            return ex.getExitCode();
        } catch (RuntimeException ex) {
            feedback.error("INSTALLER_InternalError", ex, ex.getLocalizedMessage());
            return 3;
        } finally {
            if (env != null) {
                try {
                    if (env.close()) {
                        retcode = CommonConstants.WINDOWS_RETCODE_DELAYED_OPERATION;
                    }
                } catch (IOException ex) {
                }
            }
        }
        return retcode;
    }

    Path findGraalHome() {
        String graalHome = input.getParameter(CommonConstants.ENV_GRAALVM_HOME, input.getParameter(CommonConstants.ENV_GRAALVM_HOME, false), true);
        if (graalHome == null) {
            graalHome = input.getParameter("GRAAL_HOME", input.getParameter("GRAAL_HOME", false), true);
        }
        Path graalPath = null;
        if (graalHome != null) {
            graalPath = SystemUtils.fromUserString(graalHome);
        } else {
            URL loc = null;
            ProtectionDomain pd = ComponentInstaller.class.getProtectionDomain();
            if (pd != null) {
                CodeSource cs = pd.getCodeSource();
                if (cs != null) {
                    loc = cs.getLocation();
                }
            }
            if (loc != null) {
                try {
                    File f = new File(loc.toURI());
                    Path guParent = f.isFile() ? f.toPath().getParent() : f.toPath();
                    if (guParent != null) {
                        graalPath = guParent.resolve(SystemUtils.fromCommonString(GRAAL_DEFAULT_RELATIVE_PATH)).normalize().toAbsolutePath();
                        Path p = graalPath.getFileName();
                        if (p != null && "lib".equals(p.toString())) {
                            graalPath = graalPath.getParent();
                        }
                    }
                } catch (URISyntaxException ex) {
                    Logger.getLogger(ComponentInstaller.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (graalPath == null) {
            throw SIMPLE_ENV.failure("ERROR_NoGraalVMDirectory", null);
        }
        if (!Files.isDirectory(graalPath) || !Files.exists(graalPath.resolve(SystemUtils.fileName("release")))) {
            throw SIMPLE_ENV.failure("ERROR_InvalidGraalVMDirectory", null, graalPath);
        }
        if (!Files.isDirectory(storagePath = graalPath.resolve(SystemUtils.fromCommonString(PATH_COMPONENT_STORAGE)))) {
            throw SIMPLE_ENV.failure("ERROR_InvalidGraalVMDirectory", null, graalPath);
        }
        graalHomePath = graalPath.normalize();
        String libpath = System.getProperty("java.library.path");
        if (libpath == null || libpath.isEmpty()) {
            Path newLibPath = SystemUtils.getRuntimeLibDir(graalPath, true);
            if (newLibPath == null) {
                throw SIMPLE_ENV.failure("ERROR_UnknownSystem", null, System.getProperty("os.name"));
            }
            System.setProperty("java.library.path", newLibPath.toString());
        }
        return graalPath;
    }

    static void initGlobalOptions() {
        forSoftwareChannels(true, (ch) -> {
            ch.init(SIMPLE_ENV, SIMPLE_ENV);
            globalOptions.putAll(ch.globalOptions());
        });
    }

    public void run() {
        initGlobalOptions();
        try {
            System.exit(processCommand(cmdlineParams));
        } catch (UserAbortException ex) {
            SIMPLE_ENV.message("ERROR_Aborted", ex.getMessage());
        } catch (Exception ex) {
            SIMPLE_ENV.error("ERROR_InternalError", ex, ex.getMessage());
            System.exit(3);
        }
    }

    String getExplicitCatalogURL() {
        String def = null;
        String cmdLine = input.optValue(Commands.OPTION_FOREIGN_CATALOG);
        if (cmdLine != null) {
            def = cmdLine;
        }
        String envVar = input.getParameter(CommonConstants.ENV_CATALOG_URL, false);
        if (envVar != null) {
            def = envVar;
        }
        String s = input.getParameter(CommonConstants.SYSPROP_CATALOG_URL, def, true);
        if (s == null) {
            return null;
        }
        boolean useAsFile = false;
        try {
            URI check = URI.create(s);
            if (check.getScheme() == null || check.getScheme().length() < 2) {
                useAsFile = true;
            }
        } catch (IllegalArgumentException ex) {
            useAsFile = true;
        }
        if (useAsFile) {
            Path p = SystemUtils.fromUserString(s);
            if (Files.isReadable(p) || Files.isDirectory(p)) {
                return p.toFile().toURI().toString();
            }
        }
        return s;
    }

    private String getReleaseCatalogURL() {
        String s = env.getLocalRegistry().getGraalCapabilities().get(CommonConstants.RELEASE_CATALOG_KEY);
        return s;
    }

    public static void main(String[] args) {
        new ComponentInstaller(args).run();
    }

    final class EnvStream extends PrintStream {

        private final boolean error;

        EnvStream(boolean err, OutputStream dummyStream) {
            super(dummyStream);
            this.error = err;
        }

        @Override
        public PrintStream append(char c) {
            env.verbatimPart("" + c, error);
            return this;
        }

        @Override
        public PrintStream append(CharSequence csq, int start, int end) {
            CharSequence cs = (csq == null ? "null" : csq);
            append(cs.subSequence(start, end));
            return this;
        }

        @Override
        public PrintStream append(CharSequence csq) {
            CharSequence cs = (csq == null ? "null" : csq);
            env.verbatimPart(cs.toString(), error, false);
            return this;
        }

        @Override
        public void println(Object x) {
            println(String.valueOf(x));
        }

        @Override
        public void println(String x) {
            if (error) {
                env.message(null, x);
            } else {
                env.output(null, x);
            }
        }

        @Override
        public void println(char[] x) {
            println(String.valueOf(x));
        }

        @Override
        public void println(double x) {
            println(String.valueOf(x));
        }

        @Override
        public void println(float x) {
            println(String.valueOf(x));
        }

        @Override
        public void println(long x) {
            println(String.valueOf(x));
        }

        @Override
        public void println(int x) {
            println(String.valueOf(x));
        }

        @Override
        public void println(char x) {
            println(String.valueOf(x));
        }

        @Override
        public void println(boolean x) {
            println(String.valueOf(x));
        }

        @Override
        public void println() {
            println("");
        }

        @Override
        public void print(Object obj) {
            print(String.valueOf(obj));
        }

        @Override
        public void print(String s) {
            env.verbatimPart(s, error, false);
        }

        @Override
        public void print(char[] s) {
            print(String.valueOf(s));
        }

        @Override
        public void print(double d) {
            print(String.valueOf(d));
        }

        @Override
        public void print(float f) {
            print(String.valueOf(f));
        }

        @Override
        public void print(long l) {
            print(String.valueOf(l));
        }

        @Override
        public void print(int i) {
            print(String.valueOf(i));
        }

        @Override
        public void print(char c) {
            print(String.valueOf(c));
        }

        @Override
        public void print(boolean b) {
            print(String.valueOf(b));
        }
    }

    void configureLogging(Map<String, String> properties) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        Collection<Logger> keep = new LinkedList<>();
        boolean rootLevelSet = false;
        for (String key : properties.keySet()) {
            if (key.startsWith("log.") && key.endsWith(".level")) {
                String v = properties.get(key);
                if (v == null) {
                    continue;
                }
                String k;
                if (key.length() > 10) {
                    k = key.substring(4);
                } else {
                    k = ".level";
                    rootLevelSet = true;
                }
                ps.print(k);
                ps.print('=');
                ps.println(v);
                keep.add(Logger.getLogger(k.substring(0, k.length() - 6)));
            }
        }
        if (!rootLevelSet) {
            ps.println(".level=WARNING");
        }
        ps.println("java.util.logging.SimpleFormatter.format=[%4$-7s] %5$s %n");
        ps.println("");
        try {
            LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(os.toByteArray()));
        } catch (IOException ex) {
            env.error("WARN_CouldNotInitializeLogManager", ex, ex.getLocalizedMessage());
            return;
        }
        Logger logger = Logger.getLogger("");
        Handler[] old = logger.getHandlers();
        Path p = getLogFile();
        if (old.length > 0) {
            for (int i = 0; i < old.length; i++) {
                old[i].setLevel(Level.ALL);
            }
        }
        if (old.length == 0 || p != null) {
            OutputStream logOs = new EnvStream(true, System.err);
            try {
                if (p != null) {
                    logOs = newLogStream(getLogFile());
                }
            } catch (IOException ex) {
                env.error("WARN_CouldNotCreateLog", ex, p.toString(), ex.getLocalizedMessage());
            }
            Handler h = new StreamHandler(logOs, new SimpleFormatter());
            h.setLevel(Level.ALL);
            logger.addHandler(h);
        }
    }

    @Override
    protected boolean canPolyglot() {
        return false;
    }

    public void launch(List<String> args) {
        maybeNativeExec(args, args, false);
    }

    public Map<String, String> parseUnknownOptions(List<String> uOpts) {
        List<String> ooo = uOpts.stream().map((o) -> o.length() > 1 ? "--" + o : "-" + o).collect(Collectors.toList());
        Map<String, String> polyOptions = new HashMap<>();
        parseUnrecognizedOptions(null, polyOptions, ooo);
        configureLogging(polyOptions);
        return polyOptions;
    }

    @Override
    protected void printHelp(OptionCategory maxCategory) {
        printUsage(env);
    }

    @Override
    protected void printVersion() {
        feedback.output("MSG_InstallerVersion", env.getLocalRegistry().getGraalVersion().displayString());
    }

    public boolean runLauncher() {
        return super.runLauncherAction();
    }

    @Override
    protected void collectArguments(Set<String> result) {
        result.addAll(options.getAllOptions());
    }

    @Override
    protected OptionDescriptor findOptionDescriptor(String group, String key) {
        return null;
    }

    @Override
    protected void executeJVM(String classpath, List<String> jvmArgs, List<String> remainingArgs) {
        if (SystemUtils.isWindows()) {
            int retcode = executeJVMMode(classpath, jvmArgs, remainingArgs);
            System.exit(retcode);
        } else {
            super.executeJVM(classpath, jvmArgs, remainingArgs);
        }
    }

    int executeJVMMode(String classpath, List<String> jvmArgs, List<String> remainingArgs) {
        WindowsJVMWrapper jvmWrapper = new WindowsJVMWrapper(env, env.getFileOperations(), env.getGraalHomePath());
        jvmWrapper.vm(getGraalVMBinaryPath("java").toString(), jvmArgs).mainClass(getMainClass()).classpath(classpath).args(remainingArgs);
        try {
            return jvmWrapper.execute();
        } catch (IOException ex) {
            throw env.failure("ERR_InvokingJvmMode", ex, ex.getMessage());
        }
    }
}
