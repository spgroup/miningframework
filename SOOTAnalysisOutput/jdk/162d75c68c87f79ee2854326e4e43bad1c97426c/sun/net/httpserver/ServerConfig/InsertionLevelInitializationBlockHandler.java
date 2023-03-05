package sun.net.httpserver;

import java.util.logging.Logger;
import java.security.PrivilegedAction;

class ServerConfig {

    private static final int DEFAULT_CLOCK_TICK = 10000;

    private static final long DEFAULT_IDLE_INTERVAL = 30;

    private static final int DEFAULT_MAX_IDLE_CONNECTIONS = 200;

    private static final long DEFAULT_MAX_REQ_TIME = -1;

    private static final long DEFAULT_MAX_RSP_TIME = -1;

    private static final long DEFAULT_TIMER_MILLIS = 1000;

    private static final int DEFAULT_MAX_REQ_HEADERS = 200;

    private static final long DEFAULT_DRAIN_AMOUNT = 64 * 1024;

    private static int clockTick;

    private static long idleInterval;

    private static long drainAmount;

    private static int maxIdleConnections;

    private static int maxReqHeaders;

    private static long maxReqTime;

    private static long maxRspTime;

    private static long timerMillis;

<<<<<<< MINE
static boolean debug;
=======
private static boolean debug;
>>>>>>> YOURS


<<<<<<< MINE
static boolean noDelay;
=======
private static boolean noDelay;
>>>>>>> YOURS


    static {<<<<<<< MINE

        java.security.AccessController.doPrivileged(new PrivilegedAction<Void>() {

            @Override
            public Void run() {
                idleInterval = Long.getLong("sun.net.httpserver.idleInterval", DEFAULT_IDLE_INTERVAL) * 1000;
                clockTick = Integer.getInteger("sun.net.httpserver.clockTick", DEFAULT_CLOCK_TICK);
                maxIdleConnections = Integer.getInteger("sun.net.httpserver.maxIdleConnections", DEFAULT_MAX_IDLE_CONNECTIONS);
                drainAmount = Long.getLong("sun.net.httpserver.drainAmount", DEFAULT_DRAIN_AMOUNT);
                maxReqTime = Long.getLong("sun.net.httpserver.maxReqTime", DEFAULT_MAX_REQ_TIME);
                maxRspTime = Long.getLong("sun.net.httpserver.maxRspTime", DEFAULT_MAX_RSP_TIME);
                timerMillis = Long.getLong("sun.net.httpserver.timerMillis", DEFAULT_TIMER_MILLIS);
                debug = Boolean.getBoolean("sun.net.httpserver.debug");
                noDelay = Boolean.getBoolean("sun.net.httpserver.nodelay");
                return null;
            }
        });
    
=======

        java.security.AccessController.doPrivileged(new PrivilegedAction<Void>() {

            @Override
            public Void run() {
                idleInterval = Long.getLong("sun.net.httpserver.idleInterval", DEFAULT_IDLE_INTERVAL) * 1000;
                clockTick = Integer.getInteger("sun.net.httpserver.clockTick", DEFAULT_CLOCK_TICK);
                maxIdleConnections = Integer.getInteger("sun.net.httpserver.maxIdleConnections", DEFAULT_MAX_IDLE_CONNECTIONS);
                drainAmount = Long.getLong("sun.net.httpserver.drainAmount", DEFAULT_DRAIN_AMOUNT);
                maxReqHeaders = Integer.getInteger("sun.net.httpserver.maxReqHeaders", DEFAULT_MAX_REQ_HEADERS);
                maxReqTime = Long.getLong("sun.net.httpserver.maxReqTime", DEFAULT_MAX_REQ_TIME);
                maxRspTime = Long.getLong("sun.net.httpserver.maxRspTime", DEFAULT_MAX_RSP_TIME);
                timerMillis = Long.getLong("sun.net.httpserver.timerMillis", DEFAULT_TIMER_MILLIS);
                debug = Boolean.getBoolean("sun.net.httpserver.debug");
                noDelay = Boolean.getBoolean("sun.net.httpserver.nodelay");
                return null;
            }
        });
    
>>>>>>> YOURS
}

    static void checkLegacyProperties(final Logger logger) {
        java.security.AccessController.doPrivileged(new PrivilegedAction<Void>() {

            public Void run() {
                if (System.getProperty("sun.net.httpserver.readTimeout") != null) {
                    logger.warning("sun.net.httpserver.readTimeout " + "property is no longer used. " + "Use sun.net.httpserver.maxReqTime instead.");
                }
                if (System.getProperty("sun.net.httpserver.writeTimeout") != null) {
                    logger.warning("sun.net.httpserver.writeTimeout " + "property is no longer used. Use " + "sun.net.httpserver.maxRspTime instead.");
                }
                if (System.getProperty("sun.net.httpserver.selCacheTimeout") != null) {
                    logger.warning("sun.net.httpserver.selCacheTimeout " + "property is no longer used.");
                }
                return null;
            }
        });
    }

    static boolean debugEnabled() {
        return debug;
    }

    static long getIdleInterval() {
        return idleInterval;
    }

    static int getClockTick() {
        return clockTick;
    }

    static int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    static long getDrainAmount() {
        return drainAmount;
    }

    static int getMaxReqHeaders() {
        return maxReqHeaders;
    }

    static long getMaxReqTime() {
        return maxReqTime;
    }

    static long getMaxRspTime() {
        return maxRspTime;
    }

    static long getTimerMillis() {
        return timerMillis;
    }

    static boolean noDelay() {
        return noDelay;
    }
}