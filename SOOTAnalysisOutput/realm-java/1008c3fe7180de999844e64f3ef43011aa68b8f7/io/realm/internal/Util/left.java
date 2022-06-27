package io.realm.internal;

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

    public enum Testcase {

        Exception_ClassNotFound(0),
        Exception_NoSuchField(1),
        Exception_NoSuchMethod(2),
        Exception_IllegalArgument(3),
        Exception_IOFailed(4),
        Exception_FileNotFound(5),
        Exception_FileAccessError(6),
        Exception_IndexOutOfBounds(7),
        Exception_TableInvalid(8),
        Exception_UnsupportedOperation(9),
        Exception_OutOfMemory(10),
        Exception_FatalError(11),
        Exception_RuntimeError(12),
        Exception_RowInvalid(13),
        Exception_EncryptionNotSupported(14),
        Exception_BadVersion(15);

        private final int nativeTestcase;

        Testcase(int nativeValue) {
            this.nativeTestcase = nativeValue;
        }

        public String expectedResult(long parm1) {
            return nativeTestcase(nativeTestcase, false, parm1);
        }

        public String execute(long parm1) {
            return nativeTestcase(nativeTestcase, true, parm1);
        }
    }

    static native String nativeTestcase(int testcase, boolean dotest, long parm1);

    public static Class<? extends RealmObject> getOriginalModelClass(Class<? extends RealmObject> clazz) {
        @SuppressWarnings("unchecked")
        Class<? extends RealmObject> superclass = (Class<? extends RealmObject>) clazz.getSuperclass();
        if (!superclass.equals(RealmObject.class)) {
            clazz = superclass;
        }
        return clazz;
    }
}
