package com.oracle.graal.hotspot.hsail;

import java.util.*;
import com.oracle.graal.api.code.*;
import com.oracle.graal.api.meta.*;
import com.oracle.graal.graph.*;
import com.oracle.graal.hotspot.*;
import com.oracle.graal.hotspot.meta.*;
import com.oracle.graal.nodes.*;
import com.oracle.graal.nodes.calc.*;
import com.oracle.graal.nodes.extended.*;
import com.oracle.graal.nodes.java.*;
import com.oracle.graal.nodes.spi.*;

public class HSAILHotSpotLoweringProvider extends DefaultHotSpotLoweringProvider {

    abstract static class LoweringStrategy {

        abstract void lower(Node n, LoweringTool tool);
    }

    static LoweringStrategy PassThruStrategy = new LoweringStrategy() {

        @Override
        void lower(Node n, LoweringTool tool) {
            return;
        }
    };

    static LoweringStrategy RejectStrategy = new LoweringStrategy() {

        @Override
        void lower(Node n, LoweringTool tool) {
            throw new GraalInternalError("Node implementing Lowerable not handled in HSAIL Backend: " + n);
        }
    };

    static LoweringStrategy UnwindNodeStrategy = new LoweringStrategy() {

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

    private static HashMap<Class<?>, LoweringStrategy> strategyMap = new HashMap<>();

    static {
        strategyMap.put(ConvertNode.class, PassThruStrategy);
        strategyMap.put(FloatConvertNode.class, PassThruStrategy);
        strategyMap.put(NewInstanceNode.class, RejectStrategy);
        strategyMap.put(NewArrayNode.class, RejectStrategy);
        strategyMap.put(NewMultiArrayNode.class, RejectStrategy);
        strategyMap.put(DynamicNewArrayNode.class, RejectStrategy);
        strategyMap.put(MonitorEnterNode.class, RejectStrategy);
        strategyMap.put(MonitorExitNode.class, RejectStrategy);
        strategyMap.put(UnwindNode.class, UnwindNodeStrategy);
    }

    private static LoweringStrategy getStrategy(Node n) {
        return strategyMap.get(n.getClass());
    }

    public HSAILHotSpotLoweringProvider(HotSpotGraalRuntime runtime, MetaAccessProvider metaAccess, ForeignCallsProvider foreignCalls, HotSpotRegistersProvider registers) {
        super(runtime, metaAccess, foreignCalls, registers);
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
