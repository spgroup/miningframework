package org.graalvm.compiler.replacements;

import org.graalvm.compiler.debug.Assertions;
import org.graalvm.compiler.replacements.nodes.AssertionNode;

public final class ReplacementsUtil {

    private ReplacementsUtil() {
    }

    public static final boolean REPLACEMENTS_ASSERTIONS_ENABLED = Assertions.assertionsEnabled();

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
