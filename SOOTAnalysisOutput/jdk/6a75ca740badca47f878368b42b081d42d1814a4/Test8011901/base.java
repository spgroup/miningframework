import java.lang.reflect.*;
import sun.misc.*;

public class Test8011901 {

    private long ctl;

    private static final sun.misc.Unsafe U;

    private static final long CTL;

    static {
        try {
            Field unsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafe.setAccessible(true);
            U = (sun.misc.Unsafe) unsafe.get(null);
            CTL = U.objectFieldOffset(Test8011901.class.getDeclaredField("ctl"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public static void main(String[] args) {
        for (int c = 0; c < 20000; c++) {
            new Test8011901().makeTest();
        }
        System.out.println("Test Passed");
    }

    public static final long EXPECTED = 1L << 42;

    public void makeTest() {
        U.getAndAddLong(this, CTL, EXPECTED);
        if (ctl != EXPECTED) {
            throw new RuntimeException("Test failed. Expected: " + EXPECTED + ", but got = " + ctl);
        }
    }
}
