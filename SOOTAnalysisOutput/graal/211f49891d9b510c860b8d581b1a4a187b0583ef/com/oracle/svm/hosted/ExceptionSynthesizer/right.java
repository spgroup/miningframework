package com.oracle.svm.hosted;

import static jdk.vm.ci.meta.DeoptimizationAction.InvalidateReprofile;
import static jdk.vm.ci.meta.DeoptimizationReason.UnreachedCode;
import java.lang.reflect.GenericSignatureFormatError;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.graalvm.compiler.nodes.CallTargetNode.InvokeKind;
import org.graalvm.compiler.nodes.ConstantNode;
import org.graalvm.compiler.nodes.DeoptimizeNode;
import org.graalvm.compiler.nodes.Invoke;
import org.graalvm.compiler.nodes.ValueNode;
import org.graalvm.compiler.nodes.graphbuilderconf.GraphBuilderContext;
import com.oracle.svm.core.snippets.ImplicitExceptions;
import com.oracle.svm.core.util.VMError;
import jdk.vm.ci.meta.ResolvedJavaMethod;

public final class ExceptionSynthesizer {

    private static final Map<Key, Method> exceptionMethods = new HashMap<>();

    static {
        registerMethod(ClassNotFoundException.class, String.class);
        registerMethod(NoSuchFieldException.class, String.class);
        registerMethod(NoSuchMethodException.class, String.class);
        registerMethod(LinkageError.class, String.class);
        registerMethod(ClassCircularityError.class, String.class);
        registerMethod(IncompatibleClassChangeError.class, String.class);
        registerMethod(NoSuchFieldError.class, String.class);
        registerMethod(InstantiationError.class, String.class);
        registerMethod(NoSuchMethodError.class, String.class);
        registerMethod(IllegalAccessError.class, String.class);
        registerMethod(AbstractMethodError.class, String.class);
        registerMethod(BootstrapMethodError.class, String.class);
        registerMethod(ClassFormatError.class, String.class);
        registerMethod(GenericSignatureFormatError.class, String.class);
        registerMethod(UnsupportedClassVersionError.class, String.class);
        registerMethod(UnsatisfiedLinkError.class, String.class);
        registerMethod(NoClassDefFoundError.class, String.class);
        registerMethod(ExceptionInInitializerError.class, String.class);
        registerMethod(VerifyError.class, String.class);
        registerMethod(VerifyError.class);
    }

    private static void registerMethod(Class<?> exceptionClass) {
        try {
            exceptionMethods.put(Key.from(exceptionClass), ImplicitExceptions.class.getDeclaredMethod("throw" + exceptionClass.getSimpleName()));
        } catch (NoSuchMethodException ex) {
            throw VMError.shouldNotReachHere(ex);
        }
    }

    private static void registerMethod(Class<?> exceptionClass, Class<?> paramterClass) {
        try {
            exceptionMethods.put(Key.from(exceptionClass, paramterClass), ImplicitExceptions.class.getDeclaredMethod("throw" + exceptionClass.getSimpleName(), paramterClass));
        } catch (NoSuchMethodException ex) {
            throw VMError.shouldNotReachHere(ex);
        }
    }

    private ExceptionSynthesizer() {
    }

    public static Method throwExceptionMethod(Class<?>... methodDescriptor) {
        Method method = throwExceptionMethodOrNull(methodDescriptor);
        VMError.guarantee(method != null, "Exception synthesizer method " + Arrays.toString(methodDescriptor) + " not found.");
        return method;
    }

    public static Method throwExceptionMethodOrNull(Class<?>... methodDescriptor) {
        return exceptionMethods.get(Key.from(methodDescriptor));
    }

    public static void throwException(GraphBuilderContext b, Class<?> exceptionClass, String message) {
        throwException(b, throwExceptionMethod(exceptionClass, String.class), message);
    }

    public static void throwException(GraphBuilderContext b, Method throwExceptionMethod, String message) {
        ValueNode messageNode = ConstantNode.forConstant(b.getConstantReflection().forString(message), b.getMetaAccess(), b.getGraph());
        ResolvedJavaMethod exceptionMethod = b.getMetaAccess().lookupJavaMethod(throwExceptionMethod);
        assert exceptionMethod.isStatic();
        Invoke invoke = b.handleReplacedInvoke(InvokeKind.Static, exceptionMethod, new ValueNode[] { messageNode }, false);
        if (invoke != null) {
            b.add(new DeoptimizeNode(InvalidateReprofile, UnreachedCode));
        }
    }

    static final class Key {

        static Key from(Class<?>... values) {
            return new Key(values);
        }

        private final Class<?>[] elements;

        private Key(Class<?>[] values) {
            elements = values;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Key key = (Key) o;
            return Arrays.equals(elements, key.elements);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(elements);
        }
    }
}
