package org.graalvm.compiler.replacements;

import org.graalvm.compiler.core.common.GraalOptions;
import org.graalvm.compiler.replacements.nodes.AssertionNode;

public final class ReplacementsUtil {

    private ReplacementsUtil() {
    }

    public static final boolean REPLACEMENTS_ASSERTIONS_ENABLED;

    static {
        boolean assertionsEnabled = false;
        assert (assertionsEnabled = true) != false;
        REPLACEMENTS_ASSERTIONS_ENABLED = assertionsEnabled && GraalOptions.ImmutableCode.getValue() == false;
    }

    public static void staticAssert(boolean condition, String message) {
        if (REPLACEMENTS_ASSERTIONS_ENABLED) {
            AssertionNode.assertion(true, condition, message);
        }
    }

    public static void runtimeAssert(boolean condition, String message) {
        if (REPLACEMENTS_ASSERTIONS_ENABLED) {
            AssertionNode.assertion(false, condition, message);
        }
    }
}
