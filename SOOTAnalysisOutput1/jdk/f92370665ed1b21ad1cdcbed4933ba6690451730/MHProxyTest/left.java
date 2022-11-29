import java.lang.invoke.*;
import java.security.*;
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.*;

public class MHProxyTest {

    private static final Class<?> C_Unsafe;

    private static final MethodHandle MH_getUnsafe;

    static {
        C_Unsafe = sun.misc.Unsafe.class;
        Lookup lookup = lookup();
        MethodHandle gumh = null;
        try {
            gumh = lookup.findStatic(C_Unsafe, "getUnsafe", methodType(C_Unsafe));
        } catch (ReflectiveOperationException ex) {
            throw new InternalError(ex.toString());
        }
        MH_getUnsafe = gumh;
        try {
            lookup.in(Object.class).findStatic(C_Unsafe, "getUnsafe", methodType(C_Unsafe));
        } catch (ReflectiveOperationException ex) {
            throw new InternalError(ex.toString());
        }
        lookup = lookup().in(C_Unsafe);
        try {
            lookup.in(C_Unsafe).findStatic(C_Unsafe, "getUnsafe", methodType(C_Unsafe));
        } catch (ReflectiveOperationException ex) {
            throw new InternalError(ex.toString());
        }
    }

    public static void main(String[] args) throws Throwable {
        System.setSecurityManager(new SecurityManager());
        Lookup lookup = lookup();
        testBasic(lookup);
        testDoPriv(lookup);
        testSetVar();
        Lookup l2 = lookup.in(Object.class);
        System.out.println("=== " + l2);
        testBasic(l2);
        testDoPriv(l2);
        Lookup l3 = lookup.in(C_Unsafe);
        System.out.println("=== " + l3);
        testBasic(l3);
        testDoPriv(l3);
        if (failure != null)
            throw failure;
    }

    private static Throwable failure;

    private static void fail(Throwable ex) {
        if (failure == null)
            failure = ex;
        StackTraceElement frame = new Exception().getStackTrace()[1];
        System.out.printf("Failed at %s:%d: %s\n", frame.getFileName(), frame.getLineNumber(), ex);
    }

    private static void ok(Throwable ex) {
        StackTraceElement frame = new Exception().getStackTrace()[1];
        System.out.printf("OK at %s:%d: %s\n", frame.getFileName(), frame.getLineNumber(), ex);
    }

    private static void testBasic(Lookup lookup) throws Throwable {
        try {
            MethodHandle badmh = lookup.findStatic(C_Unsafe, "getUnsafe", methodType(C_Unsafe));
            assert (badmh.type() == methodType(C_Unsafe));
            badmh = badmh.asType(badmh.type().generic());
            Object u = C_Unsafe.cast(badmh.invokeExact());
            assert (C_Unsafe.isInstance(u));
            fail(new AssertionError("got mh to getUnsafe!"));
        } catch (SecurityException ex) {
            ok(ex);
        }
        try {
            Object u = MH_getUnsafe.invokeWithArguments();
            assert (C_Unsafe.isInstance(u));
            fail(new AssertionError("got the Unsafe object! (MH invoke)"));
        } catch (SecurityException ex) {
            ok(ex);
        }
        try {
            MethodHandle mh = MH_getUnsafe;
            mh = mh.asType(mh.type().generic());
            mh = foldArguments(identity(Object.class), mh);
            mh = filterReturnValue(mh, identity(Object.class));
            Object u = mh.invokeExact();
            assert (C_Unsafe.isInstance(u));
            fail(new AssertionError("got the Unsafe object! (MH invokeWithArguments)"));
        } catch (SecurityException ex) {
            ok(ex);
        }
    }

    private static void testDoPriv(Lookup lookup) throws Throwable {
        PrivilegedAction privAct = MethodHandleProxies.asInterfaceInstance(PrivilegedAction.class, MH_getUnsafe);
        try {
            Object u = AccessController.doPrivileged(privAct);
            assert (C_Unsafe.isInstance(u));
            fail(new AssertionError("got the Unsafe object! (static doPriv)"));
        } catch (SecurityException ex) {
            ok(ex);
        }
        MethodHandle MH_doPriv = lookup.findStatic(AccessController.class, "doPrivileged", methodType(Object.class, PrivilegedAction.class));
        MH_doPriv = MH_doPriv.bindTo(privAct);
        try {
            Object u = MH_doPriv.invoke();
            assert (C_Unsafe.isInstance(u));
            fail(new AssertionError("got the Unsafe object! (MH + doPriv)"));
        } catch (SecurityException ex) {
            ok(ex);
        }
        Runnable rbl = MethodHandleProxies.asInterfaceInstance(Runnable.class, MH_doPriv);
        try {
            rbl.run();
            fail(new AssertionError("got the Unsafe object! (Runnable + MH + doPriv)"));
        } catch (SecurityException ex) {
            ok(ex);
        }
    }

    private static void testSetVar() throws Throwable {
        {
            Object[] box = new Object[1];
            MethodHandle MH_getFoo = identity(Object.class).bindTo("foo");
            MethodHandle MH_storeToBox = insertArguments(arrayElementSetter(Object[].class), 0, box, 0);
            MethodHandle mh = filterReturnValue(MH_getFoo, MH_storeToBox);
            mh.invokeExact();
            assert (box[0] == "foo");
        }
        {
            Object[] box = new Object[1];
            MethodHandle MH_storeToBox = insertArguments(arrayElementSetter(Object[].class), 0, box, 0);
            MethodHandle mh = filterReturnValue(MH_getUnsafe.asType(MH_getUnsafe.type().generic()), MH_storeToBox);
            try {
                mh.invokeExact();
                Object u = box[0];
                assert (C_Unsafe.isInstance(u));
                fail(new AssertionError("got the Unsafe object! (MH + setElement)"));
            } catch (SecurityException ex) {
                ok(ex);
            }
        }
    }
}
