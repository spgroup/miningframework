package com.oracle.svm.core.monitor;

import java.lang.ref.ReferenceQueue;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractOwnableSynchronizer;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.AbstractQueuedSynchronizer.ConditionObject;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import org.graalvm.compiler.core.common.SuppressFBWarnings;
import org.graalvm.compiler.serviceprovider.GraalUnsafeAccess;
import org.graalvm.compiler.word.BarrieredAccess;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.Platforms;
import com.oracle.svm.core.SubstrateUtil;
import com.oracle.svm.core.WeakIdentityHashMap;
import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Inject;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.RestrictHeapAccess;
import com.oracle.svm.core.annotate.RestrictHeapAccess.Access;
import com.oracle.svm.core.annotate.TargetClass;
import com.oracle.svm.core.annotate.Uninterruptible;
import com.oracle.svm.core.hub.DynamicHub;
import com.oracle.svm.core.snippets.KnownIntrinsics;
import com.oracle.svm.core.snippets.SubstrateForeignCallTarget;
import com.oracle.svm.core.stack.StackOverflowCheck;
import com.oracle.svm.core.thread.JavaThreads;
import com.oracle.svm.core.thread.ThreadStatus;
import com.oracle.svm.core.thread.ThreadingSupportImpl;
import com.oracle.svm.core.thread.VMOperationControl;
import com.oracle.svm.core.util.VMError;
import sun.misc.Unsafe;

public class MultiThreadedMonitorSupport extends MonitorSupport {

    private static final Unsafe UNSAFE = GraalUnsafeAccess.getUnsafe();

    @Platforms(Platform.HOSTED_ONLY.class)
    public static final Set<Class<?>> FORCE_MONITOR_SLOT_TYPES;

    static {
        try {
            HashSet<Class<?>> monitorTypes = new HashSet<>();
            monitorTypes.add(Class.forName("java.lang.ref.ReferenceQueue$Lock"));
            monitorTypes.add(java.lang.ref.ReferenceQueue.class);
            monitorTypes.add(java.io.FileDescriptor.class);
            monitorTypes.add(java.lang.Object.class);
            monitorTypes.add(Class.forName("com.oracle.svm.core.jdk.SplittableRandomAccessors$Lock"));
            FORCE_MONITOR_SLOT_TYPES = Collections.unmodifiableSet(monitorTypes);
        } catch (ClassNotFoundException e) {
            throw VMError.shouldNotReachHere("Error building the list of types that always need a monitor slot.", e);
        }
    }

    static final ConditionObject MONITOR_WITHOUT_CONDITION = (ConditionObject) new ReentrantLock().newCondition();

    private static long SYNC_MONITOR_CONDITION_FIELD_OFFSET = -1;

    private static long SYNC_STATE_FIELD_OFFSET = -1;

    private final Map<Object, ReentrantLock> additionalMonitors = new WeakIdentityHashMap<>();

    private final ReentrantLock additionalMonitorsLock = new ReentrantLock();

    @Override
    public int maybeAdjustNewParkStatus(int status) {
        Object blocker = LockSupport.getBlocker(Thread.currentThread());
        if (isMonitorCondition(blocker)) {
            if (status == ThreadStatus.PARKED_TIMED) {
                return ThreadStatus.IN_OBJECT_WAIT_TIMED;
            }
            return ThreadStatus.IN_OBJECT_WAIT;
        } else if (isMonitorLockSynchronizer(blocker)) {
            return ThreadStatus.BLOCKED_ON_MONITOR_ENTER;
        }
        return status;
    }

    @SubstrateForeignCallTarget(stubCallingConvention = false)
    @Uninterruptible(reason = "Avoid stack overflow error before yellow zone has been activated", calleeMustBe = false)
    private static void slowPathMonitorEnter(Object obj) {
        StackOverflowCheck.singleton().makeYellowZoneAvailable();
        ThreadingSupportImpl.pauseRecurringCallback("No exception must flow out of the monitor code.");
        VMOperationControl.guaranteeOkayToBlock("No Java synchronization must be performed within a VMOperation: if the object is already locked, the VM is deadlocked");
        try {
            singleton().monitorEnter(obj);
        } catch (OutOfMemoryError ex) {
            throw ex;
        } catch (Throwable ex) {
            throw VMError.shouldNotReachHere("Unexpected exception in MonitorSupport.monitorEnter", ex);
        } finally {
            ThreadingSupportImpl.resumeRecurringCallbackAtNextSafepoint();
            StackOverflowCheck.singleton().protectYellowZone();
        }
    }

    protected static final String NO_LONGER_UNINTERRUPTIBLE = "The monitor snippet slow path is uninterruptible to avoid stack overflow errors being thrown. Now the yellow zone is enabled and we are no longer uninterruptible, and allocation is allowed again too";

    @RestrictHeapAccess(reason = NO_LONGER_UNINTERRUPTIBLE, overridesCallers = true, access = Access.UNRESTRICTED)
    @Override
    public void monitorEnter(Object obj) {
        ReentrantLock lockObject = getOrCreateMonitor(obj, true);
        lockObject.lock();
    }

    @SubstrateForeignCallTarget(stubCallingConvention = false)
    @Uninterruptible(reason = "Avoid stack overflow error before yellow zone has been activated", calleeMustBe = false)
    private static void slowPathMonitorExit(Object obj) {
        StackOverflowCheck.singleton().makeYellowZoneAvailable();
        ThreadingSupportImpl.pauseRecurringCallback("No exception must flow out of the monitor code.");
        try {
            singleton().monitorExit(obj);
        } catch (OutOfMemoryError ex) {
            throw ex;
        } catch (Throwable ex) {
            throw VMError.shouldNotReachHere("Unexpected exception in MonitorSupport.monitorExit", ex);
        } finally {
            ThreadingSupportImpl.resumeRecurringCallbackAtNextSafepoint();
            StackOverflowCheck.singleton().protectYellowZone();
        }
    }

    @RestrictHeapAccess(reason = NO_LONGER_UNINTERRUPTIBLE, overridesCallers = true, access = Access.UNRESTRICTED)
    @Override
    public void monitorExit(Object obj) {
        ReentrantLock lockObject = getOrCreateMonitor(obj, true);
        lockObject.unlock();
    }

    @Override
    public Object prepareRelockObject(Object obj) {
        return getOrCreateMonitor(obj, true);
    }

    @Uninterruptible(reason = "called during deoptimization")
    @Override
    public void doRelockObject(Object obj, Object lockData) {
        Target_java_util_concurrent_locks_ReentrantLock lock = SubstrateUtil.cast(lockData, Target_java_util_concurrent_locks_ReentrantLock.class);
        Target_java_util_concurrent_locks_ReentrantLock_Sync lSync = lock.sync;
        Target_java_util_concurrent_locks_AbstractQueuedSynchronizer qSync = SubstrateUtil.cast(lSync, Target_java_util_concurrent_locks_AbstractQueuedSynchronizer.class);
        Target_java_util_concurrent_locks_AbstractOwnableSynchronizer aSync = SubstrateUtil.cast(lSync, Target_java_util_concurrent_locks_AbstractOwnableSynchronizer.class);
        Thread currentThread = Thread.currentThread();
        Thread ownerThread = aSync.exclusiveOwnerThread;
        VMError.guarantee(ownerThread == null || ownerThread == currentThread, "Object that needs re-locking during deoptimization is already locked by another thread");
        int oldState = qSync.state;
        int newState = oldState + 1;
        VMError.guarantee(newState > 0, "Maximum lock count exceeded");
        boolean success = UNSAFE.compareAndSwapInt(qSync, SYNC_STATE_FIELD_OFFSET, oldState, newState);
        VMError.guarantee(success, "Could not re-lock object during deoptimization");
        aSync.exclusiveOwnerThread = currentThread;
    }

    @Override
    public boolean isLockedByCurrentThread(Object obj) {
        ReentrantLock lockObject = getOrCreateMonitor(obj, false);
        return lockObject != null && lockObject.isHeldByCurrentThread();
    }

    @Override
    public boolean isLockedByAnyThread(Object obj) {
        ReentrantLock lockObject = getOrCreateMonitor(obj, false);
        return lockObject != null && lockObject.isLocked();
    }

    @SuppressFBWarnings(value = { "WA_AWAIT_NOT_IN_LOOP" }, justification = "This method is a wait implementation.")
    @Override
    protected void doWait(Object obj, long timeoutMillis) throws InterruptedException {
        ReentrantLock lock = ensureLocked(obj);
        Condition condition = getOrCreateCondition(lock, true);
        if (timeoutMillis == 0L) {
            condition.await();
        } else {
            condition.await(timeoutMillis, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void notify(Object obj, boolean notifyAll) {
        ReentrantLock lock = ensureLocked(obj);
        Condition condition = getOrCreateCondition(lock, false);
        if (condition != null) {
            if (notifyAll) {
                condition.signalAll();
            } else {
                condition.signal();
            }
        }
    }

    protected ReentrantLock ensureLocked(Object obj) {
        ReentrantLock lockObject = getOrCreateMonitor(obj, true);
        if (!lockObject.isHeldByCurrentThread()) {
            throw new IllegalMonitorStateException("Receiver is not locked by the current thread.");
        }
        return lockObject;
    }

    protected static int getMonitorOffset(Object obj) {
        return DynamicHub.fromClass(obj.getClass()).getMonitorOffset();
    }

    protected final ReentrantLock getOrCreateMonitor(Object obj, boolean createIfNotExisting) {
        assert obj != null;
        int monitorOffset = getMonitorOffset(obj);
        if (monitorOffset != 0) {
            return getOrCreateMonitorFromObject(obj, createIfNotExisting, monitorOffset);
        } else {
            return getOrCreateMonitorFromMap(obj, createIfNotExisting);
        }
    }

    protected ReentrantLock getOrCreateMonitorFromObject(Object obj, boolean createIfNotExisting, int monitorOffset) {
        ReentrantLock existingMonitor = KnownIntrinsics.convertUnknownValue(BarrieredAccess.readObject(obj, monitorOffset), ReentrantLock.class);
        if (existingMonitor != null || !createIfNotExisting) {
            assert existingMonitor == null || isMonitorLock(existingMonitor);
            return existingMonitor;
        }
        ReentrantLock newMonitor = newMonitorLock();
        if (UNSAFE.compareAndSwapObject(obj, monitorOffset, null, newMonitor)) {
            return newMonitor;
        }
        return KnownIntrinsics.convertUnknownValue(BarrieredAccess.readObject(obj, monitorOffset), ReentrantLock.class);
    }

    protected ReentrantLock getOrCreateMonitorFromMap(Object obj, boolean createIfNotExisting) {
        assert obj.getClass() != Target_java_lang_ref_ReferenceQueue_Lock.class : "ReferenceQueue.Lock must have a monitor field or we can deadlock accessing WeakIdentityHashMap below";
        VMError.guarantee(!additionalMonitorsLock.isHeldByCurrentThread(), "Recursive manipulation of the additionalMonitors map can lead to table corruptions and double insertion of a monitor for the same object");
        additionalMonitorsLock.lock();
        try {
            ReentrantLock existingMonitor = additionalMonitors.get(obj);
            if (existingMonitor != null || !createIfNotExisting) {
                assert existingMonitor == null || isMonitorLock(existingMonitor);
                return existingMonitor;
            }
            ReentrantLock newMonitor = newMonitorLock();
            ReentrantLock previousEntry = additionalMonitors.put(obj, newMonitor);
            VMError.guarantee(previousEntry == null, "Replaced monitor in secondary storage map");
            return newMonitor;
        } finally {
            additionalMonitorsLock.unlock();
        }
    }

    protected static ReentrantLock newMonitorLock() {
        ReentrantLock newMonitor = new ReentrantLock();
        Target_java_util_concurrent_locks_ReentrantLock lock = SubstrateUtil.cast(newMonitor, Target_java_util_concurrent_locks_ReentrantLock.class);
        Target_java_util_concurrent_locks_ReentrantLock_NonfairSync sync = SubstrateUtil.cast(lock.sync, Target_java_util_concurrent_locks_ReentrantLock_NonfairSync.class);
        sync.objectMonitorCondition = SubstrateUtil.cast(MONITOR_WITHOUT_CONDITION, Target_java_util_concurrent_locks_AbstractQueuedSynchronizer_ConditionObject.class);
        assert isMonitorLock(newMonitor);
        return newMonitor;
    }

    protected static ReentrantLock newLockedMonitorForThread(IsolateThread isolateThread, int recursionDepth) {
        ReentrantLock result = newMonitorLock();
        for (int i = 0; i < recursionDepth; i++) {
            result.lock();
        }
        Target_java_util_concurrent_locks_ReentrantLock lock = SubstrateUtil.cast(result, Target_java_util_concurrent_locks_ReentrantLock.class);
        Target_java_util_concurrent_locks_AbstractOwnableSynchronizer sync = SubstrateUtil.cast(lock.sync, Target_java_util_concurrent_locks_AbstractOwnableSynchronizer.class);
        assert sync.exclusiveOwnerThread == Thread.currentThread() : "Must be locked by current thread";
        sync.exclusiveOwnerThread = JavaThreads.fromVMThread(isolateThread);
        return result;
    }

    protected static boolean isMonitorLock(ReentrantLock lock) {
        return lock != null && isMonitorLockSynchronizer(SubstrateUtil.cast(lock, Target_java_util_concurrent_locks_ReentrantLock.class).sync);
    }

    protected static boolean isMonitorLockSynchronizer(Object obj) {
        if (obj != null && obj.getClass() == Target_java_util_concurrent_locks_ReentrantLock_NonfairSync.class) {
            Target_java_util_concurrent_locks_ReentrantLock_NonfairSync sync = SubstrateUtil.cast(obj, Target_java_util_concurrent_locks_ReentrantLock_NonfairSync.class);
            return sync.objectMonitorCondition != null;
        }
        return false;
    }

    public ReentrantLock getMonitorForTesting(Object obj) {
        return getOrCreateMonitor(obj, true);
    }

    protected ConditionObject getOrCreateCondition(ReentrantLock monitorLock, boolean createIfNotExisting) {
        assert isMonitorLock(monitorLock);
        Target_java_util_concurrent_locks_ReentrantLock lock = SubstrateUtil.cast(monitorLock, Target_java_util_concurrent_locks_ReentrantLock.class);
        Target_java_util_concurrent_locks_ReentrantLock_NonfairSync sync = SubstrateUtil.cast(lock.sync, Target_java_util_concurrent_locks_ReentrantLock_NonfairSync.class);
        ConditionObject existingCondition = SubstrateUtil.cast(sync.objectMonitorCondition, ConditionObject.class);
        if (existingCondition == MONITOR_WITHOUT_CONDITION) {
            existingCondition = null;
        }
        if (existingCondition != null || !createIfNotExisting) {
            assert existingCondition == null || isMonitorCondition(existingCondition);
            return existingCondition;
        }
        ConditionObject newCondition = (ConditionObject) monitorLock.newCondition();
        if (!UNSAFE.compareAndSwapObject(sync, SYNC_MONITOR_CONDITION_FIELD_OFFSET, MONITOR_WITHOUT_CONDITION, newCondition)) {
            newCondition = SubstrateUtil.cast(sync.objectMonitorCondition, ConditionObject.class);
            assert isMonitorCondition(newCondition) : "race winner must have installed valid condition";
        }
        return newCondition;
    }

    protected static boolean isMonitorCondition(Object obj) {
        if (obj != null && obj.getClass() == Target_java_util_concurrent_locks_AbstractQueuedSynchronizer_ConditionObject.class) {
            Target_java_util_concurrent_locks_AbstractQueuedSynchronizer enclosing = SubstrateUtil.cast(obj, Target_java_util_concurrent_locks_AbstractQueuedSynchronizer_ConditionObject.class).this$0;
            if (enclosing.getClass() == (Class<?>) Target_java_util_concurrent_locks_ReentrantLock_NonfairSync.class) {
                Target_java_util_concurrent_locks_ReentrantLock_NonfairSync sync = SubstrateUtil.cast(enclosing, Target_java_util_concurrent_locks_ReentrantLock_NonfairSync.class);
                return obj == sync.objectMonitorCondition;
            }
        }
        return false;
    }
}

@TargetClass(value = AbstractOwnableSynchronizer.class)
final class Target_java_util_concurrent_locks_AbstractOwnableSynchronizer {

    @Alias
    Thread exclusiveOwnerThread;
}

@TargetClass(value = ReentrantLock.class, innerClass = "Sync")
final class Target_java_util_concurrent_locks_ReentrantLock_Sync {
}

@TargetClass(value = ReentrantLock.class, innerClass = "NonfairSync")
final class Target_java_util_concurrent_locks_ReentrantLock_NonfairSync {

    @Inject
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.Reset)
    volatile Target_java_util_concurrent_locks_AbstractQueuedSynchronizer_ConditionObject objectMonitorCondition;
}

@TargetClass(MultiThreadedMonitorSupport.class)
final class Target_com_oracle_svm_core_monitor_MultiThreadedMonitorSupport {

    @Alias
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FieldOffset, name = "objectMonitorCondition", declClass = Target_java_util_concurrent_locks_ReentrantLock_NonfairSync.class)
    static long SYNC_MONITOR_CONDITION_FIELD_OFFSET;

    @Alias
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FieldOffset, name = "state", declClass = AbstractQueuedSynchronizer.class)
    static long SYNC_STATE_FIELD_OFFSET;
}

@TargetClass(ReentrantLock.class)
final class Target_java_util_concurrent_locks_ReentrantLock {

    @Alias
    Target_java_util_concurrent_locks_ReentrantLock_Sync sync;
}

@TargetClass(AbstractQueuedSynchronizer.class)
final class Target_java_util_concurrent_locks_AbstractQueuedSynchronizer {

    @Alias
    volatile int state;
}

@TargetClass(value = ConditionObject.class)
final class Target_java_util_concurrent_locks_AbstractQueuedSynchronizer_ConditionObject {

    @Alias
    Target_java_util_concurrent_locks_AbstractQueuedSynchronizer this$0;
}

@TargetClass(value = ReferenceQueue.class, innerClass = "Lock")
final class Target_java_lang_ref_ReferenceQueue_Lock {
}
