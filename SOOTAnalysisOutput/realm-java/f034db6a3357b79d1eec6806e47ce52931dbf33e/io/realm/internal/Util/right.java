package io.realm.internal;

import android.os.Build;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.log.RealmLog;

public class Util {

    static {
        RealmCore.loadLibrary();
    }

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

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic") || Build.FINGERPRINT.startsWith("unknown") || Build.MODEL.contains("google_sdk") || Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK built for x86") || Build.MANUFACTURER.contains("Genymotion") || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) || "google_sdk".equals(Build.PRODUCT);
    }

    public static boolean deleteRealm(String canonicalPath, File realmFolder, String realmFileName) {
        boolean realmDeleted = true;
        final String management = ".management";
        File managementFolder = new File(realmFolder, realmFileName + management);
        File[] files = managementFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                realmDeleted = realmDeleted && file.delete();
            }
        }
        realmDeleted = realmDeleted && managementFolder.delete();
        return realmDeleted && deletes(canonicalPath, realmFolder, realmFileName);
    }

    private static boolean deletes(String canonicalPath, File rootFolder, String realmFileName) {
        final AtomicBoolean realmDeleted = new AtomicBoolean(true);
        List<File> filesToDelete = Arrays.asList(new File(rootFolder, realmFileName), new File(rootFolder, realmFileName + ".lock"), new File(rootFolder, realmFileName + ".log_a"), new File(rootFolder, realmFileName + ".log_b"), new File(rootFolder, realmFileName + ".log"), new File(canonicalPath));
        for (File fileToDelete : filesToDelete) {
            if (fileToDelete.exists()) {
                boolean deleteResult = fileToDelete.delete();
                if (!deleteResult) {
                    realmDeleted.set(false);
                    RealmLog.warn("Could not delete the file %s", fileToDelete);
                }
            }
        }
        return realmDeleted.get();
    }
}
