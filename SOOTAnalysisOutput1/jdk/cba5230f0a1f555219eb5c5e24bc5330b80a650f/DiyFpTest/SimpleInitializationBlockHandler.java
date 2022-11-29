package jdk.nashorn.internal.runtime.doubleconv.test;

import org.testng.annotations.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@SuppressWarnings("javadoc")
public class DiyFpTest {

    static final Class<?> DiyFp;

    static final Constructor<?> ctor;

    static {
        try {
            DiyFp = Class.forName("jdk.nashorn.internal.runtime.doubleconv.DiyFp");
            ctor = DiyFp.getDeclaredConstructor(long.class, int.class);
            ctor.setAccessible(true);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Method method(final String name, final Class<?>... params) throws NoSuchMethodException {
        final Method m = DiyFp.getDeclaredMethod(name, params);
        m.setAccessible(true);
        return m;
    }

    @Test
    public void testSubtract() throws Exception {
        final Object diyFp1 = ctor.newInstance(3, 0);
        final Object diyFp2 = ctor.newInstance(1, 0);
        final Object diff = method("minus", DiyFp, DiyFp).invoke(null, diyFp1, diyFp2);
        ;
        assertTrue(2l == (long) method("f").invoke(diff));
        assertTrue(0 == (int) method("e").invoke(diff));
        method("subtract", DiyFp).invoke(diyFp1, diyFp2);
        assertTrue(2l == (long) method("f").invoke(diyFp1));
        assertTrue(0 == (int) method("e").invoke(diyFp2));
    }

    @Test
    public void testMultiply() throws Exception {
        Object diyFp1, diyFp2, product;
        diyFp1 = ctor.newInstance(3, 0);
        diyFp2 = ctor.newInstance(2, 0);
        product = method("times", DiyFp, DiyFp).invoke(null, diyFp1, diyFp2);
        assertEquals(0l, (long) method("f").invoke(product));
        assertEquals(64, (int) method("e").invoke(product));
        method("multiply", DiyFp).invoke(diyFp1, diyFp2);
        assertEquals(0l, (long) method("f").invoke(diyFp1));
        assertEquals(64, (int) method("e").invoke(diyFp1));
        diyFp1 = ctor.newInstance(0x8000000000000000L, 11);
        diyFp2 = ctor.newInstance(2, 13);
        product = method("times", DiyFp, DiyFp).invoke(null, diyFp1, diyFp2);
        assertEquals(1l, (long) method("f").invoke(product));
        assertEquals(11 + 13 + 64, (int) method("e").invoke(product));
        diyFp1 = ctor.newInstance(0x8000000000000001L, 11);
        diyFp2 = ctor.newInstance(1, 13);
        product = method("times", DiyFp, DiyFp).invoke(null, diyFp1, diyFp2);
        assertEquals(1l, (long) method("f").invoke(product));
        assertEquals(11 + 13 + 64, (int) method("e").invoke(product));
        diyFp1 = ctor.newInstance(0x7fffffffffffffffL, 11);
        diyFp2 = ctor.newInstance(1, 13);
        product = method("times", DiyFp, DiyFp).invoke(null, diyFp1, diyFp2);
        assertEquals(0l, (long) method("f").invoke(product));
        assertEquals(11 + 13 + 64, (int) method("e").invoke(product));
        diyFp1 = ctor.newInstance(0xFFFFFFFFFFFFFFFFL, 11);
        diyFp2 = ctor.newInstance(0xFFFFFFFFFFFFFFFFL, 13);
        product = method("times", DiyFp, DiyFp).invoke(null, diyFp1, diyFp2);
        assertEquals(0xFFFFFFFFFFFFFFFel, (long) method("f").invoke(product));
        assertEquals(11 + 13 + 64, (int) method("e").invoke(product));
    }
}