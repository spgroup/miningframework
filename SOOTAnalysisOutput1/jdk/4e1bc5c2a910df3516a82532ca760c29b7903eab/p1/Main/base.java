package p1;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

public class Main {

    static final int MODULE = Lookup.MODULE;

    static final Class<?> p1_Type1;

    static final Class<?> p2_Type2;

    static final Class<?> q1_Type1;

    static final Class<?> q2_Type2;

    static final Class<?> x500NameClass;

    static {
        try {
            p1_Type1 = Class.forName("p1.Type1");
            p2_Type2 = Class.forName("p2.Type2");
            q1_Type1 = Class.forName("q1.Type1");
            q2_Type2 = Class.forName("q2.Type2");
            x500NameClass = Class.forName("sun.security.x509.X500Name");
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    public static void main(String[] args) throws Exception {
        Lookup lookup, lookup2;
        lookup = MethodHandles.lookup();
        assertTrue((lookup.lookupModes() & MODULE) == MODULE);
        findConstructor(lookup, p1_Type1, void.class);
        findConstructor(lookup, p2_Type2, void.class);
        findConstructor(lookup, q1_Type1, void.class);
        findConstructorExpectingIAE(lookup, q2_Type2, void.class);
        findConstructor(lookup, Object.class, void.class);
        findConstructorExpectingIAE(lookup, x500NameClass, void.class, String.class);
        lookup2 = lookup.in(p2_Type2);
        assertTrue((lookup2.lookupModes() & MODULE) == MODULE);
        findConstructor(lookup2, p1_Type1, void.class);
        findConstructor(lookup2, p2_Type2, void.class);
        findConstructor(lookup2, q1_Type1, void.class);
        findConstructorExpectingIAE(lookup2, q2_Type2, void.class);
        findConstructor(lookup2, Object.class, void.class);
        findConstructorExpectingIAE(lookup2, x500NameClass, void.class, String.class);
        lookup2 = lookup.in(Object.class);
        assertTrue(lookup2.lookupModes() == 0);
        findConstructorExpectingIAE(lookup2, Object.class, void.class);
        Class<?> c = MethodHandles.publicLookup().lookupClass();
        assertTrue(!c.getModule().isNamed());
        lookup2 = lookup.in(c);
        assertTrue(lookup2.lookupModes() == 0);
        findConstructorExpectingIAE(lookup2, Object.class, void.class);
        lookup = MethodHandles.publicLookup();
        assertTrue((lookup.lookupModes() & MODULE) == 0);
        findConstructor(lookup, p1_Type1, void.class);
        findConstructorExpectingIAE(lookup, p2_Type2, void.class);
        findConstructor(lookup, q1_Type1, void.class);
        findConstructorExpectingIAE(lookup, q2_Type2, void.class);
        findConstructor(lookup, Object.class, void.class);
        findConstructorExpectingIAE(lookup, x500NameClass, void.class);
        lookup2 = lookup.in(Object.class);
        assertTrue((lookup2.lookupModes() & MODULE) == 0);
        findConstructor(lookup2, String.class, void.class);
        findConstructorExpectingIAE(lookup2, x500NameClass, void.class, String.class);
        findConstructorExpectingIAE(lookup2, p1_Type1, void.class);
        findConstructorExpectingIAE(lookup2, q1_Type1, void.class);
        lookup2 = lookup.in(p1_Type1);
        assertTrue((lookup2.lookupModes() & MODULE) == 0);
        findConstructor(lookup2, p1_Type1, void.class);
        findConstructor(lookup2, q1_Type1, void.class);
        findConstructor(lookup2, Object.class, void.class);
        findConstructorExpectingIAE(lookup, p2_Type2, void.class);
        findConstructorExpectingIAE(lookup, q2_Type2, void.class);
        findConstructorExpectingIAE(lookup2, x500NameClass, void.class, String.class);
        lookup2 = lookup.in(q1_Type1);
        assertTrue((lookup2.lookupModes() & MODULE) == 0);
        findConstructor(lookup2, q1_Type1, void.class);
        findConstructor(lookup2, Object.class, void.class);
        findConstructorExpectingIAE(lookup2, p1_Type1, void.class);
        findConstructorExpectingIAE(lookup, q2_Type2, void.class);
        findConstructorExpectingIAE(lookup2, x500NameClass, void.class, String.class);
        lookup2 = lookup.in(p2_Type2);
        assertTrue(lookup2.lookupModes() == 0);
        findConstructorExpectingIAE(lookup, q2_Type2, void.class);
    }

    static MethodHandle findConstructorExpectingIAE(Lookup lookup, Class<?> clazz, Class<?> rtype, Class<?>... ptypes) throws Exception {
        try {
            findConstructor(lookup, clazz, rtype, ptypes);
            throw new RuntimeException("IllegalAccessError expected");
        } catch (IllegalAccessException expected) {
            return null;
        }
    }

    static MethodHandle findConstructor(Lookup lookup, Class<?> clazz, Class<?> rtype, Class<?>... ptypes) throws Exception {
        MethodType mt = MethodType.methodType(rtype, ptypes);
        return lookup.findConstructor(clazz, mt);
    }

    static void assertTrue(boolean condition) {
        if (!condition)
            throw new RuntimeException();
    }
}
