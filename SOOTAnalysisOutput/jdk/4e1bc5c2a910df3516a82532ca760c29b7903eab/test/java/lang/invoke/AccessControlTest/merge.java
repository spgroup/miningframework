package test.java.lang.invoke;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.lang.reflect.Modifier;
import java.util.*;
import org.testng.*;
import org.testng.annotations.*;
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodHandles.Lookup.*;
import static java.lang.invoke.MethodType.*;
import static org.testng.Assert.*;
import test.java.lang.invoke.AccessControlTest_subpkg.Acquaintance_remote;

public class AccessControlTest {

    static final Class<?> THIS_CLASS = AccessControlTest.class;

    static int verbosity = 0;

    static {
        String vstr = System.getProperty(THIS_CLASS.getSimpleName() + ".verbosity");
        if (vstr == null)
            vstr = System.getProperty(THIS_CLASS.getName() + ".verbosity");
        if (vstr != null)
            verbosity = Integer.parseInt(vstr);
    }

    private class LookupCase implements Comparable<LookupCase> {

        final Lookup lookup;

        final Class<?> lookupClass;

        final int lookupModes;

        public LookupCase(Lookup lookup) {
            this.lookup = lookup;
            this.lookupClass = lookup.lookupClass();
            this.lookupModes = lookup.lookupModes();
            assert (lookupString().equals(lookup.toString()));
            numberOf(lookupClass().getClassLoader());
        }

        public LookupCase(Class<?> lookupClass, int lookupModes) {
            this.lookup = null;
            this.lookupClass = lookupClass;
            this.lookupModes = lookupModes;
            numberOf(lookupClass().getClassLoader());
        }

        public final Class<?> lookupClass() {
            return lookupClass;
        }

        public final int lookupModes() {
            return lookupModes;
        }

        public Lookup lookup() {
            lookup.getClass();
            return lookup;
        }

        @Override
        public int compareTo(LookupCase that) {
            Class<?> c1 = this.lookupClass();
            Class<?> c2 = that.lookupClass();
            if (c1 != c2) {
                int cmp = c1.getName().compareTo(c2.getName());
                if (cmp != 0)
                    return cmp;
                cmp = numberOf(c1.getClassLoader()) - numberOf(c2.getClassLoader());
                assert (cmp != 0);
                return cmp;
            }
            return -(this.lookupModes() - that.lookupModes());
        }

        @Override
        public boolean equals(Object that) {
            return (that instanceof LookupCase && equals((LookupCase) that));
        }

        public boolean equals(LookupCase that) {
            return (this.lookupClass() == that.lookupClass() && this.lookupModes() == that.lookupModes());
        }

        @Override
        public int hashCode() {
            return lookupClass().hashCode() + (lookupModes() * 31);
        }

        private String lookupString() {
            String name = lookupClass.getName();
            String suffix = "";
            if (lookupModes == 0)
                suffix = "/noaccess";
            else if (lookupModes == PUBLIC)
                suffix = "/public";
            else if (lookupModes == (PUBLIC | UNCONDITIONAL))
                suffix = "/publicLookup";
            else if (lookupModes == (PUBLIC | MODULE))
                suffix = "/module";
            else if (lookupModes == (PUBLIC | MODULE | PACKAGE))
                suffix = "/package";
            else if (lookupModes == (PUBLIC | MODULE | PACKAGE | PRIVATE))
                suffix = "/private";
            else if (lookupModes == (PUBLIC | MODULE | PACKAGE | PRIVATE | PROTECTED))
                suffix = "";
            else
                suffix = "/#" + Integer.toHexString(lookupModes);
            return name + suffix;
        }

        public LookupCase in(Class<?> c2) {
            Class<?> c1 = lookupClass();
            int m1 = lookupModes();
            int changed = 0;
            boolean sameModule = (c1.getModule() == c2.getModule()) || (!c1.getModule().isNamed() && !c2.getModule().isNamed());
            boolean samePackage = (c1.getClassLoader() == c2.getClassLoader() && c1.getPackageName().equals(c2.getPackageName()));
            boolean sameTopLevel = (topLevelClass(c1) == topLevelClass(c2));
            boolean sameClass = (c1 == c2);
            assert (samePackage || !sameTopLevel);
            assert (sameTopLevel || !sameClass);
            boolean accessible = sameClass;
            if ((m1 & PACKAGE) != 0)
                accessible |= samePackage;
            if ((m1 & PUBLIC) != 0)
                accessible |= (c2.getModifiers() & PUBLIC) != 0;
            if (!sameModule) {
                if (c1.getModule().isNamed() && (m1 & UNCONDITIONAL) == 0) {
                    accessible = false;
                } else {
                    changed |= (MODULE | PACKAGE | PRIVATE | PROTECTED);
                }
            }
            if (!accessible) {
                changed |= (PUBLIC | MODULE | PACKAGE | PRIVATE | PROTECTED);
            }
            if (!samePackage) {
                changed |= (PACKAGE | PRIVATE | PROTECTED);
            }
            if (!sameTopLevel) {
                changed |= (PRIVATE | PROTECTED);
            }
            if (!sameClass) {
                changed |= (UNCONDITIONAL);
            } else {
                assert (changed == 0);
            }
            if (accessible)
                assert ((changed & PUBLIC) == 0);
            int m2 = m1 & ~changed;
            LookupCase l2 = new LookupCase(c2, m2);
            assert (l2.lookupClass() == c2);
            assert ((m1 | m2) == m1);
            return l2;
        }

        @Override
        public String toString() {
            String s = lookupClass().getSimpleName();
            String lstr = lookupString();
            int sl = lstr.indexOf('/');
            if (sl >= 0)
                s += lstr.substring(sl);
            ClassLoader cld = lookupClass().getClassLoader();
            if (cld != THIS_LOADER)
                s += "/loader#" + numberOf(cld);
            return s;
        }

        public boolean willAccess(Method m) {
            Class<?> c1 = lookupClass();
            Class<?> c2 = m.getDeclaringClass();
            if ((lookupModes & UNCONDITIONAL) != 0 && (lookupModes & PUBLIC) != 0 && !c2.getModule().isNamed() && Modifier.isPublic(c2.getModifiers()) && Modifier.isPublic(m.getModifiers()))
                return true;
            LookupCase lc = this.in(c2);
            int m1 = lc.lookupModes();
            int m2 = fixMods(m.getModifiers());
            if (c1 != c2)
                m1 &= ~PRIVATE;
            if ((m2 & PROTECTED) != 0) {
                int prev = m2;
                m2 |= PACKAGE;
                if ((lookupModes() & PROTECTED) != 0 && c2.isAssignableFrom(c1))
                    m2 |= PUBLIC;
            }
            if (verbosity >= 2)
                System.out.println(this + " willAccess " + lc + " m1=" + m1 + " m2=" + m2 + " => " + ((m2 & m1) != 0));
            return (m2 & m1) != 0;
        }

        public boolean willAccessClass(Class<?> c2, boolean load) {
            Class<?> c1 = lookupClass();
            if (load && c2.getClassLoader() != null) {
                if (c1.getClassLoader() == null) {
                    return false;
                }
            }
            if ((lookupModes & UNCONDITIONAL) != 0 && (lookupModes & PUBLIC) != 0 && (!c2.getModule().isNamed()) && Modifier.isPublic(c2.getModifiers()))
                return true;
            LookupCase lc = this.in(c2);
            int m1 = lc.lookupModes();
            boolean r = false;
            if (m1 == 0) {
                r = false;
            } else {
                int m2 = fixMods(c2.getModifiers());
                if ((m2 & PUBLIC) != 0) {
                    r = true;
                } else if ((m1 & PACKAGE) != 0 && c1.getPackage() == c2.getPackage()) {
                    r = true;
                }
            }
            if (verbosity >= 2) {
                System.out.println(this + " willAccessClass " + lc + " c1=" + c1 + " c2=" + c2 + " => " + r);
            }
            return r;
        }
    }

    private static Class<?> topLevelClass(Class<?> cls) {
        Class<?> c = cls;
        for (Class<?> ec; (ec = c.getEnclosingClass()) != null; ) c = ec;
        assert (c.getEnclosingClass() == null);
        assert (c == cls || cls.getEnclosingClass() != null);
        return c;
    }

    private final TreeSet<LookupCase> CASES = new TreeSet<>();

    private final TreeMap<LookupCase, TreeSet<LookupCase>> CASE_EDGES = new TreeMap<>();

    private final ArrayList<ClassLoader> LOADERS = new ArrayList<>();

    private final ClassLoader THIS_LOADER = this.getClass().getClassLoader();

    {
        if (THIS_LOADER != null)
            LOADERS.add(THIS_LOADER);
    }

    private LookupCase lookupCase(String name) {
        for (LookupCase lc : CASES) {
            if (lc.toString().equals(name))
                return lc;
        }
        throw new AssertionError(name);
    }

    private int numberOf(ClassLoader cl) {
        if (cl == null)
            return 0;
        int i = LOADERS.indexOf(cl);
        if (i < 0) {
            i = LOADERS.size();
            LOADERS.add(cl);
        }
        return i + 1;
    }

    private void addLookupEdge(LookupCase l1, Class<?> c2, LookupCase l2) {
        TreeSet<LookupCase> edges = CASE_EDGES.get(l2);
        if (edges == null)
            CASE_EDGES.put(l2, edges = new TreeSet<>());
        if (edges.add(l1)) {
            Class<?> c1 = l1.lookupClass();
            assert (l2.lookupClass() == c2);
            int m1 = l1.lookupModes();
            int m2 = l2.lookupModes();
            assert ((m1 | m2) == m1);
            LookupCase expect = l1.in(c2);
            if (!expect.equals(l2))
                System.out.println("*** expect " + l1 + " => " + expect + " but got " + l2);
            assertEquals(l2, expect);
        }
    }

    private void makeCases(Lookup[] originalLookups) {
        CASES.clear();
        LOADERS.clear();
        CASE_EDGES.clear();
        ArrayList<Class<?>> classes = new ArrayList<>();
        for (Lookup l : originalLookups) {
            CASES.add(new LookupCase(l));
            classes.remove(l.lookupClass());
            classes.add(l.lookupClass());
        }
        System.out.println("loaders = " + LOADERS);
        int rounds = 0;
        for (int lastCount = -1; lastCount != CASES.size(); ) {
            lastCount = CASES.size();
            for (LookupCase lc1 : CASES.toArray(new LookupCase[0])) {
                for (Class<?> c2 : classes) {
                    LookupCase lc2 = new LookupCase(lc1.lookup().in(c2));
                    addLookupEdge(lc1, c2, lc2);
                    CASES.add(lc2);
                }
            }
            rounds++;
        }
        System.out.println("filled in " + CASES.size() + " cases from " + originalLookups.length + " original cases in " + rounds + " rounds");
        if (false) {
            System.out.println("CASES: {");
            for (LookupCase lc : CASES) {
                System.out.println(lc);
                Set<LookupCase> edges = CASE_EDGES.get(lc);
                if (edges != null)
                    for (LookupCase prev : edges) {
                        System.out.println("\t" + prev);
                    }
            }
            System.out.println("}");
        }
    }

    @Test
    public void test() {
        makeCases(lookups());
        if (verbosity > 0) {
            verbosity += 9;
            Method pro_in_self = targetMethod(THIS_CLASS, PROTECTED, methodType(void.class));
            testOneAccess(lookupCase("AccessControlTest/public"), pro_in_self, "find");
            testOneAccess(lookupCase("Remote_subclass/public"), pro_in_self, "find");
            testOneAccess(lookupCase("Remote_subclass"), pro_in_self, "find");
            verbosity -= 9;
        }
        Set<Class<?>> targetClassesDone = new HashSet<>();
        for (LookupCase targetCase : CASES) {
            Class<?> targetClass = targetCase.lookupClass();
            if (!targetClassesDone.add(targetClass))
                continue;
            String targetPlace = placeName(targetClass);
            if (targetPlace == null)
                continue;
            for (int targetAccess : ACCESS_CASES) {
                MethodType methodType = methodType(void.class);
                Method method = targetMethod(targetClass, targetAccess, methodType);
                for (LookupCase sourceCase : CASES) {
                    testOneAccess(sourceCase, method, "findClass");
                    testOneAccess(sourceCase, method, "accessClass");
                    testOneAccess(sourceCase, method, "find");
                    testOneAccess(sourceCase, method, "unreflect");
                }
            }
        }
        System.out.println("tested " + testCount + " access scenarios; " + testCountFails + " accesses were denied");
    }

    private int testCount, testCountFails;

    private void testOneAccess(LookupCase sourceCase, Method method, String kind) {
        Class<?> targetClass = method.getDeclaringClass();
        String methodName = method.getName();
        MethodType methodType = methodType(method.getReturnType(), method.getParameterTypes());
        boolean isFindOrAccessClass = "findClass".equals(kind) || "accessClass".equals(kind);
        boolean willAccess = isFindOrAccessClass ? sourceCase.willAccessClass(targetClass, "findClass".equals(kind)) : sourceCase.willAccess(method);
        boolean didAccess = false;
        ReflectiveOperationException accessError = null;
        try {
            switch(kind) {
                case "accessClass":
                    sourceCase.lookup().accessClass(targetClass);
                    break;
                case "findClass":
                    sourceCase.lookup().findClass(targetClass.getName());
                    break;
                case "find":
                    if ((method.getModifiers() & Modifier.STATIC) != 0)
                        sourceCase.lookup().findStatic(targetClass, methodName, methodType);
                    else
                        sourceCase.lookup().findVirtual(targetClass, methodName, methodType);
                    break;
                case "unreflect":
                    sourceCase.lookup().unreflect(method);
                    break;
                default:
                    throw new AssertionError(kind);
            }
            didAccess = true;
        } catch (ReflectiveOperationException ex) {
            accessError = ex;
        }
        if (willAccess != didAccess) {
            System.out.println(sourceCase + " => " + targetClass.getSimpleName() + (isFindOrAccessClass ? "" : "." + methodName + methodType));
            System.out.println("fail " + (isFindOrAccessClass ? kind : "on " + method) + " ex=" + accessError);
            assertEquals(willAccess, didAccess);
        }
        testCount++;
        if (!didAccess)
            testCountFails++;
    }

    static Method targetMethod(Class<?> targetClass, int targetAccess, MethodType methodType) {
        assert targetAccess != MODULE;
        String methodName = accessName(targetAccess) + placeName(targetClass);
        if (verbosity >= 2)
            System.out.println(targetClass.getSimpleName() + "." + methodName + methodType);
        try {
            Method method = targetClass.getDeclaredMethod(methodName, methodType.parameterArray());
            assertEquals(method.getReturnType(), methodType.returnType());
            int haveMods = method.getModifiers();
            assert (Modifier.isStatic(haveMods));
            assert (targetAccess == fixMods(haveMods));
            return method;
        } catch (NoSuchMethodException ex) {
            throw new AssertionError(methodName, ex);
        }
    }

    static String placeName(Class<?> cls) {
        if (cls == AccessControlTest.class)
            return "self";
        String cln = cls.getSimpleName();
        int under = cln.lastIndexOf('_');
        if (under < 0)
            return null;
        return cln.substring(under + 1);
    }

    static String accessName(int acc) {
        switch(acc) {
            case PUBLIC:
                return "pub_in_";
            case PROTECTED:
                return "pro_in_";
            case PACKAGE:
                return "pkg_in_";
            case PRIVATE:
                return "pri_in_";
        }
        assert (false);
        return "?";
    }

    private static final int[] ACCESS_CASES = { PUBLIC, PACKAGE, PRIVATE, PROTECTED };

    static int fixMods(int mods) {
        mods &= (PUBLIC | PRIVATE | PROTECTED);
        switch(mods) {
            case PUBLIC:
            case PRIVATE:
            case PROTECTED:
                return mods;
            case 0:
                return PACKAGE;
        }
        throw new AssertionError(mods);
    }

    static Lookup[] lookups() {
        ArrayList<Lookup> tem = new ArrayList<>();
        Collections.addAll(tem, AccessControlTest.lookup_in_self(), Inner_nestmate.lookup_in_nestmate(), AccessControlTest_sibling.lookup_in_sibling());
        if (true) {
            Collections.addAll(tem, Acquaintance_remote.lookups());
        } else {
            try {
                Class<?> remc = Class.forName("test.java.lang.invoke.AccessControlTest_subpkg.Acquaintance_remote");
                Lookup[] remls = (Lookup[]) remc.getMethod("lookups").invoke(null);
                Collections.addAll(tem, remls);
            } catch (ReflectiveOperationException ex) {
                throw new LinkageError("reflection failed", ex);
            }
        }
        tem.add(publicLookup());
        tem.add(publicLookup().in(String.class));
        tem.add(publicLookup().in(List.class));
        return tem.toArray(new Lookup[0]);
    }

    static Lookup lookup_in_self() {
        return MethodHandles.lookup();
    }

    public static void pub_in_self() {
    }

    protected static void pro_in_self() {
    }

    static void pkg_in_self() {
    }

    private static void pri_in_self() {
    }

    static class Inner_nestmate {

        static Lookup lookup_in_nestmate() {
            return MethodHandles.lookup();
        }

        public static void pub_in_nestmate() {
        }

        protected static void pro_in_nestmate() {
        }

        static void pkg_in_nestmate() {
        }

        private static void pri_in_nestmate() {
        }
    }
}

class AccessControlTest_sibling {

    static Lookup lookup_in_sibling() {
        return MethodHandles.lookup();
    }

    public static void pub_in_sibling() {
    }

    protected static void pro_in_sibling() {
    }

    static void pkg_in_sibling() {
    }

    private static void pri_in_sibling() {
    }
}
