package sun.util.logging;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
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

    private static final int defaultLevel = INFO;

    private static boolean loggingEnabled;

    static {
        loggingEnabled = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

            public Boolean run() {
                String cname = System.getProperty("java.util.logging.config.class");
                String fname = System.getProperty("java.util.logging.config.file");
                return (cname != null || fname != null);
            }
        });
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
                plog.newJavaLogger();
            }
        }
    }

    private void newJavaLogger() {
        logger = new JavaLogger(logger.name, logger.effectiveLevel);
    }

    private volatile LoggerProxy logger;

    private PlatformLogger(String name) {
        if (loggingEnabled) {
            this.logger = new JavaLogger(name);
        } else {
            this.logger = new LoggerProxy(name);
        }
    }

    public boolean isEnabled() {
        return logger.isEnabled();
    }

    public String getName() {
        return logger.name;
    }

    public boolean isLoggable(int level) {
        return logger.isLoggable(level);
    }

    public int getLevel() {
        return logger.getLevel();
    }

    public void setLevel(int newLevel) {
        logger.setLevel(newLevel);
    }

    public void severe(String msg) {
        logger.doLog(SEVERE, msg);
    }

    public void severe(String msg, Throwable t) {
        logger.doLog(SEVERE, msg, t);
    }

    public void severe(String msg, Object... params) {
        logger.doLog(SEVERE, msg, params);
    }

    public void warning(String msg) {
        logger.doLog(WARNING, msg);
    }

    public void warning(String msg, Throwable t) {
        logger.doLog(WARNING, msg, t);
    }

    public void warning(String msg, Object... params) {
        logger.doLog(WARNING, msg, params);
    }

    public void info(String msg) {
        logger.doLog(INFO, msg);
    }

    public void info(String msg, Throwable t) {
        logger.doLog(INFO, msg, t);
    }

    public void info(String msg, Object... params) {
        logger.doLog(INFO, msg, params);
    }

    public void config(String msg) {
        logger.doLog(CONFIG, msg);
    }

    public void config(String msg, Throwable t) {
        logger.doLog(CONFIG, msg, t);
    }

    public void config(String msg, Object... params) {
        logger.doLog(CONFIG, msg, params);
    }

    public void fine(String msg) {
        logger.doLog(FINE, msg);
    }

    public void fine(String msg, Throwable t) {
        logger.doLog(FINE, msg, t);
    }

    public void fine(String msg, Object... params) {
        logger.doLog(FINE, msg, params);
    }

    public void finer(String msg) {
        logger.doLog(FINER, msg);
    }

    public void finer(String msg, Throwable t) {
        logger.doLog(FINER, msg, t);
    }

    public void finer(String msg, Object... params) {
        logger.doLog(FINER, msg, params);
    }

    public void finest(String msg) {
        logger.doLog(FINEST, msg);
    }

    public void finest(String msg, Throwable t) {
        logger.doLog(FINEST, msg, t);
    }

    public void finest(String msg, Object... params) {
        logger.doLog(FINEST, msg, params);
    }

    static class LoggerProxy {

        private static final PrintStream defaultStream = System.err;

        final String name;

        volatile int levelValue;

        volatile int effectiveLevel = 0;

        LoggerProxy(String name) {
            this(name, defaultLevel);
        }

        LoggerProxy(String name, int level) {
            this.name = name;
            this.levelValue = level == 0 ? defaultLevel : level;
        }

        boolean isEnabled() {
            return levelValue != OFF;
        }

        int getLevel() {
            return effectiveLevel;
        }

        void setLevel(int newLevel) {
            levelValue = newLevel;
            effectiveLevel = newLevel;
        }

        void doLog(int level, String msg) {
            if (level < levelValue || levelValue == OFF) {
                return;
            }
            defaultStream.print(format(level, msg, null));
        }

        void doLog(int level, String msg, Throwable thrown) {
            if (level < levelValue || levelValue == OFF) {
                return;
            }
            defaultStream.print(format(level, msg, thrown));
        }

        void doLog(int level, String msg, Object... params) {
            if (level < levelValue || levelValue == OFF) {
                return;
            }
            String newMsg = formatMessage(msg, params);
            defaultStream.print(format(level, newMsg, null));
        }

        public boolean isLoggable(int level) {
            if (level < levelValue || levelValue == OFF) {
                return false;
            }
            return true;
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

        private synchronized String format(int level, String msg, Throwable thrown) {
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
            return String.format(formatString, date, getCallerInfo(), name, PlatformLogger.getLevelName(level), msg, throwable);
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

    static class JavaLogger extends LoggerProxy {

        private static final Map<Integer, Object> levelObjects = new HashMap<>();

        static {
            if (LoggingSupport.isAvailable()) {
                getLevelObjects();
            }
        }

        private static void getLevelObjects() {
            int[] levelArray = new int[] { OFF, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL };
            for (int l : levelArray) {
                Object level = LoggingSupport.parseLevel(getLevelName(l));
                levelObjects.put(l, level);
            }
        }

        private final Object javaLogger;

        JavaLogger(String name) {
            this(name, 0);
        }

        JavaLogger(String name, int level) {
            super(name, level);
            this.javaLogger = LoggingSupport.getLogger(name);
            if (level != 0) {
                LoggingSupport.setLevel(javaLogger, levelObjects.get(level));
            }
        }

        void doLog(int level, String msg) {
            LoggingSupport.log(javaLogger, levelObjects.get(level), msg);
        }

        void doLog(int level, String msg, Throwable t) {
            LoggingSupport.log(javaLogger, levelObjects.get(level), msg, t);
        }

        void doLog(int level, String msg, Object... params) {
            if (!isLoggable(level)) {
                return;
            }
            int len = (params != null) ? params.length : 0;
            Object[] sparams = new String[len];
            for (int i = 0; i < len; i++) {
                sparams[i] = String.valueOf(params[i]);
            }
            LoggingSupport.log(javaLogger, levelObjects.get(level), msg, sparams);
        }

        boolean isEnabled() {
            Object level = LoggingSupport.getLevel(javaLogger);
            return level == null || level.equals(levelObjects.get(OFF)) == false;
        }

        int getLevel() {
            Object level = LoggingSupport.getLevel(javaLogger);
            if (level != null) {
                for (Map.Entry<Integer, Object> l : levelObjects.entrySet()) {
                    if (level == l.getValue()) {
                        return l.getKey();
                    }
                }
            }
            return 0;
        }

        void setLevel(int newLevel) {
            levelValue = newLevel;
            LoggingSupport.setLevel(javaLogger, levelObjects.get(newLevel));
        }

        public boolean isLoggable(int level) {
            return LoggingSupport.isLoggable(javaLogger, levelObjects.get(level));
        }
    }

    private static String getLevelName(int level) {
        switch(level) {
            case OFF:
                return "OFF";
            case SEVERE:
                return "SEVERE";
            case WARNING:
                return "WARNING";
            case INFO:
                return "INFO";
            case CONFIG:
                return "CONFIG";
            case FINE:
                return "FINE";
            case FINER:
                return "FINER";
            case FINEST:
                return "FINEST";
            case ALL:
                return "ALL";
            default:
                return "UNKNOWN";
        }
    }
}
