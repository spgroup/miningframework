package org.graalvm.compiler.replacements;

import static org.graalvm.compiler.nodes.graphbuilderconf.InlineInvokePlugin.InlineInfo.createStandardInlineInfo;
import java.lang.reflect.Method;
import org.graalvm.compiler.api.directives.GraalDirectives;
import org.graalvm.compiler.debug.GraalError;
import org.graalvm.compiler.nodes.ValueNode;
import org.graalvm.compiler.nodes.graphbuilderconf.GraphBuilderContext;
import org.graalvm.compiler.nodes.graphbuilderconf.InlineInvokePlugin;
import jdk.vm.ci.meta.ResolvedJavaMethod;

public final class InlineGraalDirectivesPlugin implements InlineInvokePlugin {

    private static final Method ROOTNAME;

    static {
        try {
            ROOTNAME = GraalDirectives.class.getDeclaredMethod("rootName");
        } catch (Exception e) {
            throw new GraalError("unable to find GraalDirectives.rootName()", e);
        }
    }

    @Override
    public InlineInfo shouldInlineInvoke(GraphBuilderContext b, ResolvedJavaMethod method, ValueNode[] args) {
        if (method.equals(b.getMetaAccess().lookupJavaMethod(ROOTNAME))) {
            return createStandardInlineInfo(method);
        }
        return null;
    }
}