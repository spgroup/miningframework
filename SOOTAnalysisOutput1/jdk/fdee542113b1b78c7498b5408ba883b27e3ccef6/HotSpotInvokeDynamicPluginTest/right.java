package org.graalvm.compiler.hotspot.test;

import java.util.function.IntPredicate;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import java.security.PrivilegedAction;
import org.graalvm.compiler.core.common.GraalOptions;
import org.graalvm.compiler.hotspot.meta.HotSpotClassInitializationPlugin;
import org.graalvm.compiler.hotspot.meta.HotSpotInvokeDynamicPlugin;
import org.graalvm.compiler.hotspot.nodes.aot.ResolveDynamicConstantNode;
import org.graalvm.compiler.hotspot.nodes.aot.ResolveDynamicStubCall;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.StructuredGraph.AllowAssumptions;
import org.graalvm.compiler.nodes.graphbuilderconf.GraphBuilderConfiguration.Plugins;
import org.graalvm.compiler.nodes.graphbuilderconf.GraphBuilderContext;
import org.graalvm.compiler.nodes.graphbuilderconf.InlineInvokePlugin.InlineInfo;
import org.graalvm.compiler.nodes.ValueNode;
import org.graalvm.compiler.nodes.spi.LoweringTool;
import org.graalvm.compiler.options.OptionValues;
import org.graalvm.compiler.phases.OptimisticOptimizations;
import org.graalvm.compiler.phases.common.CanonicalizerPhase;
import org.graalvm.compiler.phases.common.LoweringPhase;
import org.graalvm.compiler.phases.common.FrameStateAssignmentPhase;
import org.graalvm.compiler.phases.common.GuardLoweringPhase;
import org.graalvm.compiler.phases.tiers.PhaseContext;
import org.graalvm.compiler.phases.tiers.MidTierContext;
import org.junit.Assert;
import org.junit.Test;

public class HotSpotInvokeDynamicPluginTest extends HotSpotGraalCompilerTest {

    @Override
    protected Plugins getDefaultGraphBuilderPlugins() {
        Plugins plugins = super.getDefaultGraphBuilderPlugins();
        plugins.setClassInitializationPlugin(new HotSpotClassInitializationPlugin());
        plugins.setInvokeDynamicPlugin(new HotSpotInvokeDynamicPlugin() {

            @Override
            public boolean isResolvedDynamicInvoke(GraphBuilderContext builder, int index, int opcode) {
                ResolvedJavaMethod m = builder.getMethod();
                if (m.getName().startsWith("invokeDynamic") && m.getDeclaringClass().getName().equals("Lorg/graalvm/compiler/hotspot/test/HotSpotInvokeDynamicPluginTest;")) {
                    return false;
                }
                return super.isResolvedDynamicInvoke(builder, index, opcode);
            }

            @Override
            public boolean supportsDynamicInvoke(GraphBuilderContext builder, int index, int opcode) {
                ResolvedJavaMethod m = builder.getMethod();
                if (m.getName().startsWith("invokeHandle") && m.getDeclaringClass().getName().equals("Lorg/graalvm/compiler/hotspot/test/HotSpotInvokeDynamicPluginTest;")) {
                    return true;
                }
                return super.supportsDynamicInvoke(builder, index, opcode);
            }
        });
        return plugins;
    }

    @Override
    protected InlineInfo bytecodeParserShouldInlineInvoke(GraphBuilderContext b, ResolvedJavaMethod method, ValueNode[] args) {
        return InlineInfo.DO_NOT_INLINE_NO_EXCEPTION;
    }

    private void test(String name, int expectedResolves, int expectedStubCalls) {
        StructuredGraph graph = parseEager(name, AllowAssumptions.NO, new OptionValues(getInitialOptions(), GraalOptions.GeneratePIC, true));
        MidTierContext midTierContext = new MidTierContext(getProviders(), getTargetProvider(), OptimisticOptimizations.ALL, graph.getProfilingInfo());
        CanonicalizerPhase canonicalizer = new CanonicalizerPhase();
        Assert.assertEquals(expectedResolves, graph.getNodes().filter(ResolveDynamicConstantNode.class).count());
        Assert.assertEquals(0, graph.getNodes().filter(ResolveDynamicStubCall.class).count());
        PhaseContext context = new PhaseContext(getProviders());
        new LoweringPhase(canonicalizer, LoweringTool.StandardLoweringStage.HIGH_TIER).apply(graph, context);
        new GuardLoweringPhase().apply(graph, midTierContext);
        new LoweringPhase(canonicalizer, LoweringTool.StandardLoweringStage.MID_TIER).apply(graph, context);
        new FrameStateAssignmentPhase().apply(graph);
        new LoweringPhase(canonicalizer, LoweringTool.StandardLoweringStage.LOW_TIER).apply(graph, context);
        Assert.assertEquals(0, graph.getNodes().filter(ResolveDynamicConstantNode.class).count());
        Assert.assertEquals(expectedStubCalls, graph.getNodes().filter(ResolveDynamicStubCall.class).count());
    }

    public static IntPredicate invokeDynamic1() {
        IntPredicate i = (v) -> v > 1;
        return i;
    }

    public static PrivilegedAction<Integer> invokeDynamic2(String s) {
        return s::length;
    }

    static final MethodHandle objToStringMH;

    static {
        MethodHandle mh = null;
        try {
            mh = MethodHandles.lookup().findVirtual(Object.class, "toString", MethodType.methodType(String.class));
        } catch (Exception e) {
        }
        objToStringMH = mh;
    }

    public static String invokeHandle1(Object o) throws Throwable {
        return (String) objToStringMH.invokeExact(o);
    }

    @Test
    public void test1() {
        test("invokeDynamic1", 1, 1);
    }

    @Test
    public void test2() {
        test("invokeDynamic2", 1, 1);
    }

    @Test
    public void test3() {
        test("invokeHandle1", 1, 1);
    }
}
