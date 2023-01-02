package com.sun.corba.se.impl.transport;

import sun.misc.Unsafe;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class ManagedLocalsThread extends Thread {

    private static final Unsafe UNSAFE;

    private static final long THREAD_LOCALS;

    private static final long INHERITABLE_THREAD_LOCALS;

    public ManagedLocalsThread() {
        super();
    }

    public ManagedLocalsThread(String name) {
        super(name);
        eraseThreadLocals();
    }

    public ManagedLocalsThread(Runnable target) {
        super(target);
        eraseThreadLocals();
    }

    public ManagedLocalsThread(Runnable target, String name) {
        super(target, name);
        eraseThreadLocals();
    }

    public ManagedLocalsThread(ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
        eraseThreadLocals();
    }

    public ManagedLocalsThread(ThreadGroup group, String name) {
        super(group, name);
        eraseThreadLocals();
    }

    public final void eraseThreadLocals() {
        UNSAFE.putObject(this, THREAD_LOCALS, null);
        UNSAFE.putObject(this, INHERITABLE_THREAD_LOCALS, null);
    }

    private static Unsafe getUnsafe() {
        PrivilegedAction<Unsafe> pa = () -> {
            Class<?> unsafeClass = sun.misc.Unsafe.class;
            try {
                Field f = unsafeClass.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                return (Unsafe) f.get(null);
            } catch (Exception e) {
                throw new Error(e);
            }
        };
        return AccessController.doPrivileged(pa);
    }

    private static long getThreadFieldOffset(String fieldName) {
        PrivilegedAction<Long> pa = () -> {
            Class<?> t = Thread.class;
            long fieldOffset;
            try {
                fieldOffset = UNSAFE.objectFieldOffset(t.getDeclaredField("inheritableThreadLocals"));
            } catch (Exception e) {
                throw new Error(e);
            }
            return fieldOffset;
        };
        return AccessController.doPrivileged(pa);
    }

    static {
        UNSAFE = getUnsafe();
        try {
            THREAD_LOCALS = getThreadFieldOffset("threadLocals");
            INHERITABLE_THREAD_LOCALS = getThreadFieldOffset("inheritableThreadLocals");
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
