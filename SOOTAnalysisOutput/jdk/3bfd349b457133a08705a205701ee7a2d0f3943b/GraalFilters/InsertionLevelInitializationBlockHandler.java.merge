package jdk.tools.jaotc;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.meta.ResolvedJavaType;
import org.graalvm.compiler.api.directives.GraalDirectives;
import org.graalvm.compiler.api.replacements.ClassSubstitution;
import org.graalvm.compiler.api.replacements.MethodSubstitution;
import org.graalvm.compiler.api.replacements.Snippet;
import org.graalvm.compiler.debug.GraalError;
import org.graalvm.compiler.graph.Node.NodeIntrinsic;
import org.graalvm.compiler.hotspot.replacements.HotSpotClassSubstitutions;
import org.graalvm.compiler.hotspot.word.MetaspacePointer;
import org.graalvm.compiler.replacements.Snippets;
import org.graalvm.compiler.word.WordBase;

public class GraalFilters {

    private List<ResolvedJavaType> specialClasses;

    private List<ResolvedJavaType> specialArgumentAndReturnTypes;

    private static Set<Class<?>> skipAnnotations = new HashSet<>();

    static {
        skipAnnotations.add(NodeIntrinsic.class);
        skipAnnotations.add(Snippet.class);
        skipAnnotations.add(MethodSubstitution.class);
    }

    public boolean shouldCompileMethod(ResolvedJavaMethod method) {
        if (hasExcludedAnnotation(method)) {
            return false;
        }
        ResolvedJavaType declaringClass = method.getDeclaringClass();
        List<ResolvedJavaType> signatureTypes = Arrays.asList(method.toParameterTypes()).stream().map(p -> p.resolve(declaringClass)).collect(Collectors.toList());
        signatureTypes.add(method.getSignature().getReturnType(null).resolve(declaringClass));
        if (signatureTypes.stream().flatMap(t -> specialArgumentAndReturnTypes.stream().filter(s -> s.isAssignableFrom(t))).findAny().isPresent()) {
            return false;
        }
        return true;
    }

    private static boolean hasExcludedAnnotation(ResolvedJavaMethod method) {
        for (Annotation annotation : method.getAnnotations()) {
            if (skipAnnotations.contains(annotation.annotationType())) {
                return true;
            }
        }
        return false;
    }

    public boolean shouldCompileAnyMethodInClass(ResolvedJavaType klass) {
        if (specialClasses.stream().filter(s -> s.isAssignableFrom(klass)).findAny().isPresent()) {
            return false;
        }
        return true;
    }

    private static List<ResolvedJavaType> getSpecialClasses(MetaAccessProvider meta) {
        return Arrays.asList(meta.lookupJavaType(Snippets.class), meta.lookupJavaType(HotSpotClassSubstitutions.class), meta.lookupJavaType(GraalDirectives.class), meta.lookupJavaType(ClassSubstitution.class));
    }

    private static List<ResolvedJavaType> getSpecialArgumentAndReturnTypes(MetaAccessProvider meta) {
        return Arrays.asList(meta.lookupJavaType(WordBase.class), meta.lookupJavaType(MetaspacePointer.class));
    }

    GraalFilters(MetaAccessProvider metaAccess) {
        specialClasses = getSpecialClasses(metaAccess);
        specialArgumentAndReturnTypes = getSpecialArgumentAndReturnTypes(metaAccess);
    }

    public boolean shouldIgnoreException(Throwable e) {
        if (e instanceof GraalError) {
            String m = e.getMessage();
            if (m.contains("ArrayKlass::_component_mirror")) {
                return true;
            }
        }
        if (e instanceof org.graalvm.compiler.java.BytecodeParser.BytecodeParserError) {
            Throwable cause = e.getCause();
            if (cause instanceof GraalError) {
                String m = cause.getMessage();
                if (m.contains("@NodeIntrinsic method") && m.contains("must only be called from within a replacement")) {
                    return true;
                }
            }
        }
        return false;
    }
}