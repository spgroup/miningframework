package com.oracle.svm.hosted.snippets;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.graalvm.compiler.api.replacements.SnippetReflectionProvider;
import org.graalvm.compiler.nodes.ConstantNode;
import org.graalvm.compiler.nodes.ValueNode;
import org.graalvm.compiler.nodes.graphbuilderconf.GraphBuilderContext;
import org.graalvm.compiler.nodes.graphbuilderconf.InvocationPlugin;
import org.graalvm.compiler.nodes.graphbuilderconf.InvocationPlugin.Receiver;
import org.graalvm.compiler.nodes.graphbuilderconf.InvocationPlugins;
import org.graalvm.compiler.nodes.graphbuilderconf.InvocationPlugins.Registration;
import org.graalvm.compiler.options.Option;
import org.graalvm.nativeimage.ImageSingletons;
import com.oracle.graal.pointsto.infrastructure.OriginalClassProvider;
import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.option.HostedOptionKey;
import com.oracle.svm.hosted.ExceptionSynthesizer;
import com.oracle.svm.hosted.ImageClassLoader;
import com.oracle.svm.hosted.SVMHost;
import com.oracle.svm.hosted.c.GraalAccess;
import com.oracle.svm.hosted.phases.SubstrateClassInitializationPlugin;
import com.oracle.svm.hosted.substitute.AnnotationSubstitutionProcessor;
import com.oracle.svm.hosted.substitute.DeletedElementException;
import jdk.vm.ci.meta.JavaConstant;
import jdk.vm.ci.meta.JavaKind;
import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.meta.ResolvedJavaType;

public class ReflectionPlugins {

    static class ReflectionPluginRegistry extends IntrinsificationPluginRegistry {
    }

    static class Options {

        @Option(help = "Enable trace logging for reflection plugins.")
        static final HostedOptionKey<Boolean> ReflectionPluginTracing = new HostedOptionKey<>(false);
    }

    public static void registerInvocationPlugins(ImageClassLoader imageClassLoader, SnippetReflectionProvider snippetReflection, AnnotationSubstitutionProcessor annotationSubstitutions, InvocationPlugins plugins, SVMHost hostVM, boolean analysis, boolean hosted) {
        if (hosted && analysis) {
            if (!ImageSingletons.contains(ReflectionPluginRegistry.class)) {
                ImageSingletons.add(ReflectionPluginRegistry.class, new ReflectionPluginRegistry());
            }
        }
        registerClassPlugins(imageClassLoader, snippetReflection, annotationSubstitutions, plugins, hostVM, analysis, hosted);
    }

    private static void registerClassPlugins(ImageClassLoader imageClassLoader, SnippetReflectionProvider snippetReflection, AnnotationSubstitutionProcessor annotationSubstitutions, InvocationPlugins plugins, SVMHost hostVM, boolean analysis, boolean hosted) {
        Registration r = new Registration(plugins, Class.class);
        r.register1("forName", String.class, new InvocationPlugin() {

            @Override
            public boolean apply(GraphBuilderContext b, ResolvedJavaMethod targetMethod, Receiver receiver, ValueNode name) {
                return processForName(b, hostVM, targetMethod, name, imageClassLoader, snippetReflection, analysis, hosted);
            }
        });
        r.register3("forName", String.class, boolean.class, ClassLoader.class, new InvocationPlugin() {

            @Override
            public boolean apply(GraphBuilderContext b, ResolvedJavaMethod targetMethod, Receiver receiver, ValueNode name, ValueNode initialize, ValueNode classLoader) {
                return processForName(b, hostVM, targetMethod, name, imageClassLoader, snippetReflection, analysis, hosted);
            }
        });
        r.register2("getDeclaredField", Receiver.class, String.class, new InvocationPlugin() {

            @Override
            public boolean apply(GraphBuilderContext b, ResolvedJavaMethod targetMethod, Receiver receiver, ValueNode name) {
                return processGetField(b, targetMethod, receiver, name, snippetReflection, true, analysis, hosted);
            }
        });
        r.register2("getField", Receiver.class, String.class, new InvocationPlugin() {

            @Override
            public boolean apply(GraphBuilderContext b, ResolvedJavaMethod targetMethod, Receiver receiver, ValueNode name) {
                return processGetField(b, targetMethod, receiver, name, snippetReflection, false, analysis, hosted);
            }
        });
        r.register3("getDeclaredMethod", Receiver.class, String.class, Class[].class, new InvocationPlugin() {

            @Override
            public boolean apply(GraphBuilderContext b, ResolvedJavaMethod targetMethod, Receiver receiver, ValueNode name, ValueNode parameterTypes) {
                return processGetMethod(b, targetMethod, receiver, name, parameterTypes, annotationSubstitutions, snippetReflection, true, analysis, hosted);
            }
        });
        r.register3("getMethod", Receiver.class, String.class, Class[].class, new InvocationPlugin() {

            @Override
            public boolean apply(GraphBuilderContext b, ResolvedJavaMethod targetMethod, Receiver receiver, ValueNode name, ValueNode parameterTypes) {
                return processGetMethod(b, targetMethod, receiver, name, parameterTypes, annotationSubstitutions, snippetReflection, false, analysis, hosted);
            }
        });
        r.register2("getDeclaredConstructor", Receiver.class, Class[].class, new InvocationPlugin() {

            @Override
            public boolean apply(GraphBuilderContext b, ResolvedJavaMethod targetMethod, Receiver receiver, ValueNode parameterTypes) {
                return processGetConstructor(b, targetMethod, receiver, parameterTypes, snippetReflection, annotationSubstitutions, true, analysis, hosted);
            }
        });
        r.register2("getConstructor", Receiver.class, Class[].class, new InvocationPlugin() {

            @Override
            public boolean apply(GraphBuilderContext b, ResolvedJavaMethod targetMethod, Receiver receiver, ValueNode parameterTypes) {
                return processGetConstructor(b, targetMethod, receiver, parameterTypes, snippetReflection, annotationSubstitutions, false, analysis, hosted);
            }
        });
    }

    private static boolean processForName(GraphBuilderContext b, SVMHost host, ResolvedJavaMethod targetMethod, ValueNode name, ImageClassLoader imageClassLoader, SnippetReflectionProvider snippetReflection, boolean analysis, boolean hosted) {
        if (name.isConstant()) {
            String className = snippetReflection.asObject(String.class, name.asJavaConstant());
            Class<?> clazz = imageClassLoader.findClassByName(className, false);
            if (clazz == null) {
                Method intrinsic = getIntrinsic(analysis, hosted, b, ExceptionSynthesizer.throwClassNotFoundExceptionMethod);
                if (intrinsic == null) {
                    return false;
                }
                throwClassNotFoundException(b, targetMethod, className);
            } else {
                Class<?> intrinsic = getIntrinsic(analysis, hosted, b, clazz);
                if (intrinsic == null) {
                    return false;
                }
                ResolvedJavaType type = b.getMetaAccess().lookupJavaType(clazz);
                JavaConstant hub = b.getConstantReflection().asJavaClass(type);
                pushConstant(b, targetMethod, hub, className);
                if (host.getClassInitializationSupport().shouldInitializeAtRuntime(clazz)) {
                    SubstrateClassInitializationPlugin.emitEnsureClassInitialized(b, hub);
                }
            }
            return true;
        }
        return false;
    }

    private static boolean processGetField(GraphBuilderContext b, ResolvedJavaMethod targetMethod, Receiver receiver, ValueNode name, SnippetReflectionProvider snippetReflection, boolean declared, boolean analysis, boolean hosted) {
        if (receiver.isConstant() && name.isConstant()) {
            Class<?> clazz = getReceiverClass(b, receiver);
            String fieldName = snippetReflection.asObject(String.class, name.asJavaConstant());
            String target = clazz.getTypeName() + "." + fieldName;
            try {
                Field field = declared ? clazz.getDeclaredField(fieldName) : clazz.getField(fieldName);
                Field intrinsic = getIntrinsic(analysis, hosted, b, field);
                if (intrinsic == null) {
                    return false;
                }
                pushConstant(b, targetMethod, snippetReflection.forObject(intrinsic), target);
            } catch (NoSuchFieldException e) {
                Method intrinsic = getIntrinsic(analysis, hosted, b, ExceptionSynthesizer.throwNoSuchFieldExceptionMethod);
                if (intrinsic == null) {
                    return false;
                }
                throwNoSuchFieldException(b, targetMethod, target);
            } catch (NoClassDefFoundError e) {
                Method intrinsic = getIntrinsic(analysis, hosted, b, ExceptionSynthesizer.throwNoClassDefFoundErrorMethod);
                if (intrinsic == null) {
                    return false;
                }
                throwNoClassDefFoundError(b, targetMethod, e.getMessage());
            }
            return true;
        }
        return false;
    }

    private static boolean processGetMethod(GraphBuilderContext b, ResolvedJavaMethod targetMethod, Receiver receiver, ValueNode name, ValueNode parameterTypes, AnnotationSubstitutionProcessor annotationSubstitutions, SnippetReflectionProvider snippetReflection, boolean declared, boolean analysis, boolean hosted) {
        if (receiver.isConstant() && name.isConstant()) {
            Class<?>[] paramTypes = SubstrateGraphBuilderPlugins.extractClassArray(annotationSubstitutions, snippetReflection, parameterTypes, true);
            if (paramTypes != null) {
                Class<?> clazz = getReceiverClass(b, receiver);
                String methodName = snippetReflection.asObject(String.class, name.asJavaConstant());
                String target = clazz.getTypeName() + "." + methodName + "(" + Stream.of(paramTypes).map(Class::getTypeName).collect(Collectors.joining(", ")) + ")";
                try {
                    Method method = declared ? clazz.getDeclaredMethod(methodName, paramTypes) : clazz.getMethod(methodName, paramTypes);
                    Method intrinsic = getIntrinsic(analysis, hosted, b, method);
                    if (intrinsic == null) {
                        return false;
                    }
                    pushConstant(b, targetMethod, snippetReflection.forObject(intrinsic), target);
                } catch (NoSuchMethodException e) {
                    Method intrinsic = getIntrinsic(analysis, hosted, b, ExceptionSynthesizer.throwNoSuchMethodExceptionMethod);
                    if (intrinsic == null) {
                        return false;
                    }
                    throwNoSuchMethodException(b, targetMethod, target);
                } catch (NoClassDefFoundError e) {
                    Method intrinsic = getIntrinsic(analysis, hosted, b, ExceptionSynthesizer.throwNoClassDefFoundErrorMethod);
                    if (intrinsic == null) {
                        return false;
                    }
                    throwNoClassDefFoundError(b, targetMethod, e.getMessage());
                }
                return true;
            }
        }
        return false;
    }

    private static boolean processGetConstructor(GraphBuilderContext b, ResolvedJavaMethod targetMethod, Receiver receiver, ValueNode parameterTypes, SnippetReflectionProvider snippetReflection, AnnotationSubstitutionProcessor annotationSubstitutions, boolean declared, boolean analysis, boolean hosted) {
        if (receiver.isConstant()) {
            Class<?>[] paramTypes = SubstrateGraphBuilderPlugins.extractClassArray(annotationSubstitutions, snippetReflection, parameterTypes, true);
            if (paramTypes != null) {
                Class<?> clazz = getReceiverClass(b, receiver);
                String target = clazz.getTypeName() + ".<init>(" + Stream.of(paramTypes).map(Class::getTypeName).collect(Collectors.joining(", ")) + ")";
                try {
                    Constructor<?> constructor = declared ? clazz.getDeclaredConstructor(paramTypes) : clazz.getConstructor(paramTypes);
                    Constructor<?> intrinsic = getIntrinsic(analysis, hosted, b, constructor);
                    if (intrinsic == null) {
                        return false;
                    }
                    pushConstant(b, targetMethod, snippetReflection.forObject(intrinsic), target);
                } catch (NoSuchMethodException e) {
                    Method intrinsic = getIntrinsic(analysis, hosted, b, ExceptionSynthesizer.throwNoSuchMethodExceptionMethod);
                    if (intrinsic == null) {
                        return false;
                    }
                    throwNoSuchMethodException(b, targetMethod, target);
                } catch (NoClassDefFoundError e) {
                    Method intrinsic = getIntrinsic(analysis, hosted, b, ExceptionSynthesizer.throwNoClassDefFoundErrorMethod);
                    if (intrinsic == null) {
                        return false;
                    }
                    throwNoClassDefFoundError(b, targetMethod, e.getMessage());
                }
                return true;
            }
        }
        return false;
    }

    private static Class<?> getReceiverClass(GraphBuilderContext b, Receiver receiver) {
        ResolvedJavaType javaType = b.getConstantReflection().asJavaType(receiver.get().asJavaConstant());
        return OriginalClassProvider.getJavaClass(GraalAccess.getOriginalSnippetReflection(), javaType);
    }

    private static <T> T getIntrinsic(boolean analysis, boolean hosted, GraphBuilderContext context, T element) {
        if (!hosted) {
            return element;
        }
        if (analysis) {
            if (isDeleted(element, context.getMetaAccess())) {
                return null;
            }
            ImageSingletons.lookup(ReflectionPluginRegistry.class).add(context.getMethod(), context.bci(), element);
            return element;
        }
        return ImageSingletons.lookup(ReflectionPluginRegistry.class).get(context.getMethod(), context.bci());
    }

    private static <T> boolean isDeleted(T element, MetaAccessProvider metaAccess) {
        AnnotatedElement annotated = null;
        try {
            if (element instanceof Executable) {
                annotated = metaAccess.lookupJavaMethod((Executable) element);
            } else if (element instanceof Field) {
                annotated = metaAccess.lookupJavaField((Field) element);
            }
        } catch (DeletedElementException ex) {
            return true;
        }
        if (annotated != null && annotated.isAnnotationPresent(Delete.class)) {
            return true;
        }
        return false;
    }

    private static void pushConstant(GraphBuilderContext b, ResolvedJavaMethod reflectionMethod, JavaConstant constant, String targetElement) {
        b.addPush(JavaKind.Object, ConstantNode.forConstant(constant, b.getMetaAccess(), b.getGraph()));
        traceConstant(b.getMethod(), reflectionMethod, targetElement);
    }

    private static void throwClassNotFoundException(GraphBuilderContext b, ResolvedJavaMethod reflectionMethod, String targetClass) {
        String message = targetClass + ". This exception was synthesized during native image building from a call to " + reflectionMethod.format("%H.%n(%p)") + " with a constant class name argument.";
        ExceptionSynthesizer.throwException(b, message, ExceptionSynthesizer.throwClassNotFoundExceptionMethod);
        traceException(b.getMethod(), reflectionMethod, targetClass, ExceptionSynthesizer.throwClassNotFoundExceptionMethod);
    }

    private static void throwNoClassDefFoundError(GraphBuilderContext b, ResolvedJavaMethod reflectionMethod, String targetClass) {
        String message = targetClass + ". This exception was synthesized during native image building from a call to " + reflectionMethod.format("%H.%n(%p)") + " with constant arguments.";
        ExceptionSynthesizer.throwException(b, message, ExceptionSynthesizer.throwNoClassDefFoundErrorMethod);
        traceException(b.getMethod(), reflectionMethod, targetClass, ExceptionSynthesizer.throwNoClassDefFoundErrorMethod);
    }

    private static void throwNoSuchFieldException(GraphBuilderContext b, ResolvedJavaMethod reflectionMethod, String targetField) {
        String message = targetField + ". This exception was synthesized during native image building from a call to " + reflectionMethod.format("%H.%n(%p)") + " with a constant field name argument.";
        ExceptionSynthesizer.throwException(b, message, ExceptionSynthesizer.throwNoSuchFieldExceptionMethod);
        traceException(b.getMethod(), reflectionMethod, targetField, ExceptionSynthesizer.throwNoSuchFieldExceptionMethod);
    }

    private static void throwNoSuchMethodException(GraphBuilderContext b, ResolvedJavaMethod reflectionMethod, String targetMethod) {
        String message = targetMethod + ". This exception was synthesized during native image building from a call to " + reflectionMethod.format("%H.%n(%p)") + " with constant method name and parameter types arguments.";
        ExceptionSynthesizer.throwException(b, message, ExceptionSynthesizer.throwNoSuchMethodExceptionMethod);
        traceException(b.getMethod(), reflectionMethod, targetMethod, ExceptionSynthesizer.throwNoSuchMethodExceptionMethod);
    }

    private static void traceConstant(ResolvedJavaMethod contextMethod, ResolvedJavaMethod reflectionMethod, String targetElement) {
        if (Options.ReflectionPluginTracing.getValue()) {
            System.out.println("Call to " + reflectionMethod.format("%H.%n(%p)") + " reached in " + contextMethod.format("%H.%n(%p)") + " for target " + targetElement + " was reduced to a constant.");
        }
    }

    private static void traceException(ResolvedJavaMethod contextMethod, ResolvedJavaMethod reflectionMethod, String targetElement, Method exceptionMethod) {
        if (Options.ReflectionPluginTracing.getValue()) {
            String exception = exceptionMethod.getExceptionTypes()[0].getName();
            System.out.println("Call to " + reflectionMethod.format("%H.%n(%p)") + " reached in " + contextMethod.format("%H.%n(%p)") + " for target " + targetElement + " was reduced to a \"throw new " + exception + "(...)\"");
        }
    }
}
