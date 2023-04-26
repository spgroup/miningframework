package org.springframework.boot.devtools.restart;

import java.net.URL;
import org.springframework.boot.devtools.system.DevToolsEnablementDeducer;

public class DefaultRestartInitializer implements RestartInitializer {

    @Override
    public URL[] getInitialUrls(Thread thread) {
        if (!isMain(thread)) {
            return null;
        }
        if (!DevToolsEnablementDeducer.shouldEnable(thread)) {
            return null;
        }
        return getUrls(thread);
    }

    protected boolean isMain(Thread thread) {
        return thread.getName().equals("main") && thread.getContextClassLoader().getClass().getName().contains("AppClassLoader");
    }

    protected URL[] getUrls(Thread thread) {
        return ChangeableUrls.fromClassLoader(thread.getContextClassLoader()).toArray();
    }
}