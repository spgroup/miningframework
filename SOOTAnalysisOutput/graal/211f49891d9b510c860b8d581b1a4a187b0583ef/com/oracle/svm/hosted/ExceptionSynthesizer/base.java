package com.oracle.svm.hosted;

import static jdk.vm.ci.meta.DeoptimizationAction.InvalidateReprofile;
import static jdk.vm.ci.meta.DeoptimizationReason.UnreachedCode;
import java.lang.reflect.Method;
import org.graalvm.compiler.nodes.CallTargetNode.InvokeKind;
import org.graalvm.compiler.nodes.ConstantNode;
import org.graalvm.compiler.nodes.DeoptimizeNode;
import org.graalvm.compiler.nodes.Invoke;
import org.graalvm.compiler.nodes.ValueNode;
import org.graalvm.compiler.nodes.graphbuilderconf.GraphBuilderContext;
import com.oracle.svm.core.meta.SubstrateObjectConstant;
import com.oracle.svm.core.snippets.ImplicitExceptions;
import com.oracle.svm.core.util.VMError;
import jdk.vm.ci.meta.ResolvedJavaMethod;

public class ExceptionSynthesizer {

    public static final Method throwClassNotFoundExceptionMethod;

    public static final Method throwNoSuchFieldExceptionMethod;

    public static final Method throwNoSuchMethodExceptionMethod;

    public static final Method throwNoClassDefFoundErrorMethod;

    public static final Method throwNoSuchFieldErrorMethod;

    public static final Method throwNoSuchMethodErrorMethod;

    public static final Method throwVerifyErrorMethod;

    static {
        try {
            throwClassNotFoundExceptionMethod = ImplicitExceptions.class.getDeclaredMethod("throwClassNotFoundException", String.class);
            throwNoSuchFieldExceptionMethod = ImplicitExceptions.class.getDeclaredMethod("throwNoSuchFieldException", String.class);
            throwNoSuchMethodExceptionMethod = ImplicitExceptions.class.getDeclaredMethod("throwNoSuchMethodException", String.class);
            throwNoClassDefFoundErrorMethod = ImplicitExceptions.class.getDeclaredMethod("throwNoClassDefFoundError", String.class);
            throwNoSuchFieldErrorMethod = ImplicitExceptions.class.getDeclaredMethod("throwNoSuchFieldError", String.class);
            throwNoSuchMethodErrorMethod = ImplicitExceptions.class.getDeclaredMethod("throwNoSuchMethodError", String.class);
            throwVerifyErrorMethod = ImplicitExceptions.class.getDeclaredMethod("throwVerifyError");
        } catch (NoSuchMethodException ex) {
            throw VMError.shouldNotReachHere(ex);
        }
    }

    public static void throwClassNotFoundException(GraphBuilderContext b, String targetClass) {
        throwException(b, targetClass, throwClassNotFoundExceptionMethod);
    }

    public static void throwNoSuchFieldException(GraphBuilderContext b, String targetField) {
        throwException(b, targetField, throwNoSuchFieldExceptionMethod);
    }

    public static void throwNoSuchMethodException(GraphBuilderContext b, String targetMethod) {
        throwException(b, targetMethod, throwNoSuchMethodExceptionMethod);
    }

    public static void throwNoClassDefFoundError(GraphBuilderContext b, String targetField) {
        throwException(b, targetField, throwNoClassDefFoundErrorMethod);
    }

    public static void throwNoSuchFieldError(GraphBuilderContext b, String targetField) {
        throwException(b, targetField, throwNoSuchFieldErrorMethod);
    }

    public static void throwNoSuchMethodError(GraphBuilderContext b, String targetMethod) {
        throwException(b, targetMethod, throwNoSuchMethodErrorMethod);
    }

    public static void throwException(GraphBuilderContext b, String message, Method reportExceptionMethod) {
        ValueNode messageNode = ConstantNode.forConstant(SubstrateObjectConstant.forObject(message), b.getMetaAccess(), b.getGraph());
        ResolvedJavaMethod exceptionMethod = b.getMetaAccess().lookupJavaMethod(reportExceptionMethod);
        assert exceptionMethod.isStatic();
        Invoke invoke = b.handleReplacedInvoke(InvokeKind.Static, exceptionMethod, new ValueNode[] { messageNode }, false);
        if (invoke != null) {
            b.add(new DeoptimizeNode(InvalidateReprofile, UnreachedCode));
        }
    }
}
