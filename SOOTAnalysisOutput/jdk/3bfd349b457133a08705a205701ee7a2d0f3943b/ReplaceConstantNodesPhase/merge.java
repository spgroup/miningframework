package org.graalvm.compiler.hotspot.phases.aot;

import static org.graalvm.compiler.hotspot.nodes.aot.LoadMethodCountersNode.getLoadMethodCountersNodes;
import static org.graalvm.compiler.nodes.ConstantNode.getConstantNodes;
import java.util.HashSet;
import jdk.vm.ci.hotspot.HotSpotMetaspaceConstant;
import jdk.vm.ci.hotspot.HotSpotObjectConstant;
import jdk.vm.ci.hotspot.HotSpotResolvedJavaType;
import jdk.vm.ci.hotspot.HotSpotResolvedObjectType;
import jdk.vm.ci.meta.Constant;
import jdk.vm.ci.meta.ConstantReflectionProvider;
import jdk.vm.ci.meta.ResolvedJavaType;
import org.graalvm.compiler.core.common.type.ObjectStamp;
import org.graalvm.compiler.core.common.type.Stamp;
import org.graalvm.compiler.core.common.type.StampFactory;
import org.graalvm.compiler.debug.GraalError;
import org.graalvm.compiler.graph.Node;
import org.graalvm.compiler.hotspot.FingerprintUtil;
import org.graalvm.compiler.hotspot.meta.HotSpotConstantLoadAction;
import org.graalvm.compiler.hotspot.nodes.aot.InitializeKlassNode;
import org.graalvm.compiler.hotspot.nodes.aot.LoadConstantIndirectlyFixedNode;
import org.graalvm.compiler.hotspot.nodes.aot.LoadConstantIndirectlyNode;
import org.graalvm.compiler.hotspot.nodes.aot.LoadMethodCountersNode;
import org.graalvm.compiler.hotspot.nodes.aot.ResolveConstantNode;
import org.graalvm.compiler.hotspot.nodes.aot.ResolveMethodAndLoadCountersNode;
import org.graalvm.compiler.nodes.ConstantNode;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.ValueNode;
import org.graalvm.compiler.phases.BasePhase;
import org.graalvm.compiler.phases.tiers.PhaseContext;

public class ReplaceConstantNodesPhase extends BasePhase<PhaseContext> {

    private static final HashSet<Class<?>> builtIns = new HashSet<>();

    static {
        builtIns.add(Boolean.class);
        Class<?> characterCacheClass = Character.class.getDeclaredClasses()[0];
        assert "java.lang.Character$CharacterCache".equals(characterCacheClass.getName());
        builtIns.add(characterCacheClass);
        Class<?> byteCacheClass = Byte.class.getDeclaredClasses()[0];
        assert "java.lang.Byte$ByteCache".equals(byteCacheClass.getName());
        builtIns.add(byteCacheClass);
        Class<?> shortCacheClass = Short.class.getDeclaredClasses()[0];
        assert "java.lang.Short$ShortCache".equals(shortCacheClass.getName());
        builtIns.add(shortCacheClass);
        Class<?> integerCacheClass = Integer.class.getDeclaredClasses()[0];
        assert "java.lang.Integer$IntegerCache".equals(integerCacheClass.getName());
        builtIns.add(integerCacheClass);
        Class<?> longCacheClass = Long.class.getDeclaredClasses()[0];
        assert "java.lang.Long$LongCache".equals(longCacheClass.getName());
        builtIns.add(longCacheClass);
    }

    private static boolean isReplacementNode(Node n) {
        return n instanceof LoadConstantIndirectlyNode || n instanceof LoadConstantIndirectlyFixedNode || n instanceof ResolveConstantNode || n instanceof InitializeKlassNode;
    }

    private static boolean checkForBadFingerprint(HotSpotResolvedJavaType type) {
        if (type.isArray()) {
            if (type.getElementalType().isPrimitive()) {
                return false;
            }
            return FingerprintUtil.getFingerprint((HotSpotResolvedObjectType) (type.getElementalType())) == 0;
        }
        return FingerprintUtil.getFingerprint((HotSpotResolvedObjectType) type) == 0;
    }

    private static void handleHotSpotMetaspaceConstant(StructuredGraph graph, ConstantNode node) {
        HotSpotMetaspaceConstant metaspaceConstant = (HotSpotMetaspaceConstant) node.asConstant();
        HotSpotResolvedJavaType type = (HotSpotResolvedJavaType) metaspaceConstant.asResolvedJavaType();
        if (type != null) {
            if (checkForBadFingerprint(type)) {
                throw new GraalError("Type with bad fingerprint: " + type);
            }
            assert !metaspaceConstant.isCompressed() : "No support for replacing compressed metaspace constants";
            ResolvedJavaType topMethodHolder = graph.method().getDeclaringClass();
            ValueNode replacement;
            if (type.isArray() && type.getComponentType().isPrimitive()) {
                replacement = new LoadConstantIndirectlyNode(node);
            } else if (type.equals(topMethodHolder) || (type.isAssignableFrom(topMethodHolder) && !type.isInterface())) {
                replacement = new LoadConstantIndirectlyNode(node);
            } else if (builtIns.contains(type.mirror())) {
                replacement = new ResolveConstantNode(node, HotSpotConstantLoadAction.INITIALIZE);
            } else {
                replacement = new ResolveConstantNode(node);
            }
            node.replaceAtUsages(graph.addOrUnique(replacement), n -> !isReplacementNode(n));
        } else {
            throw new GraalError("Unsupported metaspace constant type: " + type);
        }
    }

    private static void handleHotSpotObjectConstant(StructuredGraph graph, ConstantNode node) {
        HotSpotObjectConstant constant = (HotSpotObjectConstant) node.asJavaConstant();
        HotSpotResolvedJavaType type = (HotSpotResolvedJavaType) constant.getType();
        if (type.mirror().equals(String.class)) {
            assert !constant.isCompressed() : "No support for replacing compressed oop constants";
            ValueNode replacement = graph.unique(new ResolveConstantNode(node));
            node.replaceAtUsages(replacement, n -> !(n instanceof ResolveConstantNode));
        } else {
            throw new GraalError("Unsupported object constant type: " + type);
        }
    }

    private static void handleLoadMethodCounters(StructuredGraph graph, LoadMethodCountersNode node, PhaseContext context) {
        ResolvedJavaType type = node.getMethod().getDeclaringClass();
        Stamp hubStamp = context.getStampProvider().createHubStamp((ObjectStamp) StampFactory.objectNonNull());
        ConstantReflectionProvider constantReflection = context.getConstantReflection();
        ConstantNode klassHint = ConstantNode.forConstant(hubStamp, constantReflection.asObjectHub(type), context.getMetaAccess(), graph);
        ValueNode replacement = graph.unique(new ResolveMethodAndLoadCountersNode(node.getMethod(), klassHint));
        node.replaceAtUsages(replacement, n -> !(n instanceof ResolveMethodAndLoadCountersNode));
    }

    @Override
    protected void run(StructuredGraph graph, PhaseContext context) {
        for (LoadMethodCountersNode node : getLoadMethodCountersNodes(graph)) {
            handleLoadMethodCounters(graph, node, context);
        }
        for (ConstantNode node : getConstantNodes(graph)) {
            Constant constant = node.asConstant();
            if (constant instanceof HotSpotMetaspaceConstant) {
                handleHotSpotMetaspaceConstant(graph, node);
            } else if (constant instanceof HotSpotObjectConstant) {
                handleHotSpotObjectConstant(graph, node);
            }
        }
    }

    @Override
    public boolean checkContract() {
        return false;
    }
}
