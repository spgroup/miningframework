package java.lang;

public class BootNativeLibrary {

    static {
        System.loadLibrary("bootLoaderTest");
    }

    public static native Class<?> findClass(String name);
}