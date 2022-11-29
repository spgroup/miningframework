package com.orientechnologies.orient.core.db;

import com.orientechnologies.common.thread.OSoftThread;
import com.orientechnologies.orient.core.OOrientListenerAbstract;
import com.orientechnologies.orient.core.Orient;

public class OExecutionThreadLocal extends ThreadLocal<OExecutionThreadLocal.OExecutionThreadData> {

    class OExecutionThreadData {
    }

    public static volatile OExecutionThreadLocal INSTANCE = new OExecutionThreadLocal();

    public static boolean isInterruptCurrentOperation() {
        final Thread t = Thread.currentThread();
        if (t instanceof OSoftThread)
            return ((OSoftThread) t).isShutdownFlag();
        return false;
    }

    public void setInterruptCurrentOperation(final Thread t) {
        if (t instanceof OSoftThread)
            ((OSoftThread) t).interruptCurrentOperation();
    }

    public static void setInterruptCurrentOperation() {
        final Thread t = Thread.currentThread();
        if (t instanceof OSoftThread)
            ((OSoftThread) t).interruptCurrentOperation();
    }

    static {
        final Orient inst = Orient.instance();
        inst.registerListener(new OOrientListenerAbstract() {

            @Override
            public void onStartup() {
                if (INSTANCE == null)
                    INSTANCE = new OExecutionThreadLocal();
            }

            @Override
            public void onShutdown() {
                INSTANCE = null;
            }
        });
    }
}
