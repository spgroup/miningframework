package com.oracle.svm.hosted.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.graalvm.compiler.api.replacements.SnippetReflectionProvider;
import org.graalvm.compiler.core.common.type.StampFactory;
import org.graalvm.compiler.core.common.type.StampPair;
import org.graalvm.compiler.core.common.type.TypeReference;
import org.graalvm.compiler.debug.DebugContext;
import org.graalvm.compiler.java.FrameStateBuilder;
import org.graalvm.compiler.nodes.CallTargetNode.InvokeKind;
import org.graalvm.compiler.nodes.ConstantNode;
import org.graalvm.compiler.nodes.DeoptimizeNode;
import org.graalvm.compiler.nodes.LogicNode;
import org.graalvm.compiler.nodes.PiNode;
import org.graalvm.compiler.nodes.ReturnNode;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.UnwindNode;
import org.graalvm.compiler.nodes.ValueNode;
import org.graalvm.compiler.nodes.calc.AddNode;
import org.graalvm.compiler.nodes.calc.IntegerEqualsNode;
import org.graalvm.compiler.nodes.calc.ObjectEqualsNode;
import org.graalvm.compiler.nodes.calc.XorNode;
import org.graalvm.compiler.nodes.extended.BoxNode;
import org.graalvm.compiler.nodes.extended.BranchProbabilityNode;
import org.graalvm.compiler.nodes.java.ArrayLengthNode;
import org.graalvm.compiler.nodes.java.InstanceOfNode;
import org.graalvm.compiler.nodes.java.LoadFieldNode;
import org.graalvm.compiler.replacements.nodes.BasicObjectCloneNode;
import org.graalvm.nativeimage.hosted.Feature;
import com.oracle.graal.pointsto.constraints.UnsupportedFeatureException;
import com.oracle.graal.pointsto.meta.HostedProviders;
import com.oracle.svm.core.SubstrateAnnotationInvocationHandler;
import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.jdk.AnnotationSupportConfig;
import com.oracle.svm.core.meta.SubstrateObjectConstant;
import com.oracle.svm.core.util.VMError;
import com.oracle.svm.hosted.analysis.Inflation;
import com.oracle.svm.hosted.phases.HostedGraphKit;
import com.oracle.svm.hosted.snippets.SubstrateGraphBuilderPlugins;
import jdk.vm.ci.meta.DeoptimizationAction;
import jdk.vm.ci.meta.DeoptimizationReason;
import jdk.vm.ci.meta.JavaConstant;
import jdk.vm.ci.meta.JavaKind;
import jdk.vm.ci.meta.JavaType;
import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.ResolvedJavaField;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.meta.ResolvedJavaType;
import sun.reflect.annotation.TypeNotPresentExceptionProxy;

public class AnnotationSupport extends CustomSubstitution<AnnotationSubstitutionType> {

    static final Class<?> constantAnnotationMarkerInterface = java.lang.Override.class;

    private final SnippetReflectionProvider snippetReflection;

    private final ResolvedJavaType javaLangAnnotationAnnotation;

    private final ResolvedJavaType javaLangReflectProxy;

    private final ResolvedJavaType constantAnnotationMarker;

    public AnnotationSupport(MetaAccessProvider metaAccess, SnippetReflectionProvider snippetReflection) {
        super(metaAccess);
        this.snippetReflection = snippetReflection;
        javaLangAnnotationAnnotation = metaAccess.lookupJavaType(java.lang.annotation.Annotation.class);
        javaLangReflectProxy = metaAccess.lookupJavaType(java.lang.reflect.Proxy.class);
        constantAnnotationMarker = metaAccess.lookupJavaType(constantAnnotationMarkerInterface);
        AnnotationSupportConfig.initialize();
    }

    private boolean isConstantAnnotationType(ResolvedJavaType type) {
        return javaLangAnnotationAnnotation.isAssignableFrom(type) && javaLangReflectProxy.isAssignableFrom(type) && constantAnnotationMarker.isAssignableFrom(type);
    }

    @Override
    public ResolvedJavaType lookup(ResolvedJavaType type) {
        if (isConstantAnnotationType(type)) {
            return getSubstitution(type);
        }
        return type;
    }

    @Override
    public ResolvedJavaType resolve(ResolvedJavaType type) {
        if (type instanceof AnnotationSubstitutionType) {
            return ((AnnotationSubstitutionType) type).original;
        }
        return type;
    }

    @Override
    public ResolvedJavaField lookup(ResolvedJavaField field) {
        if (isConstantAnnotationType(field.getDeclaringClass())) {
            throw new UnsupportedFeatureException("Field of annotation proxy is not accessible: " + field);
        }
        return field;
    }

    @Override
    public ResolvedJavaMethod lookup(ResolvedJavaMethod method) {
        if (isConstantAnnotationType(method.getDeclaringClass())) {
            AnnotationSubstitutionType declaringClass = getSubstitution(method.getDeclaringClass());
            AnnotationSubstitutionMethod result = declaringClass.getSubstitutionMethod(method);
            assert result != null && result.original.equals(method);
            return result;
        }
        return method;
    }

    @Override
    public ResolvedJavaMethod resolve(ResolvedJavaMethod method) {
        if (method instanceof AnnotationSubstitutionMethod) {
            return ((AnnotationSubstitutionMethod) method).original;
        }
        return method;
    }

    private synchronized AnnotationSubstitutionType getSubstitution(ResolvedJavaType type) {
        AnnotationSubstitutionType result = getSubstitutionType(type);
        if (result == null) {
            result = new AnnotationSubstitutionType(metaAccess, type);
            for (ResolvedJavaMethod originalMethod : type.getDeclaredMethods()) {
                AnnotationSubstitutionMethod substitutionMethod;
                String methodName = canonicalMethodName(originalMethod);
                if (methodName.equals("equals")) {
                    substitutionMethod = new AnnotationEqualsMethod(originalMethod);
                } else if (methodName.equals("hashCode")) {
                    substitutionMethod = new AnnotationHashCodeMethod(originalMethod);
                } else if (methodName.equals("toString")) {
                    substitutionMethod = new AnnotationToStringMethod(originalMethod);
                } else if (methodName.equals("annotationType")) {
                    substitutionMethod = new AnnotationAnnotationTypeMethod(originalMethod);
                } else {
                    substitutionMethod = new AnnotationAccessorMethod(originalMethod);
                    result.addSubstitutionField(new AnnotationSubstitutionField(result, originalMethod, snippetReflection, metaAccess));
                }
                result.addSubstitutionMethod(originalMethod, substitutionMethod);
            }
            for (ResolvedJavaMethod originalMethod : type.getDeclaredConstructors()) {
                AnnotationSubstitutionMethod substitutionMethod = new AnnotationConstructorMethod(originalMethod);
                result.addSubstitutionMethod(originalMethod, substitutionMethod);
            }
            typeSubstitutions.put(type, result);
        }
        return result;
    }

    static class AnnotationConstructorMethod extends AnnotationSubstitutionMethod {

        AnnotationConstructorMethod(ResolvedJavaMethod original) {
            super(original);
        }

        @Override
        public StructuredGraph buildGraph(DebugContext debug, ResolvedJavaMethod method, HostedProviders providers, Purpose purpose) {
            HostedGraphKit kit = new HostedGraphKit(debug, providers, method);
            StructuredGraph graph = kit.getGraph();
            graph.addAfterFixed(graph.start(), graph.add(new DeoptimizeNode(DeoptimizationAction.None, DeoptimizationReason.UnreachedCode)));
            return graph;
        }
    }

    static boolean isClassType(JavaType type, MetaAccessProvider metaAccess) {
        return type.getJavaKind() == JavaKind.Object && (type.equals(metaAccess.lookupJavaType(Class.class)) || type.equals(metaAccess.lookupJavaType(Class[].class)));
    }

    static class AnnotationAccessorMethod extends AnnotationSubstitutionMethod {

        AnnotationAccessorMethod(ResolvedJavaMethod original) {
            super(original);
        }

        @Override
        public StructuredGraph buildGraph(DebugContext debug, ResolvedJavaMethod method, HostedProviders providers, Purpose purpose) {
            ResolvedJavaType annotationType = method.getDeclaringClass();
            assert !Modifier.isStatic(method.getModifiers()) && method.getSignature().getParameterCount(false) == 0;
            HostedGraphKit kit = new HostedGraphKit(debug, providers, method);
            StructuredGraph graph = kit.getGraph();
            FrameStateBuilder state = new FrameStateBuilder(null, method, graph);
            state.initializeForMethodStart(null, true, providers.getGraphBuilderPlugins());
            int bci = 0;
            graph.start().setStateAfter(state.create(bci++, graph.start()));
            ValueNode receiver = state.loadLocal(0, JavaKind.Object);
            ResolvedJavaField field = findField(annotationType, canonicalMethodName(method));
            ValueNode loadField = kit.append(LoadFieldNode.create(null, receiver, field));
            ResolvedJavaType resultType = method.getSignature().getReturnType(null).resolve(null);
            if (isClassType(resultType, providers.getMetaAccess())) {
                ResolvedJavaType exceptionProxyType = providers.getMetaAccess().lookupJavaType(TypeNotPresentExceptionProxy.class);
                TypeReference exceptionProxyTypeRef = TypeReference.createTrusted(kit.getAssumptions(), exceptionProxyType);
                LogicNode condition = kit.append(InstanceOfNode.create(exceptionProxyTypeRef, loadField));
                kit.startIf(condition, BranchProbabilityNode.SLOW_PATH_PROBABILITY);
                kit.thenPart();
                PiNode casted = kit.createPiNode(loadField, StampFactory.object(exceptionProxyTypeRef, true));
                ResolvedJavaMethod generateExceptionMethod = kit.findMethod(TypeNotPresentExceptionProxy.class, "generateException", false);
                ValueNode exception = kit.createJavaCallWithExceptionAndUnwind(InvokeKind.Virtual, generateExceptionMethod, casted);
                kit.append(new UnwindNode(exception));
                kit.elsePart();
                TypeReference resultTypeRef = TypeReference.createTrusted(kit.getAssumptions(), resultType);
                loadField = kit.createPiNode(loadField, StampFactory.object(resultTypeRef, true));
                kit.endIf();
            }
            if (resultType.isArray()) {
                ValueNode arrayLength = kit.append(new ArrayLengthNode(loadField));
                kit.startIf(graph.unique(new IntegerEqualsNode(arrayLength, ConstantNode.forInt(0, graph))), BranchProbabilityNode.NOT_LIKELY_PROBABILITY);
                kit.elsePart();
                ResolvedJavaMethod cloneMethod = kit.findMethod(Object.class, "clone", false);
                JavaType returnType = cloneMethod.getSignature().getReturnType(null);
                StampPair returnStampPair = StampFactory.forDeclaredType(null, returnType, false);
                BasicObjectCloneNode cloned = kit.append(SubstrateGraphBuilderPlugins.objectCloneNode(InvokeKind.Virtual, bci++, returnStampPair, cloneMethod, loadField));
                state.push(returnType.getJavaKind(), cloned);
                cloned.setStateAfter(state.create(bci, cloned));
                state.pop(returnType.getJavaKind());
                ValueNode casted = kit.unique(new PiNode(cloned, resultType, false, false));
                kit.append(new ReturnNode(casted));
                kit.endIf();
            }
            kit.append(new ReturnNode(loadField));
            return kit.finalizeGraph();
        }
    }

    static class AnnotationAnnotationTypeMethod extends AnnotationSubstitutionMethod {

        AnnotationAnnotationTypeMethod(ResolvedJavaMethod original) {
            super(original);
        }

        @Override
        public StructuredGraph buildGraph(DebugContext debug, ResolvedJavaMethod method, HostedProviders providers, Purpose purpose) {
            ResolvedJavaType annotationType = method.getDeclaringClass();
            ResolvedJavaType annotationInterfaceType = findAnnotationInterfaceType(annotationType);
            JavaConstant returnValue = providers.getConstantReflection().asJavaClass(annotationInterfaceType);
            HostedGraphKit kit = new HostedGraphKit(debug, providers, method);
            ValueNode returnConstant = kit.unique(ConstantNode.forConstant(returnValue, providers.getMetaAccess()));
            kit.append(new ReturnNode(returnConstant));
            return kit.finalizeGraph();
        }
    }

    static class AnnotationEqualsMethod extends AnnotationSubstitutionMethod {

        AnnotationEqualsMethod(ResolvedJavaMethod original) {
            super(original);
        }

        @Override
        public StructuredGraph buildGraph(DebugContext debug, ResolvedJavaMethod method, HostedProviders providers, Purpose purpose) {
            assert !Modifier.isStatic(method.getModifiers()) && method.getSignature().getParameterCount(false) == 1;
            ResolvedJavaType annotationType = method.getDeclaringClass();
            ResolvedJavaType annotationInterfaceType = findAnnotationInterfaceType(annotationType);
            HostedGraphKit kit = new HostedGraphKit(debug, providers, method);
            StructuredGraph graph = kit.getGraph();
            FrameStateBuilder state = new FrameStateBuilder(null, method, graph);
            state.initializeForMethodStart(null, true, providers.getGraphBuilderPlugins());
            int bci = 0;
            graph.start().setStateAfter(state.create(bci++, graph.start()));
            ValueNode receiver = state.loadLocal(0, JavaKind.Object);
            ValueNode other = state.loadLocal(1, JavaKind.Object);
            ValueNode trueValue = ConstantNode.forBoolean(true, graph);
            ValueNode falseValue = ConstantNode.forBoolean(false, graph);
            kit.startIf(graph.unique(new ObjectEqualsNode(receiver, other)), BranchProbabilityNode.LIKELY_PROBABILITY);
            kit.thenPart();
            kit.append(new ReturnNode(trueValue));
            kit.endIf();
            kit.startIf(graph.unique(InstanceOfNode.create(TypeReference.createTrustedWithoutAssumptions(annotationInterfaceType), other)), BranchProbabilityNode.NOT_LIKELY_PROBABILITY);
            kit.elsePart();
            kit.append(new ReturnNode(falseValue));
            kit.endIf();
            for (String attribute : findAttributes(annotationType)) {
                ResolvedJavaField ourField = findField(annotationType, attribute);
                ResolvedJavaMethod otherMethod = findMethod(annotationInterfaceType, attribute);
                ResolvedJavaType attributeType = ourField.getType().resolve(null);
                ValueNode otherAttribute = kit.createInvokeWithExceptionAndUnwind(otherMethod, InvokeKind.Interface, state, bci++, bci++, other);
                ValueNode ourAttribute = kit.append(LoadFieldNode.create(null, receiver, ourField));
                if (attributeType.isPrimitive()) {
                    ResolvedJavaType boxedAttributeType = providers.getMetaAccess().lookupJavaType(attributeType.getJavaKind().toBoxedJavaClass());
                    ourAttribute = kit.append(new BoxNode(ourAttribute, boxedAttributeType, attributeType.getJavaKind()));
                    otherAttribute = kit.append(new BoxNode(otherAttribute, boxedAttributeType, attributeType.getJavaKind()));
                }
                ValueNode attributeEqual;
                if (attributeType.isArray()) {
                    ResolvedJavaMethod m = findMethod(providers.getMetaAccess().lookupJavaType(Arrays.class), "equals", attributeType, attributeType);
                    attributeEqual = kit.createInvokeWithExceptionAndUnwind(m, InvokeKind.Static, state, bci++, bci++, ourAttribute, otherAttribute);
                } else {
                    ResolvedJavaMethod m = kit.findMethod(Object.class, "equals", false);
                    attributeEqual = kit.createInvokeWithExceptionAndUnwind(m, InvokeKind.Virtual, state, bci++, bci++, ourAttribute, otherAttribute);
                }
                kit.startIf(graph.unique(new IntegerEqualsNode(attributeEqual, trueValue)), BranchProbabilityNode.LIKELY_PROBABILITY);
                kit.elsePart();
                kit.append(new ReturnNode(falseValue));
                kit.endIf();
            }
            kit.append(new ReturnNode(trueValue));
            return kit.finalizeGraph();
        }
    }

    static class AnnotationHashCodeMethod extends AnnotationSubstitutionMethod {

        AnnotationHashCodeMethod(ResolvedJavaMethod original) {
            super(original);
        }

        @Override
        public StructuredGraph buildGraph(DebugContext debug, ResolvedJavaMethod method, HostedProviders providers, Purpose purpose) {
            assert !Modifier.isStatic(method.getModifiers()) && method.getSignature().getParameterCount(false) == 0;
            ResolvedJavaType annotationType = method.getDeclaringClass();
            HostedGraphKit kit = new HostedGraphKit(debug, providers, method);
            StructuredGraph graph = kit.getGraph();
            FrameStateBuilder state = new FrameStateBuilder(null, method, graph);
            state.initializeForMethodStart(null, true, providers.getGraphBuilderPlugins());
            int bci = 0;
            graph.start().setStateAfter(state.create(bci++, graph.start()));
            ValueNode receiver = state.loadLocal(0, JavaKind.Object);
            ValueNode result = ConstantNode.forInt(0, graph);
            for (String attribute : findAttributes(annotationType)) {
                ResolvedJavaField ourField = findField(annotationType, attribute);
                ResolvedJavaType attributeType = ourField.getType().resolve(null);
                ValueNode ourAttribute = kit.append(LoadFieldNode.create(null, receiver, ourField));
                if (attributeType.isPrimitive()) {
                    ResolvedJavaType boxedAttributeType = providers.getMetaAccess().lookupJavaType(attributeType.getJavaKind().toBoxedJavaClass());
                    ourAttribute = kit.append(new BoxNode(ourAttribute, boxedAttributeType, attributeType.getJavaKind()));
                }
                ValueNode attributeHashCode;
                if (attributeType.isArray()) {
                    ResolvedJavaMethod m = findMethod(providers.getMetaAccess().lookupJavaType(Arrays.class), "hashCode", attributeType);
                    attributeHashCode = kit.createInvokeWithExceptionAndUnwind(m, InvokeKind.Static, state, bci++, bci++, ourAttribute);
                } else {
                    ResolvedJavaMethod m = kit.findMethod(Object.class, "hashCode", false);
                    attributeHashCode = kit.createInvokeWithExceptionAndUnwind(m, InvokeKind.Virtual, state, bci++, bci++, ourAttribute);
                }
                attributeHashCode = kit.unique(new XorNode(attributeHashCode, ConstantNode.forInt(127 * attribute.hashCode(), graph)));
                result = kit.unique(new AddNode(result, attributeHashCode));
            }
            kit.append(new ReturnNode(result));
            return kit.finalizeGraph();
        }
    }

    static class AnnotationToStringMethod extends AnnotationSubstitutionMethod {

        AnnotationToStringMethod(ResolvedJavaMethod original) {
            super(original);
        }

        @Override
        public StructuredGraph buildGraph(DebugContext debug, ResolvedJavaMethod method, HostedProviders providers, Purpose purpose) {
            assert !Modifier.isStatic(method.getModifiers()) && method.getSignature().getParameterCount(false) == 0;
            ResolvedJavaType annotationType = method.getDeclaringClass();
            ResolvedJavaType annotationInterfaceType = findAnnotationInterfaceType(annotationType);
            HostedGraphKit kit = new HostedGraphKit(debug, providers, method);
            StructuredGraph graph = kit.getGraph();
            FrameStateBuilder state = new FrameStateBuilder(null, method, graph);
            state.initializeForMethodStart(null, true, providers.getGraphBuilderPlugins());
            graph.start().setStateAfter(state.create(0, graph.start()));
            String returnValue = "@" + annotationInterfaceType.toJavaName(true);
            ValueNode returnConstant = kit.unique(ConstantNode.forConstant(SubstrateObjectConstant.forObject(returnValue), providers.getMetaAccess()));
            kit.append(new ReturnNode(returnConstant));
            return kit.finalizeGraph();
        }
    }

    private static ResolvedJavaType findAnnotationInterfaceType(ResolvedJavaType annotationType) {
        VMError.guarantee(Inflation.toWrappedType(annotationType) instanceof AnnotationSubstitutionType);
        ResolvedJavaType[] interfaces = annotationType.getInterfaces();
        VMError.guarantee(interfaces.length == 1, "Unexpected number of interfaces for annotation proxy class.");
        return interfaces[0];
    }

    static ResolvedJavaType findAnnotationInterfaceTypeForMarkedAnnotationType(ResolvedJavaType annotationType, MetaAccessProvider metaAccess) {
        ResolvedJavaType[] interfaces = annotationType.getInterfaces();
        VMError.guarantee(interfaces.length == 2, "Unexpected number of interfaces for annotation proxy class.");
        VMError.guarantee(interfaces[1].equals(metaAccess.lookupJavaType(constantAnnotationMarkerInterface)));
        return interfaces[0];
    }

    static Class<?> findAnnotationInterfaceTypeForMarkedAnnotationType(Class<? extends Proxy> clazz) {
        Class<?>[] interfaces = clazz.getInterfaces();
        VMError.guarantee(interfaces.length == 2, "Unexpected number of interfaces for annotation proxy class.");
        VMError.guarantee(interfaces[1].equals(constantAnnotationMarkerInterface));
        return interfaces[0];
    }

    static boolean isAnnotationMarkerInterface(ResolvedJavaType type, MetaAccessProvider metaAccess) {
        return type.equals(metaAccess.lookupJavaType(constantAnnotationMarkerInterface));
    }
}

@AutomaticFeature
class AnnotationSupportFeature implements Feature {

    @Override
    public void duringSetup(DuringSetupAccess access) {
        access.registerObjectReplacer(new AnnotationObjectReplacer());
    }
}

class AnnotationObjectReplacer implements Function<Object, Object> {

    private ConcurrentHashMap<Object, Object> objectCache = new ConcurrentHashMap<>();

    private static final SubstrateAnnotationInvocationHandler SINGLETON_HANDLER = new SubstrateAnnotationInvocationHandler(null);

    private static final Class<?> HOSTED_INVOCATION_HANDLER_CLASS;

    static {
        try {
            HOSTED_INVOCATION_HANDLER_CLASS = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        } catch (ClassNotFoundException ex) {
            throw VMError.shouldNotReachHere(ex);
        }
    }

    @Override
    public Object apply(Object original) {
        Class<?> clazz = original.getClass();
        if (Annotation.class.isAssignableFrom(clazz) && Proxy.class.isAssignableFrom(clazz)) {
            return objectCache.computeIfAbsent(original, AnnotationObjectReplacer::replacementComputer);
        } else if (original instanceof SubstrateAnnotationInvocationHandler) {
            return SINGLETON_HANDLER;
        } else if (HOSTED_INVOCATION_HANDLER_CLASS.isInstance(original)) {
            throw VMError.shouldNotReachHere("Instance of the hosted AnnotationInvocationHandler is reachable at run time");
        }
        return original;
    }

    private static Object replacementComputer(Object original) {
        Class<?>[] interfaces = original.getClass().getInterfaces();
        Class<?>[] extendedInterfaces = Arrays.copyOf(interfaces, interfaces.length + 1);
        extendedInterfaces[extendedInterfaces.length - 1] = AnnotationSupport.constantAnnotationMarkerInterface;
        return Proxy.newProxyInstance(original.getClass().getClassLoader(), extendedInterfaces, new SubstrateAnnotationInvocationHandler(Proxy.getInvocationHandler(original)));
    }
}