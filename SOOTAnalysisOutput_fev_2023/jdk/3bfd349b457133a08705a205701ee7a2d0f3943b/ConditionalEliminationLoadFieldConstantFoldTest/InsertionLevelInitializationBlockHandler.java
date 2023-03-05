package org.graalvm.compiler.core.test;

import java.lang.reflect.Field;
import org.junit.Assert;
import org.junit.Test;
import org.graalvm.compiler.nodes.IfNode;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.phases.common.CanonicalizerPhase;
import org.graalvm.compiler.phases.common.IterativeConditionalEliminationPhase;
import org.graalvm.compiler.virtual.phases.ea.EarlyReadEliminationPhase;
import sun.misc.Unsafe;

public class ConditionalEliminationLoadFieldConstantFoldTest extends GraalCompilerTest {

    public static int intSideEffect;

    public static final B FinalField = new B(10);

    private abstract static class A {
    }

    private static class B extends A {

        final int a;

        B(int a) {
            this.a = a;
        }
    }

    private static class C extends A {

        final B b;

        C(B b) {
            this.b = b;
        }
    }

    private static class D extends A {

        final C c;

        D(C c) {
            this.c = c;
        }
    }

    private static class E extends D {

        final Object o;

        E(C c, Object o) {
            super(c);
            this.o = o;
        }
    }

    public static final B CONST_B = new B(10);

    public static final C CONST_C = new C(CONST_B);

    public static final D CONST_D = new D(CONST_C);

    public int testReadConstInBranch(B b) {
        if (b == CONST_B) {
            if (b.a == 5) {
                intSideEffect = b.a;
            } else {
                intSideEffect = 10;
            }
        }
        return 0;
    }

    public int testMultipleReadsConstInBranch(D d) {
        if (d == CONST_D) {
            C c = d.c;
            B b = c.b;
            int res = b.a + 12;
            if (res == 125) {
                intSideEffect = 12;
            }
        }
        return 0;
    }

    public int testLoadFinalInstanceOf(E e) {
        Object o = e.o;
        if (o == CONST_C) {
            if (o instanceof A) {
                intSideEffect = 1;
            } else {
                intSideEffect = 10;
            }
        }
        return 0;
    }

    public int testLoadFinalTwiceInstanceOf(E e) {
        if (e.o == CONST_C) {
            if (e.o instanceof A) {
                intSideEffect = 1;
            } else {
                intSideEffect = 10;
            }
        }
        return 0;
    }

    static class C1 {

        final int a;

        C1(int a) {
            this.a = a;
        }
    }

    static class C2 {

        final C1 c1;

        C2(C1 c1) {
            this.c1 = c1;
        }
    }

    public static int foldThatIsNotAllowed(C2 c2) {
        C1 c1Unknown = c2.c1;
        UNSAFE.putObject(c2, C2_C1_OFFSET, C1_AFTER_READ_CONST);
        if (c2 == C2_CONST) {
            if (c1Unknown == C1_CONST) {
                if (c2.c1.a == 10) {
                    intSideEffect = 1;
                    return 1;
                } else {
                    intSideEffect = 2;
                    return 2;
                }
            } else {
                intSideEffect = -2;
                return -2;
            }
        } else {
            intSideEffect = -1;
            return -1;
        }
    }

    public int testLoadFinalTwiceNoReadEliminationInstanceOf(E e) {
        if (e.o == CONST_C) {
            System.gc();
            C c = (C) e.o;
            if (c.b.a == 10) {
                intSideEffect = 1;
            } else {
                intSideEffect = 10;
            }
        }
        return 0;
    }

    private static final C1 C1_CONST = new C1(0);

    private static final C2 C2_CONST = new C2(C1_CONST);

    private static final C1 C1_AFTER_READ_CONST = new C1(10);

    private static Unsafe getUnsafe() {
        try {
            return Unsafe.getUnsafe();
        } catch (SecurityException e) {
        }
        try {
            Field theUnsafeInstance = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeInstance.setAccessible(true);
            return (Unsafe) theUnsafeInstance.get(Unsafe.class);
        } catch (Exception e) {
            throw new RuntimeException("exception while trying to get Unsafe.theUnsafe via reflection:", e);
        }
    }

    private static final sun.misc.Unsafe UNSAFE = getUnsafe();

    private static final long C2_C1_OFFSET;

    static {
        try {
            Field f = C2.class.getDeclaredField("c1");
            C2_C1_OFFSET = UNSAFE.objectFieldOffset(f);
        } catch (NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test01() {
        checkGraph("testReadConstInBranch", 1);
        test("testReadConstInBranch", new B(1));
    }

    @Test
    public void test02() {
        checkGraph("testMultipleReadsConstInBranch", 1);
    }

    @Test
    public void test03() {
        checkGraph("testLoadFinalInstanceOf", 1);
    }

    @Test
    public void test04() {
        checkGraph("testLoadFinalTwiceInstanceOf", 1);
    }

    @Test
    public void test05() {
        checkGraph("testLoadFinalTwiceNoReadEliminationInstanceOf", 2);
    }

    @Test(expected = AssertionError.class)
    @SuppressWarnings("try")
    public void test06() {
        Result actual = executeActual(getResolvedJavaMethod("foldThatIsNotAllowed"), null, C2_CONST);
        UNSAFE.putObject(C2_CONST, C2_C1_OFFSET, C1_CONST);
        Result expected = executeExpected(getResolvedJavaMethod("foldThatIsNotAllowed"), null, C2_CONST);
        Assert.assertEquals(expected.returnValue, actual.returnValue);
    }

    @SuppressWarnings("try")
    private StructuredGraph checkGraph(String name, int nrOfIfsAfter) {
        StructuredGraph g = parseForCompile(getResolvedJavaMethod(name));
        CanonicalizerPhase c = new CanonicalizerPhase();
        c.apply(g, getDefaultHighTierContext());
        new EarlyReadEliminationPhase(c).apply(g, getDefaultHighTierContext());
        new IterativeConditionalEliminationPhase(c, false).apply(g, getDefaultHighTierContext());
        Assert.assertEquals("Nr of Ifs left does not match", nrOfIfsAfter, g.getNodes().filter(IfNode.class).count());
        c.apply(g, getDefaultHighTierContext());
        return g;
    }
}