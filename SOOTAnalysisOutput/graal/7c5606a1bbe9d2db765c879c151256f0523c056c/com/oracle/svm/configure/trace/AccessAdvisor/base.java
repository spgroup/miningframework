package com.oracle.svm.configure.trace;

import java.util.Arrays;
import org.graalvm.compiler.phases.common.LazyValue;

public class AccessAdvisor {

    private boolean ignoreInternalAccesses = true;

    private boolean isInLivePhase = false;

    private int launchPhase = 0;

    public void setIgnoreInternalAccesses(boolean enabled) {
        ignoreInternalAccesses = enabled;
    }

    public void setInLivePhase(boolean live) {
        isInLivePhase = live;
    }

    private static boolean isInternalClass(String qualifiedClass) {
        assert qualifiedClass == null || qualifiedClass.indexOf('/') == -1 : "expecting Java-format qualifiers, not internal format";
        return qualifiedClass != null && Arrays.asList("java.", "javax.", "sun.", "com.sun.", "jdk.", "org.graalvm.compiler.").stream().anyMatch(qualifiedClass::startsWith);
    }

    public boolean shouldIgnore(LazyValue<String> callerClass) {
        return ignoreInternalAccesses && (!isInLivePhase || isInternalClass(callerClass.get()));
    }

    public boolean shouldIgnoreJniMethodLookup(LazyValue<String> queriedClass, LazyValue<String> name, LazyValue<String> signature, LazyValue<String> callerClass) {
        if (!ignoreInternalAccesses) {
            return false;
        }
        if (shouldIgnore(callerClass)) {
            return true;
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
        if (!ignoreInternalAccesses) {
            return false;
        }
        if (shouldIgnore(callerClass)) {
            return true;
        }
        if (callerClass.get() == null && "jdk.vm.ci.services.Services".equals(name.get())) {
            return true;
        }
        return false;
    }

    public boolean shouldIgnoreJniNewObjectArray(LazyValue<String> arrayClass, LazyValue<String> callerClass) {
        if (!ignoreInternalAccesses) {
            return false;
        }
        if (shouldIgnore(callerClass)) {
            return true;
        }
        if (callerClass.get() == null && "[Ljava.lang.String;".equals(arrayClass.get())) {
            return true;
        }
        return false;
    }
}
