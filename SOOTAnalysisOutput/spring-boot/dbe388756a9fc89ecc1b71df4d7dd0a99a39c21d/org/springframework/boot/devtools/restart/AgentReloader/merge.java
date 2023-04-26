package org.springframework.boot.devtools.restart;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.util.ClassUtils;

public abstract class AgentReloader {

    private static final Set<String> AGENT_CLASSES;

    static {
        Set<String> agentClasses = new LinkedHashSet<>();
        agentClasses.add("org.zeroturnaround.javarebel.Integration");
        agentClasses.add("org.zeroturnaround.javarebel.ReloaderFactory");
        agentClasses.add("org.hotswap.agent.HotswapAgent");
        AGENT_CLASSES = Collections.unmodifiableSet(agentClasses);
    }

    private AgentReloader() {
    }

    public static boolean isActive() {
        return isActive(null) || isActive(AgentReloader.class.getClassLoader()) || isActive(ClassLoader.getSystemClassLoader());
    }

    private static boolean isActive(ClassLoader classLoader) {
        for (String agentClass : AGENT_CLASSES) {
            if (ClassUtils.isPresent(agentClass, classLoader)) {
                return true;
            }
        }
        return false;
    }
}
