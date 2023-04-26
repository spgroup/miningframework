package org.springframework.boot.devtools.restart;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class DefaultRestartInitializer implements RestartInitializer {

    private static final Set<String> SKIPPED_STACK_ELEMENTS;

    static {
        Set<String> skipped = new LinkedHashSet<>();
        skipped.add("org.junit.runners.");
        skipped.add("org.junit.platform.");
        skipped.add("org.springframework.boot.test.");
        skipped.add("cucumber.runtime.");
        SKIPPED_STACK_ELEMENTS = Collections.unmodifiableSet(skipped);
    }

    @Override
    public URL[] getInitialUrls(Thread thread) {
        if (!isMain(thread)) {
            return null;
        }
        for (StackTraceElement element : thread.getStackTrace()) {
            if (isSkippedStackElement(element)) {
                return null;
            }
        }
        return getUrls(thread);
    }

    protected boolean isMain(Thread thread) {
        return thread.getName().equals("main") && thread.getContextClassLoader().getClass().getName().contains("AppClassLoader");
    }

    private boolean isSkippedStackElement(StackTraceElement element) {
        for (String skipped : SKIPPED_STACK_ELEMENTS) {
            if (element.getClassName().startsWith(skipped)) {
                return true;
            }
        }
        return false;
    }

    protected URL[] getUrls(Thread thread) {
        return ChangeableUrls.fromClassLoader(thread.getContextClassLoader()).toArray();
    }
}
