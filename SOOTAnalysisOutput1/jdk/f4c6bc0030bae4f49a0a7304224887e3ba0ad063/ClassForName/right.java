import java.net.URLClassLoader;

public class ClassForName {

    static {
        if (!(ClassForName.class.getClassLoader() instanceof URLClassLoader)) {
            throw new RuntimeException("Supposed to be loaded by URLClassLoader");
        }
    }

    public ClassForName() {
        try {
            Class.forName(java.util.List.class.getName(), false, ClassLoader.getSystemClassLoader());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
