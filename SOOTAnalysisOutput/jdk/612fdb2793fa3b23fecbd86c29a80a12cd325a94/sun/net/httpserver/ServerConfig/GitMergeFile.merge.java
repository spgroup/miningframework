package sun.net.httpserver;

import com.sun.net.httpserver.*;
import com.sun.net.httpserver.spi.*;
import java.util.logging.Logger;
import java.security.PrivilegedAction;

class ServerConfig {

    static int clockTick;

    static final int DEFAULT_CLOCK_TICK = 10000;

<<<<<<< MINE
    static final long DEFAULT_IDLE_INTERVAL = 30;
=======
    static final long DEFAULT_IDLE_INTERVAL = 300;
>>>>>>> YOURS

    static final int DEFAULT_MAX_IDLE_CONNECTIONS = 200;

    static final long DEFAULT_MAX_REQ_TIME = -1;

    static final long DEFAULT_MAX_RSP_TIME = -1;

    static final long DEFAULT_TIMER_MILLIS = 1000;

    static final long DEFAULT_DRAIN_AMOUNT = 64 * 1024;

    static long idleInterval;

    static long drainAmount;

    static int maxIdleConnections;

    static long maxReqTime;

    static long maxRspTime;

    static long timerMillis;

    static boolean debug = false;

    static {
        idleInterval = ((Long) java.security.AccessController.doPrivileged(new sun.security.action.GetLongAction("sun.net.httpserver.idleInterval", DEFAULT_IDLE_INTERVAL))).longValue() * 1000;
        clockTick = ((Integer) java.security.AccessController.doPrivileged(new sun.security.action.GetIntegerAction("sun.net.httpserver.clockTick", DEFAULT_CLOCK_TICK))).intValue();
        maxIdleConnections = ((Integer) java.security.AccessController.doPrivileged(new sun.security.action.GetIntegerAction("sun.net.httpserver.maxIdleConnections", DEFAULT_MAX_IDLE_CONNECTIONS))).intValue();
        drainAmount = ((Long) java.security.AccessController.doPrivileged(new sun.security.action.GetLongAction("sun.net.httpserver.drainAmount", DEFAULT_DRAIN_AMOUNT))).longValue();
        maxReqTime = ((Long) java.security.AccessController.doPrivileged(new sun.security.action.GetLongAction("sun.net.httpserver.maxReqTime", DEFAULT_MAX_REQ_TIME))).longValue();
        maxRspTime = ((Long) java.security.AccessController.doPrivileged(new sun.security.action.GetLongAction("sun.net.httpserver.maxRspTime", DEFAULT_MAX_RSP_TIME))).longValue();
        timerMillis = ((Long) java.security.AccessController.doPrivileged(new sun.security.action.GetLongAction("sun.net.httpserver.timerMillis", DEFAULT_TIMER_MILLIS))).longValue();
        debug = ((Boolean) java.security.AccessController.doPrivileged(new sun.security.action.GetBooleanAction("sun.net.httpserver.debug"))).booleanValue();
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

    static long getMaxReqTime() {
        return maxReqTime;
    }

    static long getMaxRspTime() {
        return maxRspTime;
    }

    static long getTimerMillis() {
        return timerMillis;
    }
}
