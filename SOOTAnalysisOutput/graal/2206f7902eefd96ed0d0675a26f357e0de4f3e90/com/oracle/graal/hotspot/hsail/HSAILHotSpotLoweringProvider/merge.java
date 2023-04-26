package com.oracle.graal.hotspot.hsail;

import com.oracle.graal.api.code.*;
import com.oracle.graal.api.meta.*;
import com.oracle.graal.compiler.common.*;
import com.oracle.graal.graph.*;
import com.oracle.graal.hotspot.*;
import com.oracle.graal.hotspot.meta.*;
import com.oracle.graal.nodes.*;
import com.oracle.graal.nodes.calc.*;
import com.oracle.graal.nodes.spi.*;
import com.oracle.graal.nodes.extended.*;
import com.oracle.graal.nodes.java.*;
import com.oracle.graal.hotspot.hsail.replacements.*;
import java.util.HashMap;

public class HSAILHotSpotLoweringProvider extends DefaultHotSpotLoweringProvider {

    private HSAILNewObjectSnippets.Templates hsailNewObjectSnippets;

    abstract class LoweringStrategy {

        abstract void lower(Node n, LoweringTool tool);
    }

    LoweringStrategy PassThruStrategy = new LoweringStrategy() {

        @Override
        void lower(Node n, LoweringTool tool) {
            return;
        }
    };

    LoweringStrategy RejectStrategy = new LoweringStrategy() {

        @Override
        void lower(Node n, LoweringTool tool) {
            throw new GraalInternalError("Node implementing Lowerable not handled in HSAIL Backend: " + n);
        }
    };

    LoweringStrategy NewObjectStrategy = new LoweringStrategy() {

        @Override
        void lower(Node n, LoweringTool tool) {
            StructuredGraph graph = (StructuredGraph) n.graph();
            if (graph.getGuardsStage() == StructuredGraph.GuardsStage.AFTER_FSA) {
                if (n instanceof NewInstanceNode) {
                    hsailNewObjectSnippets.lower((NewInstanceNode) n, tool);
                } else if (n instanceof NewArrayNode) {
                    hsailNewObjectSnippets.lower((NewArrayNode) n, tool);
                }
            }
        }
    };

    LoweringStrategy UnwindNodeStrategy = new LoweringStrategy() {

        @Override
        void lower(Node n, LoweringTool tool) {
            StructuredGraph graph = (StructuredGraph) n.graph();
            UnwindNode unwind = (UnwindNode) n;
            ValueNode exception = unwind.exception();
            if (exception instanceof ForeignCallNode) {
                String callName = ((ForeignCallNode) exception).getDescriptor().getName();
                DeoptimizationReason reason;
                switch(callName) {
                    case "createOutOfBoundsException":
                        reason = DeoptimizationReason.BoundsCheckException;
                        break;
                    case "createNullPointerException":
                        reason = DeoptimizationReason.NullCheckException;
                        break;
                    default:
                        reason = DeoptimizationReason.None;
                }
                unwind.replaceAtPredecessor(graph.add(new DeoptimizeNode(DeoptimizationAction.InvalidateReprofile, reason)));
                unwind.safeDelete();
            } else {
                throw new GraalInternalError("UnwindNode seen without ForeignCallNode: " + exception);
            }
        }
    };

    private HashMap<Class<?>, LoweringStrategy> strategyMap = new HashMap<>();

    void initStrategyMap() {
        strategyMap.put(ConvertNode.class, PassThruStrategy);
        strategyMap.put(FloatConvertNode.class, PassThruStrategy);
        strategyMap.put(NewInstanceNode.class, NewObjectStrategy);
        strategyMap.put(NewArrayNode.class, NewObjectStrategy);
        strategyMap.put(NewMultiArrayNode.class, RejectStrategy);
        strategyMap.put(DynamicNewArrayNode.class, RejectStrategy);
        strategyMap.put(MonitorEnterNode.class, RejectStrategy);
        strategyMap.put(MonitorExitNode.class, RejectStrategy);
        strategyMap.put(UnwindNode.class, UnwindNodeStrategy);
    }

    private LoweringStrategy getStrategy(Node n) {
        return strategyMap.get(n.getClass());
    }

    public HSAILHotSpotLoweringProvider(HotSpotGraalRuntime runtime, MetaAccessProvider metaAccess, ForeignCallsProvider foreignCalls, HotSpotRegistersProvider registers) {
        super(runtime, metaAccess, foreignCalls, registers);
        initStrategyMap();
    }

    @Override
    public void initialize(HotSpotProviders providers, HotSpotVMConfig config) {
        super.initialize(providers, config);
        TargetDescription target = providers.getCodeCache().getTarget();
        hsailNewObjectSnippets = new HSAILNewObjectSnippets.Templates(providers, target);
    }

    @Override
    public void lower(Node n, LoweringTool tool) {
        LoweringStrategy strategy = getStrategy(n);
        if (strategy == null) {
            super.lower(n, tool);
        } else {
            strategy.lower(n, tool);
        }
    }
}
