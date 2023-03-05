import java.lang.reflect.Field;
import jdk.internal.misc.Unsafe;

public class TestCAEAntiDep {

    static final jdk.internal.misc.Unsafe UNSAFE = Unsafe.getUnsafe();

    static final long O_OFFSET;

    static class C {

        int f1;
    }

    C o = new C();

    static {
        try {
            Field oField = TestCAEAntiDep.class.getDeclaredField("o");
            O_OFFSET = UNSAFE.objectFieldOffset(oField);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static int m(TestCAEAntiDep test, Object expected, Object x) {
        C old = (C) UNSAFE.compareAndExchangeObjectVolatile(test, O_OFFSET, expected, x);
        int res = old.f1;
        old.f1 = 0x42;
        return res;
    }

    static public void main(String[] args) {
        TestCAEAntiDep test = new TestCAEAntiDep();
        for (int i = 0; i < 20000; i++) {
            m(test, test.o, test.o);
        }
    }
}
