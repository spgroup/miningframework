package test.java.lang.invoke;

import java.lang.invoke.*;
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.*;
import java.util.*;
import org.testng.*;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.*;

public class JavaDocExamplesTest {

    public static void main(String... ignore) throws Throwable {
        new JavaDocExamplesTest().run();
    }

    public void run() throws Throwable {
        testFindVirtual();
        testPermuteArguments();
        testDropArguments();
        testFilterArguments();
        testFoldArguments();
        testFoldArguments2();
        testMethodHandlesSummary();
        testAsSpreader();
        testAsCollector();
        testAsVarargsCollector();
        testAsFixedArity();
        testAsTypeCornerCases();
        testMutableCallSite();
    }

    static final Class<?> THIS_CLASS = JavaDocExamplesTest.class;

    static int verbosity = Integer.getInteger(THIS_CLASS.getSimpleName() + ".verbosity", 0);

    {
    }

    static final private Lookup LOOKUP = lookup();

    static final private MethodHandle CONCAT_2, HASHCODE_2, ADD_2, SUB_2;

    static {
        try {
            Class<?> THIS_CLASS = LOOKUP.lookupClass();
            CONCAT_2 = LOOKUP.findVirtual(String.class, "concat", methodType(String.class, String.class));
            HASHCODE_2 = LOOKUP.findVirtual(Object.class, "hashCode", methodType(int.class));
            ADD_2 = LOOKUP.findStatic(THIS_CLASS, "add", methodType(int.class, int.class, int.class));
            SUB_2 = LOOKUP.findStatic(THIS_CLASS, "sub", methodType(int.class, int.class, int.class));
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    static int add(int x, int y) {
        return x + y;
    }

    static int sub(int x, int y) {
        return x - y;
    }

    {
    }

    @Test
    public void testFindVirtual() throws Throwable {
        {
        }
        MethodHandle CONCAT_3 = LOOKUP.findVirtual(String.class, "concat", methodType(String.class, String.class));
        MethodHandle HASHCODE_3 = LOOKUP.findVirtual(Object.class, "hashCode", methodType(int.class));
        assertEquals("xy", (String) CONCAT_2.invokeExact("x", "y"));
        assertEquals("xy", (String) CONCAT_3.invokeExact("x", "y"));
        assertEquals("xy".hashCode(), (int) HASHCODE_2.invokeExact((Object) "xy"));
        assertEquals("xy".hashCode(), (int) HASHCODE_3.invokeExact((Object) "xy"));
        {
        }
    }

    @Test
    public void testPermuteArguments() throws Throwable {
        {
            {
                {
                }
                MethodType intfn1 = methodType(int.class, int.class);
                MethodType intfn2 = methodType(int.class, int.class, int.class);
                MethodHandle sub = SUB_2;
                assert (sub.type().equals(intfn2));
                MethodHandle sub1 = permuteArguments(sub, intfn2, 0, 1);
                MethodHandle rsub = permuteArguments(sub, intfn2, 1, 0);
                assert ((int) rsub.invokeExact(1, 100) == 99);
                MethodHandle add = ADD_2;
                assert (add.type().equals(intfn2));
                MethodHandle twice = permuteArguments(add, intfn1, 0, 0);
                assert (twice.type().equals(intfn1));
                assert ((int) twice.invokeExact(21) == 42);
            }
        }
        {
            {
                {
                }
                MethodHandle cat = lookup().findVirtual(String.class, "concat", methodType(String.class, String.class));
                assertEquals("xy", (String) cat.invokeExact("x", "y"));
                MethodHandle d0 = dropArguments(cat, 0, String.class);
                assertEquals("yz", (String) d0.invokeExact("x", "y", "z"));
                MethodHandle d1 = dropArguments(cat, 1, String.class);
                assertEquals("xz", (String) d1.invokeExact("x", "y", "z"));
                MethodHandle d2 = dropArguments(cat, 2, String.class);
                assertEquals("xy", (String) d2.invokeExact("x", "y", "z"));
                MethodHandle d12 = dropArguments(cat, 1, int.class, boolean.class);
                assertEquals("xz", (String) d12.invokeExact("x", 12, true, "z"));
            }
        }
    }

    @Test
    public void testDropArguments() throws Throwable {
        {
            {
                {
                }
                MethodHandle cat = lookup().findVirtual(String.class, "concat", methodType(String.class, String.class));
                assertEquals("xy", (String) cat.invokeExact("x", "y"));
                MethodType bigType = cat.type().insertParameterTypes(0, int.class, String.class);
                MethodHandle d0 = dropArguments(cat, 0, bigType.parameterList().subList(0, 2));
                assertEquals(bigType, d0.type());
                assertEquals("yz", (String) d0.invokeExact(123, "x", "y", "z"));
            }
        }
        {
            {
                {
                }
                MethodHandle cat = lookup().findVirtual(String.class, "concat", methodType(String.class, String.class));
                assertEquals("xy", (String) cat.invokeExact("x", "y"));
                MethodHandle d0 = dropArguments(cat, 0, String.class);
                assertEquals("yz", (String) d0.invokeExact("x", "y", "z"));
                MethodHandle d1 = dropArguments(cat, 1, String.class);
                assertEquals("xz", (String) d1.invokeExact("x", "y", "z"));
                MethodHandle d2 = dropArguments(cat, 2, String.class);
                assertEquals("xy", (String) d2.invokeExact("x", "y", "z"));
                MethodHandle d12 = dropArguments(cat, 1, int.class, boolean.class);
                assertEquals("xz", (String) d12.invokeExact("x", 12, true, "z"));
            }
        }
    }

    @Test
    public void testFilterArguments() throws Throwable {
        {
            {
                {
                }
                MethodHandle cat = lookup().findVirtual(String.class, "concat", methodType(String.class, String.class));
                MethodHandle upcase = lookup().findVirtual(String.class, "toUpperCase", methodType(String.class));
                assertEquals("xy", (String) cat.invokeExact("x", "y"));
                MethodHandle f0 = filterArguments(cat, 0, upcase);
                assertEquals("Xy", (String) f0.invokeExact("x", "y"));
                MethodHandle f1 = filterArguments(cat, 1, upcase);
                assertEquals("xY", (String) f1.invokeExact("x", "y"));
                MethodHandle f2 = filterArguments(cat, 0, upcase, upcase);
                assertEquals("XY", (String) f2.invokeExact("x", "y"));
            }
        }
    }

    @Test
    public void testFoldArguments() throws Throwable {
        {
            {
                {
                }
                MethodHandle trace = publicLookup().findVirtual(java.io.PrintStream.class, "println", methodType(void.class, String.class)).bindTo(System.out);
                MethodHandle cat = lookup().findVirtual(String.class, "concat", methodType(String.class, String.class));
                assertEquals("boojum", (String) cat.invokeExact("boo", "jum"));
                MethodHandle catTrace = foldArguments(cat, trace);
                assertEquals("boojum", (String) catTrace.invokeExact("boo", "jum"));
            }
        }
    }

    static void assertEquals(Object exp, Object act) {
        if (verbosity > 0)
            System.out.println("result: " + act);
        Assert.assertEquals(exp, act);
    }

    @Test
    public void testMethodHandlesSummary() throws Throwable {
        {
            {
                {
                }
                Object x, y;
                String s;
                int i;
                MethodType mt;
                MethodHandle mh;
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                mt = MethodType.methodType(String.class, char.class, char.class);
                mh = lookup.findVirtual(String.class, "replace", mt);
                s = (String) mh.invokeExact("daddy", 'd', 'n');
                assertEquals(s, "nanny");
                s = (String) mh.invokeWithArguments("sappy", 'p', 'v');
                assertEquals(s, "savvy");
                mt = MethodType.methodType(java.util.List.class, Object[].class);
                mh = lookup.findStatic(java.util.Arrays.class, "asList", mt);
                assert (mh.isVarargsCollector());
                x = mh.invoke("one", "two");
                assertEquals(x, java.util.Arrays.asList("one", "two"));
                mt = MethodType.genericMethodType(3);
                mh = mh.asType(mt);
                x = mh.invokeExact((Object) 1, (Object) 2, (Object) 3);
                assertEquals(x, java.util.Arrays.asList(1, 2, 3));
                mt = MethodType.methodType(int.class);
                mh = lookup.findVirtual(java.util.List.class, "size", mt);
                i = (int) mh.invokeExact(java.util.Arrays.asList(1, 2, 3));
                assert (i == 3);
                mt = MethodType.methodType(void.class, String.class);
                mh = lookup.findVirtual(java.io.PrintStream.class, "println", mt);
                mh.invokeExact(System.out, "Hello, world.");
                {
                }
            }
        }
    }

    @Test
    public void testAsSpreader() throws Throwable {
        {
            {
                {
                }
                MethodHandle equals = publicLookup().findVirtual(String.class, "equals", methodType(boolean.class, Object.class));
                assert ((boolean) equals.invokeExact("me", (Object) "me"));
                assert (!(boolean) equals.invokeExact("me", (Object) "thee"));
                MethodHandle eq2 = equals.asSpreader(Object[].class, 2);
                assert ((boolean) eq2.invokeExact(new Object[] { "me", "me" }));
                assert (!(boolean) eq2.invokeExact(new Object[] { "me", "thee" }));
                MethodHandle eq2s = equals.asSpreader(String[].class, 2);
                assert ((boolean) eq2s.invokeExact(new String[] { "me", "me" }));
                assert (!(boolean) eq2s.invokeExact(new String[] { "me", "thee" }));
                MethodHandle eq1 = equals.asSpreader(Object[].class, 1);
                assert ((boolean) eq1.invokeExact("me", new Object[] { "me" }));
                assert (!(boolean) eq1.invokeExact("me", new Object[] { "thee" }));
                MethodHandle eq0 = equals.asSpreader(Object[].class, 0);
                assert ((boolean) eq0.invokeExact("me", (Object) "me", new Object[0]));
                assert (!(boolean) eq0.invokeExact("me", (Object) "thee", (Object[]) null));
                for (int n = 0; n <= 2; n++) {
                    for (Class<?> a : new Class<?>[] { Object[].class, String[].class, CharSequence[].class }) {
                        MethodHandle equals2 = equals.asSpreader(a, n).asCollector(a, n);
                        assert ((boolean) equals2.invokeWithArguments("me", "me"));
                        assert (!(boolean) equals2.invokeWithArguments("me", "thee"));
                    }
                }
                MethodHandle caToString = publicLookup().findStatic(Arrays.class, "toString", methodType(String.class, char[].class));
                assertEquals("[A, B, C]", (String) caToString.invokeExact("ABC".toCharArray()));
                MethodHandle caString3 = caToString.asCollector(char[].class, 3);
                assertEquals("[A, B, C]", (String) caString3.invokeExact('A', 'B', 'C'));
                MethodHandle caToString2 = caString3.asSpreader(char[].class, 2);
                assertEquals("[A, B, C]", (String) caToString2.invokeExact('A', "BC".toCharArray()));
            }
        }
    }

    @Test
    public void testAsCollector() throws Throwable {
        {
            {
                {
                }
                MethodHandle deepToString = publicLookup().findStatic(Arrays.class, "deepToString", methodType(String.class, Object[].class));
                assertEquals("[won]", (String) deepToString.invokeExact(new Object[] { "won" }));
                MethodHandle ts1 = deepToString.asCollector(Object[].class, 1);
                assertEquals(methodType(String.class, Object.class), ts1.type());
                assertEquals("[[won]]", (String) ts1.invokeExact((Object) new Object[] { "won" }));
                MethodHandle ts2 = deepToString.asCollector(String[].class, 2);
                assertEquals(methodType(String.class, String.class, String.class), ts2.type());
                assertEquals("[two, too]", (String) ts2.invokeExact("two", "too"));
                MethodHandle ts0 = deepToString.asCollector(Object[].class, 0);
                assertEquals("[]", (String) ts0.invokeExact());
                MethodHandle ts22 = deepToString.asCollector(Object[].class, 3).asCollector(String[].class, 2);
                assertEquals("[A, B, [C, D]]", ((String) ts22.invokeExact((Object) 'A', (Object) "B", "C", "D")));
                MethodHandle bytesToString = publicLookup().findStatic(Arrays.class, "toString", methodType(String.class, byte[].class)).asCollector(byte[].class, 3);
                assertEquals("[1, 2, 3]", (String) bytesToString.invokeExact((byte) 1, (byte) 2, (byte) 3));
                MethodHandle longsToString = publicLookup().findStatic(Arrays.class, "toString", methodType(String.class, long[].class)).asCollector(long[].class, 1);
                assertEquals("[123]", (String) longsToString.invokeExact((long) 123));
            }
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testAsVarargsCollector() throws Throwable {
        {
            {
                {
                }
                MethodHandle deepToString = publicLookup().findStatic(Arrays.class, "deepToString", methodType(String.class, Object[].class));
                MethodHandle ts1 = deepToString.asVarargsCollector(Object[].class);
                assertEquals("[won]", (String) ts1.invokeExact(new Object[] { "won" }));
                assertEquals("[won]", (String) ts1.invoke(new Object[] { "won" }));
                assertEquals("[won]", (String) ts1.invoke("won"));
                assertEquals("[[won]]", (String) ts1.invoke((Object) new Object[] { "won" }));
                MethodHandle asList = publicLookup().findStatic(Arrays.class, "asList", methodType(List.class, Object[].class));
                assertEquals(methodType(List.class, Object[].class), asList.type());
                assert (asList.isVarargsCollector());
                assertEquals("[]", asList.invoke().toString());
                assertEquals("[1]", asList.invoke(1).toString());
                assertEquals("[two, too]", asList.invoke("two", "too").toString());
                String[] argv = { "three", "thee", "tee" };
                assertEquals("[three, thee, tee]", asList.invoke(argv).toString());
                assertEquals("[three, thee, tee]", asList.invoke((Object[]) argv).toString());
                List ls = (List) asList.invoke((Object) argv);
                assertEquals(1, ls.size());
                assertEquals("[three, thee, tee]", Arrays.toString((Object[]) ls.get(0)));
            }
        }
    }

    @Test
    public void testAsFixedArity() throws Throwable {
        {
            {
                {
                }
                MethodHandle asListVar = publicLookup().findStatic(Arrays.class, "asList", methodType(List.class, Object[].class)).asVarargsCollector(Object[].class);
                MethodHandle asListFix = asListVar.asFixedArity();
                assertEquals("[1]", asListVar.invoke(1).toString());
                Exception caught = null;
                try {
                    asListFix.invoke((Object) 1);
                } catch (Exception ex) {
                    caught = ex;
                }
                assert (caught instanceof ClassCastException);
                assertEquals("[two, too]", asListVar.invoke("two", "too").toString());
                try {
                    asListFix.invoke("two", "too");
                } catch (Exception ex) {
                    caught = ex;
                }
                assert (caught instanceof WrongMethodTypeException);
                Object[] argv = { "three", "thee", "tee" };
                assertEquals("[three, thee, tee]", asListVar.invoke(argv).toString());
                assertEquals("[three, thee, tee]", asListFix.invoke(argv).toString());
                assertEquals(1, ((List) asListVar.invoke((Object) argv)).size());
                assertEquals("[three, thee, tee]", asListFix.invoke((Object) argv).toString());
            }
        }
    }

    @Test
    public void testAsTypeCornerCases() throws Throwable {
        {
            {
                {
                }
                MethodHandle i2s = publicLookup().findVirtual(Integer.class, "toString", methodType(String.class));
                i2s = i2s.asType(i2s.type().unwrap());
                MethodHandle l2s = publicLookup().findVirtual(Long.class, "toString", methodType(String.class));
                l2s = l2s.asType(l2s.type().unwrap());
                Exception caught = null;
                try {
                    i2s.asType(methodType(String.class, String.class));
                } catch (Exception ex) {
                    caught = ex;
                }
                assert (caught instanceof WrongMethodTypeException);
                i2s.asType(methodType(String.class, byte.class));
                i2s.asType(methodType(String.class, Byte.class));
                i2s.asType(methodType(String.class, Character.class));
                i2s.asType(methodType(String.class, Integer.class));
                l2s.asType(methodType(String.class, byte.class));
                l2s.asType(methodType(String.class, Byte.class));
                l2s.asType(methodType(String.class, Character.class));
                l2s.asType(methodType(String.class, Integer.class));
                l2s.asType(methodType(String.class, Long.class));
                caught = null;
                try {
                    i2s.asType(methodType(String.class, Long.class));
                } catch (Exception ex) {
                    caught = ex;
                }
                assert (caught instanceof WrongMethodTypeException);
                MethodHandle i2sGen = i2s.asType(methodType(String.class, Object.class));
                MethodHandle l2sGen = l2s.asType(methodType(String.class, Object.class));
                i2sGen.invoke(42);
                i2sGen.invoke((byte) 4);
                l2sGen.invoke(42);
                l2sGen.invoke((byte) 4);
                l2sGen.invoke(0x420000000L);
                caught = null;
                try {
                    i2sGen.invoke(0x420000000L);
                } catch (Exception ex) {
                    caught = ex;
                }
                assert (caught instanceof ClassCastException);
                caught = null;
                try {
                    i2sGen.invoke("asdf");
                } catch (Exception ex) {
                    caught = ex;
                }
                assert (caught instanceof ClassCastException);
                {
                }
            }
        }
    }

    @Test
    public void testMutableCallSite() throws Throwable {
        {
            {
                {
                }
                MutableCallSite name = new MutableCallSite(MethodType.methodType(String.class));
                MethodHandle MH_name = name.dynamicInvoker();
                MethodType MT_str1 = MethodType.methodType(String.class);
                MethodHandle MH_upcase = MethodHandles.lookup().findVirtual(String.class, "toUpperCase", MT_str1);
                MethodHandle worker1 = MethodHandles.filterReturnValue(MH_name, MH_upcase);
                name.setTarget(MethodHandles.constant(String.class, "Rocky"));
                assertEquals("ROCKY", (String) worker1.invokeExact());
                name.setTarget(MethodHandles.constant(String.class, "Fred"));
                assertEquals("FRED", (String) worker1.invokeExact());
                MethodType MT_str2 = MethodType.methodType(String.class, String.class);
                MethodHandle MH_cat = lookup().findVirtual(String.class, "concat", methodType(String.class, String.class));
                MethodHandle MH_dear = MethodHandles.insertArguments(MH_cat, 1, ", dear?");
                MethodHandle worker2 = MethodHandles.filterReturnValue(MH_name, MH_dear);
                assertEquals("Fred, dear?", (String) worker2.invokeExact());
                name.setTarget(MethodHandles.constant(String.class, "Wilma"));
                assertEquals("WILMA", (String) worker1.invokeExact());
                assertEquals("Wilma, dear?", (String) worker2.invokeExact());
                {
                }
            }
        }
    }

    @Test
    public void testSwitchPoint() throws Throwable {
        {
            {
                {
                }
                MethodHandle MH_strcat = MethodHandles.lookup().findVirtual(String.class, "concat", MethodType.methodType(String.class, String.class));
                SwitchPoint spt = new SwitchPoint();
                assert (!spt.hasBeenInvalidated());
                MethodHandle worker1 = MH_strcat;
                MethodHandle worker2 = MethodHandles.permuteArguments(MH_strcat, MH_strcat.type(), 1, 0);
                MethodHandle worker = spt.guardWithTest(worker1, worker2);
                assertEquals("method", (String) worker.invokeExact("met", "hod"));
                SwitchPoint.invalidateAll(new SwitchPoint[] { spt });
                assert (spt.hasBeenInvalidated());
                assertEquals("hodmet", (String) worker.invokeExact("met", "hod"));
                {
                }
            }
        }
    }

    @Test
    public void testFoldArguments2() throws Throwable {
        {
            {
                {
                }
                Lookup lookup = publicLookup();
                MethodHandle println = lookup.findVirtual(java.io.PrintStream.class, "println", methodType(void.class, String.class));
                MethodHandle arrayToString = lookup.findStatic(Arrays.class, "toString", methodType(String.class, Object[].class));
                MethodHandle concat = lookup.findVirtual(String.class, "concat", methodType(String.class, String.class));
                MethodHandle arrayToString_DIS = filterReturnValue(arrayToString, concat.bindTo("DIS:"));
                MethodHandle arrayToString_INV = filterReturnValue(arrayToString, concat.bindTo("INV:"));
                MethodHandle printArgs_DIS = filterReturnValue(arrayToString_DIS, println.bindTo(System.out)).asVarargsCollector(Object[].class);
                MethodHandle printArgs_INV = filterReturnValue(arrayToString_INV, println.bindTo(System.out)).asVarargsCollector(Object[].class);
                MethodType mtype = methodType(boolean.class, String.class);
                MethodHandle findVirtual = lookup.findVirtual(Lookup.class, "findVirtual", methodType(MethodHandle.class, Class.class, String.class, MethodType.class));
                MethodHandle getClass = lookup.findVirtual(Object.class, "getClass", methodType(Class.class));
                MethodHandle dispatch = findVirtual;
                dispatch = filterArguments(dispatch, 1, getClass);
                dispatch = insertArguments(dispatch, 3, mtype);
                dispatch = dispatch.bindTo(lookup);
                assertEquals(methodType(MethodHandle.class, Object.class, String.class), dispatch.type());
                MethodHandle invoker = invoker(mtype.insertParameterTypes(0, Object.class));
                dispatch = foldArguments(dispatch, printArgs_DIS.asType(dispatch.type().changeReturnType(void.class)));
                invoker = foldArguments(invoker, printArgs_INV.asType(invoker.type().changeReturnType(void.class)));
                invoker = dropArguments(invoker, 2, String.class);
                MethodHandle invokeDispatched = foldArguments(invoker, dispatch);
                Object x = "football", y = new java.util.Scanner("bar");
                assert ((boolean) invokeDispatched.invokeExact(x, "startsWith", "foo"));
                assert (!(boolean) invokeDispatched.invokeExact(x, "startsWith", "#"));
                assert ((boolean) invokeDispatched.invokeExact(x, "endsWith", "all"));
                assert (!(boolean) invokeDispatched.invokeExact(x, "endsWith", "foo"));
                assert ((boolean) invokeDispatched.invokeExact(y, "hasNext", "[abc]+[rst]"));
                assert (!(boolean) invokeDispatched.invokeExact(y, "hasNext", "[123]+[789]"));
            }
        }
    }
}
