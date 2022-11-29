import org.testng.annotations.*;
import java.lang.reflect.*;
import java.util.List;
import java.util.stream.Collectors;

public class LocalsCrash {

    static Class<?> liveStackFrameClass;

    static Method getStackWalker;

    static {
        try {
            liveStackFrameClass = Class.forName("java.lang.LiveStackFrame");
            getStackWalker = liveStackFrameClass.getMethod("getStackWalker");
            getStackWalker.setAccessible(true);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private StackWalker walker;

    LocalsCrash() {
        try {
            walker = (StackWalker) getStackWalker.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test00() {
        doStackWalk();
    }

    @Test
    public void test01() {
        doStackWalk();
    }

    private synchronized List<StackWalker.StackFrame> doStackWalk() {
        try {
            int x = 10;
            char c = 'z';
            String hi = "himom";
            long l = 1000000L;
            double d = 3.1415926;
            return walker.walk(s -> s.collect(Collectors.toList()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
