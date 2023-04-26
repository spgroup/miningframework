package test.java.lang.invoke;

import java.io.*;
import java.lang.invoke.*;
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.*;

public class CallSiteTest {

    private static final Class<?> CLASS = CallSiteTest.class;

    private static CallSite mcs;

    private static CallSite vcs;

    private static MethodHandle mh_foo;

    private static MethodHandle mh_bar;

    static {
        try {
            mh_foo = lookup().findStatic(CLASS, "foo", methodType(int.class, int.class, int.class));
            mh_bar = lookup().findStatic(CLASS, "bar", methodType(int.class, int.class, int.class));
            mcs = new MutableCallSite(mh_foo);
            vcs = new VolatileCallSite(mh_foo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String... av) throws Throwable {
        testMutableCallSite();
        testVolatileCallSite();
    }

    private static final int N = Integer.MAX_VALUE / 100;

    private static final int RESULT1 = 762786192;

    private static final int RESULT2 = -21474836;

    private static void assertEquals(int expected, int actual) {
        if (expected != actual)
            throw new AssertionError("expected: " + expected + ", actual: " + actual);
    }

    private static void testMutableCallSite() throws Throwable {
        for (int i = 0; i < 20000; i++) {
            mcs.setTarget(mh_foo);
        }
        for (int n = 0; n < 2; n++) {
            mcs.setTarget(mh_foo);
            for (int i = 0; i < 5; i++) {
                assertEquals(RESULT1, runMutableCallSite());
            }
            mcs.setTarget(mh_bar);
            for (int i = 0; i < 5; i++) {
                assertEquals(RESULT2, runMutableCallSite());
            }
        }
    }

    private static void testVolatileCallSite() throws Throwable {
        for (int i = 0; i < 20000; i++) {
            vcs.setTarget(mh_foo);
        }
        for (int n = 0; n < 2; n++) {
            vcs.setTarget(mh_foo);
            for (int i = 0; i < 5; i++) {
                assertEquals(RESULT1, runVolatileCallSite());
            }
            vcs.setTarget(mh_bar);
            for (int i = 0; i < 5; i++) {
                assertEquals(RESULT2, runVolatileCallSite());
            }
        }
    }

    private static int runMutableCallSite() throws Throwable {
        int sum = 0;
        for (int i = 0; i < N; i++) {
            sum += (int) INDY_mcs().invokeExact(i, i + 1);
        }
        return sum;
    }

    private static int runVolatileCallSite() throws Throwable {
        int sum = 0;
        for (int i = 0; i < N; i++) {
            sum += (int) INDY_vcs().invokeExact(i, i + 1);
        }
        return sum;
    }

    static int foo(int a, int b) {
        return a + b;
    }

    static int bar(int a, int b) {
        return a - b;
    }

    private static MethodType MT_bsm() {
        shouldNotCallThis();
        return methodType(CallSite.class, Lookup.class, String.class, MethodType.class);
    }

    private static CallSite bsm_mcs(Lookup caller, String name, MethodType type) throws ReflectiveOperationException {
        return mcs;
    }

    private static MethodHandle MH_bsm_mcs() throws ReflectiveOperationException {
        shouldNotCallThis();
        return lookup().findStatic(lookup().lookupClass(), "bsm_mcs", MT_bsm());
    }

    private static MethodHandle INDY_mcs() throws Throwable {
        shouldNotCallThis();
        return ((CallSite) MH_bsm_mcs().invoke(lookup(), "foo", methodType(int.class, int.class, int.class))).dynamicInvoker();
    }

    private static CallSite bsm_vcs(Lookup caller, String name, MethodType type) throws ReflectiveOperationException {
        return vcs;
    }

    private static MethodHandle MH_bsm_vcs() throws ReflectiveOperationException {
        shouldNotCallThis();
        return lookup().findStatic(lookup().lookupClass(), "bsm_vcs", MT_bsm());
    }

    private static MethodHandle INDY_vcs() throws Throwable {
        shouldNotCallThis();
        return ((CallSite) MH_bsm_vcs().invoke(lookup(), "foo", methodType(int.class, int.class, int.class))).dynamicInvoker();
    }

    private static void shouldNotCallThis() {
        throw new AssertionError("this code should be statically transformed away by Indify");
    }
}
