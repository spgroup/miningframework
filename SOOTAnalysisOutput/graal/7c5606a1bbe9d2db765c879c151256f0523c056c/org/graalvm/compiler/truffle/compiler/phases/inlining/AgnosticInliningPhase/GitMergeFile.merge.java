package org.graalvm.compiler.truffle.compiler.phases.inlining;

import static org.graalvm.compiler.truffle.compiler.TruffleCompilerOptions.getPolyglotOptionValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.spi.CoreProviders;
import org.graalvm.compiler.phases.BasePhase;
import org.graalvm.compiler.serviceprovider.GraalServices;
import org.graalvm.compiler.truffle.common.CallNodeProvider;
import org.graalvm.compiler.truffle.common.CompilableTruffleAST;
import org.graalvm.compiler.truffle.compiler.PartialEvaluator;
import org.graalvm.compiler.truffle.options.PolyglotCompilerOptions;
import org.graalvm.options.OptionValues;

public final class AgnosticInliningPhase extends BasePhase<CoreProviders> {

    private static final ArrayList<InliningPolicyProvider> POLICY_PROVIDERS;

    static {
        final Iterable<InliningPolicyProvider> services = GraalServices.load(InliningPolicyProvider.class);
        final ArrayList<InliningPolicyProvider> providers = new ArrayList<>();
        for (InliningPolicyProvider provider : services) {
            providers.add(provider);
        }
        Collections.sort(providers);
        POLICY_PROVIDERS = providers;
    }

    private final PartialEvaluator partialEvaluator;

    private final CallNodeProvider callNodeProvider;

    private final CompilableTruffleAST compilableTruffleAST;

    private final OptionValues options;

    public AgnosticInliningPhase(OptionValues options, PartialEvaluator partialEvaluator, CallNodeProvider callNodeProvider, CompilableTruffleAST compilableTruffleAST) {
        this.options = options;
        this.partialEvaluator = partialEvaluator;
        this.callNodeProvider = callNodeProvider;
        this.compilableTruffleAST = compilableTruffleAST;
    }

    private static InliningPolicyProvider chosenProvider(List<? extends InliningPolicyProvider> providers, String name) {
        for (InliningPolicyProvider provider : providers) {
            if (provider.getName().equals(name)) {
                return provider;
            }
        }
        throw new IllegalStateException("No inlining policy provider with provided name: " + name);
    }

    private InliningPolicyProvider getInliningPolicyProvider() {
        final String policy = getPolyglotOptionValue(options, PolyglotCompilerOptions.InliningPolicy);
        return policy.equals("") ? POLICY_PROVIDERS.get(0) : chosenProvider(POLICY_PROVIDERS, policy);
    }

    @Override
    protected void run(StructuredGraph graph, CoreProviders coreProviders) {
        if (!getPolyglotOptionValue(options, PolyglotCompilerOptions.Inlining)) {
            return;
        }
        final InliningPolicy policy = getInliningPolicyProvider().get(options, coreProviders);
        final CallTree tree = new CallTree(options, partialEvaluator, callNodeProvider, compilableTruffleAST, graph, policy);
        tree.dumpBasic("Before Inline", "");
        policy.run(tree);
        tree.dumpBasic("After Inline", "");
        tree.trace();
        tree.dequeueInlined();
    }
}
