package org.graalvm.compiler.core.test.ea;

import org.graalvm.compiler.core.common.GraalOptions;
import org.graalvm.compiler.core.test.GraalCompilerTest;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.extended.RawLoadNode;
import org.graalvm.compiler.nodes.extended.RawStoreNode;
import org.graalvm.compiler.nodes.virtual.CommitAllocationNode;
import org.graalvm.compiler.phases.common.CanonicalizerPhase;
import org.graalvm.compiler.phases.common.inlining.InliningPhase;
import org.graalvm.compiler.phases.tiers.HighTierContext;
import org.graalvm.compiler.virtual.phases.ea.PartialEscapePhase;
import org.junit.Test;
import sun.misc.Unsafe;
import java.lang.reflect.Field;

public class TrufflePEATest extends GraalCompilerTest {

    static class Frame {

        long[] primitiveLocals;

        Frame(int size) {
            primitiveLocals = new long[size];
        }
    }

    static class DynamicObject {

        int primitiveField0;

        int primitiveField1;
    }

    private static final long offsetLong1 = Unsafe.ARRAY_LONG_BASE_OFFSET + Unsafe.ARRAY_LONG_INDEX_SCALE * 1;

    private static final long offsetLong2 = Unsafe.ARRAY_LONG_BASE_OFFSET + Unsafe.ARRAY_LONG_INDEX_SCALE * 2;

    private static final long primitiveField0Offset;

    static {
        try {
            Field primitiveField0 = DynamicObject.class.getDeclaredField("primitiveField0");
            primitiveField0Offset = UNSAFE.objectFieldOffset(primitiveField0);
        } catch (NoSuchFieldException | SecurityException e) {
            throw new AssertionError(e);
        }
    }

    public static int unsafeAccessToLongArray(int v, Frame frame) {
        long[] array = frame.primitiveLocals;
        int s = UNSAFE.getInt(array, offsetLong1);
        UNSAFE.putInt(array, offsetLong1, v);
        UNSAFE.putInt(array, offsetLong2, v);
        return s + UNSAFE.getInt(array, offsetLong1) + UNSAFE.getInt(array, offsetLong2);
    }

    @Test
    public void testUnsafeAccessToLongArray() {
        StructuredGraph graph = processMethod("unsafeAccessToLongArray");
        assertDeepEquals(1, graph.getNodes().filter(RawLoadNode.class).count());
    }

    private static final int FRAME_SIZE = 16;

    public static long newFrame(long v) {
        Frame frame = new Frame(FRAME_SIZE);
        UNSAFE.putLong(frame.primitiveLocals, offsetLong1, v);
        return UNSAFE.getLong(frame.primitiveLocals, offsetLong1);
    }

    @Test
    public void testNewFrame() {
        StructuredGraph graph = processMethod("newFrame");
        assertDeepEquals(0, graph.getNodes().filter(CommitAllocationNode.class).count());
        assertDeepEquals(0, graph.getNodes().filter(RawLoadNode.class).count());
        assertDeepEquals(0, graph.getNodes().filter(RawStoreNode.class).count());
    }

    protected StructuredGraph processMethod(final String snippet) {
        StructuredGraph graph = parseEager(snippet, StructuredGraph.AllowAssumptions.NO);
        HighTierContext context = getDefaultHighTierContext();
        new InliningPhase(new CanonicalizerPhase()).apply(graph, context);
        new PartialEscapePhase(true, true, new CanonicalizerPhase(), null, graph.getOptions()).apply(graph, context);
        return graph;
    }

    public static double accessDynamicObject(double v) {
        DynamicObject obj = new DynamicObject();
        UNSAFE.putDouble(obj, primitiveField0Offset, v);
        return UNSAFE.getDouble(obj, primitiveField0Offset);
    }

    @Test
    public void testAccessDynamicObject() {
        StructuredGraph graph = processMethod("accessDynamicObject");
        assertDeepEquals(0, graph.getNodes().filter(CommitAllocationNode.class).count());
        assertDeepEquals(0, graph.getNodes().filter(RawLoadNode.class).count());
        assertDeepEquals(0, graph.getNodes().filter(RawStoreNode.class).count());
    }
}
