package org.graalvm.compiler.hotspot.test;

import static org.graalvm.compiler.hotspot.replacements.HotSpotReplacementsUtil.config;
import static org.graalvm.compiler.hotspot.replacements.HotSpotReplacementsUtil.referentOffset;
import java.lang.ref.WeakReference;
import org.junit.Assert;
import org.junit.Test;
import org.graalvm.compiler.debug.Debug;
import org.graalvm.compiler.debug.Debug.Scope;
import org.graalvm.compiler.hotspot.GraalHotSpotVMConfig;
import org.graalvm.compiler.hotspot.nodes.G1PostWriteBarrier;
import org.graalvm.compiler.hotspot.nodes.G1PreWriteBarrier;
import org.graalvm.compiler.hotspot.nodes.G1ReferentFieldReadBarrier;
import org.graalvm.compiler.hotspot.nodes.SerialWriteBarrier;
import org.graalvm.compiler.hotspot.phases.WriteBarrierAdditionPhase;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.StructuredGraph.AllowAssumptions;
import org.graalvm.compiler.nodes.memory.HeapAccess.BarrierType;
import org.graalvm.compiler.nodes.memory.ReadNode;
import org.graalvm.compiler.nodes.memory.WriteNode;
import org.graalvm.compiler.nodes.memory.address.OffsetAddressNode;
import org.graalvm.compiler.nodes.spi.LoweringTool;
import org.graalvm.compiler.phases.OptimisticOptimizations;
import org.graalvm.compiler.phases.common.CanonicalizerPhase;
import org.graalvm.compiler.phases.common.GuardLoweringPhase;
import org.graalvm.compiler.phases.common.LoweringPhase;
import org.graalvm.compiler.phases.common.inlining.InliningPhase;
import org.graalvm.compiler.phases.common.inlining.policy.InlineEverythingPolicy;
import org.graalvm.compiler.phases.tiers.HighTierContext;
import org.graalvm.compiler.phases.tiers.MidTierContext;
import jdk.vm.ci.hotspot.HotSpotInstalledCode;
import jdk.vm.ci.meta.JavaConstant;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import sun.misc.Unsafe;

public class WriteBarrierAdditionTest extends HotSpotGraalCompilerTest {

    private final GraalHotSpotVMConfig config = runtime().getVMConfig();

    private static final long referentOffset = referentOffset();

    public static class Container {

        public Container a;

        public Container b;
    }

    @Test
    public void test1() throws Exception {
        testHelper("test1Snippet", (config.useG1GC) ? 4 : 2);
    }

    public static void test1Snippet() {
        Container main = new Container();
        Container temp1 = new Container();
        Container temp2 = new Container();
        main.a = temp1;
        main.b = temp2;
    }

    @Test
    public void test2() throws Exception {
        testHelper("test2Snippet", config.useG1GC ? 8 : 4);
    }

    public static void test2Snippet(boolean test) {
        Container main = new Container();
        Container temp1 = new Container();
        Container temp2 = new Container();
        for (int i = 0; i < 10; i++) {
            if (test) {
                main.a = temp1;
                main.b = temp2;
            } else {
                main.a = temp2;
                main.b = temp1;
            }
        }
    }

    @Test
    public void test3() throws Exception {
        testHelper("test3Snippet", config.useG1GC ? 8 : 4);
    }

    public static void test3Snippet() {
        Container[] main = new Container[10];
        Container temp1 = new Container();
        Container temp2 = new Container();
        for (int i = 0; i < 10; i++) {
            main[i].a = main[i].b = temp1;
        }
        for (int i = 0; i < 10; i++) {
            main[i].a = main[i].b = temp2;
        }
    }

    @Test
    public void test4() throws Exception {
        testHelper("test4Snippet", config.useG1GC ? 5 : 2);
    }

    public static Object test4Snippet() {
        WeakReference<Object> weakRef = new WeakReference<>(new Object());
        return weakRef.get();
    }

    static WeakReference<Object> wr = new WeakReference<>(new Object());

    static Container con = new Container();

    @Test
    public void test5() throws Exception {
        testHelper("test5Snippet", config.useG1GC ? 1 : 0);
    }

    public static Object test5Snippet() throws Exception {
        return UNSAFE.getObject(wr, config(null).useCompressedOops ? 12L : 16L);
    }

    @Test
    public void test6() throws Exception {
        test2("testUnsafeLoad", UNSAFE, wr, new Long(referentOffset), null);
    }

    @Test
    public void test7() throws Exception {
        test2("testUnsafeLoad", UNSAFE, con, new Long(referentOffset), null);
    }

    @Test
    public void test8() throws Exception {
        test2("testUnsafeLoad", UNSAFE, wr, new Long(config.useCompressedOops ? 20 : 32), null);
    }

    @Test
    public void test10() throws Exception {
        test2("testUnsafeLoad", UNSAFE, wr, new Long(config.useCompressedOops ? 6 : 8), new Integer(config.useCompressedOops ? 6 : 8));
    }

    @Test
    public void test9() throws Exception {
        test2("testUnsafeLoad", UNSAFE, wr, new Long(config.useCompressedOops ? 10 : 16), new Integer(config.useCompressedOops ? 10 : 16));
    }

    static Object[] src = new Object[1];

    static Object[] dst = new Object[1];

    static {
        for (int i = 0; i < src.length; i++) {
            src[i] = new Object();
        }
        for (int i = 0; i < dst.length; i++) {
            dst[i] = new Object();
        }
    }

    public static void testArrayCopy(Object a, Object b, Object c) throws Exception {
        System.arraycopy(a, 0, b, 0, (int) c);
    }

    @Test
    public void test11() throws Exception {
        test2("testArrayCopy", src, dst, dst.length);
    }

    public static Object testUnsafeLoad(Unsafe theUnsafe, Object a, Object b, Object c) throws Exception {
        final int offset = (c == null ? 0 : ((Integer) c).intValue());
        final long displacement = (b == null ? 0 : ((Long) b).longValue());
        return theUnsafe.getObject(a, offset + displacement);
    }

    private HotSpotInstalledCode getInstalledCode(String name, boolean withUnsafePrefix) throws Exception {
        final ResolvedJavaMethod javaMethod = withUnsafePrefix ? getResolvedJavaMethod(WriteBarrierAdditionTest.class, name, Unsafe.class, Object.class, Object.class, Object.class) : getResolvedJavaMethod(WriteBarrierAdditionTest.class, name, Object.class, Object.class, Object.class);
        final HotSpotInstalledCode installedCode = (HotSpotInstalledCode) getCode(javaMethod);
        return installedCode;
    }

    @SuppressWarnings("try")
    private void testHelper(final String snippetName, final int expectedBarriers) throws Exception, SecurityException {
        ResolvedJavaMethod snippet = getResolvedJavaMethod(snippetName);
        try (Scope s = Debug.scope("WriteBarrierAdditionTest", snippet)) {
            StructuredGraph graph = parseEager(snippet, AllowAssumptions.NO);
            HighTierContext highContext = getDefaultHighTierContext();
            MidTierContext midContext = new MidTierContext(getProviders(), getTargetProvider(), OptimisticOptimizations.ALL, graph.getProfilingInfo());
            new InliningPhase(new InlineEverythingPolicy(), new CanonicalizerPhase()).apply(graph, highContext);
            new CanonicalizerPhase().apply(graph, highContext);
            new LoweringPhase(new CanonicalizerPhase(), LoweringTool.StandardLoweringStage.HIGH_TIER).apply(graph, highContext);
            new GuardLoweringPhase().apply(graph, midContext);
            new LoweringPhase(new CanonicalizerPhase(), LoweringTool.StandardLoweringStage.MID_TIER).apply(graph, midContext);
            new WriteBarrierAdditionPhase(config).apply(graph);
            Debug.dump(Debug.BASIC_LOG_LEVEL, graph, "After Write Barrier Addition");
            int barriers = 0;
            if (config.useG1GC) {
                barriers = graph.getNodes().filter(G1ReferentFieldReadBarrier.class).count() + graph.getNodes().filter(G1PreWriteBarrier.class).count() + graph.getNodes().filter(G1PostWriteBarrier.class).count();
            } else {
                barriers = graph.getNodes().filter(SerialWriteBarrier.class).count();
            }
            if (expectedBarriers != barriers) {
                Assert.assertEquals(getScheduledGraphString(graph), expectedBarriers, barriers);
            }
            for (WriteNode write : graph.getNodes().filter(WriteNode.class)) {
                if (config.useG1GC) {
                    if (write.getBarrierType() != BarrierType.NONE) {
                        Assert.assertEquals(1, write.successors().count());
                        Assert.assertTrue(write.next() instanceof G1PostWriteBarrier);
                        Assert.assertTrue(write.predecessor() instanceof G1PreWriteBarrier);
                    }
                } else {
                    if (write.getBarrierType() != BarrierType.NONE) {
                        Assert.assertEquals(1, write.successors().count());
                        Assert.assertTrue(write.next() instanceof SerialWriteBarrier);
                    }
                }
            }
            for (ReadNode read : graph.getNodes().filter(ReadNode.class)) {
                if (read.getBarrierType() != BarrierType.NONE) {
                    Assert.assertTrue(read.getAddress() instanceof OffsetAddressNode);
                    JavaConstant constDisp = ((OffsetAddressNode) read.getAddress()).getOffset().asJavaConstant();
                    Assert.assertNotNull(constDisp);
                    Assert.assertEquals(referentOffset, constDisp.asLong());
                    Assert.assertTrue(config.useG1GC);
                    Assert.assertEquals(BarrierType.PRECISE, read.getBarrierType());
                    Assert.assertTrue(read.next() instanceof G1ReferentFieldReadBarrier);
                }
            }
        } catch (Throwable e) {
            throw Debug.handle(e);
        }
    }

    private void test2(final String snippet, Object... args) throws Exception {
        HotSpotInstalledCode code = getInstalledCode(snippet, args[0] instanceof Unsafe);
        code.executeVarargs(args);
    }
}
