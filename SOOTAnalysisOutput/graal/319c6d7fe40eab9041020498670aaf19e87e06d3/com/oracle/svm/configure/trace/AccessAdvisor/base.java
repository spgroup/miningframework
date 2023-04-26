package com.oracle.svm.configure.trace;

import org.graalvm.compiler.phases.common.LazyValue;
import com.oracle.svm.configure.filters.RuleNode;

public final class AccessAdvisor {

    private static final RuleNode internalCallerFilter;

    private static final RuleNode accessWithoutCallerFilter;

    static {
        internalCallerFilter = RuleNode.createRoot();
        internalCallerFilter.addOrGetChildren("**", RuleNode.Inclusion.Include);
        internalCallerFilter.addOrGetChildren("java.**", RuleNode.Inclusion.Exclude);
        internalCallerFilter.addOrGetChildren("javax.**", RuleNode.Inclusion.Exclude);
        internalCallerFilter.addOrGetChildren("javax.security.auth.**", RuleNode.Inclusion.Include);
        internalCallerFilter.addOrGetChildren("sun.**", RuleNode.Inclusion.Exclude);
        internalCallerFilter.addOrGetChildren("com.sun.**", RuleNode.Inclusion.Exclude);
        internalCallerFilter.addOrGetChildren("jdk.**", RuleNode.Inclusion.Exclude);
        internalCallerFilter.addOrGetChildren("org.graalvm.compiler.**", RuleNode.Inclusion.Exclude);
        internalCallerFilter.addOrGetChildren("org.graalvm.libgraal.**", RuleNode.Inclusion.Exclude);
        internalCallerFilter.removeRedundantNodes();
        accessWithoutCallerFilter = RuleNode.createRoot();
        accessWithoutCallerFilter.addOrGetChildren("**", RuleNode.Inclusion.Include);
        accessWithoutCallerFilter.addOrGetChildren("jdk.vm.ci.**", RuleNode.Inclusion.Exclude);
        accessWithoutCallerFilter.addOrGetChildren("org.graalvm.compiler.**", RuleNode.Inclusion.Exclude);
        accessWithoutCallerFilter.addOrGetChildren("org.graalvm.libgraal.**", RuleNode.Inclusion.Exclude);
        accessWithoutCallerFilter.addOrGetChildren("[Ljava.lang.String;", RuleNode.Inclusion.Exclude);
        accessWithoutCallerFilter.removeRedundantNodes();
    }

    public static RuleNode copyBuiltinCallerFilterTree() {
        return internalCallerFilter.copy();
    }

    public static RuleNode copyBuiltinAccessFilterTree() {
        RuleNode root = RuleNode.createRoot();
        root.addOrGetChildren("**", RuleNode.Inclusion.Include);
        return root;
    }

    private RuleNode callerFilter = internalCallerFilter;

    private RuleNode accessFilter = null;

    private boolean heuristicsEnabled = true;

    private boolean isInLivePhase = false;

    private int launchPhase = 0;

    public void setHeuristicsEnabled(boolean enable) {
        heuristicsEnabled = enable;
    }

    public void setCallerFilterTree(RuleNode rootNode) {
        callerFilter = rootNode;
    }

    public void setAccessFilterTree(RuleNode rootNode) {
        accessFilter = rootNode;
    }

    public void setInLivePhase(boolean live) {
        isInLivePhase = live;
    }

    public boolean shouldIgnore(LazyValue<String> queriedClass, LazyValue<String> callerClass) {
        if (heuristicsEnabled && !isInLivePhase) {
            return true;
        }
        String qualifiedCaller = callerClass.get();
        assert qualifiedCaller == null || qualifiedCaller.indexOf('/') == -1 : "expecting Java-format qualifiers, not internal format";
        if (qualifiedCaller != null && !callerFilter.treeIncludes(qualifiedCaller)) {
            return true;
        }
        if (callerClass.get() == null && queriedClass.get() != null && !accessWithoutCallerFilter.treeIncludes(queriedClass.get())) {
            return true;
        }
        return accessFilter != null && queriedClass.get() != null && !accessFilter.treeIncludes(queriedClass.get());
    }

    public boolean shouldIgnoreJniMethodLookup(LazyValue<String> queriedClass, LazyValue<String> name, LazyValue<String> signature, LazyValue<String> callerClass) {
        assert !shouldIgnore(queriedClass, callerClass) : "must have been checked before";
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
        return false;
    }

    public boolean shouldIgnoreLoadClass(LazyValue<String> queriedClass, LazyValue<String> callerClass) {
        assert !shouldIgnore(queriedClass, callerClass) : "must have been checked before";
        if (!heuristicsEnabled) {
            return false;
        }
        return callerClass.get() == null;
    }
}
