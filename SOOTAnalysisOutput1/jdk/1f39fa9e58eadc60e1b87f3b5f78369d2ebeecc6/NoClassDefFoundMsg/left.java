import jdk.test.lib.compiler.InMemoryJavaCompiler;
import jdk.internal.misc.Unsafe;

public class NoClassDefFoundMsg {

    static native void callDefineClass(String className);

    static native void callFindClass(String className);

    static {
        System.loadLibrary("NoClassDefFoundMsg");
    }

    public static void main(String[] args) throws Exception {
        Unsafe unsafe = Unsafe.getUnsafe();
        byte[] klassbuf = InMemoryJavaCompiler.compile("TestClass", "class TestClass { }");
        StringBuilder tooBigClassName = new StringBuilder("z");
        for (int x = 0; x < 16; x++) {
            tooBigClassName = tooBigClassName.append(tooBigClassName);
        }
        try {
            unsafe.defineClass(tooBigClassName.toString(), klassbuf, 4, klassbuf.length - 4, null, null);
            throw new RuntimeException("defineClass did not throw expected NoClassDefFoundError");
        } catch (NoClassDefFoundError e) {
            if (!e.getMessage().contains("Class name exceeds maximum length of ")) {
                throw new RuntimeException("Wrong NoClassDefFoundError: " + e.getMessage());
            }
        }
        try {
            callDefineClass(tooBigClassName.toString());
            throw new RuntimeException("DefineClass did not throw expected NoClassDefFoundError");
        } catch (NoClassDefFoundError e) {
            if (!e.getMessage().contains("Class name exceeds maximum length of ")) {
                throw new RuntimeException("Wrong NoClassDefFoundError: " + e.getMessage());
            }
        }
        try {
            callFindClass(tooBigClassName.toString());
            throw new RuntimeException("DefineClass did not throw expected NoClassDefFoundError");
        } catch (NoClassDefFoundError e) {
            if (!e.getMessage().contains("Class name exceeds maximum length of ")) {
                throw new RuntimeException("Wrong NoClassDefFoundError: " + e.getMessage());
            }
        }
        try {
            callFindClass(null);
            throw new RuntimeException("FindClass did not throw expected NoClassDefFoundError");
        } catch (NoClassDefFoundError e) {
            if (!e.getMessage().contains("No class name given")) {
                throw new RuntimeException("Wrong NoClassDefFoundError: " + e.getMessage());
            }
        }
    }
}
