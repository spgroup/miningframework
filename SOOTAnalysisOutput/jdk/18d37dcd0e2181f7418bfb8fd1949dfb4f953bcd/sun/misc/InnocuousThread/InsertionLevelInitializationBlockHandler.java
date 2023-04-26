package sun.misc;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.ProtectionDomain;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;

public final class InnocuousThread extends ManagedLocalsThread {

    private static final jdk.internal.misc.Unsafe UNSAFE;

    private static final ThreadGroup INNOCUOUSTHREADGROUP;

    private static final AccessControlContext ACC;

    private static final long INHERITEDACCESSCONTROLCONTEXT;

    private static final long CONTEXTCLASSLOADER;

    private static final AtomicInteger threadNumber = new AtomicInteger(1);

    public InnocuousThread(Runnable target) {
        this(INNOCUOUSTHREADGROUP, target, "InnocuousThread-" + threadNumber.getAndIncrement());
    }

    public InnocuousThread(Runnable target, String name) {
        this(INNOCUOUSTHREADGROUP, target, name);
    }

    public InnocuousThread(ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
        UNSAFE.putOrderedObject(this, INHERITEDACCESSCONTROLCONTEXT, ACC);
        UNSAFE.putOrderedObject(this, CONTEXTCLASSLOADER, ClassLoader.getSystemClassLoader());
    }

    @Override
    public void setUncaughtExceptionHandler(UncaughtExceptionHandler x) {
    }

    @Override
    public void setContextClassLoader(ClassLoader cl) {
        if (cl == null)
            super.setContextClassLoader(null);
        else
            throw new SecurityException("setContextClassLoader");
    }

    private volatile boolean hasRun;

    @Override
    public void run() {
        if (Thread.currentThread() == this && !hasRun) {
            hasRun = true;
            super.run();
        }
    }

    static {
        try {
            ACC = new AccessControlContext(new ProtectionDomain[] { new ProtectionDomain(null, null) });
            UNSAFE = jdk.internal.misc.Unsafe.getUnsafe();
            Class<?> tk = Thread.class;
            Class<?> gk = ThreadGroup.class;
            INHERITEDACCESSCONTROLCONTEXT = UNSAFE.objectFieldOffset(tk.getDeclaredField("inheritedAccessControlContext"));
            CONTEXTCLASSLOADER = UNSAFE.objectFieldOffset(tk.getDeclaredField("contextClassLoader"));
            long tg = UNSAFE.objectFieldOffset(tk.getDeclaredField("group"));
            long gp = UNSAFE.objectFieldOffset(gk.getDeclaredField("parent"));
            ThreadGroup group = (ThreadGroup) UNSAFE.getObject(Thread.currentThread(), tg);
            while (group != null) {
                ThreadGroup parent = (ThreadGroup) UNSAFE.getObject(group, gp);
                if (parent == null)
                    break;
                group = parent;
            }
            final ThreadGroup root = group;
            INNOCUOUSTHREADGROUP = AccessController.doPrivileged((PrivilegedAction<ThreadGroup>) () -> {
                return new ThreadGroup(root, "InnocuousThreadGroup");
            });
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}