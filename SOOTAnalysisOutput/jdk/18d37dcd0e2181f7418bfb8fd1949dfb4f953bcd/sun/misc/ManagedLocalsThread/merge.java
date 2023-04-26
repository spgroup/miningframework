package sun.misc;

public class ManagedLocalsThread extends Thread {

    private static final jdk.internal.misc.Unsafe UNSAFE;

    private static final long THREAD_LOCALS;

    private static final long INHERITABLE_THREAD_LOCALS;

    public ManagedLocalsThread() {
        eraseThreadLocals();
    }

    public ManagedLocalsThread(Runnable target) {
        super(target);
        eraseThreadLocals();
    }

    public ManagedLocalsThread(String name) {
        super(name);
        eraseThreadLocals();
    }

    public ManagedLocalsThread(ThreadGroup group, Runnable target) {
        super(group, target);
        eraseThreadLocals();
    }

    public ManagedLocalsThread(Runnable target, String name) {
        super(target, name);
        eraseThreadLocals();
    }

    public ManagedLocalsThread(ThreadGroup group, String name) {
        super(group, name);
        eraseThreadLocals();
    }

    public ManagedLocalsThread(ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
        eraseThreadLocals();
    }

    public final void eraseThreadLocals() {
        UNSAFE.putObject(this, THREAD_LOCALS, null);
        UNSAFE.putObject(this, INHERITABLE_THREAD_LOCALS, null);
    }

    static {
        UNSAFE = jdk.internal.misc.Unsafe.getUnsafe();
        Class<?> t = Thread.class;
        try {
            THREAD_LOCALS = UNSAFE.objectFieldOffset(t.getDeclaredField("threadLocals"));
            INHERITABLE_THREAD_LOCALS = UNSAFE.objectFieldOffset(t.getDeclaredField("inheritableThreadLocals"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
