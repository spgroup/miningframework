package com.oracle.svm.hosted.snippets;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
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
import org.graalvm.compiler.serviceprovider.JavaVersionUtil;
import org.graalvm.nativeimage.ImageSingletons;
import com.oracle.graal.pointsto.infrastructure.OriginalClassProvider;
import com.oracle.graal.pointsto.meta.AnalysisUniverse;
import com.oracle.svm.core.TypeResult;
import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.option.HostedOptionKey;
import com.oracle.svm.core.util.VMError;
import com.oracle.svm.hosted.ExceptionSynthesizer;
import com.oracle.svm.hosted.ImageClassLoader;
import com.oracle.svm.hosted.SVMHost;
import com.oracle.svm.hosted.c.GraalAccess;
import com.oracle.svm.hosted.phases.SubstrateClassInitializationPlugin;
import com.oracle.svm.hosted.substitute.AnnotationSubstitutionProcessor;
import com.oracle.svm.hosted.substitute.DeletedElementException;
import com.oracle.svm.util.ReflectionUtil;
import jdk.vm.ci.meta.JavaConstant;
import jdk.vm.ci.meta.JavaKind;
import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.meta.ResolvedJavaType;

public final class ReflectionPlugins {

    public static class ReflectionPluginRegistry extends IntrinsificationPluginRegistry {

        public static AutoCloseable startThreadLocalRegistry() {
            return ImageSingletons.lookup(ReflectionPluginRegistry.class).startThreadLocalReflectionRegistry();
        }
    }

    static class Options {

        @Option(help = "Enable trace logging for reflection plugins.")
        static final HostedOptionKey<Boolean> ReflectionPluginTracing = new HostedOptionKey<>(false);
    }

    private static final Object NULL_MARKER = new Object() {
    };

    private final ImageClassLoader imageClassLoader;

    private final SnippetReflectionProvider snippetReflection;

    private final AnnotationSubstitutionProcessor annotationSubstitutions;

    private final SVMHost hostVM;

    private final AnalysisUniverse aUniverse;

    private final boolean analysis;

    private final boolean hosted;

    private ReflectionPlugins(ImageClassLoader imageClassLoader, SnippetReflectionProvider snippetReflection, AnnotationSubstitutionProcessor annotationSubstitutions, SVMHost hostVM, AnalysisUniverse aUniverse, boolean analysis, boolean hosted) {
        this.imageClassLoader = imageClassLoader;
        this.snippetReflection = snippetReflection;
        this.annotationSubstitutions = annotationSubstitutions;
        this.hostVM = hostVM;
        this.aUniverse = aUniverse;
        this.analysis = analysis;
        this.hosted = hosted;
    }

    public static void registerInvocationPlugins(ImageClassLoader imageClassLoader, SnippetReflectionProvider snippetReflection, AnnotationSubstitutionProcessor annotationSubstitutions, InvocationPlugins plugins, SVMHost hostVM, AnalysisUniverse aUniverse, boolean analysis, boolean hosted) {
        if (hosted && analysis) {
            if (!ImageSingletons.contains(ReflectionPluginRegistry.class)) {
                ImageSingletons.add(ReflectionPluginRegistry.class, new ReflectionPluginRegistry());
            }
        }
        ReflectionPlugins rp = new ReflectionPlugins(imageClassLoader, snippetReflection, annotationSubstitutions, hostVM, aUniverse, analysis, hosted);
        rp.registerMethodHandlesPlugins(plugins);
        rp.registerClassPlugins(plugins);
    }

    private static final Set<Class<?>> ALLOWED_CONSTANT_CLASSES;

    static {
        ALLOWED_CONSTANT_CLASSES = new HashSet<>(Arrays.asList(Class.class, String.class, Method.class, Constructor.class, Field.class, MethodHandle.class, MethodHandles.Lookup.class, MethodType.class, ByteOrder.class));
        if (JavaVersionUtil.JAVA_SPEC >= 11) {
            try {
                ALLOWED_CONSTANT_CLASSES.add(Class.forName("java.lang.invoke.VarHandle"));
            } catch (ClassNotFoundException ex) {
                throw VMError.shouldNotReachHere(ex);
            }
        }
    }

    private void registerMethodHandlesPlugins(InvocationPlugins plugins) {
        registerFoldInvocationPlugins(plugins, MethodHandles.class, "publicLookup", "privateLookupIn", "arrayConstructor", "arrayLength", "arrayElementGetter", "arrayElementSetter", "arrayElementVarHandle", "byteArrayViewVarHandle", "byteBufferViewVarHandle");
        registerFoldInvocationPlugins(plugins, MethodHandles.Lookup.class, "in", "findStatic", "findVirtual", "findConstructor", "findClass", "accessClass", "findSpecial", "findGetter", "findSetter", "findVarHandle", "findStaticGetter", "findStaticSetter", "findStaticVarHandle", "unreflect", "unreflectSpecial", "unreflectConstructor", "unreflectGetter", "unreflectSetter", "unreflectVarHandle");
        registerFoldInvocationPlugins(plugins, MethodType.class, "methodType", "genericMethodType", "changeParameterType", "insertParameterTypes", "appendParameterTypes", "replaceParameterTypes", "dropParameterTypes", "changeReturnType", "erase", "generic", "wrap", "unwrap", "parameterType", "parameterCount", "returnType", "lastParameterType");
        Registration r = new Registration(plugins, MethodHandles.class);
        r.register0("lookup", new InvocationPlugin() {

            @Override
            public boolean apply(GraphBuilderContext b, ResolvedJavaMethod targetMethod, Receiver receiver) {
                return processMethodHandlesLookup(b, targetMethod);
            }
        });
    }

    private void registerClassPlugins(InvocationPlugins plugins) {
        registerFoldInvocationPlugins(plugins, Class.class, "getField", "getMethod", "getConstructor", "getDeclaredField", "getDeclaredMethod", "getDeclaredConstructor");
        Registration r = new Registration(plugins, Class.class);
        r.register1("forName", String.class, new InvocationPlugin() {

            @Override
            public boolean apply(GraphBuilderContext b, ResolvedJavaMethod targetMethod, Receiver receiver, ValueNode nameNode) {
                return processClassForName(b, targetMethod, nameNode, ConstantNode.forBoolean(true));
            }
        });
        r.register3("forName", String.class, boolean.class, ClassLoader.class, new InvocationPlugin() {

            @Override
            public boolean apply(GraphBuilderContext b, ResolvedJavaMethod targetMethod, Receiver receiver, ValueNode nameNode, ValueNode initializeNode, ValueNode classLoaderNode) {
                return processClassForName(b, targetMethod, nameNode, initializeNode);
            }
        });
    }

    private static final Constructor<MethodHandles.Lookup> LOOKUP_CONSTRUCTOR = ReflectionUtil.lookupConstructor(MethodHandles.Lookup.class, Class.class);

    private boolean processMethodHandlesLookup(GraphBuilderContext b, ResolvedJavaMethod targetMethod) {
        Supplier<String> targetParameters = () -> "";
        Class<?> callerClass = OriginalClassProvider.getJavaClass(snippetReflection, b.getMethod().getDeclaringClass());
        MethodHandles.Lookup lookup;
        try {
            lookup = LOOKUP_CONSTRUCTOR.newInstance(callerClass);
        } catch (Throwable ex) {
            return throwException(b, targetMethod, targetParameters, ex.getClass(), ex.getMessage());
        }
        return pushConstant(b, targetMethod, targetParameters, JavaKind.Object, lookup) != null;
    }

    private boolean processClassForName(GraphBuilderContext b, ResolvedJavaMethod targetMethod, ValueNode nameNode, ValueNode initializeNode) {
        Object classNameValue = unbox(b, nameNode, JavaKind.Object);
        Object initializeValue = unbox(b, initializeNode, JavaKind.Boolean);
        if (!(classNameValue instanceof String) || !(initializeValue instanceof Boolean)) {
            return false;
        }
        String className = (String) classNameValue;
        boolean initialize = (Boolean) initializeValue;
        Supplier<String> targetParameters = () -> className + ", " + initialize;
        TypeResult<Class<?>> typeResult = imageClassLoader.findClass(className);
        if (!typeResult.isPresent()) {
            Throwable e = typeResult.getException();
            return throwException(b, targetMethod, targetParameters, e.getClass(), e.getMessage());
        }
        Class<?> clazz = typeResult.get();
        JavaConstant classConstant = pushConstant(b, targetMethod, targetParameters, JavaKind.Object, clazz);
        if (classConstant == null) {
            return false;
        }
        if (initialize && hostVM.getClassInitializationSupport().shouldInitializeAtRuntime(clazz)) {
            SubstrateClassInitializationPlugin.emitEnsureClassInitialized(b, classConstant);
        }
        return true;
    }

    private void registerFoldInvocationPlugins(InvocationPlugins plugins, Class<?> declaringClass, String... methodNames) {
        Set<String> methodNamesSet = new HashSet<>(Arrays.asList(methodNames));
        for (Method method : declaringClass.getDeclaredMethods()) {
            if (methodNamesSet.contains(method.getName()) && !method.isSynthetic()) {
                registerFoldInvocationPlugin(plugins, method);
            }
        }
    }

    private void registerFoldInvocationPlugin(InvocationPlugins plugins, Method reflectionMethod) {
        if (!ALLOWED_CONSTANT_CLASSES.contains(reflectionMethod.getReturnType()) && !reflectionMethod.getReturnType().isPrimitive()) {
            throw VMError.shouldNotReachHere("Return type of method " + reflectionMethod + " is not on the allow-list for types that are immutable");
        }
        reflectionMethod.setAccessible(true);
        List<Class<?>> parameterTypes = new ArrayList<>();
        if (!Modifier.isStatic(reflectionMethod.getModifiers())) {
            parameterTypes.add(Receiver.class);
        }
        parameterTypes.addAll(Arrays.asList(reflectionMethod.getParameterTypes()));
        InvocationPlugin foldInvocationPlugin = new InvocationPlugin() {

            @Override
            public boolean defaultHandler(GraphBuilderContext b, ResolvedJavaMethod targetMethod, Receiver receiver, ValueNode... args) {
                return foldInvocationUsingReflection(b, targetMethod, reflectionMethod, receiver, args);
            }
        };
        plugins.register(foldInvocationPlugin, reflectionMethod.getDeclaringClass(), reflectionMethod.getName(), parameterTypes.toArray(new Class<?>[0]));
    }

    private boolean foldInvocationUsingReflection(GraphBuilderContext b, ResolvedJavaMethod targetMethod, Method reflectionMethod, Receiver receiver, ValueNode[] args) {
        assert b.getMetaAccess().lookupJavaMethod(reflectionMethod).equals(targetMethod) : "Fold method mismatch: " + reflectionMethod + " != " + targetMethod;
        Object receiverValue;
        if (targetMethod.isStatic()) {
            receiverValue = null;
        } else {
            receiverValue = unbox(b, receiver.get(), JavaKind.Object);
            if (receiverValue == null || receiverValue == NULL_MARKER) {
                return false;
            }
        }
        Object[] argValues = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object argValue = unbox(b, args[i], targetMethod.getSignature().getParameterKind(i));
            if (argValue == null) {
                return false;
            } else if (argValue == NULL_MARKER) {
                argValues[i] = null;
            } else {
                argValues[i] = argValue;
            }
        }
        Supplier<String> targetParameters = () -> (receiverValue == null ? "" : receiverValue.toString() + "; ") + Stream.of(argValues).map(arg -> arg instanceof Object[] ? Arrays.toString((Object[]) arg) : arg.toString()).collect(Collectors.joining(", "));
        Object returnValue;
        try {
            returnValue = reflectionMethod.invoke(receiverValue, argValues);
        } catch (InvocationTargetException ex) {
            return throwException(b, targetMethod, targetParameters, ex.getTargetException().getClass(), ex.getTargetException().getMessage());
        } catch (Throwable ex) {
            return throwException(b, targetMethod, targetParameters, ex.getClass(), ex.getMessage());
        }
        JavaKind returnKind = targetMethod.getSignature().getReturnKind();
        if (returnKind == JavaKind.Void) {
            traceConstant(b, targetMethod, targetParameters, JavaKind.Void);
            return true;
        }
        return pushConstant(b, targetMethod, targetParameters, returnKind, returnValue) != null;
    }

    private Object unbox(GraphBuilderContext b, ValueNode arg, JavaKind argKind) {
        if (!arg.isJavaConstant()) {
            return SubstrateGraphBuilderPlugins.extractClassArray(annotationSubstitutions, snippetReflection, arg, true);
        }
        JavaConstant argConstant = arg.asJavaConstant();
        if (argConstant.isNull()) {
            return NULL_MARKER;
        }
        switch(argKind) {
            case Boolean:
                return argConstant.asInt() != 0L;
            case Byte:
                return (byte) argConstant.asInt();
            case Short:
                return (short) argConstant.asInt();
            case Char:
                return (char) argConstant.asInt();
            case Int:
                return argConstant.asInt();
            case Long:
                return argConstant.asLong();
            case Float:
                return argConstant.asFloat();
            case Double:
                return argConstant.asDouble();
            case Object:
                return unboxObjectConstant(b, argConstant);
            default:
                throw VMError.shouldNotReachHere();
        }
    }

    private Object unboxObjectConstant(GraphBuilderContext b, JavaConstant argConstant) {
        ResolvedJavaType javaType = b.getConstantReflection().asJavaType(argConstant);
        if (javaType != null) {
            return OriginalClassProvider.getJavaClass(GraalAccess.getOriginalSnippetReflection(), javaType);
        }
        Object result = snippetReflection.asObject(Object.class, argConstant);
        if (ALLOWED_CONSTANT_CLASSES.contains(result.getClass())) {
            return result;
        }
        return null;
    }

    private <T> T getIntrinsic(GraphBuilderContext context, T element) {
        if (!hosted) {
            return element;
        }
        if (context.bciCanBeDuplicated()) {
            return null;
        }
        if (analysis) {
            if (isDeleted(element, context.getMetaAccess())) {
                return null;
            }
            Object replaced = aUniverse.replaceObject(element);
            ImageSingletons.lookup(ReflectionPluginRegistry.class).add(context.getCallingContext(), replaced);
        }
        return ImageSingletons.lookup(ReflectionPluginRegistry.class).get(context.getCallingContext());
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

    private JavaConstant pushConstant(GraphBuilderContext b, ResolvedJavaMethod targetMethod, Supplier<String> targetParameters, JavaKind returnKind, Object returnValue) {
        Object intrinsicValue = getIntrinsic(b, returnValue);
        if (intrinsicValue == null) {
            return null;
        }
        JavaConstant intrinsicConstant;
        if (returnKind.isPrimitive()) {
            intrinsicConstant = JavaConstant.forBoxedPrimitive(intrinsicValue);
        } else {
            intrinsicConstant = snippetReflection.forObject(intrinsicValue);
        }
        b.addPush(returnKind, ConstantNode.forConstant(intrinsicConstant, b.getMetaAccess()));
        traceConstant(b, targetMethod, targetParameters, intrinsicValue);
        return intrinsicConstant;
    }

    private boolean throwException(GraphBuilderContext b, ResolvedJavaMethod targetMethod, Supplier<String> targetParameters, Class<? extends Throwable> exceptionClass, String originalMessage) {
        Method exceptionMethod = ExceptionSynthesizer.throwExceptionMethodOrNull(exceptionClass, String.class);
        if (exceptionMethod == null) {
            return false;
        }
        Method intrinsic = getIntrinsic(b, exceptionMethod);
        if (intrinsic == null) {
            return false;
        }
        String message = originalMessage + ". This exception was synthesized during native image building from a call to " + targetMethod.format("%H.%n(%p)") + " with constant arguments.";
        ExceptionSynthesizer.throwException(b, exceptionMethod, message);
        traceException(b, targetMethod, targetParameters, exceptionClass);
        return true;
    }

    private static void traceConstant(GraphBuilderContext b, ResolvedJavaMethod targetMethod, Supplier<String> targetParameters, Object value) {
        if (Options.ReflectionPluginTracing.getValue()) {
            System.out.println("Call to " + targetMethod.format("%H.%n(%p)") + " reached in " + b.getMethod().format("%H.%n(%p)") + " with parameters (" + targetParameters.get() + ")" + " was reduced to the constant " + value);
        }
    }

    private static void traceException(GraphBuilderContext b, ResolvedJavaMethod targetMethod, Supplier<String> targetParameters, Class<? extends Throwable> exceptionClass) {
        if (Options.ReflectionPluginTracing.getValue()) {
            System.out.println("Call to " + targetMethod.format("%H.%n(%p)") + " reached in " + b.getMethod().format("%H.%n(%p)") + " with parameters (" + targetParameters.get() + ")" + " was reduced to a \"throw new " + exceptionClass.getName() + "(...)\"");
        }
    }
}