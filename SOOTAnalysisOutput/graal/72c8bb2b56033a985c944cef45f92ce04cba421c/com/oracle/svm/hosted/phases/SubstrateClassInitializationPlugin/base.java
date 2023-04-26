package com.oracle.svm.hosted.phases;

import java.lang.reflect.Method;
import java.util.function.Supplier;
import org.graalvm.compiler.nodes.CallTargetNode.InvokeKind;
import org.graalvm.compiler.nodes.ConstantNode;
import org.graalvm.compiler.nodes.FrameState;
import org.graalvm.compiler.nodes.ValueNode;
import org.graalvm.compiler.nodes.graphbuilderconf.ClassInitializationPlugin;
import org.graalvm.compiler.nodes.graphbuilderconf.GraphBuilderContext;
import com.oracle.svm.core.hub.DynamicHub;
import com.oracle.svm.core.meta.SubstrateObjectConstant;
import com.oracle.svm.core.util.VMError;
import com.oracle.svm.hosted.SVMHost;
import jdk.vm.ci.meta.ConstantPool;
import jdk.vm.ci.meta.JavaConstant;
import jdk.vm.ci.meta.ResolvedJavaType;

public class SubstrateClassInitializationPlugin implements ClassInitializationPlugin {

    public static final Method ENSURE_INITIALIZED_METHOD;

    static {
        try {
            ENSURE_INITIALIZED_METHOD = DynamicHub.class.getDeclaredMethod("ensureInitialized");
        } catch (ReflectiveOperationException ex) {
            throw VMError.shouldNotReachHere(ex);
        }
    }

    private final SVMHost host;

    public SubstrateClassInitializationPlugin(SVMHost host) {
        this.host = host;
    }

    @Override
    public boolean supportsLazyInitialization(ConstantPool cp) {
        return true;
    }

    @Override
    public void loadReferencedType(GraphBuilderContext builder, ConstantPool constantPool, int cpi, int bytecode) {
        constantPool.loadReferencedType(cpi, bytecode);
    }

    @Override
    public boolean apply(GraphBuilderContext builder, ResolvedJavaType type, Supplier<FrameState> frameState, ValueNode[] classInit) {
        if (needsRuntimeInitialization(builder.getMethod().getDeclaringClass(), type)) {
            emitEnsureClassInitialized(builder, SubstrateObjectConstant.forObject(host.dynamicHub(type)));
            if (classInit != null) {
                classInit[0] = null;
            }
            return true;
        }
        return false;
    }

    public static void emitEnsureClassInitialized(GraphBuilderContext builder, JavaConstant hubConstant) {
        ValueNode[] args = { ConstantNode.forConstant(hubConstant, builder.getMetaAccess(), builder.getGraph()) };
        builder.handleReplacedInvoke(InvokeKind.Special, builder.getMetaAccess().lookupJavaMethod(ENSURE_INITIALIZED_METHOD), args, false);
    }

    static boolean needsRuntimeInitialization(ResolvedJavaType declaringClass, ResolvedJavaType type) {
        return !declaringClass.equals(type) && !type.isInitialized() && !type.isArray();
    }
}
