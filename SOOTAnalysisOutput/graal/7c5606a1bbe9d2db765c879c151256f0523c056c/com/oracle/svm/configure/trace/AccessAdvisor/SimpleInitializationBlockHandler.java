package com.oracle.svm.configure.trace;

import org.graalvm.compiler.phases.common.LazyValue;
import com.oracle.svm.configure.filters.RuleNode;

public class AccessAdvisor {

    private static final RuleNode internalsFilter;

    static {
        internalsFilter = RuleNode.createRoot();
        internalsFilter.addOrGetChildren("java.**", RuleNode.Inclusion.Include);
        internalsFilter.addOrGetChildren("javax.**", RuleNode.Inclusion.Include);
        internalsFilter.addOrGetChildren("sun.**", RuleNode.Inclusion.Include);
        internalsFilter.addOrGetChildren("com.sun.**", RuleNode.Inclusion.Include);
        internalsFilter.addOrGetChildren("jdk.**", RuleNode.Inclusion.Include);
        internalsFilter.addOrGetChildren("org.graalvm.compiler.**", RuleNode.Inclusion.Include);
        internalsFilter.removeRedundantNodes();
    }

    public static RuleNode copyBuiltinFilterTree() {
        return internalsFilter.copy();
    }

    private RuleNode callerFilter = internalsFilter;

    private boolean heuristicsEnabled = true;

    private boolean isInLivePhase = false;

    private int launchPhase = 0;

    private boolean callerFilterIncludes(String qualifiedClass) {
        if (callerFilter != null && qualifiedClass != null) {
            assert qualifiedClass.indexOf('/') == -1 : "expecting Java-format qualifiers, not internal format";
            return callerFilter.treeIncludes(qualifiedClass);
        }
        return false;
    }

    public void setHeuristicsEnabled(boolean enable) {
        heuristicsEnabled = enable;
    }

    public void setCallerFilterTree(RuleNode rootNode) {
        callerFilter = rootNode;
    }

    public void setInLivePhase(boolean live) {
        isInLivePhase = live;
    }

    public boolean shouldIgnoreCaller(LazyValue<String> qualifiedClass) {
        return (heuristicsEnabled && !isInLivePhase) || callerFilterIncludes(qualifiedClass.get());
    }

    public boolean shouldIgnoreJniMethodLookup(LazyValue<String> queriedClass, LazyValue<String> name, LazyValue<String> signature, LazyValue<String> callerClass) {
        if (shouldIgnoreCaller(callerClass)) {
            return true;
        }
        if (!heuristicsEnabled) {
            return false;
        }
        if ("sun.launcher.LauncherHelper".equals(queriedClass.get())) {
            if (launchPhase == 0 && "getApplicationClass".equals(name.get()) && "()Ljava/lang/Class;".equals(signature.get())) {
                launchPhase = 1;
            }
            return true;
        }
        if (launchPhase == 1 && "getCanonicalName".equals(name.get()) && "()Ljava/lang/String;".equals(signature.get())) {
            launchPhase = 2;
            return true;
        }
        if (launchPhase > 0) {
            launchPhase = -1;
            if ("main".equals(name.get()) && "([Ljava/lang/String;)V".equals(signature.get())) {
                return true;
            }
        }
        if (callerClass.get() == null && "jdk.vm.ci.services.Services".equals(queriedClass.get()) && "getJVMCIClassLoader".equals(name.get()) && "()Ljava/lang/ClassLoader;".equals(signature.get())) {
            return true;
        }
        if (callerClass.get() == null && "org.graalvm.compiler.hotspot.management.libgraal.runtime.SVMToHotSpotEntryPoints".equals(queriedClass.get())) {
            return true;
        }
        return false;
    }

    public boolean shouldIgnoreJniClassLookup(LazyValue<String> name, LazyValue<String> callerClass) {
        if (shouldIgnoreCaller(callerClass)) {
            return true;
        }
        if (!heuristicsEnabled) {
            return false;
        }
        if (callerClass.get() == null && "jdk.vm.ci.services.Services".equals(name.get())) {
            return true;
        }
        return false;
    }

    public boolean shouldIgnoreJniNewObjectArray(LazyValue<String> arrayClass, LazyValue<String> callerClass) {
        if (!heuristicsEnabled) {
            return false;
        }
        if (shouldIgnoreCaller(callerClass)) {
            return true;
        }
        if (callerClass.get() == null && "[Ljava.lang.String;".equals(arrayClass.get())) {
            return true;
        }
        return false;
    }
}