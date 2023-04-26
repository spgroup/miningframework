package java.util.logging;

import java.io.*;
import java.util.*;
import java.security.*;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.file.Paths;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jdk.internal.misc.JavaAWTAccess;
import jdk.internal.misc.SharedSecrets;
import sun.util.logging.internal.LoggingProviderImpl;
import java.lang.reflect.Module;
import static jdk.internal.logger.DefaultLoggerFinder.isSystem;

public class LogManager {

    private static final LogManager manager;

    private volatile Properties props = new Properties();

    private final static Level defaultLevel = Level.INFO;

    private final LoggerContext systemContext = new SystemLoggerContext();

    private final LoggerContext userContext = new LoggerContext();

    private volatile Logger rootLogger;

    private volatile boolean readPrimordialConfiguration;

    private static final int STATE_INITIALIZED = 0, STATE_INITIALIZING = 1, STATE_READING_CONFIG = 2, STATE_UNINITIALIZED = 3, STATE_SHUTDOWN = 4;

    private volatile int globalHandlersState;

    private final ReentrantLock configurationLock = new ReentrantLock();

    private static final class CloseOnReset {

        private final Logger logger;

        private CloseOnReset(Logger ref) {
            this.logger = Objects.requireNonNull(ref);
        }

        @Override
        public boolean equals(Object other) {
            return (other instanceof CloseOnReset) && ((CloseOnReset) other).logger == logger;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(logger);
        }

        public Logger get() {
            return logger;
        }

        public static CloseOnReset create(Logger logger) {
            return new CloseOnReset(logger);
        }
    }

    private final CopyOnWriteArrayList<CloseOnReset> closeOnResetLoggers = new CopyOnWriteArrayList<>();

    private final Map<Object, Runnable> listeners = Collections.synchronizedMap(new IdentityHashMap<>());

    static {
        manager = AccessController.doPrivileged(new PrivilegedAction<LogManager>() {

            @Override
            public LogManager run() {
                LogManager mgr = null;
                String cname = null;
                try {
                    cname = System.getProperty("java.util.logging.manager");
                    if (cname != null) {
                        try {
                            @SuppressWarnings("deprecation")
                            Object tmp = ClassLoader.getSystemClassLoader().loadClass(cname).newInstance();
                            mgr = (LogManager) tmp;
                        } catch (ClassNotFoundException ex) {
                            @SuppressWarnings("deprecation")
                            Object tmp = Thread.currentThread().getContextClassLoader().loadClass(cname).newInstance();
                            mgr = (LogManager) tmp;
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Could not load Logmanager \"" + cname + "\"");
                    ex.printStackTrace();
                }
                if (mgr == null) {
                    mgr = new LogManager();
                }
                return mgr;
            }
        });
    }

    private class Cleaner extends Thread {

        private Cleaner() {
            super(null, null, "Logging-Cleaner", 0, false);
            this.setContextClassLoader(null);
        }

        @Override
        public void run() {
            LogManager mgr = manager;
            configurationLock.lock();
            globalHandlersState = STATE_SHUTDOWN;
            configurationLock.unlock();
            reset();
        }
    }

    protected LogManager() {
        this(checkSubclassPermissions());
    }

    private LogManager(Void checked) {
        try {
            Runtime.getRuntime().addShutdownHook(new Cleaner());
        } catch (IllegalStateException e) {
        }
    }

    private static Void checkSubclassPermissions() {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("shutdownHooks"));
            sm.checkPermission(new RuntimePermission("setContextClassLoader"));
        }
        return null;
    }

    private boolean initializedCalled = false;

    private volatile boolean initializationDone = false;

    final void ensureLogManagerInitialized() {
        final LogManager owner = this;
        if (initializationDone || owner != manager) {
            return;
        }
        configurationLock.lock();
        try {
            final boolean isRecursiveInitialization = (initializedCalled == true);
            assert initializedCalled || !initializationDone : "Initialization can't be done if initialized has not been called!";
            if (isRecursiveInitialization || initializationDone) {
                return;
            }
            initializedCalled = true;
            try {
                AccessController.doPrivileged(new PrivilegedAction<Object>() {

                    @Override
                    public Object run() {
                        assert rootLogger == null;
                        assert initializedCalled && !initializationDone;
                        owner.rootLogger = owner.new RootLogger();
                        owner.readPrimordialConfiguration();
                        owner.addLogger(owner.rootLogger);
                        if (!owner.rootLogger.isLevelInitialized()) {
                            owner.rootLogger.setLevel(defaultLevel);
                        }
                        @SuppressWarnings("deprecation")
                        final Logger global = Logger.global;
                        owner.addLogger(global);
                        return null;
                    }
                });
            } finally {
                initializationDone = true;
            }
        } finally {
            configurationLock.unlock();
        }
    }

    public static LogManager getLogManager() {
        if (manager != null) {
            manager.ensureLogManagerInitialized();
        }
        return manager;
    }

    private void readPrimordialConfiguration() {
        if (!readPrimordialConfiguration) {
            if (System.out == null) {
                return;
            }
            readPrimordialConfiguration = true;
            try {
                readConfiguration();
                jdk.internal.logger.BootstrapLogger.redirectTemporaryLoggers();
            } catch (Exception ex) {
                assert false : "Exception raised while reading logging configuration: " + ex;
            }
        }
    }

    private WeakHashMap<Object, LoggerContext> contextsMap = null;

    private LoggerContext getUserContext() {
        LoggerContext context = null;
        SecurityManager sm = System.getSecurityManager();
        JavaAWTAccess javaAwtAccess = SharedSecrets.getJavaAWTAccess();
        if (sm != null && javaAwtAccess != null) {
            final Object ecx = javaAwtAccess.getAppletContext();
            if (ecx != null) {
                synchronized (javaAwtAccess) {
                    if (contextsMap == null) {
                        contextsMap = new WeakHashMap<>();
                    }
                    context = contextsMap.get(ecx);
                    if (context == null) {
                        context = new LoggerContext();
                        contextsMap.put(ecx, context);
                    }
                }
            }
        }
        return context != null ? context : userContext;
    }

    final LoggerContext getSystemContext() {
        return systemContext;
    }

    private List<LoggerContext> contexts() {
        List<LoggerContext> cxs = new ArrayList<>();
        cxs.add(getSystemContext());
        cxs.add(getUserContext());
        return cxs;
    }

    Logger demandLogger(String name, String resourceBundleName, Class<?> caller) {
        final Module module = caller == null ? null : caller.getModule();
        return demandLogger(name, resourceBundleName, module);
    }

    Logger demandLogger(String name, String resourceBundleName, Module module) {
        Logger result = getLogger(name);
        if (result == null) {
            Logger newLogger = new Logger(name, resourceBundleName, module, this, false);
            do {
                if (addLogger(newLogger)) {
                    return newLogger;
                }
                result = getLogger(name);
            } while (result == null);
        }
        return result;
    }

    Logger demandSystemLogger(String name, String resourceBundleName, Class<?> caller) {
        final Module module = caller == null ? null : caller.getModule();
        return demandSystemLogger(name, resourceBundleName, module);
    }

    Logger demandSystemLogger(String name, String resourceBundleName, Module module) {
        final Logger sysLogger = getSystemContext().demandLogger(name, resourceBundleName, module);
        Logger logger;
        do {
            if (addLogger(sysLogger)) {
                logger = sysLogger;
            } else {
                logger = getLogger(name);
            }
        } while (logger == null);
        if (logger != sysLogger) {
            final Logger l = logger;
            AccessController.doPrivileged(new PrivilegedAction<Void>() {

                @Override
                public Void run() {
                    l.mergeWithSystemLogger(sysLogger);
                    return null;
                }
            });
        }
        return sysLogger;
    }

    class LoggerContext {

        private final ConcurrentHashMap<String, LoggerWeakRef> namedLoggers = new ConcurrentHashMap<>();

        private final LogNode root;

        private LoggerContext() {
            this.root = new LogNode(null, this);
        }

        final boolean requiresDefaultLoggers() {
            final boolean requiresDefaultLoggers = (getOwner() == manager);
            if (requiresDefaultLoggers) {
                getOwner().ensureLogManagerInitialized();
            }
            return requiresDefaultLoggers;
        }

        final LogManager getOwner() {
            return LogManager.this;
        }

        final Logger getRootLogger() {
            return getOwner().rootLogger;
        }

        final Logger getGlobalLogger() {
            @SuppressWarnings("deprecation")
            final Logger global = Logger.global;
            return global;
        }

        Logger demandLogger(String name, String resourceBundleName, Module module) {
            final LogManager owner = getOwner();
            return owner.demandLogger(name, resourceBundleName, module);
        }

        private void ensureInitialized() {
            if (requiresDefaultLoggers()) {
                ensureDefaultLogger(getRootLogger());
                ensureDefaultLogger(getGlobalLogger());
            }
        }

        Logger findLogger(String name) {
            LoggerWeakRef ref = namedLoggers.get(name);
            Logger logger = ref == null ? null : ref.get();
            if (logger != null || (ref == null && !name.isEmpty() && !name.equals(Logger.GLOBAL_LOGGER_NAME))) {
                return logger;
            }
            synchronized (this) {
                ensureInitialized();
                ref = namedLoggers.get(name);
                if (ref == null) {
                    return null;
                }
                logger = ref.get();
                if (logger == null) {
                    ref.dispose();
                }
                return logger;
            }
        }

        private void ensureAllDefaultLoggers(Logger logger) {
            if (requiresDefaultLoggers()) {
                final String name = logger.getName();
                if (!name.isEmpty()) {
                    ensureDefaultLogger(getRootLogger());
                    if (!Logger.GLOBAL_LOGGER_NAME.equals(name)) {
                        ensureDefaultLogger(getGlobalLogger());
                    }
                }
            }
        }

        private void ensureDefaultLogger(Logger logger) {
            if (!requiresDefaultLoggers() || logger == null || logger != getGlobalLogger() && logger != LogManager.this.rootLogger) {
                assert logger == null;
                return;
            }
            if (!namedLoggers.containsKey(logger.getName())) {
                addLocalLogger(logger, false);
            }
        }

        boolean addLocalLogger(Logger logger) {
            return addLocalLogger(logger, requiresDefaultLoggers());
        }

        synchronized boolean addLocalLogger(Logger logger, boolean addDefaultLoggersIfNeeded) {
            if (addDefaultLoggersIfNeeded) {
                ensureAllDefaultLoggers(logger);
            }
            final String name = logger.getName();
            if (name == null) {
                throw new NullPointerException();
            }
            LoggerWeakRef ref = namedLoggers.get(name);
            if (ref != null) {
                if (ref.get() == null) {
                    ref.dispose();
                } else {
                    return false;
                }
            }
            final LogManager owner = getOwner();
            logger.setLogManager(owner);
            ref = owner.new LoggerWeakRef(logger);
            Level level = owner.getLevelProperty(name + ".level", null);
            if (level != null && !logger.isLevelInitialized()) {
                doSetLevel(logger, level);
            }
            processParentHandlers(logger, name, VisitedLoggers.NEVER);
            LogNode node = getNode(name);
            node.loggerRef = ref;
            Logger parent = null;
            LogNode nodep = node.parent;
            while (nodep != null) {
                LoggerWeakRef nodeRef = nodep.loggerRef;
                if (nodeRef != null) {
                    parent = nodeRef.get();
                    if (parent != null) {
                        break;
                    }
                }
                nodep = nodep.parent;
            }
            if (parent != null) {
                doSetParent(logger, parent);
            }
            node.walkAndSetParent(logger);
            ref.setNode(node);
            namedLoggers.put(name, ref);
            return true;
        }

        void removeLoggerRef(String name, LoggerWeakRef ref) {
            namedLoggers.remove(name, ref);
        }

        synchronized Enumeration<String> getLoggerNames() {
            ensureInitialized();
            return Collections.enumeration(namedLoggers.keySet());
        }

        private void processParentHandlers(final Logger logger, final String name, Predicate<Logger> visited) {
            final LogManager owner = getOwner();
            AccessController.doPrivileged(new PrivilegedAction<Void>() {

                @Override
                public Void run() {
                    if (logger != owner.rootLogger) {
                        boolean useParent = owner.getBooleanProperty(name + ".useParentHandlers", true);
                        if (!useParent) {
                            logger.setUseParentHandlers(false);
                        }
                    }
                    return null;
                }
            });
            int ix = 1;
            for (; ; ) {
                int ix2 = name.indexOf('.', ix);
                if (ix2 < 0) {
                    break;
                }
                String pname = name.substring(0, ix2);
                if (owner.getProperty(pname + ".level") != null || owner.getProperty(pname + ".handlers") != null) {
                    if (visited.test(demandLogger(pname, null, null))) {
                        break;
                    }
                }
                ix = ix2 + 1;
            }
        }

        LogNode getNode(String name) {
            if (name == null || name.equals("")) {
                return root;
            }
            LogNode node = root;
            while (name.length() > 0) {
                int ix = name.indexOf('.');
                String head;
                if (ix > 0) {
                    head = name.substring(0, ix);
                    name = name.substring(ix + 1);
                } else {
                    head = name;
                    name = "";
                }
                if (node.children == null) {
                    node.children = new HashMap<>();
                }
                LogNode child = node.children.get(head);
                if (child == null) {
                    child = new LogNode(node, this);
                    node.children.put(head, child);
                }
                node = child;
            }
            return node;
        }
    }

    final class SystemLoggerContext extends LoggerContext {

        @Override
        Logger demandLogger(String name, String resourceBundleName, Module module) {
            Logger result = findLogger(name);
            if (result == null) {
                Logger newLogger = new Logger(name, resourceBundleName, module, getOwner(), true);
                do {
                    if (addLocalLogger(newLogger)) {
                        result = newLogger;
                    } else {
                        result = findLogger(name);
                    }
                } while (result == null);
            }
            return result;
        }
    }

    private void loadLoggerHandlers(final Logger logger, final String name, final String handlersPropertyName) {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

            @Override
            public Void run() {
                setLoggerHandlers(logger, name, handlersPropertyName, createLoggerHandlers(name, handlersPropertyName));
                return null;
            }
        });
    }

    private void setLoggerHandlers(final Logger logger, final String name, final String handlersPropertyName, List<Handler> handlers) {
        final boolean ensureCloseOnReset = !handlers.isEmpty() && getBooleanProperty(handlersPropertyName + ".ensureCloseOnReset", true);
        int count = 0;
        for (Handler hdl : handlers) {
            logger.addHandler(hdl);
            if (++count == 1 && ensureCloseOnReset) {
                closeOnResetLoggers.addIfAbsent(CloseOnReset.create(logger));
            }
        }
    }

    private List<Handler> createLoggerHandlers(final String name, final String handlersPropertyName) {
        String[] names = parseClassNames(handlersPropertyName);
        List<Handler> handlers = new ArrayList<>(names.length);
        for (String type : names) {
            try {
                @SuppressWarnings("deprecation")
                Object o = ClassLoader.getSystemClassLoader().loadClass(type).newInstance();
                Handler hdl = (Handler) o;
                String levs = getProperty(type + ".level");
                if (levs != null) {
                    Level l = Level.findLevel(levs);
                    if (l != null) {
                        hdl.setLevel(l);
                    } else {
                        System.err.println("Can't set level for " + type);
                    }
                }
                handlers.add(hdl);
            } catch (Exception ex) {
                System.err.println("Can't load log handler \"" + type + "\"");
                System.err.println("" + ex);
                ex.printStackTrace();
            }
        }
        return handlers;
    }

    private final ReferenceQueue<Logger> loggerRefQueue = new ReferenceQueue<>();

    final class LoggerWeakRef extends WeakReference<Logger> {

        private String name;

        private LogNode node;

        private WeakReference<Logger> parentRef;

        private boolean disposed = false;

        LoggerWeakRef(Logger logger) {
            super(logger, loggerRefQueue);
            name = logger.getName();
        }

        void dispose() {
            synchronized (this) {
                if (disposed)
                    return;
                disposed = true;
            }
            final LogNode n = node;
            if (n != null) {
                synchronized (n.context) {
                    n.context.removeLoggerRef(name, this);
                    name = null;
                    if (n.loggerRef == this) {
                        n.loggerRef = null;
                    }
                    node = null;
                }
            }
            if (parentRef != null) {
                Logger parent = parentRef.get();
                if (parent != null) {
                    parent.removeChildLogger(this);
                }
                parentRef = null;
            }
        }

        void setNode(LogNode node) {
            this.node = node;
        }

        void setParentRef(WeakReference<Logger> parentRef) {
            this.parentRef = parentRef;
        }
    }

    private final static int MAX_ITERATIONS = 400;

    final void drainLoggerRefQueueBounded() {
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            if (loggerRefQueue == null) {
                break;
            }
            LoggerWeakRef ref = (LoggerWeakRef) loggerRefQueue.poll();
            if (ref == null) {
                break;
            }
            ref.dispose();
        }
    }

    public boolean addLogger(Logger logger) {
        final String name = logger.getName();
        if (name == null) {
            throw new NullPointerException();
        }
        drainLoggerRefQueueBounded();
        LoggerContext cx = getUserContext();
        if (cx.addLocalLogger(logger)) {
            loadLoggerHandlers(logger, name, name + ".handlers");
            return true;
        } else {
            return false;
        }
    }

    private static void doSetLevel(final Logger logger, final Level level) {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            logger.setLevel(level);
            return;
        }
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                logger.setLevel(level);
                return null;
            }
        });
    }

    private static void doSetParent(final Logger logger, final Logger parent) {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            logger.setParent(parent);
            return;
        }
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                logger.setParent(parent);
                return null;
            }
        });
    }

    public Logger getLogger(String name) {
        return getUserContext().findLogger(name);
    }

    public Enumeration<String> getLoggerNames() {
        return getUserContext().getLoggerNames();
    }

    public void readConfiguration() throws IOException, SecurityException {
        checkPermission();
        String cname = System.getProperty("java.util.logging.config.class");
        if (cname != null) {
            try {
                try {
                    Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(cname);
                    @SuppressWarnings("deprecation")
                    Object witness = clz.newInstance();
                    return;
                } catch (ClassNotFoundException ex) {
                    Class<?> clz = Thread.currentThread().getContextClassLoader().loadClass(cname);
                    @SuppressWarnings("deprecation")
                    Object witness = clz.newInstance();
                    return;
                }
            } catch (Exception ex) {
                System.err.println("Logging configuration class \"" + cname + "\" failed");
                System.err.println("" + ex);
            }
        }
        String fname = getConfigurationFileName();
        try (final InputStream in = new FileInputStream(fname)) {
            final BufferedInputStream bin = new BufferedInputStream(in);
            readConfiguration(bin);
        }
    }

    String getConfigurationFileName() throws IOException {
        String fname = System.getProperty("java.util.logging.config.file");
        if (fname == null) {
            fname = System.getProperty("java.home");
            if (fname == null) {
                throw new Error("Can't find java.home ??");
            }
            fname = Paths.get(fname, "conf", "logging.properties").toAbsolutePath().normalize().toString();
        }
        return fname;
    }

    public void reset() throws SecurityException {
        checkPermission();
        List<CloseOnReset> persistent;
        configurationLock.lock();
        try {
            props = new Properties();
            persistent = new ArrayList<>(closeOnResetLoggers);
            closeOnResetLoggers.clear();
            if (globalHandlersState != STATE_SHUTDOWN && globalHandlersState != STATE_READING_CONFIG) {
                globalHandlersState = STATE_INITIALIZED;
            }
            for (LoggerContext cx : contexts()) {
                resetLoggerContext(cx);
            }
            persistent.clear();
        } finally {
            configurationLock.unlock();
        }
    }

    private void resetLoggerContext(LoggerContext cx) {
        Enumeration<String> enum_ = cx.getLoggerNames();
        while (enum_.hasMoreElements()) {
            String name = enum_.nextElement();
            Logger logger = cx.findLogger(name);
            if (logger != null) {
                resetLogger(logger);
            }
        }
    }

    private void closeHandlers(Logger logger) {
        Handler[] targets = logger.getHandlers();
        for (Handler h : targets) {
            logger.removeHandler(h);
            try {
                h.close();
            } catch (Exception ex) {
            } catch (Error e) {
                if (globalHandlersState != STATE_SHUTDOWN) {
                    throw e;
                }
            }
        }
    }

    private void resetLogger(Logger logger) {
        closeHandlers(logger);
        String name = logger.getName();
        if (name != null && name.equals("")) {
            logger.setLevel(defaultLevel);
        } else {
            logger.setLevel(null);
        }
    }

    private String[] parseClassNames(String propertyName) {
        String hands = getProperty(propertyName);
        if (hands == null) {
            return new String[0];
        }
        hands = hands.trim();
        int ix = 0;
        final List<String> result = new ArrayList<>();
        while (ix < hands.length()) {
            int end = ix;
            while (end < hands.length()) {
                if (Character.isWhitespace(hands.charAt(end))) {
                    break;
                }
                if (hands.charAt(end) == ',') {
                    break;
                }
                end++;
            }
            String word = hands.substring(ix, end);
            ix = end + 1;
            word = word.trim();
            if (word.length() == 0) {
                continue;
            }
            result.add(word);
        }
        return result.toArray(new String[result.size()]);
    }

    public void readConfiguration(InputStream ins) throws IOException, SecurityException {
        checkPermission();
        configurationLock.lock();
        try {
            if (globalHandlersState == STATE_SHUTDOWN) {
                return;
            }
            globalHandlersState = STATE_READING_CONFIG;
            try {
                reset();
                try {
                    props.load(ins);
                } catch (IllegalArgumentException x) {
                    throw new IOException(x.getMessage(), x);
                }
                String[] names = parseClassNames("config");
                for (String word : names) {
                    try {
                        Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(word);
                        @SuppressWarnings("deprecation")
                        Object witness = clz.newInstance();
                    } catch (Exception ex) {
                        System.err.println("Can't load config class \"" + word + "\"");
                        System.err.println("" + ex);
                    }
                }
                setLevelsOnExistingLoggers();
                globalHandlersState = STATE_UNINITIALIZED;
            } catch (Throwable t) {
                globalHandlersState = STATE_INITIALIZED;
                throw t;
            }
        } finally {
            configurationLock.unlock();
        }
        invokeConfigurationListeners();
    }

    static enum ConfigProperty {

        LEVEL(".level"), HANDLERS(".handlers"), USEPARENT(".useParentHandlers");

        final String suffix;

        final int length;

        private ConfigProperty(String suffix) {
            this.suffix = Objects.requireNonNull(suffix);
            length = suffix.length();
        }

        public boolean handleKey(String key) {
            if (this == HANDLERS && suffix.substring(1).equals(key))
                return true;
            if (this == HANDLERS && suffix.equals(key))
                return false;
            return key.endsWith(suffix);
        }

        String key(String loggerName) {
            if (this == HANDLERS && (loggerName == null || loggerName.isEmpty())) {
                return suffix.substring(1);
            }
            return loggerName + suffix;
        }

        String loggerName(String key) {
            assert key.equals(suffix.substring(1)) && this == HANDLERS || key.endsWith(suffix);
            if (this == HANDLERS && suffix.substring(1).equals(key))
                return "";
            return key.substring(0, key.length() - length);
        }

        static String getLoggerName(String property) {
            for (ConfigProperty p : ConfigProperty.ALL) {
                if (p.handleKey(property)) {
                    return p.loggerName(property);
                }
            }
            return null;
        }

        static Optional<ConfigProperty> find(String property) {
            return ConfigProperty.ALL.stream().filter(p -> p.handleKey(property)).findFirst();
        }

        static boolean matches(String property) {
            return find(property).isPresent();
        }

        static boolean needsUpdating(String k, Properties previous, Properties next) {
            final String p = trim(previous.getProperty(k, null));
            final String n = trim(next.getProperty(k, null));
            return !Objects.equals(p, n);
        }

        static void merge(String k, Properties previous, Properties next, BiFunction<String, String, String> mappingFunction) {
            String p = trim(previous.getProperty(k, null));
            String n = trim(next.getProperty(k, null));
            String mapped = trim(mappingFunction.apply(p, n));
            if (!Objects.equals(n, mapped)) {
                if (mapped == null) {
                    next.remove(k);
                } else {
                    next.setProperty(k, mapped);
                }
            }
        }

        private static final EnumSet<ConfigProperty> ALL = EnumSet.allOf(ConfigProperty.class);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    static final class VisitedLoggers implements Predicate<Logger> {

        final IdentityHashMap<Logger, Boolean> visited;

        private VisitedLoggers(IdentityHashMap<Logger, Boolean> visited) {
            this.visited = visited;
        }

        VisitedLoggers() {
            this(new IdentityHashMap<>());
        }

        @Override
        public boolean test(Logger logger) {
            return visited != null && visited.put(logger, Boolean.TRUE) != null;
        }

        public void clear() {
            if (visited != null)
                visited.clear();
        }

        static final VisitedLoggers NEVER = new VisitedLoggers(null);
    }

    static enum ModType {

        SAME, ADDED, CHANGED, REMOVED;

        static ModType of(String previous, String next) {
            if (previous == null && next != null) {
                return ADDED;
            }
            if (next == null && previous != null) {
                return REMOVED;
            }
            if (!Objects.equals(trim(previous), trim(next))) {
                return CHANGED;
            }
            return SAME;
        }
    }

    public void updateConfiguration(Function<String, BiFunction<String, String, String>> mapper) throws IOException {
        checkPermission();
        ensureLogManagerInitialized();
        drainLoggerRefQueueBounded();
        String fname = getConfigurationFileName();
        try (final InputStream in = new FileInputStream(fname)) {
            final BufferedInputStream bin = new BufferedInputStream(in);
            updateConfiguration(bin, mapper);
        }
    }

    public void updateConfiguration(InputStream ins, Function<String, BiFunction<String, String, String>> mapper) throws IOException {
        checkPermission();
        ensureLogManagerInitialized();
        drainLoggerRefQueueBounded();
        final Properties previous;
        final Set<String> updatePropertyNames;
        List<LoggerContext> cxs = Collections.emptyList();
        final VisitedLoggers visited = new VisitedLoggers();
        final Properties next = new Properties();
        try {
            next.load(ins);
        } catch (IllegalArgumentException x) {
            throw new IOException(x.getMessage(), x);
        }
        if (globalHandlersState == STATE_SHUTDOWN)
            return;
        configurationLock.lock();
        try {
            if (globalHandlersState == STATE_SHUTDOWN)
                return;
            previous = props;
            updatePropertyNames = Stream.concat(previous.stringPropertyNames().stream(), next.stringPropertyNames().stream()).collect(Collectors.toCollection(TreeSet::new));
            if (mapper != null) {
                updatePropertyNames.stream().forEachOrdered(k -> ConfigProperty.merge(k, previous, next, Objects.requireNonNull(mapper.apply(k))));
            }
            props = next;
            final Stream<String> allKeys = updatePropertyNames.stream().filter(ConfigProperty::matches).filter(k -> ConfigProperty.needsUpdating(k, previous, next));
            final Map<String, TreeSet<String>> loggerConfigs = allKeys.collect(Collectors.groupingBy(ConfigProperty::getLoggerName, TreeMap::new, Collectors.toCollection(TreeSet::new)));
            if (!loggerConfigs.isEmpty()) {
                cxs = contexts();
            }
            final List<Logger> loggers = cxs.isEmpty() ? Collections.emptyList() : new ArrayList<>(cxs.size());
            for (Map.Entry<String, TreeSet<String>> e : loggerConfigs.entrySet()) {
                final String name = e.getKey();
                final Set<String> properties = e.getValue();
                loggers.clear();
                for (LoggerContext cx : cxs) {
                    Logger l = cx.findLogger(name);
                    if (l != null && !visited.test(l)) {
                        loggers.add(l);
                    }
                }
                if (loggers.isEmpty())
                    continue;
                for (String pk : properties) {
                    ConfigProperty cp = ConfigProperty.find(pk).get();
                    String p = previous.getProperty(pk, null);
                    String n = next.getProperty(pk, null);
                    ModType mod = ModType.of(p, n);
                    if (mod == ModType.SAME)
                        continue;
                    switch(cp) {
                        case LEVEL:
                            if (mod == ModType.REMOVED)
                                continue;
                            Level level = Level.findLevel(trim(n));
                            if (level != null) {
                                if (name.isEmpty()) {
                                    rootLogger.setLevel(level);
                                }
                                for (Logger l : loggers) {
                                    if (!name.isEmpty() || l != rootLogger) {
                                        l.setLevel(level);
                                    }
                                }
                            }
                            break;
                        case USEPARENT:
                            if (!name.isEmpty()) {
                                boolean useParent = getBooleanProperty(pk, true);
                                if (n != null || p != null) {
                                    for (Logger l : loggers) {
                                        l.setUseParentHandlers(useParent);
                                    }
                                }
                            }
                            break;
                        case HANDLERS:
                            List<Handler> hdls = null;
                            if (name.isEmpty()) {
                                globalHandlersState = STATE_READING_CONFIG;
                                try {
                                    closeHandlers(rootLogger);
                                    globalHandlersState = STATE_UNINITIALIZED;
                                } catch (Throwable t) {
                                    globalHandlersState = STATE_INITIALIZED;
                                    throw t;
                                }
                            }
                            for (Logger l : loggers) {
                                if (l == rootLogger)
                                    continue;
                                closeHandlers(l);
                                if (mod == ModType.REMOVED) {
                                    closeOnResetLoggers.removeIf(c -> c.logger == l);
                                    continue;
                                }
                                if (hdls == null) {
                                    hdls = name.isEmpty() ? Arrays.asList(rootLogger.getHandlers()) : createLoggerHandlers(name, pk);
                                }
                                setLoggerHandlers(l, name, pk, hdls);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        } finally {
            configurationLock.unlock();
            visited.clear();
        }
        drainLoggerRefQueueBounded();
        for (LoggerContext cx : cxs) {
            for (Enumeration<String> names = cx.getLoggerNames(); names.hasMoreElements(); ) {
                String name = names.nextElement();
                if (name.isEmpty())
                    continue;
                Logger l = cx.findLogger(name);
                if (l != null && !visited.test(l)) {
                    cx.processParentHandlers(l, name, visited);
                }
            }
        }
        invokeConfigurationListeners();
    }

    public String getProperty(String name) {
        return props.getProperty(name);
    }

    String getStringProperty(String name, String defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        return val.trim();
    }

    int getIntProperty(String name, int defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    long getLongProperty(String name, long defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(val.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    boolean getBooleanProperty(String name, boolean defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        val = val.toLowerCase();
        if (val.equals("true") || val.equals("1")) {
            return true;
        } else if (val.equals("false") || val.equals("0")) {
            return false;
        }
        return defaultValue;
    }

    Level getLevelProperty(String name, Level defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        Level l = Level.findLevel(val.trim());
        return l != null ? l : defaultValue;
    }

    Filter getFilterProperty(String name, Filter defaultValue) {
        String val = getProperty(name);
        try {
            if (val != null) {
                @SuppressWarnings("deprecation")
                Object o = ClassLoader.getSystemClassLoader().loadClass(val).newInstance();
                return (Filter) o;
            }
        } catch (Exception ex) {
        }
        return defaultValue;
    }

    Formatter getFormatterProperty(String name, Formatter defaultValue) {
        String val = getProperty(name);
        try {
            if (val != null) {
                @SuppressWarnings("deprecation")
                Object o = ClassLoader.getSystemClassLoader().loadClass(val).newInstance();
                return (Formatter) o;
            }
        } catch (Exception ex) {
        }
        return defaultValue;
    }

    private void initializeGlobalHandlers() {
        int state = globalHandlersState;
        if (state == STATE_INITIALIZED || state == STATE_SHUTDOWN) {
            return;
        }
        configurationLock.lock();
        try {
            if (globalHandlersState != STATE_UNINITIALIZED) {
                return;
            }
            globalHandlersState = STATE_INITIALIZING;
            try {
                loadLoggerHandlers(rootLogger, null, "handlers");
            } finally {
                globalHandlersState = STATE_INITIALIZED;
            }
        } finally {
            configurationLock.unlock();
        }
    }

    static final Permission controlPermission = new LoggingPermission("control", null);

    void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(controlPermission);
    }

    public void checkAccess() throws SecurityException {
        checkPermission();
    }

    private static class LogNode {

        HashMap<String, LogNode> children;

        LoggerWeakRef loggerRef;

        LogNode parent;

        final LoggerContext context;

        LogNode(LogNode parent, LoggerContext context) {
            this.parent = parent;
            this.context = context;
        }

        void walkAndSetParent(Logger parent) {
            if (children == null) {
                return;
            }
            for (LogNode node : children.values()) {
                LoggerWeakRef ref = node.loggerRef;
                Logger logger = (ref == null) ? null : ref.get();
                if (logger == null) {
                    node.walkAndSetParent(parent);
                } else {
                    doSetParent(logger, parent);
                }
            }
        }
    }

    private final class RootLogger extends Logger {

        private RootLogger() {
            super("", null, null, LogManager.this, true);
        }

        @Override
        public void log(LogRecord record) {
            initializeGlobalHandlers();
            super.log(record);
        }

        @Override
        public void addHandler(Handler h) {
            initializeGlobalHandlers();
            super.addHandler(h);
        }

        @Override
        public void removeHandler(Handler h) {
            initializeGlobalHandlers();
            super.removeHandler(h);
        }

        @Override
        Handler[] accessCheckedHandlers() {
            initializeGlobalHandlers();
            return super.accessCheckedHandlers();
        }
    }

    private void setLevelsOnExistingLoggers() {
        Enumeration<?> enum_ = props.propertyNames();
        while (enum_.hasMoreElements()) {
            String key = (String) enum_.nextElement();
            if (!key.endsWith(".level")) {
                continue;
            }
            int ix = key.length() - 6;
            String name = key.substring(0, ix);
            Level level = getLevelProperty(key, null);
            if (level == null) {
                System.err.println("Bad level value for property: " + key);
                continue;
            }
            for (LoggerContext cx : contexts()) {
                Logger l = cx.findLogger(name);
                if (l == null) {
                    continue;
                }
                l.setLevel(level);
            }
        }
    }

    public final static String LOGGING_MXBEAN_NAME = "java.util.logging:type=Logging";

    @Deprecated(since = "9")
    public static synchronized LoggingMXBean getLoggingMXBean() {
        return Logging.getInstance();
    }

    public LogManager addConfigurationListener(Runnable listener) {
        final Runnable r = Objects.requireNonNull(listener);
        checkPermission();
        final SecurityManager sm = System.getSecurityManager();
        final AccessControlContext acc = sm == null ? null : AccessController.getContext();
        final PrivilegedAction<Void> pa = acc == null ? null : () -> {
            r.run();
            return null;
        };
        final Runnable pr = acc == null ? r : () -> AccessController.doPrivileged(pa, acc);
        listeners.putIfAbsent(r, pr);
        return this;
    }

    public void removeConfigurationListener(Runnable listener) {
        final Runnable key = Objects.requireNonNull(listener);
        checkPermission();
        listeners.remove(key);
    }

    private void invokeConfigurationListeners() {
        Throwable t = null;
        for (Runnable c : listeners.values().toArray(new Runnable[0])) {
            try {
                c.run();
            } catch (ThreadDeath death) {
                throw death;
            } catch (Error | RuntimeException x) {
                if (t == null)
                    t = x;
                else
                    t.addSuppressed(x);
            }
        }
        if (t instanceof Error)
            throw (Error) t;
        if (t instanceof RuntimeException)
            throw (RuntimeException) t;
    }

    private static final class LoggingProviderAccess implements LoggingProviderImpl.LogManagerAccess, PrivilegedAction<Void> {

        private LoggingProviderAccess() {
        }

        @Override
        public Logger demandLoggerFor(LogManager manager, String name, Module module) {
            if (manager != getLogManager()) {
                throw new IllegalArgumentException("manager");
            }
            Objects.requireNonNull(name);
            Objects.requireNonNull(module);
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(controlPermission);
            }
            if (isSystem(module)) {
                return manager.demandSystemLogger(name, Logger.SYSTEM_LOGGER_RB_NAME, module);
            } else {
                return manager.demandLogger(name, null, module);
            }
        }

        @Override
        public Void run() {
            LoggingProviderImpl.setLogManagerAccess(INSTANCE);
            return null;
        }

        static final LoggingProviderAccess INSTANCE = new LoggingProviderAccess();
    }

    static {
        AccessController.doPrivileged(LoggingProviderAccess.INSTANCE, null, controlPermission);
    }
}