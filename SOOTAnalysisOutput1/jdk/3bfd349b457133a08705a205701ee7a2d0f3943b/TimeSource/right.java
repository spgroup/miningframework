package org.graalvm.compiler.debug;

import java.lang.management.ThreadMXBean;

public class TimeSource {

    private static final boolean USING_BEAN;

    private static final ThreadMXBean threadMXBean;

    static {
        threadMXBean = Management.getThreadMXBean();
        if (threadMXBean.isThreadCpuTimeSupported()) {
            USING_BEAN = true;
        } else {
            USING_BEAN = false;
        }
    }

    public static long getTimeNS() {
        return USING_BEAN ? threadMXBean.getCurrentThreadCpuTime() : System.nanoTime();
    }
}
