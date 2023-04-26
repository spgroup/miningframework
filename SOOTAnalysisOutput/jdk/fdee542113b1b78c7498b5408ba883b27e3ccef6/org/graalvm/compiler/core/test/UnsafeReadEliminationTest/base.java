package org.graalvm.compiler.core.test;

import org.graalvm.compiler.api.directives.GraalDirectives;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.StructuredGraph.AllowAssumptions;
import org.graalvm.compiler.nodes.extended.UnsafeAccessNode;
import org.graalvm.compiler.nodes.memory.ReadNode;
import org.graalvm.compiler.nodes.memory.WriteNode;
import org.graalvm.compiler.nodes.spi.LoweringTool;
import org.graalvm.compiler.options.OptionValues;
import org.graalvm.compiler.phases.common.CanonicalizerPhase;
import org.graalvm.compiler.phases.common.LoweringPhase;
import org.graalvm.compiler.phases.tiers.PhaseContext;
import org.graalvm.compiler.virtual.phases.ea.EarlyReadEliminationPhase;
import org.graalvm.compiler.virtual.phases.ea.PartialEscapePhase;
import org.junit.Assert;
import org.junit.Test;
import sun.misc.Unsafe;

public class UnsafeReadEliminationTest extends GraalCompilerTest {

    public static long[] Memory = new long[] { 1, 2 };

    public static double SideEffectD;

    public static double SideEffectL;

    private static final long byteArrayBaseOffset;

    private static final long intArrayBaseOffset;

    private static final long longArrayBaseOffset;

    static {
        byteArrayBaseOffset = UNSAFE.arrayBaseOffset(byte[].class);
        intArrayBaseOffset = UNSAFE.arrayBaseOffset(int[].class);
        longArrayBaseOffset = UNSAFE.arrayBaseOffset(long[].class);
    }

    public static long test1Snippet(double a) {
        final Object m = Memory;
        if (a > 0) {
            UNSAFE.putDouble(m, (long) Unsafe.ARRAY_LONG_BASE_OFFSET, a);
        } else {
            SideEffectL = UNSAFE.getLong(m, (long) Unsafe.ARRAY_LONG_BASE_OFFSET);
        }
        return UNSAFE.getLong(m, (long) Unsafe.ARRAY_LONG_BASE_OFFSET);
    }

    public static class A {

        long[][] o;

        long[][] p;
    }

    public static Object test2Snippet(A a, int c) {
        Object phi = null;
        if (c != 0) {
            long[][] r = a.o;
            phi = r;
            UNSAFE.putDouble(r, (long) Unsafe.ARRAY_LONG_BASE_OFFSET, 12d);
        } else {
            long[][] r = a.p;
            phi = r;
            UNSAFE.putLong(r, (long) Unsafe.ARRAY_LONG_BASE_OFFSET, 123);
        }
        GraalDirectives.controlFlowAnchor();
        SideEffectD = UNSAFE.getDouble(phi, (long) Unsafe.ARRAY_LONG_BASE_OFFSET);
        return phi;
    }

    @Test
    public void test01() {
        StructuredGraph graph = parseEager("test1Snippet", AllowAssumptions.NO);
        testEarlyReadElimination(graph, 3, 2);
    }

    @Test
    public void test02() {
        StructuredGraph graph = parseEager("test1Snippet", AllowAssumptions.NO);
        testPartialEscapeReadElimination(graph, 3, 2);
    }

    @Test
    public void test03() {
        StructuredGraph graph = parseEager("test2Snippet", AllowAssumptions.NO);
        testEarlyReadElimination(graph, 3, 3);
    }

    @Test
    public void test04() {
        StructuredGraph graph = parseEager("test2Snippet", AllowAssumptions.NO);
        testEarlyReadElimination(graph, 3, 3);
    }

    public void testEarlyReadElimination(StructuredGraph graph, int reads, int writes) {
        PhaseContext context = getDefaultHighTierContext();
        CanonicalizerPhase canonicalizer = new CanonicalizerPhase();
        canonicalizer.apply(graph, context);
        new EarlyReadEliminationPhase(canonicalizer).apply(graph, context);
        Assert.assertEquals(3, graph.getNodes().filter(UnsafeAccessNode.class).count());
        new LoweringPhase(canonicalizer, LoweringTool.StandardLoweringStage.HIGH_TIER).apply(graph, context);
        canonicalizer.apply(graph, context);
        new EarlyReadEliminationPhase(canonicalizer).apply(graph, context);
        Assert.assertEquals(reads, graph.getNodes().filter(ReadNode.class).count());
        Assert.assertEquals(writes, graph.getNodes().filter(WriteNode.class).count());
    }

    public void testPartialEscapeReadElimination(StructuredGraph graph, int reads, int writes) {
        OptionValues options = graph.getOptions();
        PhaseContext context = getDefaultHighTierContext();
        CanonicalizerPhase canonicalizer = new CanonicalizerPhase();
        canonicalizer.apply(graph, context);
        new PartialEscapePhase(true, true, canonicalizer, null, options).apply(graph, context);
        Assert.assertEquals(3, graph.getNodes().filter(UnsafeAccessNode.class).count());
        new LoweringPhase(canonicalizer, LoweringTool.StandardLoweringStage.HIGH_TIER).apply(graph, context);
        canonicalizer.apply(graph, context);
        new PartialEscapePhase(true, true, canonicalizer, null, options).apply(graph, context);
        Assert.assertEquals(reads, graph.getNodes().filter(ReadNode.class).count());
        Assert.assertEquals(writes, graph.getNodes().filter(WriteNode.class).count());
    }

    public static int testWriteIntToByteArraySnippet() {
        byte[] array = new byte[4];
        UNSAFE.putInt(array, byteArrayBaseOffset, 0x01020304);
        return array[0];
    }

    @Test
    public void testWriteIntToByteArray() {
        test("testWriteIntToByteArraySnippet");
    }

    public static byte testWriteSignedExtendedByteToByteArraySnippet(byte b) {
        byte[] array = new byte[4];
        array[0] = 0x01;
        array[1] = 0x02;
        array[2] = 0x03;
        array[3] = 0x04;
        UNSAFE.putInt(array, byteArrayBaseOffset, b);
        return array[3];
    }

    @Test
    public void testWriteSignedExtendedByteToByteArray() {
        test("testWriteSignedExtendedByteToByteArraySnippet", (byte) 0);
    }

    public static int testWriteLongToIntArraySnippet() {
        int[] array = new int[2];
        UNSAFE.putLong(array, intArrayBaseOffset, 0x0102030405060708L);
        return array[0];
    }

    @Test
    public void testWriteLongToIntArray() {
        test("testWriteLongToIntArraySnippet");
    }

    public static int testWriteByteToIntArraySnippet() {
        int[] array = new int[1];
        array[0] = 0x01020304;
        UNSAFE.putByte(array, intArrayBaseOffset, (byte) 0x05);
        return array[0];
    }

    @Test
    public void testWriteByteToIntArray() {
        test("testWriteByteToIntArraySnippet");
    }

    public static long testWriteIntToLongArraySnippet() {
        long[] array = new long[1];
        array[0] = 0x0102030405060708L;
        UNSAFE.putInt(array, longArrayBaseOffset, 0x04030201);
        return array[0];
    }

    @Test
    public void testWriteIntToLongArray() {
        test("testWriteIntToLongArraySnippet");
    }

    public static float testWriteFloatToIntArraySnippet() {
        float[] array = new float[1];
        UNSAFE.putInt(array, intArrayBaseOffset, Float.floatToRawIntBits(0.5f));
        return array[0];
    }

    @Test
    public void testWriteFloatToIntArray() {
        test("testWriteFloatToIntArraySnippet");
    }
}
