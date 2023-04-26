package org.graalvm.compiler.truffle.compiler.phases.inlining;

import java.util.ArrayList;
import java.util.Collections;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.spi.CoreProviders;
import org.graalvm.compiler.phases.BasePhase;
import org.graalvm.compiler.serviceprovider.GraalServices;
import org.graalvm.compiler.truffle.common.CallNodeProvider;
import org.graalvm.compiler.truffle.common.CompilableTruffleAST;
import org.graalvm.compiler.truffle.compiler.PartialEvaluator;
import org.graalvm.compiler.truffle.compiler.SharedTruffleCompilerOptions;
import org.graalvm.compiler.truffle.compiler.TruffleCompilerOptions;

public final class AgnosticInliningPhase extends BasePhase<CoreProviders> {

    private static final InliningPolicyProvider POLICY_PROVIDER;

    static {
        final Iterable<InliningPolicyProvider> services = GraalServices.load(InliningPolicyProvider.class);
        final ArrayList<InliningPolicyProvider> providers = new ArrayList<>();
        for (InliningPolicyProvider provider : services) {
            providers.add(provider);
        }
        final String policy = TruffleCompilerOptions.getValue(TruffleCompilerOptions.TruffleInliningPolicy);
        POLICY_PROVIDER = policy.equals("") ? maxPriorityProvider(providers) : chosenProvider(providers, policy);
    }

    private final PartialEvaluator partialEvaluator;

    private final CallNodeProvider callNodeProvider;

    private final CompilableTruffleAST compilableTruffleAST;

    public AgnosticInliningPhase(PartialEvaluator partialEvaluator, CallNodeProvider callNodeProvider, CompilableTruffleAST compilableTruffleAST) {
        this.partialEvaluator = partialEvaluator;
        this.callNodeProvider = callNodeProvider;
        this.compilableTruffleAST = compilableTruffleAST;
    }

    private static InliningPolicyProvider chosenProvider(ArrayList<InliningPolicyProvider> providers, String name) {
        for (InliningPolicyProvider provider : providers) {
            if (provider.getName().equals(name)) {
                return provider;
            }
        }
        throw new IllegalStateException("No inlining policy provider with provided name: " + name);
    }

    private static InliningPolicyProvider maxPriorityProvider(ArrayList<InliningPolicyProvider> providers) {
        Collections.sort(providers);
        return providers.get(0);
    }

    @Override
    protected void run(StructuredGraph graph, CoreProviders coreProviders) {
        if (!TruffleCompilerOptions.getValue(SharedTruffleCompilerOptions.TruffleFunctionInlining)) {
            return;
        }
        final InliningPolicy policy = POLICY_PROVIDER.get(coreProviders, graph.getOptions());
        final CallTree tree = new CallTree(partialEvaluator, callNodeProvider, compilableTruffleAST, graph, policy);
        tree.dumpBasic("Before Inline", "");
        policy.run(tree);
        tree.dumpBasic("After Inline", "");
        tree.trace();
        tree.dequeueInlined();
    }
}
