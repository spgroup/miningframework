import jdk.internal.misc.Unsafe;
import java.lang.reflect.Field;

public class TestMaybeNullUnsafeAccess {

    static final jdk.internal.misc.Unsafe UNSAFE = Unsafe.getUnsafe();

    static final long F_OFFSET;

    static class A {

        int f;

        A(int f) {
            this.f = f;
        }
    }

    static {
        try {
            Field fField = A.class.getDeclaredField("f");
            F_OFFSET = UNSAFE.objectFieldOffset(fField);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static A test_helper(Object o) {
        return (A) o;
    }

    static int test1(Object o, long offset) {
        int f = 0;
        for (int i = 0; i < 100; i++) {
            A a = test_helper(o);
            f = UNSAFE.getInt(a, offset);
        }
        return f;
    }

    static int test2(Object o) {
        int f = 0;
        for (int i = 0; i < 100; i++) {
            A a = test_helper(o);
            f = UNSAFE.getInt(a, F_OFFSET);
        }
        return f;
    }

    static public void main(String[] args) {
        A a = new A(0x42);
        for (int i = 0; i < 20000; i++) {
            test_helper(null);
            test_helper(a);
            test1(a, F_OFFSET);
            test2(a);
        }
    }
}
