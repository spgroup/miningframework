package io.realm.internal;

import io.realm.RealmModel;
import io.realm.RealmObject;

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
}
