package com.oracle.svm.core.thread;

import com.oracle.svm.core.Uninterruptible;
import com.oracle.svm.core.util.VMError;
import jdk.internal.misc.Unsafe;

public final class ThreadData extends UnacquiredThreadData {

    private static final Unsafe UNSAFE = Unsafe.getUnsafe();

    private static final long LOCK_OFFSET;

    private static final long UNSAFE_PARK_EVENT_OFFSET;

    private static final long SLEEP_PARK_EVENT_OFFSET;

    static {
        try {
            LOCK_OFFSET = UNSAFE.objectFieldOffset(ThreadData.class.getDeclaredField("lock"));
            UNSAFE_PARK_EVENT_OFFSET = UNSAFE.objectFieldOffset(ThreadData.class.getDeclaredField("unsafeParkEvent"));
            SLEEP_PARK_EVENT_OFFSET = UNSAFE.objectFieldOffset(ThreadData.class.getDeclaredField("sleepParkEvent"));
        } catch (Throwable ex) {
            throw VMError.shouldNotReachHere(ex);
        }
    }

    private volatile int lock;

    private boolean detached;

    private long refCount;

    private volatile ParkEvent unsafeParkEvent;

    private volatile ParkEvent sleepParkEvent;

    public ParkEvent getSleepParkEvent() {
        assert isForCurrentThread() || refCount > 0;
        return sleepParkEvent;
    }

    public ParkEvent ensureUnsafeParkEvent() {
        assert isForCurrentThread() || refCount > 0;
        ParkEvent existingEvent = unsafeParkEvent;
        if (existingEvent != null) {
            return existingEvent;
        }
        initializeParkEvent(UNSAFE_PARK_EVENT_OFFSET, false);
        return unsafeParkEvent;
    }

    public ParkEvent ensureSleepParkEvent() {
        assert isForCurrentThread() || refCount > 0;
        ParkEvent existingEvent = sleepParkEvent;
        if (existingEvent != null) {
            return existingEvent;
        }
        initializeParkEvent(SLEEP_PARK_EVENT_OFFSET, true);
        return sleepParkEvent;
    }

    @Override
    @Uninterruptible(reason = "Locking without transition requires that the whole critical section is uninterruptible.")
    public ThreadData acquire() {
        JavaSpinLockUtils.lockNoTransition(this, LOCK_OFFSET);
        try {
            if (detached) {
                return null;
            }
            assert refCount >= 0;
            refCount++;
            return this;
        } finally {
            JavaSpinLockUtils.unlock(this, LOCK_OFFSET);
        }
    }

    @Uninterruptible(reason = "Locking without transition requires that the whole critical section is uninterruptible.")
    public void release() {
        JavaSpinLockUtils.lockNoTransition(this, LOCK_OFFSET);
        try {
            assert refCount > 0;
            refCount--;
            if (detached && refCount == 0) {
                free();
            }
        } finally {
            JavaSpinLockUtils.unlock(this, LOCK_OFFSET);
        }
    }

    @Override
    @Uninterruptible(reason = "Locking without transition requires that the whole critical section is uninterruptible.")
    public void detach() {
        assert isForCurrentThread() || VMOperation.isInProgressAtSafepoint() : "may only be called by the detaching thread or at a safepoint";
        assert !detached : "may only be called once";
        JavaSpinLockUtils.lockNoTransition(this, LOCK_OFFSET);
        try {
            detached = true;
            if (refCount == 0) {
                free();
            }
        } finally {
            JavaSpinLockUtils.unlock(this, LOCK_OFFSET);
        }
    }

    @Uninterruptible(reason = "Called from uninterruptible code.", mayBeInlined = true)
    private void free() {
        assert isLocked();
        if (unsafeParkEvent != null) {
            unsafeParkEvent.release();
            unsafeParkEvent = null;
        }
        if (sleepParkEvent != null) {
            sleepParkEvent.release();
            sleepParkEvent = null;
        }
    }

    private void initializeParkEvent(long offset, boolean isSleepEvent) {
        ParkEvent newEvent = ParkEvent.acquire(isSleepEvent);
        if (!tryToStoreParkEvent(offset, newEvent)) {
            newEvent.release();
        }
    }

    @Uninterruptible(reason = "Locking without transition requires that the whole critical section is uninterruptible.")
    private boolean tryToStoreParkEvent(long offset, ParkEvent newEvent) {
        JavaSpinLockUtils.lockNoTransition(this, LOCK_OFFSET);
        try {
            if (UNSAFE.getObject(this, offset) != null) {
                return false;
            }
            UNSAFE.putObjectVolatile(this, offset, newEvent);
            return true;
        } finally {
            JavaSpinLockUtils.unlock(this, LOCK_OFFSET);
        }
    }

    @Uninterruptible(reason = "Called from uninterruptible code.", mayBeInlined = true)
    private boolean isForCurrentThread() {
        return this == PlatformThreads.getCurrentThreadData();
    }

    @Uninterruptible(reason = "Called from uninterruptible code.", mayBeInlined = true)
    private boolean isLocked() {
        return lock == 1;
    }
}

abstract class UnacquiredThreadData {

    @Uninterruptible(reason = "Called from uninterruptible code.", mayBeInlined = true)
    public abstract ThreadData acquire();

    @Uninterruptible(reason = "Called from uninterruptible code.", mayBeInlined = true)
    public abstract void detach();
}
