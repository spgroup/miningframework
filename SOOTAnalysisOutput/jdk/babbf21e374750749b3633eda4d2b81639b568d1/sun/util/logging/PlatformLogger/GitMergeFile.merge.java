package sun.util.logging;

import java.lang.ref.WeakReference;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import sun.misc.JavaLangAccess;
import sun.misc.SharedSecrets;

public class PlatformLogger {

    public static final int OFF = Integer.MAX_VALUE;

    public static final int SEVERE = 1000;

    public static final int WARNING = 900;

    public static final int INFO = 800;

    public static final int CONFIG = 700;

    public static final int FINE = 500;

    public static final int FINER = 400;

    public static final int FINEST = 300;

    public static final int ALL = Integer.MIN_VALUE;

    public static enum Level {

        ALL,
        FINEST,
        FINER,
        FINE,
        CONFIG,
        INFO,
        WARNING,
        SEVERE,
        OFF;

        Object javaLevel;

        private static final int[] levelValues = new int[] { PlatformLogger.ALL, PlatformLogger.FINEST, PlatformLogger.FINER, PlatformLogger.FINE, PlatformLogger.CONFIG, PlatformLogger.INFO, PlatformLogger.WARNING, PlatformLogger.SEVERE, PlatformLogger.OFF };

        public int intValue() {
            return levelValues[this.ordinal()];
        }

        static Level valueOf(int level) {
            switch(level) {
                case PlatformLogger.FINEST:
                    return Level.FINEST;
                case PlatformLogger.FINE:
                    return Level.FINE;
                case PlatformLogger.FINER:
                    return Level.FINER;
                case PlatformLogger.INFO:
                    return Level.INFO;
                case PlatformLogger.WARNING:
                    return Level.WARNING;
                case PlatformLogger.CONFIG:
                    return Level.CONFIG;
                case PlatformLogger.SEVERE:
                    return Level.SEVERE;
                case PlatformLogger.OFF:
                    return Level.OFF;
                case PlatformLogger.ALL:
                    return Level.ALL;
            }
            int i = Arrays.binarySearch(levelValues, 0, levelValues.length - 2, level);
            return values()[i >= 0 ? i : (-i - 1)];
        }
    }

    private static final Level DEFAULT_LEVEL = Level.INFO;

    private static boolean loggingEnabled;

    static {
        loggingEnabled = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

            public Boolean run() {
                String cname = System.getProperty("java.util.logging.config.class");
                String fname = System.getProperty("java.util.logging.config.file");
                return (cname != null || fname != null);
            }
        });
        try {
            Class.forName("sun.util.logging.PlatformLogger$DefaultLoggerProxy", false, PlatformLogger.class.getClassLoader());
            Class.forName("sun.util.logging.PlatformLogger$JavaLoggerProxy", false, PlatformLogger.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            throw new InternalError(ex);
        }
    }

    private static Map<String, WeakReference<PlatformLogger>> loggers = new HashMap<>();

    public static synchronized PlatformLogger getLogger(String name) {
        PlatformLogger log = null;
        WeakReference<PlatformLogger> ref = loggers.get(name);
        if (ref != null) {
            log = ref.get();
        }
        if (log == null) {
            log = new PlatformLogger(name);
            loggers.put(name, new WeakReference<>(log));
        }
        return log;
    }

    public static synchronized void redirectPlatformLoggers() {
        if (loggingEnabled || !LoggingSupport.isAvailable())
            return;
        loggingEnabled = true;
        for (Map.Entry<String, WeakReference<PlatformLogger>> entry : loggers.entrySet()) {
            WeakReference<PlatformLogger> ref = entry.getValue();
            PlatformLogger plog = ref.get();
            if (plog != null) {
                plog.redirectToJavaLoggerProxy();
            }
        }
    }

    private void redirectToJavaLoggerProxy() {
        DefaultLoggerProxy lp = DefaultLoggerProxy.class.cast(this.loggerProxy);
        JavaLoggerProxy jlp = new JavaLoggerProxy(lp.name, lp.level);
        this.javaLoggerProxy = jlp;
        this.loggerProxy = jlp;
    }

    private volatile LoggerProxy loggerProxy;

    private volatile JavaLoggerProxy javaLoggerProxy;

    private PlatformLogger(String name) {
        if (loggingEnabled) {
            this.loggerProxy = this.javaLoggerProxy = new JavaLoggerProxy(name);
        } else {
            this.loggerProxy = new DefaultLoggerProxy(name);
        }
    }

    public boolean isEnabled() {
        return loggerProxy.isEnabled();
    }

    public String getName() {
        return loggerProxy.name;
    }

    @Deprecated
    public boolean isLoggable(int levelValue) {
        return isLoggable(Level.valueOf(levelValue));
    }

    @Deprecated
    public int getLevel() {
        Level level = loggerProxy.getLevel();
        return level != null ? level.intValue() : 0;
    }

    @Deprecated
    public void setLevel(int newLevel) {
        loggerProxy.setLevel(newLevel == 0 ? null : Level.valueOf(newLevel));
    }

    public boolean isLoggable(Level level) {
        if (level == null) {
            throw new NullPointerException();
        }
        JavaLoggerProxy jlp = javaLoggerProxy;
        return jlp != null ? jlp.isLoggable(level) : loggerProxy.isLoggable(level);
    }

    public Level level() {
        return loggerProxy.getLevel();
    }

    public void setLevel(Level newLevel) {
        loggerProxy.setLevel(newLevel);
    }

    public void severe(String msg) {
        loggerProxy.doLog(Level.SEVERE, msg);
    }

    public void severe(String msg, Throwable t) {
        loggerProxy.doLog(Level.SEVERE, msg, t);
    }

    public void severe(String msg, Object... params) {
        loggerProxy.doLog(Level.SEVERE, msg, params);
    }

    public void warning(String msg) {
        loggerProxy.doLog(Level.WARNING, msg);
    }

    public void warning(String msg, Throwable t) {
        loggerProxy.doLog(Level.WARNING, msg, t);
    }

    public void warning(String msg, Object... params) {
        loggerProxy.doLog(Level.WARNING, msg, params);
    }

    public void info(String msg) {
        loggerProxy.doLog(Level.INFO, msg);
    }

    public void info(String msg, Throwable t) {
        loggerProxy.doLog(Level.INFO, msg, t);
    }

    public void info(String msg, Object... params) {
        loggerProxy.doLog(Level.INFO, msg, params);
    }

    public void config(String msg) {
        loggerProxy.doLog(Level.CONFIG, msg);
    }

    public void config(String msg, Throwable t) {
        loggerProxy.doLog(Level.CONFIG, msg, t);
    }

    public void config(String msg, Object... params) {
        loggerProxy.doLog(Level.CONFIG, msg, params);
    }

    public void fine(String msg) {
        loggerProxy.doLog(Level.FINE, msg);
    }

    public void fine(String msg, Throwable t) {
        loggerProxy.doLog(Level.FINE, msg, t);
    }

    public void fine(String msg, Object... params) {
        loggerProxy.doLog(Level.FINE, msg, params);
    }

    public void finer(String msg) {
        loggerProxy.doLog(Level.FINER, msg);
    }

    public void finer(String msg, Throwable t) {
        loggerProxy.doLog(Level.FINER, msg, t);
    }

    public void finer(String msg, Object... params) {
        loggerProxy.doLog(Level.FINER, msg, params);
    }

    public void finest(String msg) {
        loggerProxy.doLog(Level.FINEST, msg);
    }

    public void finest(String msg, Throwable t) {
        loggerProxy.doLog(Level.FINEST, msg, t);
    }

    public void finest(String msg, Object... params) {
        loggerProxy.doLog(Level.FINEST, msg, params);
    }

    private static abstract class LoggerProxy {

        final String name;

        protected LoggerProxy(String name) {
            this.name = name;
        }

        abstract boolean isEnabled();

        abstract Level getLevel();

        abstract void setLevel(Level newLevel);

        abstract void doLog(Level level, String msg);

        abstract void doLog(Level level, String msg, Throwable thrown);

        abstract void doLog(Level level, String msg, Object... params);

        abstract boolean isLoggable(Level level);
    }

    private static final class DefaultLoggerProxy extends LoggerProxy {

        private static PrintStream outputStream() {
            return System.err;
        }

        volatile Level effectiveLevel;

        volatile Level level;

        DefaultLoggerProxy(String name) {
            super(name);
            this.effectiveLevel = deriveEffectiveLevel(null);
            this.level = null;
        }

        boolean isEnabled() {
            return effectiveLevel != Level.OFF;
        }

        Level getLevel() {
            return level;
        }

        void setLevel(Level newLevel) {
            Level oldLevel = level;
            if (oldLevel != newLevel) {
                level = newLevel;
                effectiveLevel = deriveEffectiveLevel(newLevel);
            }
        }

        void doLog(Level level, String msg) {
            if (isLoggable(level)) {
                outputStream().print(format(level, msg, null));
            }
        }

        void doLog(Level level, String msg, Throwable thrown) {
            if (isLoggable(level)) {
                outputStream().print(format(level, msg, thrown));
            }
        }

        void doLog(Level level, String msg, Object... params) {
            if (isLoggable(level)) {
                String newMsg = formatMessage(msg, params);
                outputStream().print(format(level, newMsg, null));
            }
        }

        boolean isLoggable(Level level) {
            Level effectiveLevel = this.effectiveLevel;
            return level.intValue() >= effectiveLevel.intValue() && effectiveLevel != Level.OFF;
        }

        private Level deriveEffectiveLevel(Level level) {
            return level == null ? DEFAULT_LEVEL : level;
        }

        private String formatMessage(String format, Object... parameters) {
            try {
                if (parameters == null || parameters.length == 0) {
                    return format;
                }
                if (format.indexOf("{0") >= 0 || format.indexOf("{1") >= 0 || format.indexOf("{2") >= 0 || format.indexOf("{3") >= 0) {
                    return java.text.MessageFormat.format(format, parameters);
                }
                return format;
            } catch (Exception ex) {
                return format;
            }
        }

        private static final String formatString = LoggingSupport.getSimpleFormat(false);

        private Date date = new Date();

        private synchronized String format(Level level, String msg, Throwable thrown) {
            date.setTime(System.currentTimeMillis());
            String throwable = "";
            if (thrown != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println();
                thrown.printStackTrace(pw);
                pw.close();
                throwable = sw.toString();
            }
            return String.format(formatString, date, getCallerInfo(), name, level.name(), msg, throwable);
        }

        private String getCallerInfo() {
            String sourceClassName = null;
            String sourceMethodName = null;
            JavaLangAccess access = SharedSecrets.getJavaLangAccess();
            Throwable throwable = new Throwable();
            int depth = access.getStackTraceDepth(throwable);
            String logClassName = "sun.util.logging.PlatformLogger";
            boolean lookingForLogger = true;
            for (int ix = 0; ix < depth; ix++) {
                StackTraceElement frame = access.getStackTraceElement(throwable, ix);
                String cname = frame.getClassName();
                if (lookingForLogger) {
                    if (cname.equals(logClassName)) {
                        lookingForLogger = false;
                    }
                } else {
                    if (!cname.equals(logClassName)) {
                        sourceClassName = cname;
                        sourceMethodName = frame.getMethodName();
                        break;
                    }
                }
            }
            if (sourceClassName != null) {
                return sourceClassName + " " + sourceMethodName;
            } else {
                return name;
            }
        }
    }

    private static final class JavaLoggerProxy extends LoggerProxy {

        static {
            for (Level level : Level.values()) {
                level.javaLevel = LoggingSupport.parseLevel(level.name());
            }
        }

        private final Object javaLogger;

        JavaLoggerProxy(String name) {
            this(name, null);
        }

        JavaLoggerProxy(String name, Level level) {
            super(name);
            this.javaLogger = LoggingSupport.getLogger(name);
            if (level != null) {
                LoggingSupport.setLevel(javaLogger, level.javaLevel);
            }
        }

        void doLog(Level level, String msg) {
            LoggingSupport.log(javaLogger, level.javaLevel, msg);
        }

        void doLog(Level level, String msg, Throwable t) {
            LoggingSupport.log(javaLogger, level.javaLevel, msg, t);
        }

        void doLog(Level level, String msg, Object... params) {
            if (!isLoggable(level)) {
                return;
            }
            int len = (params != null) ? params.length : 0;
            Object[] sparams = new String[len];
            for (int i = 0; i < len; i++) {
                sparams[i] = String.valueOf(params[i]);
            }
            LoggingSupport.log(javaLogger, level.javaLevel, msg, sparams);
        }

        boolean isEnabled() {
            return LoggingSupport.isLoggable(javaLogger, Level.OFF.javaLevel);
        }

        Level getLevel() {
            Object javaLevel = LoggingSupport.getLevel(javaLogger);
            if (javaLevel == null)
                return null;
            try {
                return Level.valueOf(LoggingSupport.getLevelName(javaLevel));
            } catch (IllegalArgumentException e) {
                return Level.valueOf(LoggingSupport.getLevelValue(javaLevel));
            }
        }

        void setLevel(Level level) {
            LoggingSupport.setLevel(javaLogger, level == null ? null : level.javaLevel);
        }

        boolean isLoggable(Level level) {
            return LoggingSupport.isLoggable(javaLogger, level.javaLevel);
        }
    }
}
