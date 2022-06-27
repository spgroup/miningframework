package io.realm.internal;

import android.os.Build;
import java.io.PrintWriter;
import java.io.StringWriter;
import io.realm.RealmModel;
import io.realm.RealmObject;

public class Util {

    public static long getNativeMemUsage() {
        return nativeGetMemUsage();
    }

    static native long nativeGetMemUsage();

    public static void setDebugLevel(int level) {
        nativeSetDebugLevel(level);
    }

    static native void nativeSetDebugLevel(int level);

    static void javaPrint(String txt) {
        System.out.print(txt);
    }

    public static String getTablePrefix() {
        return nativeGetTablePrefix();
    }

    static native String nativeGetTablePrefix();

    public static Class<? extends RealmModel> getOriginalModelClass(Class<? extends RealmModel> clazz) {
        @SuppressWarnings("unchecked")
        Class<? extends RealmModel> superclass = (Class<? extends RealmModel>) clazz.getSuperclass();
        if (!superclass.equals(Object.class) && !superclass.equals(RealmObject.class)) {
            clazz = superclass;
        }
        return clazz;
    }

    public static long calculateExponentialDelay(int failedAttempts, long maxDelayInMs) {
        double SCALE = 1.0D;
        double delayInMs = ((Math.pow(2.0D, failedAttempts) - 1d) / 2.0D) * 1000 * SCALE;
        return maxDelayInMs < delayInMs ? maxDelayInMs : (long) delayInMs;
    }

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic") || Build.FINGERPRINT.startsWith("unknown") || Build.MODEL.contains("google_sdk") || Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK built for x86") || Build.MANUFACTURER.contains("Genymotion") || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) || "google_sdk".equals(Build.PRODUCT);
    }
}