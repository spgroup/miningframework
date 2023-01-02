package org.graalvm.compiler.core.test;

import java.lang.reflect.Field;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.StructuredGraph.AllowAssumptions;
import org.graalvm.compiler.options.OptionValues;
import org.graalvm.compiler.phases.common.CanonicalizerPhase;
import org.graalvm.compiler.phases.tiers.PhaseContext;
import org.graalvm.compiler.virtual.phases.ea.PartialEscapePhase;
import org.junit.Test;

public class UnsafeVirtualizationTest extends GraalCompilerTest {

    public static class A {

        int f1;

        int f2;
    }

    private static final long AF1Offset;

    private static final long AF2Offset;

    static {
        long o1 = -1;
        long o2 = -1;
        try {
            Field f1 = A.class.getDeclaredField("f1");
            Field f2 = A.class.getDeclaredField("f2");
            o1 = UNSAFE.objectFieldOffset(f1);
            o2 = UNSAFE.objectFieldOffset(f2);
        } catch (NoSuchFieldException | SecurityException e) {
            throw new AssertionError(e);
        }
        AF1Offset = o1;
        AF2Offset = o2;
    }

    public static int unsafeSnippet0(int i1, int i2) {
        A a = new A();
        UNSAFE.putDouble(a, AF1Offset, i1 + i2);
        return UNSAFE.getInt(a, AF1Offset) + UNSAFE.getInt(a, AF2Offset);
    }

    public static int unsafeSnippet1(int i1, int i2) {
        A a = new A();
        UNSAFE.putDouble(a, AF1Offset, i1 + i2);
        a.f2 = i1;
        return (int) UNSAFE.getDouble(a, AF1Offset);
    }

    @Test
    public void testUnsafePEA01() {
        testPartialEscapeReadElimination(parseEager("unsafeSnippet0", AllowAssumptions.NO), false);
        testPartialEscapeReadElimination(parseEager("unsafeSnippet0", AllowAssumptions.NO), true);
    }

    @Test
    public void testUnsafePEA02() {
        testPartialEscapeReadElimination(parseEager("unsafeSnippet1", AllowAssumptions.NO), false);
        testPartialEscapeReadElimination(parseEager("unsafeSnippet1", AllowAssumptions.NO), true);
    }

    public void testPartialEscapeReadElimination(StructuredGraph graph, boolean canonicalizeBefore) {
        OptionValues options = graph.getOptions();
        PhaseContext context = getDefaultHighTierContext();
        CanonicalizerPhase canonicalizer = new CanonicalizerPhase();
        if (canonicalizeBefore) {
            canonicalizer.apply(graph, context);
        }
        new PartialEscapePhase(true, true, canonicalizer, null, options).apply(graph, context);
    }
}