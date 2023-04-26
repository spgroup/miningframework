package com.oracle.svm.core.thread;

import com.oracle.svm.core.Uninterruptible;
import jdk.internal.misc.Unsafe;

public final class ThreadData extends UnacquiredThreadData {

    private static final Unsafe U = Unsafe.getUnsafe();

    private static final long LOCK_OFFSET = U.objectFieldOffset(ThreadData.class, "lock");

    private static final long UNSAFE_PARK_EVENT_OFFSET = U.objectFieldOffset(ThreadData.class, "unsafeParkEvent");

<<<<<<< MINE
    private static final long SLEEP_PARK_EVENT_OFFSET = U.objectFieldOffset(ThreadData.class, "sleepParkEvent");
=======
    private static final long SLEEP_PARK_EVENT_OFFSET;

    static {
        try {
            LOCK_OFFSET = UNSAFE.objectFieldOffset(ThreadData.class.getDeclaredField("lock"));
            UNSAFE_PARK_EVENT_OFFSET = UNSAFE.objectFieldOffset(ThreadData.class.getDeclaredField("unsafeParker"));
            SLEEP_PARK_EVENT_OFFSET = UNSAFE.objectFieldOffset(ThreadData.class.getDeclaredField("sleepParker"));
        } catch (Throwable ex) {
            throw VMError.shouldNotReachHere(ex);
        }
    }
>>>>>>> YOURS

    private volatile int lock;

    private boolean detached;

    private long refCount;

    private volatile Parker unsafeParker;

    private volatile Parker sleepParker;

    public Parker getSleepParker() {
        assert isForCurrentThread() || refCount > 0;
        return sleepParker;
    }

    public Parker ensureUnsafeParker() {
        assert isForCurrentThread() || refCount > 0;
        Parker existingEvent = unsafeParker;
        if (existingEvent != null) {
            return existingEvent;
        }
        initializeParker(UNSAFE_PARK_EVENT_OFFSET, false);
        return unsafeParker;
    }

    public Parker ensureSleepParker() {
        assert isForCurrentThread() || refCount > 0;
        Parker existingEvent = sleepParker;
        if (existingEvent != null) {
            return existingEvent;
        }
        initializeParker(SLEEP_PARK_EVENT_OFFSET, true);
        return sleepParker;
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
        if (unsafeParker != null) {
            unsafeParker.release();
            unsafeParker = null;
        }
        if (sleepParker != null) {
            sleepParker.release();
            sleepParker = null;
        }
    }

    private void initializeParker(long offset, boolean isSleepEvent) {
        Parker newEvent = Parker.acquire(isSleepEvent);
        if (!tryToStoreParker(offset, newEvent)) {
            newEvent.release();
        }
    }

    @Uninterruptible(reason = "Locking without transition requires that the whole critical section is uninterruptible.")
    private boolean tryToStoreParker(long offset, Parker newEvent) {
        JavaSpinLockUtils.lockNoTransition(this, LOCK_OFFSET);
        try {
            if (U.getObject(this, offset) != null) {
                return false;
            }
            U.putObjectVolatile(this, offset, newEvent);
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
