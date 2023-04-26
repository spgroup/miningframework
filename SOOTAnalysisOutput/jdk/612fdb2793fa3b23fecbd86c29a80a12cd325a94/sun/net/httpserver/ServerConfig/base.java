package sun.net.httpserver;

import com.sun.net.httpserver.*;
import com.sun.net.httpserver.spi.*;

class ServerConfig {

    static int clockTick;

    static int defaultClockTick = 10000;

    static long defaultReadTimeout = 20;

    static long defaultWriteTimeout = 60;

    static long defaultIdleInterval = 300;

    static long defaultSelCacheTimeout = 120;

    static int defaultMaxIdleConnections = 200;

    static long defaultDrainAmount = 64 * 1024;

    static long readTimeout;

    static long writeTimeout;

    static long idleInterval;

    static long selCacheTimeout;

    static long drainAmount;

    static int maxIdleConnections;

    static boolean debug = false;

    static {
        idleInterval = ((Long) java.security.AccessController.doPrivileged(new sun.security.action.GetLongAction("sun.net.httpserver.idleInterval", defaultIdleInterval))).longValue() * 1000;
        clockTick = ((Integer) java.security.AccessController.doPrivileged(new sun.security.action.GetIntegerAction("sun.net.httpserver.clockTick", defaultClockTick))).intValue();
        maxIdleConnections = ((Integer) java.security.AccessController.doPrivileged(new sun.security.action.GetIntegerAction("sun.net.httpserver.maxIdleConnections", defaultMaxIdleConnections))).intValue();
        readTimeout = ((Long) java.security.AccessController.doPrivileged(new sun.security.action.GetLongAction("sun.net.httpserver.readTimeout", defaultReadTimeout))).longValue() * 1000;
        selCacheTimeout = ((Long) java.security.AccessController.doPrivileged(new sun.security.action.GetLongAction("sun.net.httpserver.selCacheTimeout", defaultSelCacheTimeout))).longValue() * 1000;
        writeTimeout = ((Long) java.security.AccessController.doPrivileged(new sun.security.action.GetLongAction("sun.net.httpserver.writeTimeout", defaultWriteTimeout))).longValue() * 1000;
        drainAmount = ((Long) java.security.AccessController.doPrivileged(new sun.security.action.GetLongAction("sun.net.httpserver.drainAmount", defaultDrainAmount))).longValue();
        debug = ((Boolean) java.security.AccessController.doPrivileged(new sun.security.action.GetBooleanAction("sun.net.httpserver.debug"))).booleanValue();
    }

    static long getReadTimeout() {
        return readTimeout;
    }

    static long getSelCacheTimeout() {
        return selCacheTimeout;
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

    static long getWriteTimeout() {
        return writeTimeout;
    }

    static long getDrainAmount() {
        return drainAmount;
    }
}
