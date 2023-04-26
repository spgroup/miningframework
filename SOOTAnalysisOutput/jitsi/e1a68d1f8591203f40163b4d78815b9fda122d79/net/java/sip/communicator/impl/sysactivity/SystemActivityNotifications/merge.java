package net.java.sip.communicator.impl.sysactivity;

import net.java.sip.communicator.util.Logger;
import org.jitsi.util.*;

public class SystemActivityNotifications {

    private static final Logger logger = Logger.getLogger(SystemActivityNotifications.class);

    public static final int NOTIFY_DISPLAY_SLEEP = 2;

    public static final int NOTIFY_DISPLAY_WAKE = 3;

    public static final int NOTIFY_DNS_CHANGE = 10;

    public static final int NOTIFY_ENDSESSION = 12;

    public static final int NOTIFY_NETWORK_CHANGE = 9;

    public static final int NOTIFY_QUERY_ENDSESSION = 11;

    public static final int NOTIFY_SCREEN_LOCKED = 7;

    public static final int NOTIFY_SCREEN_UNLOCKED = 8;

    public static final int NOTIFY_SCREENSAVER_START = 4;

    public static final int NOTIFY_SCREENSAVER_STOP = 6;

    public static final int NOTIFY_SCREENSAVER_WILL_STOP = 5;

    public static final int NOTIFY_SLEEP = 0;

    public static final int NOTIFY_WAKE = 1;

    private static long ptr;

    static {
        try {
            if (!org.jitsi.util.OSUtils.IS_ANDROID) {
                JNIUtils.loadLibrary("sysactivitynotifications", SystemActivityNotifications.class.getClassLoader());
                ptr = allocAndInit();
                if (ptr == -1)
                    ptr = 0;
            }
        } catch (Throwable t) {
            if (t instanceof ThreadDeath)
                throw (ThreadDeath) t;
            else
                logger.warn("Failed to initialize native counterpart", t);
        }
    }

    private static native long allocAndInit();

    public static native long getLastInput();

    public static boolean isLoaded() {
        return (ptr != 0);
    }

    private static native void release(long ptr);

    public static native void setDelegate(long ptr, NotificationsDelegate delegate);

    public static void setDelegate(NotificationsDelegate delegate) {
        if (ptr != 0)
            setDelegate(ptr, delegate);
    }

    public static void start() {
        if (ptr != 0)
            start(ptr);
    }

    private static native void start(long ptr);

    public static void stop() {
        if (ptr != 0) {
            stop(ptr);
            release(ptr);
            ptr = 0;
        }
    }

    private static native void stop(long ptr);

    public interface NotificationsDelegate {

        public void notify(int type);

        public void notifyNetworkChange(int family, long luidIndex, String name, long type, boolean connected);
    }
}
