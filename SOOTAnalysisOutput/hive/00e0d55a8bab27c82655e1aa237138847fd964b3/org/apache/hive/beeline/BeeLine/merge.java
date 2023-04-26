package org.apache.hive.beeline;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.SequenceInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.text.ChoiceFormat;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;
import jline.console.completer.FileNameCompleter;
import jline.console.ConsoleReader;
import jline.console.history.History;
import jline.console.history.FileHistory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.io.IOUtils;
import org.apache.hive.beeline.cli.CliOptionsProcessor;
import org.apache.hive.jdbc.Utils;

public class BeeLine implements Closeable {

    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle(BeeLine.class.getSimpleName());

    private final BeeLineSignalHandler signalHandler = null;

    private static final String separator = System.getProperty("line.separator");

    private boolean exit = false;

    private final DatabaseConnections connections = new DatabaseConnections();

    public static final String COMMAND_PREFIX = "!";

    private final Completer beeLineCommandCompleter;

    private Collection<Driver> drivers = null;

    private final BeeLineOpts opts = new BeeLineOpts(this, System.getProperties());

    private String lastProgress = null;

    private final Map<SQLWarning, Date> seenWarnings = new HashMap<SQLWarning, Date>();

    private final Commands commands = new Commands(this);

    private OutputFile scriptOutputFile = null;

    private OutputFile recordOutputFile = null;

    private PrintStream outputStream = new PrintStream(System.out, true);

    private PrintStream errorStream = new PrintStream(System.err, true);

    private ConsoleReader consoleReader;

    private List<String> batch = null;

    private final Reflector reflector;

    private String dbName = null;

    private History history;

    private boolean isBeeLine = true;

    private static final Options options = new Options();

    public static final String BEELINE_DEFAULT_JDBC_DRIVER = "org.apache.hive.jdbc.HiveDriver";

    public static final String BEELINE_DEFAULT_JDBC_URL = "jdbc:hive2://";

    private static final String SCRIPT_OUTPUT_PREFIX = ">>>";

    private static final int SCRIPT_OUTPUT_PAD_SIZE = 5;

    private static final int ERRNO_OK = 0;

    private static final int ERRNO_ARGS = 1;

    private static final int ERRNO_OTHER = 2;

    private static final String HIVE_VAR_PREFIX = "--hivevar";

    private static final String HIVE_CONF_PREFIX = "--hiveconf";

    private final Map<Object, Object> formats = map(new Object[] { "vertical", new VerticalOutputFormat(this), "table", new TableOutputFormat(this), "csv2", new SeparatedValuesOutputFormat(this, ','), "tsv2", new SeparatedValuesOutputFormat(this, '\t'), "dsv", new SeparatedValuesOutputFormat(this, BeeLineOpts.DEFAULT_DELIMITER_FOR_DSV), "csv", new DeprecatedSeparatedValuesOutputFormat(this, ','), "tsv", new DeprecatedSeparatedValuesOutputFormat(this, '\t'), "xmlattr", new XMLAttributeOutputFormat(this), "xmlelements", new XMLElementOutputFormat(this) });

    private List<String> supportedLocalDriver = new ArrayList<String>(Arrays.asList("com.mysql.jdbc.Driver", "org.postgresql.Driver"));

    final CommandHandler[] commandHandlers = new CommandHandler[] { new ReflectiveCommandHandler(this, new String[] { "quit", "done", "exit" }, null), new ReflectiveCommandHandler(this, new String[] { "connect", "open" }, new Completer[] { new StringsCompleter(getConnectionURLExamples()) }), new ReflectiveCommandHandler(this, new String[] { "describe" }, new Completer[] { new TableNameCompletor(this) }), new ReflectiveCommandHandler(this, new String[] { "indexes" }, new Completer[] { new TableNameCompletor(this) }), new ReflectiveCommandHandler(this, new String[] { "primarykeys" }, new Completer[] { new TableNameCompletor(this) }), new ReflectiveCommandHandler(this, new String[] { "exportedkeys" }, new Completer[] { new TableNameCompletor(this) }), new ReflectiveCommandHandler(this, new String[] { "manual" }, null), new ReflectiveCommandHandler(this, new String[] { "importedkeys" }, new Completer[] { new TableNameCompletor(this) }), new ReflectiveCommandHandler(this, new String[] { "procedures" }, null), new ReflectiveCommandHandler(this, new String[] { "tables" }, null), new ReflectiveCommandHandler(this, new String[] { "typeinfo" }, null), new ReflectiveCommandHandler(this, new String[] { "columns" }, new Completer[] { new TableNameCompletor(this) }), new ReflectiveCommandHandler(this, new String[] { "reconnect" }, null), new ReflectiveCommandHandler(this, new String[] { "dropall" }, new Completer[] { new TableNameCompletor(this) }), new ReflectiveCommandHandler(this, new String[] { "history" }, null), new ReflectiveCommandHandler(this, new String[] { "metadata" }, new Completer[] { new StringsCompleter(getMetadataMethodNames()) }), new ReflectiveCommandHandler(this, new String[] { "nativesql" }, null), new ReflectiveCommandHandler(this, new String[] { "dbinfo" }, null), new ReflectiveCommandHandler(this, new String[] { "rehash" }, null), new ReflectiveCommandHandler(this, new String[] { "verbose" }, null), new ReflectiveCommandHandler(this, new String[] { "run" }, new Completer[] { new FileNameCompleter() }), new ReflectiveCommandHandler(this, new String[] { "batch" }, null), new ReflectiveCommandHandler(this, new String[] { "list" }, null), new ReflectiveCommandHandler(this, new String[] { "all" }, null), new ReflectiveCommandHandler(this, new String[] { "go", "#" }, null), new ReflectiveCommandHandler(this, new String[] { "script" }, new Completer[] { new FileNameCompleter() }), new ReflectiveCommandHandler(this, new String[] { "record" }, new Completer[] { new FileNameCompleter() }), new ReflectiveCommandHandler(this, new String[] { "brief" }, null), new ReflectiveCommandHandler(this, new String[] { "close" }, null), new ReflectiveCommandHandler(this, new String[] { "closeall" }, null), new ReflectiveCommandHandler(this, new String[] { "isolation" }, new Completer[] { new StringsCompleter(getIsolationLevels()) }), new ReflectiveCommandHandler(this, new String[] { "outputformat" }, new Completer[] { new StringsCompleter(formats.keySet().toArray(new String[0])) }), new ReflectiveCommandHandler(this, new String[] { "autocommit" }, null), new ReflectiveCommandHandler(this, new String[] { "commit" }, null), new ReflectiveCommandHandler(this, new String[] { "properties" }, new Completer[] { new FileNameCompleter() }), new ReflectiveCommandHandler(this, new String[] { "rollback" }, null), new ReflectiveCommandHandler(this, new String[] { "help", "?" }, null), new ReflectiveCommandHandler(this, new String[] { "set" }, getOpts().optionCompleters()), new ReflectiveCommandHandler(this, new String[] { "save" }, null), new ReflectiveCommandHandler(this, new String[] { "scan" }, null), new ReflectiveCommandHandler(this, new String[] { "sql" }, null), new ReflectiveCommandHandler(this, new String[] { "sh" }, null), new ReflectiveCommandHandler(this, new String[] { "call" }, null), new ReflectiveCommandHandler(this, new String[] { "nullemptystring" }, new Completer[] { new BooleanCompleter() }), new ReflectiveCommandHandler(this, new String[] { "addlocaldriverjar" }, null), new ReflectiveCommandHandler(this, new String[] { "addlocaldrivername" }, null) };

    static final SortedSet<String> KNOWN_DRIVERS = new TreeSet<String>(Arrays.asList(new String[] { "org.apache.hive.jdbc.HiveDriver", "org.apache.hadoop.hive.jdbc.HiveDriver" }));

    static {
        try {
            Class.forName("jline.console.ConsoleReader");
        } catch (Throwable t) {
            throw new ExceptionInInitializerError("jline-missing");
        }
    }

    static {
        options.addOption(OptionBuilder.hasArg().withArgName("driver class").withDescription("the driver class to use").create('d'));
        options.addOption(OptionBuilder.hasArg().withArgName("database url").withDescription("the JDBC URL to connect to").create('u'));
        options.addOption(OptionBuilder.hasArg().withArgName("username").withDescription("the username to connect as").create('n'));
        options.addOption(OptionBuilder.hasArg().withArgName("password").withDescription("the password to connect as").create('p'));
        options.addOption(OptionBuilder.hasArg().withArgName("password-file").withDescription("the password file to read password from").withLongOpt("password-file").create('w'));
        options.addOption(OptionBuilder.hasArg().withArgName("authType").withDescription("the authentication type").create('a'));
        options.addOption(OptionBuilder.hasArg().withArgName("init").withDescription("script file for initialization").create('i'));
        options.addOption(OptionBuilder.hasArgs().withArgName("query").withDescription("query that should be executed").create('e'));
        options.addOption(OptionBuilder.hasArg().withArgName("file").withDescription("script file that should be executed").create('f'));
        options.addOption(OptionBuilder.withLongOpt("help").withDescription("display this message").create('h'));
        options.addOption(OptionBuilder.withValueSeparator().hasArgs(2).withArgName("key=value").withLongOpt("hivevar").withDescription("hive variable name and value").create());
        options.addOption(OptionBuilder.withValueSeparator().hasArgs(2).withArgName("property=value").withLongOpt("hiveconf").withDescription("Use value for given property").create());
    }

    static Manifest getManifest() throws IOException {
        URL base = BeeLine.class.getResource("/META-INF/MANIFEST.MF");
        URLConnection c = base.openConnection();
        if (c instanceof JarURLConnection) {
            return ((JarURLConnection) c).getManifest();
        }
        return null;
    }

    String getManifestAttribute(String name) {
        try {
            Manifest m = getManifest();
            if (m == null) {
                return "??";
            }
            Attributes attrs = m.getAttributes("beeline");
            if (attrs == null) {
                return "???";
            }
            String val = attrs.getValue(name);
            if (val == null || "".equals(val)) {
                return "????";
            }
            return val;
        } catch (Exception e) {
            e.printStackTrace(errorStream);
            return "?????";
        }
    }

    String getApplicationTitle() {
        Package pack = BeeLine.class.getPackage();
        return loc("app-introduction", new Object[] { "Beeline", pack.getImplementationVersion() == null ? "???" : pack.getImplementationVersion(), "Apache Hive" });
    }

    String getApplicationContactInformation() {
        return getManifestAttribute("Implementation-Vendor");
    }

    String loc(String res) {
        return loc(res, new Object[0]);
    }

    String loc(String res, int param) {
        try {
            return MessageFormat.format(new ChoiceFormat(resourceBundle.getString(res)).format(param), new Object[] { new Integer(param) });
        } catch (Exception e) {
            return res + ": " + param;
        }
    }

    String loc(String res, Object param1) {
        return loc(res, new Object[] { param1 });
    }

    String loc(String res, Object param1, Object param2) {
        return loc(res, new Object[] { param1, param2 });
    }

    String loc(String res, Object[] params) {
        try {
            return MessageFormat.format(resourceBundle.getString(res), params);
        } catch (Exception e) {
            e.printStackTrace(getErrorStream());
            try {
                return res + ": " + Arrays.asList(params);
            } catch (Exception e2) {
                return res;
            }
        }
    }

    protected String locElapsedTime(long milliseconds) {
        if (getOpts().getShowElapsedTime()) {
            return loc("time-ms", new Object[] { new Double(milliseconds / 1000d) });
        }
        return "";
    }

    public static void main(String[] args) throws IOException {
        mainWithInputRedirection(args, null);
    }

    public static void mainWithInputRedirection(String[] args, InputStream inputStream) throws IOException {
        BeeLine beeLine = new BeeLine();
        int status = beeLine.begin(args, inputStream);
        if (!Boolean.getBoolean(BeeLineOpts.PROPERTY_NAME_EXIT)) {
            System.exit(status);
        }
    }

    public BeeLine() {
        this(true);
    }

    public BeeLine(boolean isBeeLine) {
        beeLineCommandCompleter = new BeeLineCommandCompleter(BeeLineCommandCompleter.getCompleters(this));
        reflector = new Reflector(this);
        this.isBeeLine = isBeeLine;
    }

    DatabaseConnection getDatabaseConnection() {
        return getDatabaseConnections().current();
    }

    Connection getConnection() throws SQLException {
        if (getDatabaseConnections().current() == null) {
            throw new IllegalArgumentException(loc("no-current-connection"));
        }
        if (getDatabaseConnections().current().getConnection() == null) {
            throw new IllegalArgumentException(loc("no-current-connection"));
        }
        return getDatabaseConnections().current().getConnection();
    }

    DatabaseMetaData getDatabaseMetaData() {
        if (getDatabaseConnections().current() == null) {
            throw new IllegalArgumentException(loc("no-current-connection"));
        }
        if (getDatabaseConnections().current().getDatabaseMetaData() == null) {
            throw new IllegalArgumentException(loc("no-current-connection"));
        }
        return getDatabaseConnections().current().getDatabaseMetaData();
    }

    public String[] getIsolationLevels() {
        return new String[] { "TRANSACTION_NONE", "TRANSACTION_READ_COMMITTED", "TRANSACTION_READ_UNCOMMITTED", "TRANSACTION_REPEATABLE_READ", "TRANSACTION_SERIALIZABLE" };
    }

    public String[] getMetadataMethodNames() {
        try {
            TreeSet<String> mnames = new TreeSet<String>();
            Method[] m = DatabaseMetaData.class.getDeclaredMethods();
            for (int i = 0; m != null && i < m.length; i++) {
                mnames.add(m[i].getName());
            }
            return mnames.toArray(new String[0]);
        } catch (Throwable t) {
            return new String[0];
        }
    }

    public String[] getConnectionURLExamples() {
        return new String[] { "jdbc:JSQLConnect://<hostname>/database=<database>", "jdbc:cloudscape:<database>;create=true", "jdbc:twtds:sqlserver://<hostname>/<database>", "jdbc:daffodilDB_embedded:<database>;create=true", "jdbc:datadirect:db2://<hostname>:50000;databaseName=<database>", "jdbc:inetdae:<hostname>:1433", "jdbc:datadirect:oracle://<hostname>:1521;SID=<database>;MaxPooledStatements=0", "jdbc:datadirect:sqlserver://<hostname>:1433;SelectMethod=cursor;DatabaseName=<database>", "jdbc:datadirect:sybase://<hostname>:5000", "jdbc:db2://<hostname>/<database>", "jdbc:hive2://<hostname>", "jdbc:hsqldb:<database>", "jdbc:idb:<database>.properties", "jdbc:informix-sqli://<hostname>:1526/<database>:INFORMIXSERVER=<database>", "jdbc:interbase://<hostname>//<database>.gdb", "jdbc:microsoft:sqlserver://<hostname>:1433;DatabaseName=<database>;SelectMethod=cursor", "jdbc:mysql://<hostname>/<database>?autoReconnect=true", "jdbc:oracle:thin:@<hostname>:1521:<database>", "jdbc:pointbase:<database>,database.home=<database>,create=true", "jdbc:postgresql://<hostname>:5432/<database>", "jdbc:postgresql:net//<hostname>/<database>", "jdbc:sybase:Tds:<hostname>:4100/<database>?ServiceName=<database>", "jdbc:weblogic:mssqlserver4:<database>@<hostname>:1433", "jdbc:odbc:<database>", "jdbc:sequelink://<hostname>:4003/[Oracle]", "jdbc:sequelink://<hostname>:4004/[Informix];Database=<database>", "jdbc:sequelink://<hostname>:4005/[Sybase];Database=<database>", "jdbc:sequelink://<hostname>:4006/[SQLServer];Database=<database>", "jdbc:sequelink://<hostname>:4011/[ODBC MS Access];Database=<database>", "jdbc:openlink://<hostname>/DSN=SQLServerDB/UID=sa/PWD=", "jdbc:solid://<hostname>:<port>/<UID>/<PWD>", "jdbc:dbaw://<hostname>:8889/<database>" };
    }

    ColorBuffer getColorBuffer() {
        return new ColorBuffer(getOpts().getColor());
    }

    ColorBuffer getColorBuffer(String msg) {
        return new ColorBuffer(msg, getOpts().getColor());
    }

    public class BeelineParser extends GnuParser {

        @Override
        protected void processOption(final String arg, final ListIterator iter) throws ParseException {
            if ((arg.startsWith("--")) && !(arg.equals(HIVE_VAR_PREFIX) || (arg.equals(HIVE_CONF_PREFIX)) || (arg.equals("--help")))) {
                String stripped = arg.substring(2, arg.length());
                String[] parts = split(stripped, "=");
                debug(loc("setting-prop", Arrays.asList(parts)));
                if (parts.length >= 2) {
                    getOpts().set(parts[0], parts[1], true);
                } else {
                    getOpts().set(parts[0], "true", true);
                }
            } else {
                super.processOption(arg, iter);
            }
        }
    }

    int initArgsFromCliVars(String[] args) {
        List<String> commands = Collections.emptyList();
        CliOptionsProcessor optionsProcessor = new CliOptionsProcessor();
        if (!optionsProcessor.process(args)) {
            return 1;
        }
        CommandLine commandLine = optionsProcessor.getCommandLine();
        Properties confProps = commandLine.getOptionProperties("hiveconf");
        for (String propKey : confProps.stringPropertyNames()) {
            getOpts().getHiveConfVariables().put(propKey, confProps.getProperty(propKey));
        }
        Properties hiveVars = commandLine.getOptionProperties("define");
        for (String propKey : hiveVars.stringPropertyNames()) {
            getOpts().getHiveConfVariables().put(propKey, hiveVars.getProperty(propKey));
        }
        Properties hiveVars2 = commandLine.getOptionProperties("hivevar");
        for (String propKey : hiveVars2.stringPropertyNames()) {
            getOpts().getHiveConfVariables().put(propKey, hiveVars2.getProperty(propKey));
        }
        getOpts().setScriptFile(commandLine.getOptionValue("f"));
        dbName = commandLine.getOptionValue("database");
        getOpts().setVerbose(Boolean.valueOf(commandLine.getOptionValue("verbose")));
        getOpts().setSilent(Boolean.valueOf(commandLine.getOptionValue("slient")));
        int code = 0;
        if (commandLine.getOptionValues("e") != null) {
            commands = Arrays.asList(commandLine.getOptionValues("e"));
        }
        if (!commands.isEmpty() && getOpts().getScriptFile() != null) {
            System.err.println("The '-e' and '-f' options cannot be specified simultaneously");
            optionsProcessor.printCliUsage();
            return 1;
        }
        if (!commands.isEmpty()) {
            embeddedConnect();
            connectDBInEmbededMode();
            for (Iterator<String> i = commands.iterator(); i.hasNext(); ) {
                String command = i.next().toString();
                debug(loc("executing-command", command));
                if (!dispatch(command)) {
                    code++;
                }
            }
            exit = true;
        }
        return code;
    }

    int initArgs(String[] args) {
        List<String> commands = Collections.emptyList();
        List<String> files = Collections.emptyList();
        CommandLine cl;
        BeelineParser beelineParser;
        try {
            beelineParser = new BeelineParser();
            cl = beelineParser.parse(options, args);
        } catch (ParseException e1) {
            output(e1.getMessage());
            usage();
            return -1;
        }
        String driver = null, user = null, pass = null, url = null;
        String auth = null;
        if (cl.hasOption("help")) {
            usage();
            return 0;
        }
        Properties hiveVars = cl.getOptionProperties("hivevar");
        for (String key : hiveVars.stringPropertyNames()) {
            getOpts().getHiveVariables().put(key, hiveVars.getProperty(key));
        }
        Properties hiveConfs = cl.getOptionProperties("hiveconf");
        for (String key : hiveConfs.stringPropertyNames()) {
            getOpts().getHiveConfVariables().put(key, hiveConfs.getProperty(key));
        }
        driver = cl.getOptionValue("d");
        auth = cl.getOptionValue("a");
        user = cl.getOptionValue("n");
        getOpts().setAuthType(auth);
        if (cl.hasOption("w")) {
            pass = obtainPasswordFromFile(cl.getOptionValue("w"));
        } else {
            pass = cl.getOptionValue("p");
        }
        url = cl.getOptionValue("u");
        getOpts().setInitFile(cl.getOptionValue("i"));
        getOpts().setScriptFile(cl.getOptionValue("f"));
        if (cl.getOptionValues('e') != null) {
            commands = Arrays.asList(cl.getOptionValues('e'));
        }
        if (url != null) {
            String com = "!connect " + url + " " + (user == null || user.length() == 0 ? "''" : user) + " " + (pass == null || pass.length() == 0 ? "''" : pass) + " " + (driver == null ? "" : driver);
            debug("issuing: " + com);
            dispatch(com);
        }
        for (Iterator<String> i = files.iterator(); i.hasNext(); ) {
            dispatch("!properties " + i.next());
        }
        int code = 0;
        if (!commands.isEmpty()) {
            for (Iterator<String> i = commands.iterator(); i.hasNext(); ) {
                String command = i.next().toString();
                debug(loc("executing-command", command));
                if (!dispatch(command)) {
                    code++;
                }
            }
            exit = true;
        }
        return code;
    }

    private String obtainPasswordFromFile(String passwordFilePath) {
        try {
            Path path = Paths.get(passwordFilePath);
            byte[] passwordFileContents = Files.readAllBytes(path);
            return new String(passwordFileContents, "UTF-8").trim();
        } catch (Exception e) {
            throw new RuntimeException("Unable to read user password from the password file: " + passwordFilePath, e);
        }
    }

    public int begin(String[] args, InputStream inputStream) throws IOException {
        try {
            getOpts().load();
        } catch (Exception e) {
        }
        try {
            if (isBeeLine) {
                int code = initArgs(args);
                if (code != 0) {
                    return code;
                }
            } else {
                int code = initArgsFromCliVars(args);
                defaultConnect(false);
                if (code != 0)
                    return code;
            }
            if (getOpts().getScriptFile() != null) {
                return executeFile(getOpts().getScriptFile(), false);
            }
            try {
                info(getApplicationTitle());
            } catch (Exception e) {
            }
            ConsoleReader reader = getConsoleReader(inputStream);
            return execute(reader, false);
        } finally {
            close();
        }
    }

    int runInit() {
        String initFile = getOpts().getInitFile();
        if (initFile != null) {
            info("Running init script " + initFile);
            try {
                return executeFile(initFile, false);
            } finally {
                exit = false;
            }
        }
        return ERRNO_OK;
    }

    private int embeddedConnect() {
        if (!dispatch("!connect " + Utils.URL_PREFIX + " '' ''")) {
            return ERRNO_OTHER;
        } else {
            return ERRNO_OK;
        }
    }

    private int connectDBInEmbededMode() {
        if (dbName != null && !dbName.isEmpty()) {
            if (!dispatch("use " + dbName + ";")) {
                return ERRNO_OTHER;
            }
        }
        return ERRNO_OK;
    }

    public int defaultConnect(boolean exitOnError) {
        if (embeddedConnect() != ERRNO_OK && exitOnError) {
            return ERRNO_OTHER;
        }
        if (connectDBInEmbededMode() != ERRNO_OK && exitOnError) {
            return ERRNO_OTHER;
        }
        return ERRNO_OK;
    }

    private int executeFile(String fileName, boolean isSourceCMD) {
        FileInputStream initStream = null;
        try {
            initStream = new FileInputStream(fileName);
            return execute(getConsoleReader(initStream), true);
        } catch (Throwable t) {
            handleException(t);
            return ERRNO_OTHER;
        } finally {
            IOUtils.closeStream(initStream);
            if (!isSourceCMD) {
                consoleReader = null;
                output("");
            }
        }
    }

    private boolean isSourceCMD(String cmd) {
        if (cmd == null || cmd.isEmpty())
            return false;
        String[] tokens = tokenizeCmd(cmd);
        return tokens[0].equalsIgnoreCase("!source");
    }

    private boolean sourceFile(String cmd) {
        String[] tokens = tokenizeCmd(cmd);
        String cmd_1 = getFirstCmd(cmd, tokens[0].length());
        File sourceFile = new File(cmd_1);
        if (!sourceFile.isFile()) {
            return false;
        } else {
            boolean ret = (executeFile(cmd_1, true) == ERRNO_OK);
            setExit(false);
            return ret;
        }
    }

    private int execute(ConsoleReader reader, boolean exitOnError) {
        String line;
        while (!exit) {
            try {
                line = (getOpts().isSilent() && getOpts().getScriptFile() != null) ? reader.readLine(null, ConsoleReader.NULL_MASK) : reader.readLine(getPrompt());
                line = (line == null) ? null : line.trim();
                if (!isBeeLine) {
                    line = cliToBeelineCmd(line);
                }
                if (!dispatch(line) && exitOnError) {
                    return ERRNO_OTHER;
                }
            } catch (Throwable t) {
                handleException(t);
                return ERRNO_OTHER;
            }
        }
        return ERRNO_OK;
    }

    @Override
    public void close() {
        commands.closeall(null);
    }

    public ConsoleReader getConsoleReader(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            InputStream inputStreamAppendedNewline = new SequenceInputStream(inputStream, new ByteArrayInputStream((new String("\n")).getBytes()));
            consoleReader = new ConsoleReader(inputStreamAppendedNewline, getOutputStream());
        } else {
            consoleReader = new ConsoleReader();
        }
        consoleReader.setExpandEvents(false);
        ByteArrayOutputStream hist = null;
        if (new File(getOpts().getHistoryFile()).isFile()) {
            try {
                FileInputStream historyIn = new FileInputStream(getOpts().getHistoryFile());
                hist = new ByteArrayOutputStream();
                int n;
                while ((n = historyIn.read()) != -1) {
                    hist.write(n);
                }
                historyIn.close();
            } catch (Exception e) {
                handleException(e);
            }
        }
        try {
            consoleReader.setHistory(new FileHistory(new File(getOpts().getHistoryFile())));
        } catch (Exception e) {
            handleException(e);
        }
        if (inputStream instanceof FileInputStream) {
            return consoleReader;
        }
        try {
            if (hist != null) {
                History h = consoleReader.getHistory();
                if (h instanceof FileHistory) {
                    ((FileHistory) consoleReader.getHistory()).load(new ByteArrayInputStream(hist.toByteArray()));
                } else {
                    consoleReader.getHistory().add(hist.toString());
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                History h = consoleReader.getHistory();
                if (h instanceof FileHistory) {
                    try {
                        ((FileHistory) h).flush();
                    } catch (IOException e) {
                        error(e);
                    }
                }
            }
        }));
        consoleReader.addCompleter(new BeeLineCompleter(this));
        return consoleReader;
    }

    void usage() {
        output(loc("cmd-usage"));
    }

    private String[] tokenizeCmd(String cmd) {
        return cmd.split("\\s+");
    }

    private String getFirstCmd(String cmd, int length) {
        return cmd.substring(length).trim();
    }

    private String cliToBeelineCmd(String cmd) {
        if (cmd == null)
            return null;
        String[] tokens = tokenizeCmd(cmd);
        if (cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("exit") || cmd.equalsIgnoreCase("quit;") || cmd.equals("exit;")) {
            return null;
        } else if (tokens[0].equalsIgnoreCase("source")) {
            return COMMAND_PREFIX + cmd;
        } else if (cmd.startsWith("!")) {
            String shell_cmd = cmd.substring(1);
            return "!sh " + shell_cmd;
        } else {
            return cmd;
        }
    }

    boolean dispatch(String line) {
        if (line == null) {
            exit = true;
            return true;
        }
        if (line.trim().length() == 0) {
            return true;
        }
        if (isComment(line)) {
            return true;
        }
        if (isSourceCMD(line)) {
            return sourceFile(line);
        }
        line = line.trim();
        if (scriptOutputFile != null) {
            scriptOutputFile.addLine(line);
        }
        if (isHelpRequest(line)) {
            line = "!help";
        }
        if (line.startsWith(COMMAND_PREFIX)) {
            Map<String, CommandHandler> cmdMap = new TreeMap<String, CommandHandler>();
            line = line.substring(1);
            for (int i = 0; i < commandHandlers.length; i++) {
                String match = commandHandlers[i].matches(line);
                if (match != null) {
                    CommandHandler prev = cmdMap.put(match, commandHandlers[i]);
                    if (prev != null) {
                        return error(loc("multiple-matches", Arrays.asList(prev.getName(), commandHandlers[i].getName())));
                    }
                }
            }
            if (cmdMap.size() == 0) {
                return error(loc("unknown-command", line));
            }
            if (cmdMap.size() > 1) {
                CommandHandler handler = cmdMap.get(line);
                if (handler == null) {
                    return error(loc("multiple-matches", cmdMap.keySet().toString()));
                }
                return handler.execute(line);
            }
            return cmdMap.values().iterator().next().execute(line);
        } else {
            return commands.sql(line, getOpts().getEntireLineAsCommand());
        }
    }

    boolean needsContinuation(String line) {
        if (isHelpRequest(line)) {
            return false;
        }
        if (line.startsWith(COMMAND_PREFIX)) {
            return false;
        }
        if (isComment(line)) {
            return false;
        }
        String trimmed = line.trim();
        if (trimmed.length() == 0) {
            return false;
        }
        if (!getOpts().isAllowMultiLineCommand()) {
            return false;
        }
        return !trimmed.endsWith(";");
    }

    boolean isHelpRequest(String line) {
        return line.equals("?") || line.equalsIgnoreCase("help");
    }

    boolean isComment(String line) {
        String lineTrimmed = line.trim();
        return lineTrimmed.startsWith("#") || lineTrimmed.startsWith("--");
    }

    void output(String msg) {
        output(msg, true);
    }

    void info(String msg) {
        if (!(getOpts().isSilent())) {
            output(msg, true, getErrorStream());
        }
    }

    void info(ColorBuffer msg) {
        if (!(getOpts().isSilent())) {
            output(msg, true, getErrorStream());
        }
    }

    boolean error(String msg) {
        output(getColorBuffer().red(msg), true, getErrorStream());
        return false;
    }

    boolean error(Throwable t) {
        handleException(t);
        return false;
    }

    void debug(String msg) {
        if (getOpts().getVerbose()) {
            output(getColorBuffer().blue(msg), true, getErrorStream());
        }
    }

    void output(ColorBuffer msg) {
        output(msg, true);
    }

    void output(String msg, boolean newline, PrintStream out) {
        output(getColorBuffer(msg), newline, out);
    }

    void output(ColorBuffer msg, boolean newline) {
        output(msg, newline, getOutputStream());
    }

    void output(ColorBuffer msg, boolean newline, PrintStream out) {
        if (newline) {
            out.println(msg.getColor());
        } else {
            out.print(msg.getColor());
        }
        if (recordOutputFile == null) {
            return;
        }
        if (newline) {
            recordOutputFile.addLine(msg.getMono());
        } else {
            recordOutputFile.print(msg.getMono());
        }
    }

    void output(String msg, boolean newline) {
        output(getColorBuffer(msg), newline);
    }

    void autocommitStatus(Connection c) throws SQLException {
        info(loc("autocommit-status", c.getAutoCommit() + ""));
    }

    boolean assertAutoCommit() {
        if (!(assertConnection())) {
            return false;
        }
        try {
            if (getDatabaseConnection().getConnection().getAutoCommit()) {
                return error(loc("autocommit-needs-off"));
            }
        } catch (Exception e) {
            return error(e);
        }
        return true;
    }

    boolean assertConnection() {
        try {
            if (getDatabaseConnection() == null || getDatabaseConnection().getConnection() == null) {
                return error(loc("no-current-connection"));
            }
            if (getDatabaseConnection().getConnection().isClosed()) {
                return error(loc("connection-is-closed"));
            }
        } catch (SQLException sqle) {
            return error(loc("no-current-connection"));
        }
        return true;
    }

    void showWarnings() {
        try {
            if (getDatabaseConnection().getConnection() == null || !getOpts().getVerbose()) {
                return;
            }
            showWarnings(getDatabaseConnection().getConnection().getWarnings());
        } catch (Exception e) {
            handleException(e);
        }
    }

    void showWarnings(SQLWarning warn) {
        if (warn == null) {
            return;
        }
        if (seenWarnings.get(warn) == null) {
            seenWarnings.put(warn, new java.util.Date());
            handleSQLException(warn);
        }
        SQLWarning next = warn.getNextWarning();
        if (next != warn) {
            showWarnings(next);
        }
    }

    String getPrompt() {
        if (getDatabaseConnection() == null || getDatabaseConnection().getUrl() == null) {
            return "beeline> ";
        } else {
            String printClosed = getDatabaseConnection().isClosed() ? " (closed)" : "";
            return getPrompt(getDatabaseConnections().getIndex() + ": " + getDatabaseConnection().getUrl()) + printClosed + "> ";
        }
    }

    static String getPrompt(String url) {
        if (url == null || url.length() == 0) {
            url = "beeline";
        }
        if (url.indexOf(";") > -1) {
            url = url.substring(0, url.indexOf(";"));
        }
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.length() > 45) {
            url = url.substring(0, 45);
        }
        return url;
    }

    int getSize(ResultSet rs) {
        try {
            if (rs.getType() == rs.TYPE_FORWARD_ONLY) {
                return -1;
            }
            rs.last();
            int total = rs.getRow();
            rs.beforeFirst();
            return total;
        } catch (SQLException sqle) {
            return -1;
        } catch (AbstractMethodError ame) {
            return -1;
        }
    }

    ResultSet getColumns(String table) throws SQLException {
        if (!(assertConnection())) {
            return null;
        }
        return getDatabaseConnection().getDatabaseMetaData().getColumns(getDatabaseConnection().getDatabaseMetaData().getConnection().getCatalog(), null, table, "%");
    }

    ResultSet getTables() throws SQLException {
        if (!(assertConnection())) {
            return null;
        }
        return getDatabaseConnection().getDatabaseMetaData().getTables(getDatabaseConnection().getDatabaseMetaData().getConnection().getCatalog(), null, "%", new String[] { "TABLE" });
    }

    String[] getColumnNames(DatabaseMetaData meta) throws SQLException {
        Set<String> names = new HashSet<String>();
        info(loc("building-tables"));
        try {
            ResultSet columns = getColumns("%");
            try {
                int total = getSize(columns);
                int index = 0;
                while (columns.next()) {
                    progress(index++, total);
                    String name = columns.getString("TABLE_NAME");
                    names.add(name);
                    names.add(columns.getString("COLUMN_NAME"));
                    names.add(columns.getString("TABLE_NAME") + "." + columns.getString("COLUMN_NAME"));
                }
                progress(index, index);
            } finally {
                columns.close();
            }
            info(loc("done"));
            return names.toArray(new String[0]);
        } catch (Throwable t) {
            handleException(t);
            return new String[0];
        }
    }

    String[] split(String line) {
        return split(line, " ");
    }

    String dequote(String str) {
        if (str == null) {
            return null;
        }
        while ((str.startsWith("'") && str.endsWith("'")) || (str.startsWith("\"") && str.endsWith("\""))) {
            str = str.substring(1, str.length() - 1);
        }
        return str;
    }

    String[] split(String line, String delim) {
        StringTokenizer tok = new StringTokenizer(line, delim);
        String[] ret = new String[tok.countTokens()];
        int index = 0;
        while (tok.hasMoreTokens()) {
            String t = tok.nextToken();
            t = dequote(t);
            ret[index++] = t;
        }
        return ret;
    }

    static Map<Object, Object> map(Object[] obs) {
        Map<Object, Object> m = new HashMap<Object, Object>();
        for (int i = 0; i < obs.length - 1; i += 2) {
            m.put(obs[i], obs[i + 1]);
        }
        return Collections.unmodifiableMap(m);
    }

    static boolean getMoreResults(Statement stmnt) {
        try {
            return stmnt.getMoreResults();
        } catch (Throwable t) {
            return false;
        }
    }

    static String xmlattrencode(String str) {
        str = replace(str, "\"", "&quot;");
        str = replace(str, "<", "&lt;");
        return str;
    }

    static String replace(String source, String from, String to) {
        if (source == null) {
            return null;
        }
        if (from.equals(to)) {
            return source;
        }
        StringBuilder replaced = new StringBuilder();
        int index = -1;
        while ((index = source.indexOf(from)) != -1) {
            replaced.append(source.substring(0, index));
            replaced.append(to);
            source = source.substring(index + from.length());
        }
        replaced.append(source);
        return replaced.toString();
    }

    String[] split(String line, int assertLen, String usage) {
        String[] ret = split(line);
        if (ret.length != assertLen) {
            error(usage);
            return null;
        }
        return ret;
    }

    String wrap(String toWrap, int len, int start) {
        StringBuilder buff = new StringBuilder();
        StringBuilder line = new StringBuilder();
        char[] head = new char[start];
        Arrays.fill(head, ' ');
        for (StringTokenizer tok = new StringTokenizer(toWrap, " "); tok.hasMoreTokens(); ) {
            String next = tok.nextToken();
            if (line.length() + next.length() > len) {
                buff.append(line).append(separator).append(head);
                line.setLength(0);
            }
            line.append(line.length() == 0 ? "" : " ").append(next);
        }
        buff.append(line);
        return buff.toString();
    }

    void progress(int cur, int max) {
        StringBuilder out = new StringBuilder();
        if (lastProgress != null) {
            char[] back = new char[lastProgress.length()];
            Arrays.fill(back, '\b');
            out.append(back);
        }
        String progress = cur + "/" + (max == -1 ? "?" : "" + max) + " " + (max == -1 ? "(??%)" : ("(" + (cur * 100 / (max == 0 ? 1 : max)) + "%)"));
        if (cur >= max && max != -1) {
            progress += " " + loc("done") + separator;
            lastProgress = null;
        } else {
            lastProgress = progress;
        }
        out.append(progress);
        outputStream.print(out.toString());
        outputStream.flush();
    }

    void handleException(Throwable e) {
        while (e instanceof InvocationTargetException) {
            e = ((InvocationTargetException) e).getTargetException();
        }
        if (e instanceof SQLException) {
            handleSQLException((SQLException) e);
        } else if (e instanceof EOFException) {
            setExit(true);
        } else if (!(getOpts().getVerbose())) {
            if (e.getMessage() == null) {
                error(e.getClass().getName());
            } else {
                error(e.getMessage());
            }
        } else {
            e.printStackTrace(getErrorStream());
        }
    }

    void handleSQLException(SQLException e) {
        if (e instanceof SQLWarning && !(getOpts().getShowWarnings())) {
            return;
        }
        error(loc(e instanceof SQLWarning ? "Warning" : "Error", new Object[] { e.getMessage() == null ? "" : e.getMessage().trim(), e.getSQLState() == null ? "" : e.getSQLState().trim(), new Integer(e.getErrorCode()) }));
        if (getOpts().getVerbose()) {
            e.printStackTrace(getErrorStream());
        }
        if (!getOpts().getShowNestedErrs()) {
            return;
        }
        for (SQLException nested = e.getNextException(); nested != null && nested != e; nested = nested.getNextException()) {
            handleSQLException(nested);
        }
    }

    boolean scanForDriver(String url) {
        try {
            if (findRegisteredDriver(url) != null) {
                return true;
            }
            scanDrivers(true);
            if (findRegisteredDriver(url) != null) {
                return true;
            }
            scanDrivers(false);
            if (findRegisteredDriver(url) != null) {
                return true;
            }
            if (findLocalDriver(url) != null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            debug(e.toString());
            return false;
        }
    }

    private Driver findRegisteredDriver(String url) {
        for (Enumeration drivers = DriverManager.getDrivers(); drivers != null && drivers.hasMoreElements(); ) {
            Driver driver = (Driver) drivers.nextElement();
            try {
                if (driver.acceptsURL(url)) {
                    return driver;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    public Driver findLocalDriver(String url) throws Exception {
        if (drivers == null) {
            return null;
        }
        for (Driver d : drivers) {
            try {
                String clazzName = d.getClass().getName();
                Driver driver = (Driver) Class.forName(clazzName, true, Thread.currentThread().getContextClassLoader()).newInstance();
                if (driver.acceptsURL(url) && isSupportedLocalDriver(driver)) {
                    return driver;
                }
            } catch (SQLException e) {
                error(e);
                throw new Exception(e);
            }
        }
        return null;
    }

    public boolean isSupportedLocalDriver(Driver driver) {
        String driverName = driver.getClass().getName();
        for (String name : supportedLocalDriver) {
            if (name.equals(driverName)) {
                return true;
            }
        }
        return false;
    }

    public void addLocalDriverClazz(String driverClazz) {
        supportedLocalDriver.add(driverClazz);
    }

    Driver[] scanDrivers(String line) throws IOException {
        return scanDrivers(false);
    }

    Driver[] scanDrivers(boolean knownOnly) throws IOException {
        long start = System.currentTimeMillis();
        Set<String> classNames = new HashSet<String>();
        if (!knownOnly) {
            classNames.addAll(Arrays.asList(ClassNameCompleter.getClassNames()));
        }
        classNames.addAll(KNOWN_DRIVERS);
        Set driverClasses = new HashSet();
        for (Iterator<String> i = classNames.iterator(); i.hasNext(); ) {
            String className = i.next().toString();
            if (className.toLowerCase().indexOf("driver") == -1) {
                continue;
            }
            try {
                Class c = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
                if (!Driver.class.isAssignableFrom(c)) {
                    continue;
                }
                if (Modifier.isAbstract(c.getModifiers())) {
                    continue;
                }
                driverClasses.add(c.newInstance());
            } catch (Throwable t) {
            }
        }
        info("scan complete in " + (System.currentTimeMillis() - start) + "ms");
        return (Driver[]) driverClasses.toArray(new Driver[0]);
    }

    private Driver[] scanDriversOLD(String line) {
        long start = System.currentTimeMillis();
        Set<String> paths = new HashSet<String>();
        Set driverClasses = new HashSet();
        for (StringTokenizer tok = new StringTokenizer(System.getProperty("java.ext.dirs"), System.getProperty("path.separator")); tok.hasMoreTokens(); ) {
            File[] files = new File(tok.nextToken()).listFiles();
            for (int i = 0; files != null && i < files.length; i++) {
                paths.add(files[i].getAbsolutePath());
            }
        }
        for (StringTokenizer tok = new StringTokenizer(System.getProperty("java.class.path"), System.getProperty("path.separator")); tok.hasMoreTokens(); ) {
            paths.add(new File(tok.nextToken()).getAbsolutePath());
        }
        for (Iterator<String> i = paths.iterator(); i.hasNext(); ) {
            File f = new File(i.next());
            output(getColorBuffer().pad(loc("scanning", f.getAbsolutePath()), 60), false);
            try {
                ZipFile zf = new ZipFile(f);
                int total = zf.size();
                int index = 0;
                for (Enumeration zfEnum = zf.entries(); zfEnum.hasMoreElements(); ) {
                    ZipEntry entry = (ZipEntry) zfEnum.nextElement();
                    String name = entry.getName();
                    progress(index++, total);
                    if (name.endsWith(".class")) {
                        name = name.replace('/', '.');
                        name = name.substring(0, name.length() - 6);
                        try {
                            if (name.toLowerCase().indexOf("driver") != -1) {
                                Class c = Class.forName(name, false, getClass().getClassLoader());
                                if (Driver.class.isAssignableFrom(c) && !(Modifier.isAbstract(c.getModifiers()))) {
                                    try {
                                        Class.forName(name);
                                    } catch (Exception e) {
                                    }
                                    driverClasses.add(c.newInstance());
                                }
                            }
                        } catch (Throwable t) {
                        }
                    }
                }
                progress(total, total);
            } catch (Exception e) {
            }
        }
        info("scan complete in " + (System.currentTimeMillis() - start) + "ms");
        return (Driver[]) driverClasses.toArray(new Driver[0]);
    }

    int print(ResultSet rs) throws SQLException {
        String format = getOpts().getOutputFormat();
        OutputFormat f = (OutputFormat) formats.get(format);
        if (f == null) {
            error(loc("unknown-format", new Object[] { format, formats.keySet() }));
            f = new TableOutputFormat(this);
        }
        Rows rows;
        if (getOpts().getIncremental()) {
            rows = new IncrementalRows(this, rs);
        } else {
            rows = new BufferedRows(this, rs);
        }
        return f.print(rows);
    }

    Statement createStatement() throws SQLException {
        Statement stmnt = getDatabaseConnection().getConnection().createStatement();
        if (getOpts().timeout > -1) {
            stmnt.setQueryTimeout(getOpts().timeout);
        }
        if (signalHandler != null) {
            signalHandler.setStatement(stmnt);
        }
        return stmnt;
    }

    void runBatch(List<String> statements) {
        try {
            Statement stmnt = createStatement();
            try {
                for (Iterator<String> i = statements.iterator(); i.hasNext(); ) {
                    stmnt.addBatch(i.next().toString());
                }
                int[] counts = stmnt.executeBatch();
                output(getColorBuffer().pad(getColorBuffer().bold("COUNT"), 8).append(getColorBuffer().bold("STATEMENT")));
                for (int i = 0; counts != null && i < counts.length; i++) {
                    output(getColorBuffer().pad(counts[i] + "", 8).append(statements.get(i).toString()));
                }
            } finally {
                try {
                    stmnt.close();
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public int runCommands(String[] cmds) {
        return runCommands(Arrays.asList(cmds));
    }

    public int runCommands(List<String> cmds) {
        int successCount = 0;
        try {
            for (String cmd : cmds) {
                info(getColorBuffer().pad(SCRIPT_OUTPUT_PREFIX, SCRIPT_OUTPUT_PAD_SIZE).append(cmd));
                if (dispatch(cmd) || getOpts().getForce()) {
                    ++successCount;
                } else {
                    error(loc("abort-on-error", cmd));
                    return successCount;
                }
            }
        } catch (Exception e) {
            handleException(e);
        }
        return successCount;
    }

    void setCompletions() throws SQLException, IOException {
        if (getDatabaseConnection() != null) {
            getDatabaseConnection().setCompletions(getOpts().getFastConnect());
        }
    }

    public BeeLineOpts getOpts() {
        return opts;
    }

    DatabaseConnections getDatabaseConnections() {
        return connections;
    }

    Completer getCommandCompletor() {
        return beeLineCommandCompleter;
    }

    public boolean isExit() {
        return exit;
    }

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    Collection<Driver> getDrivers() {
        return drivers;
    }

    void setDrivers(Collection<Driver> drivers) {
        this.drivers = drivers;
    }

    public static String getSeparator() {
        return separator;
    }

    Commands getCommands() {
        return commands;
    }

    OutputFile getScriptOutputFile() {
        return scriptOutputFile;
    }

    void setScriptOutputFile(OutputFile script) {
        this.scriptOutputFile = script;
    }

    OutputFile getRecordOutputFile() {
        return recordOutputFile;
    }

    void setRecordOutputFile(OutputFile record) {
        this.recordOutputFile = record;
    }

    public void setOutputStream(PrintStream outputStream) {
        this.outputStream = new PrintStream(outputStream, true);
    }

    PrintStream getOutputStream() {
        return outputStream;
    }

    public void setErrorStream(PrintStream errorStream) {
        this.errorStream = new PrintStream(errorStream, true);
    }

    PrintStream getErrorStream() {
        return errorStream;
    }

    ConsoleReader getConsoleReader() {
        return consoleReader;
    }

    void setConsoleReader(ConsoleReader reader) {
        this.consoleReader = reader;
    }

    List<String> getBatch() {
        return batch;
    }

    void setBatch(List<String> batch) {
        this.batch = batch;
    }

    protected Reflector getReflector() {
        return reflector;
    }
}
