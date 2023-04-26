package com.sun.btrace.runtime;

import com.sun.btrace.DebugSupport;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import com.sun.btrace.org.objectweb.asm.AnnotationVisitor;
import com.sun.btrace.org.objectweb.asm.Attribute;
import com.sun.btrace.org.objectweb.asm.ClassReader;
import com.sun.btrace.org.objectweb.asm.FieldVisitor;
import com.sun.btrace.org.objectweb.asm.MethodVisitor;
import com.sun.btrace.annotations.BTrace;
import java.lang.ref.Reference;
import java.util.*;
import java.util.regex.PatternSyntaxException;

public class ClassFilter {

    private static final Class<?> REFERENCE_CLASS = Reference.class;

    private Set<String> sourceClasses;

    private Pattern[] sourceClassPatterns;

    private String[] annotationClasses;

    private Pattern[] annotationClassPatterns;

    private String[] superTypes;

    private String[] superTypesInternal;

    private final List<OnMethod> onMethods;

    static {
        ClassReader.class.getClassLoader();
        AnnotationVisitor.class.getClassLoader();
        FieldVisitor.class.getClassLoader();
        MethodVisitor.class.getClassLoader();
        Attribute.class.getClassLoader();
    }

    public ClassFilter(List<OnMethod> onMethods) {
        this.onMethods = new ArrayList<>(onMethods);
        init();
    }

    public boolean isCandidate(Class target) {
        if (target.isInterface() || target.isPrimitive() || target.isArray()) {
            return false;
        }
        if (REFERENCE_CLASS.equals(target)) {
            return false;
        }
        try {
            if (target.getAnnotation(BTrace.class) != null) {
                return false;
            }
        } catch (Throwable t) {
            return false;
        }
        String className = target.getName();
        if (isNameMatching(className)) {
            return true;
        }
        for (Pattern pat : sourceClassPatterns) {
            if (pat.matcher(className).matches()) {
                return true;
            }
        }
        for (String st : superTypes) {
            if (isSubTypeOf(target, st)) {
                return true;
            }
        }
        Annotation[] annotations = target.getAnnotations();
        String[] annoTypes = new String[annotations.length];
        for (int i = 0; i < annotations.length; i++) {
            annoTypes[i] = annotations[i].annotationType().getName();
        }
        for (String name : annotationClasses) {
            for (String annoType : annoTypes) {
                if (name.equals(annoType)) {
                    return true;
                }
            }
        }
        for (Pattern pat : annotationClassPatterns) {
            for (String annoType : annoTypes) {
                if (pat.matcher(annoType).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    Collection<OnMethod> getApplicableHandlers(BTraceClassReader cr) {
        final Collection<OnMethod> applicables = new ArrayList<>(onMethods.size());
        final String targetName = cr.getClassName().replace('/', '.');
        outer: for (OnMethod om : onMethods) {
            String probeClass = om.getClazz();
            if (probeClass == null || probeClass.isEmpty())
                continue;
            if (probeClass.equals(targetName)) {
                applicables.add(om);
                continue;
            }
            if (om.isClassRegexMatcher() && !om.isClassAnnotationMatcher()) {
                if (Pattern.matches(probeClass, targetName)) {
                    applicables.add(om);
                    continue;
                }
            }
            if (om.isClassAnnotationMatcher()) {
                Collection<String> annoTypes = cr.getAnnotationTypes();
                if (om.isClassRegexMatcher()) {
                    Pattern annoCheck = Pattern.compile(probeClass);
                    for (String annoType : annoTypes) {
                        if (annoCheck.matcher(annoType).matches()) {
                            applicables.add(om);
                            continue outer;
                        }
                    }
                } else {
                    if (annoTypes.contains(probeClass)) {
                        applicables.add(om);
                        continue;
                    }
                }
            }
            if (om.isSubtypeMatcher()) {
                if (isSubTypeOf(cr.getClassName(), cr.getClassLoader(), probeClass)) {
                    applicables.add(om);
                }
            }
        }
        return applicables;
    }

    public boolean isNameMatching(String clzName) {
        if (sourceClasses.contains(clzName)) {
            return true;
        }
        for (Pattern pat : sourceClassPatterns) {
            if (pat.matcher(clzName).matches()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSubTypeOf(Class clazz, String typeName) {
        if (clazz == null) {
            return false;
        } else if (clazz.getName().equals(typeName)) {
            return true;
        } else {
            for (Class iface : clazz.getInterfaces()) {
                if (isSubTypeOf(iface, typeName)) {
                    return true;
                }
            }
            return isSubTypeOf(clazz.getSuperclass(), typeName);
        }
    }

    public static boolean isSubTypeOf(String typeA, ClassLoader loader, String... types) {
        if (typeA == null || typeA.equals(Constants.JAVA_LANG_OBJECT)) {
            return false;
        }
        if (types.length == 0) {
            return false;
        }
        boolean internal = types[0].contains("/");
        loader = (loader != null ? loader : ClassLoader.getSystemClassLoader());
        if (internal) {
            typeA = typeA.replace('.', '/');
        } else {
            typeA = typeA.replace('/', '.');
        }
        Set<String> typeSet = new HashSet<>(Arrays.asList(types));
        if (typeSet.contains(typeA)) {
            return true;
        }
        ClassInfo ci = ClassCache.getInstance().get(loader, typeA);
        Collection<String> sTypes = new LinkedList<>();
        for (ClassInfo sCi : ci.getSupertypes(false)) {
            sTypes.add(internal ? sCi.getClassName() : sCi.getJavaClassName());
        }
        sTypes.retainAll(typeSet);
        return !sTypes.isEmpty();
    }

    public static boolean isSensitiveClass(String name) {
        return name.equals("java/lang/Object") || name.startsWith("java/lang/ThreadLocal") || name.startsWith("sun/reflect") || name.equals("sun/misc/Unsafe") || name.startsWith("sun/security/") || name.equals("java/lang/VerifyError") || name.startsWith("sun/instrument/") || name.startsWith("java/lang/instrument/");
    }

    public static boolean isBTraceClass(String name) {
        return name.startsWith("com/sun/btrace/");
    }

    private void init() {
        List<String> strSrcList = new ArrayList<>();
        List<Pattern> patSrcList = new ArrayList<>();
        List<String> superTypesList = new ArrayList<>();
        List<String> superTypesInternalList = new ArrayList<>();
        List<String> strAnoList = new ArrayList<>();
        List<Pattern> patAnoList = new ArrayList<>();
        for (OnMethod om : onMethods) {
            String className = om.getClazz();
            if (className.length() == 0) {
                continue;
            }
            if (om.isClassRegexMatcher()) {
                try {
                    Pattern p = Pattern.compile(className);
                    if (om.isClassAnnotationMatcher()) {
                        patAnoList.add(p);
                    } else {
                        patSrcList.add(p);
                    }
                } catch (PatternSyntaxException pse) {
                    System.err.println("btrace ERROR: invalid regex pattern - " + className.substring(1, className.length() - 1));
                }
            } else if (om.isClassAnnotationMatcher()) {
                strAnoList.add(className);
            } else if (om.isSubtypeMatcher()) {
                superTypesList.add(className);
                superTypesInternalList.add(className.replace('.', '/'));
            } else {
                strSrcList.add(className);
            }
        }
        sourceClasses = new HashSet(strSrcList.size());
        sourceClasses.addAll(strSrcList);
        sourceClassPatterns = new Pattern[patSrcList.size()];
        patSrcList.toArray(sourceClassPatterns);
        superTypes = new String[superTypesList.size()];
        superTypesList.toArray(superTypes);
        superTypesInternal = new String[superTypesInternalList.size()];
        superTypesInternalList.toArray(superTypesInternal);
        annotationClasses = new String[strAnoList.size()];
        strAnoList.toArray(annotationClasses);
        annotationClassPatterns = new Pattern[patAnoList.size()];
        patAnoList.toArray(annotationClassPatterns);
    }
}