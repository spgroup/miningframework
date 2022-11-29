import sun.misc.Unsafe;
import java.lang.reflect.Field;
import java.util.*;
import jdk.test.lib.Asserts;

public class TestUnsafeUnalignedSwap {

    private final static Unsafe U;

    private static long sum = 4;

    static volatile long[] arrayLong = new long[1001];

    static volatile int[] arrayInt = new int[1001];

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            U = (Unsafe) f.get(null);
        } catch (ReflectiveOperationException e) {
            throw new InternalError(e);
        }
    }

    public static void testCompareAndSwapLong() {
        try {
            if (U.compareAndSwapLong(arrayLong, Unsafe.ARRAY_LONG_BASE_OFFSET + 1, 3243, 2334)) {
                sum++;
            } else {
                sum--;
            }
        } catch (InternalError e) {
            System.out.println(e.getMessage());
        }
    }

    public static void testCompareAndSwapInt() {
        try {
            if (U.compareAndSwapInt(arrayInt, Unsafe.ARRAY_INT_BASE_OFFSET + 1, 3243, 2334)) {
                sum++;
            } else {
                sum--;
            }
        } catch (InternalError e) {
            System.out.println(e.getMessage());
        }
    }

    public static void test() {
        testCompareAndSwapLong();
        testCompareAndSwapInt();
    }

    public static void main(String[] args) {
        test();
    }
}
