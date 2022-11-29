import jdk.internal.misc.Unsafe;
import java.lang.reflect.Field;

public class TestSplitIf {

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

    static int test(A a1, A a2, boolean flag1) {
        boolean flag2;
        int f = 0;
        A a = null;
        if (flag1) {
            flag2 = true;
            a = a1;
        } else {
            flag2 = false;
            a = a2;
        }
        if (flag2) {
            f = UNSAFE.getInt(a, F_OFFSET);
        } else {
            f = UNSAFE.getInt(a, F_OFFSET);
        }
        return f;
    }

    static public void main(String[] args) {
        A a = new A(0x42);
        for (int i = 0; i < 20000; i++) {
            test(a, a, (i % 2) == 0);
        }
    }
}
