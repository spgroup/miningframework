package java.util.concurrent;

import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class CompletableFuture<T> implements Future<T> {

    static final class AltResult {

        final Throwable ex;

        AltResult(Throwable ex) {
            this.ex = ex;
        }
    }

    static final AltResult NIL = new AltResult(null);

    volatile Object result;

    volatile WaitNode waiters;

    volatile CompletionNode completions;

    final void postComplete() {
        WaitNode q;
        Thread t;
        while ((q = waiters) != null) {
            if (UNSAFE.compareAndSwapObject(this, WAITERS, q, q.next) && (t = q.thread) != null) {
                q.thread = null;
                LockSupport.unpark(t);
            }
        }
        CompletionNode h;
        Completion c;
        while ((h = completions) != null) {
            if (UNSAFE.compareAndSwapObject(this, COMPLETIONS, h, h.next) && (c = h.completion) != null)
                c.run();
        }
    }

    final void internalComplete(T v, Throwable ex) {
        if (result == null)
            UNSAFE.compareAndSwapObject(this, RESULT, null, (ex == null) ? (v == null) ? NIL : v : new AltResult((ex instanceof CompletionException) ? ex : new CompletionException(ex)));
        postComplete();
    }

    final void helpPostComplete() {
        if (result != null)
            postComplete();
    }

    static final int NCPU = Runtime.getRuntime().availableProcessors();

    static final int SPINS = (NCPU > 1) ? 1 << 8 : 0;

    static final class WaitNode implements ForkJoinPool.ManagedBlocker {

        long nanos;

        final long deadline;

        volatile int interruptControl;

        volatile Thread thread;

        volatile WaitNode next;

        WaitNode(boolean interruptible, long nanos, long deadline) {
            this.thread = Thread.currentThread();
            this.interruptControl = interruptible ? 1 : 0;
            this.nanos = nanos;
            this.deadline = deadline;
        }

        public boolean isReleasable() {
            if (thread == null)
                return true;
            if (Thread.interrupted()) {
                int i = interruptControl;
                interruptControl = -1;
                if (i > 0)
                    return true;
            }
            if (deadline != 0L && (nanos <= 0L || (nanos = deadline - System.nanoTime()) <= 0L)) {
                thread = null;
                return true;
            }
            return false;
        }

        public boolean block() {
            if (isReleasable())
                return true;
            else if (deadline == 0L)
                LockSupport.park(this);
            else if (nanos > 0L)
                LockSupport.parkNanos(this, nanos);
            return isReleasable();
        }
    }

    private Object waitingGet(boolean interruptible) {
        WaitNode q = null;
        boolean queued = false;
        int spins = SPINS;
        for (Object r; ; ) {
            if ((r = result) != null) {
                if (q != null) {
                    q.thread = null;
                    if (q.interruptControl < 0) {
                        if (interruptible) {
                            removeWaiter(q);
                            return null;
                        }
                        Thread.currentThread().interrupt();
                    }
                }
                postComplete();
                return r;
            } else if (spins > 0) {
                int rnd = ThreadLocalRandom.nextSecondarySeed();
                if (rnd == 0)
                    rnd = ThreadLocalRandom.current().nextInt();
                if (rnd >= 0)
                    --spins;
            } else if (q == null)
                q = new WaitNode(interruptible, 0L, 0L);
            else if (!queued)
                queued = UNSAFE.compareAndSwapObject(this, WAITERS, q.next = waiters, q);
            else if (interruptible && q.interruptControl < 0) {
                removeWaiter(q);
                return null;
            } else if (q.thread != null && result == null) {
                try {
                    ForkJoinPool.managedBlock(q);
                } catch (InterruptedException ex) {
                    q.interruptControl = -1;
                }
            }
        }
    }

    private Object timedAwaitDone(long nanos) throws InterruptedException, TimeoutException {
        WaitNode q = null;
        boolean queued = false;
        for (Object r; ; ) {
            if ((r = result) != null) {
                if (q != null) {
                    q.thread = null;
                    if (q.interruptControl < 0) {
                        removeWaiter(q);
                        throw new InterruptedException();
                    }
                }
                postComplete();
                return r;
            } else if (q == null) {
                if (nanos <= 0L)
                    throw new TimeoutException();
                long d = System.nanoTime() + nanos;
                q = new WaitNode(true, nanos, d == 0L ? 1L : d);
            } else if (!queued)
                queued = UNSAFE.compareAndSwapObject(this, WAITERS, q.next = waiters, q);
            else if (q.interruptControl < 0) {
                removeWaiter(q);
                throw new InterruptedException();
            } else if (q.nanos <= 0L) {
                if (result == null) {
                    removeWaiter(q);
                    throw new TimeoutException();
                }
            } else if (q.thread != null && result == null) {
                try {
                    ForkJoinPool.managedBlock(q);
                } catch (InterruptedException ex) {
                    q.interruptControl = -1;
                }
            }
        }
    }

    private void removeWaiter(WaitNode node) {
        if (node != null) {
            node.thread = null;
            retry: for (; ; ) {
                for (WaitNode pred = null, q = waiters, s; q != null; q = s) {
                    s = q.next;
                    if (q.thread != null)
                        pred = q;
                    else if (pred != null) {
                        pred.next = s;
                        if (pred.thread == null)
                            continue retry;
                    } else if (!UNSAFE.compareAndSwapObject(this, WAITERS, q, s))
                        continue retry;
                }
                break;
            }
        }
    }

    public static interface AsynchronousCompletionTask {
    }

    abstract static class Async extends ForkJoinTask<Void> implements Runnable, AsynchronousCompletionTask {

        public final Void getRawResult() {
            return null;
        }

        public final void setRawResult(Void v) {
        }

        public final void run() {
            exec();
        }
    }

    static final class AsyncRun extends Async {

        final Runnable fn;

        final CompletableFuture<Void> dst;

        AsyncRun(Runnable fn, CompletableFuture<Void> dst) {
            this.fn = fn;
            this.dst = dst;
        }

        public final boolean exec() {
            CompletableFuture<Void> d;
            Throwable ex;
            if ((d = this.dst) != null && d.result == null) {
                try {
                    fn.run();
                    ex = null;
                } catch (Throwable rex) {
                    ex = rex;
                }
                d.internalComplete(null, ex);
            }
            return true;
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class AsyncSupply<U> extends Async {

        final Supplier<U> fn;

        final CompletableFuture<U> dst;

        AsyncSupply(Supplier<U> fn, CompletableFuture<U> dst) {
            this.fn = fn;
            this.dst = dst;
        }

        public final boolean exec() {
            CompletableFuture<U> d;
            U u;
            Throwable ex;
            if ((d = this.dst) != null && d.result == null) {
                try {
                    u = fn.get();
                    ex = null;
                } catch (Throwable rex) {
                    ex = rex;
                    u = null;
                }
                d.internalComplete(u, ex);
            }
            return true;
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class AsyncApply<T, U> extends Async {

        final T arg;

        final Function<? super T, ? extends U> fn;

        final CompletableFuture<U> dst;

        AsyncApply(T arg, Function<? super T, ? extends U> fn, CompletableFuture<U> dst) {
            this.arg = arg;
            this.fn = fn;
            this.dst = dst;
        }

        public final boolean exec() {
            CompletableFuture<U> d;
            U u;
            Throwable ex;
            if ((d = this.dst) != null && d.result == null) {
                try {
                    u = fn.apply(arg);
                    ex = null;
                } catch (Throwable rex) {
                    ex = rex;
                    u = null;
                }
                d.internalComplete(u, ex);
            }
            return true;
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class AsyncCombine<T, U, V> extends Async {

        final T arg1;

        final U arg2;

        final BiFunction<? super T, ? super U, ? extends V> fn;

        final CompletableFuture<V> dst;

        AsyncCombine(T arg1, U arg2, BiFunction<? super T, ? super U, ? extends V> fn, CompletableFuture<V> dst) {
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.fn = fn;
            this.dst = dst;
        }

        public final boolean exec() {
            CompletableFuture<V> d;
            V v;
            Throwable ex;
            if ((d = this.dst) != null && d.result == null) {
                try {
                    v = fn.apply(arg1, arg2);
                    ex = null;
                } catch (Throwable rex) {
                    ex = rex;
                    v = null;
                }
                d.internalComplete(v, ex);
            }
            return true;
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class AsyncAccept<T> extends Async {

        final T arg;

        final Consumer<? super T> fn;

        final CompletableFuture<Void> dst;

        AsyncAccept(T arg, Consumer<? super T> fn, CompletableFuture<Void> dst) {
            this.arg = arg;
            this.fn = fn;
            this.dst = dst;
        }

        public final boolean exec() {
            CompletableFuture<Void> d;
            Throwable ex;
            if ((d = this.dst) != null && d.result == null) {
                try {
                    fn.accept(arg);
                    ex = null;
                } catch (Throwable rex) {
                    ex = rex;
                }
                d.internalComplete(null, ex);
            }
            return true;
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class AsyncAcceptBoth<T, U> extends Async {

        final T arg1;

        final U arg2;

        final BiConsumer<? super T, ? super U> fn;

        final CompletableFuture<Void> dst;

        AsyncAcceptBoth(T arg1, U arg2, BiConsumer<? super T, ? super U> fn, CompletableFuture<Void> dst) {
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.fn = fn;
            this.dst = dst;
        }

        public final boolean exec() {
            CompletableFuture<Void> d;
            Throwable ex;
            if ((d = this.dst) != null && d.result == null) {
                try {
                    fn.accept(arg1, arg2);
                    ex = null;
                } catch (Throwable rex) {
                    ex = rex;
                }
                d.internalComplete(null, ex);
            }
            return true;
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class AsyncCompose<T, U> extends Async {

        final T arg;

        final Function<? super T, CompletableFuture<U>> fn;

        final CompletableFuture<U> dst;

        AsyncCompose(T arg, Function<? super T, CompletableFuture<U>> fn, CompletableFuture<U> dst) {
            this.arg = arg;
            this.fn = fn;
            this.dst = dst;
        }

        public final boolean exec() {
            CompletableFuture<U> d, fr;
            U u;
            Throwable ex;
            if ((d = this.dst) != null && d.result == null) {
                try {
                    fr = fn.apply(arg);
                    ex = (fr == null) ? new NullPointerException() : null;
                } catch (Throwable rex) {
                    ex = rex;
                    fr = null;
                }
                if (ex != null)
                    u = null;
                else {
                    Object r = fr.result;
                    if (r == null)
                        r = fr.waitingGet(false);
                    if (r instanceof AltResult) {
                        ex = ((AltResult) r).ex;
                        u = null;
                    } else {
                        @SuppressWarnings("unchecked")
                        U ur = (U) r;
                        u = ur;
                    }
                }
                d.internalComplete(u, ex);
            }
            return true;
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class CompletionNode {

        final Completion completion;

        volatile CompletionNode next;

        CompletionNode(Completion completion) {
            this.completion = completion;
        }
    }

    abstract static class Completion extends AtomicInteger implements Runnable {
    }

    static final class ThenApply<T, U> extends Completion {

        final CompletableFuture<? extends T> src;

        final Function<? super T, ? extends U> fn;

        final CompletableFuture<U> dst;

        final Executor executor;

        ThenApply(CompletableFuture<? extends T> src, Function<? super T, ? extends U> fn, CompletableFuture<U> dst, Executor executor) {
            this.src = src;
            this.fn = fn;
            this.dst = dst;
            this.executor = executor;
        }

        public final void run() {
            final CompletableFuture<? extends T> a;
            final Function<? super T, ? extends U> fn;
            final CompletableFuture<U> dst;
            Object r;
            T t;
            Throwable ex;
            if ((dst = this.dst) != null && (fn = this.fn) != null && (a = this.src) != null && (r = a.result) != null && compareAndSet(0, 1)) {
                if (r instanceof AltResult) {
                    ex = ((AltResult) r).ex;
                    t = null;
                } else {
                    ex = null;
                    @SuppressWarnings("unchecked")
                    T tr = (T) r;
                    t = tr;
                }
                Executor e = executor;
                U u = null;
                if (ex == null) {
                    try {
                        if (e != null)
                            e.execute(new AsyncApply<T, U>(t, fn, dst));
                        else
                            u = fn.apply(t);
                    } catch (Throwable rex) {
                        ex = rex;
                    }
                }
                if (e == null || ex != null)
                    dst.internalComplete(u, ex);
            }
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class ThenAccept<T> extends Completion {

        final CompletableFuture<? extends T> src;

        final Consumer<? super T> fn;

        final CompletableFuture<Void> dst;

        final Executor executor;

        ThenAccept(CompletableFuture<? extends T> src, Consumer<? super T> fn, CompletableFuture<Void> dst, Executor executor) {
            this.src = src;
            this.fn = fn;
            this.dst = dst;
            this.executor = executor;
        }

        public final void run() {
            final CompletableFuture<? extends T> a;
            final Consumer<? super T> fn;
            final CompletableFuture<Void> dst;
            Object r;
            T t;
            Throwable ex;
            if ((dst = this.dst) != null && (fn = this.fn) != null && (a = this.src) != null && (r = a.result) != null && compareAndSet(0, 1)) {
                if (r instanceof AltResult) {
                    ex = ((AltResult) r).ex;
                    t = null;
                } else {
                    ex = null;
                    @SuppressWarnings("unchecked")
                    T tr = (T) r;
                    t = tr;
                }
                Executor e = executor;
                if (ex == null) {
                    try {
                        if (e != null)
                            e.execute(new AsyncAccept<T>(t, fn, dst));
                        else
                            fn.accept(t);
                    } catch (Throwable rex) {
                        ex = rex;
                    }
                }
                if (e == null || ex != null)
                    dst.internalComplete(null, ex);
            }
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class ThenRun extends Completion {

        final CompletableFuture<?> src;

        final Runnable fn;

        final CompletableFuture<Void> dst;

        final Executor executor;

        ThenRun(CompletableFuture<?> src, Runnable fn, CompletableFuture<Void> dst, Executor executor) {
            this.src = src;
            this.fn = fn;
            this.dst = dst;
            this.executor = executor;
        }

        public final void run() {
            final CompletableFuture<?> a;
            final Runnable fn;
            final CompletableFuture<Void> dst;
            Object r;
            Throwable ex;
            if ((dst = this.dst) != null && (fn = this.fn) != null && (a = this.src) != null && (r = a.result) != null && compareAndSet(0, 1)) {
                if (r instanceof AltResult)
                    ex = ((AltResult) r).ex;
                else
                    ex = null;
                Executor e = executor;
                if (ex == null) {
                    try {
                        if (e != null)
                            e.execute(new AsyncRun(fn, dst));
                        else
                            fn.run();
                    } catch (Throwable rex) {
                        ex = rex;
                    }
                }
                if (e == null || ex != null)
                    dst.internalComplete(null, ex);
            }
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class ThenCombine<T, U, V> extends Completion {

        final CompletableFuture<? extends T> src;

        final CompletableFuture<? extends U> snd;

        final BiFunction<? super T, ? super U, ? extends V> fn;

        final CompletableFuture<V> dst;

        final Executor executor;

        ThenCombine(CompletableFuture<? extends T> src, CompletableFuture<? extends U> snd, BiFunction<? super T, ? super U, ? extends V> fn, CompletableFuture<V> dst, Executor executor) {
            this.src = src;
            this.snd = snd;
            this.fn = fn;
            this.dst = dst;
            this.executor = executor;
        }

        public final void run() {
            final CompletableFuture<? extends T> a;
            final CompletableFuture<? extends U> b;
            final BiFunction<? super T, ? super U, ? extends V> fn;
            final CompletableFuture<V> dst;
            Object r, s;
            T t;
            U u;
            Throwable ex;
            if ((dst = this.dst) != null && (fn = this.fn) != null && (a = this.src) != null && (r = a.result) != null && (b = this.snd) != null && (s = b.result) != null && compareAndSet(0, 1)) {
                if (r instanceof AltResult) {
                    ex = ((AltResult) r).ex;
                    t = null;
                } else {
                    ex = null;
                    @SuppressWarnings("unchecked")
                    T tr = (T) r;
                    t = tr;
                }
                if (ex != null)
                    u = null;
                else if (s instanceof AltResult) {
                    ex = ((AltResult) s).ex;
                    u = null;
                } else {
                    @SuppressWarnings("unchecked")
                    U us = (U) s;
                    u = us;
                }
                Executor e = executor;
                V v = null;
                if (ex == null) {
                    try {
                        if (e != null)
                            e.execute(new AsyncCombine<T, U, V>(t, u, fn, dst));
                        else
                            v = fn.apply(t, u);
                    } catch (Throwable rex) {
                        ex = rex;
                    }
                }
                if (e == null || ex != null)
                    dst.internalComplete(v, ex);
            }
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class ThenAcceptBoth<T, U> extends Completion {

        final CompletableFuture<? extends T> src;

        final CompletableFuture<? extends U> snd;

        final BiConsumer<? super T, ? super U> fn;

        final CompletableFuture<Void> dst;

        final Executor executor;

        ThenAcceptBoth(CompletableFuture<? extends T> src, CompletableFuture<? extends U> snd, BiConsumer<? super T, ? super U> fn, CompletableFuture<Void> dst, Executor executor) {
            this.src = src;
            this.snd = snd;
            this.fn = fn;
            this.dst = dst;
            this.executor = executor;
        }

        public final void run() {
            final CompletableFuture<? extends T> a;
            final CompletableFuture<? extends U> b;
            final BiConsumer<? super T, ? super U> fn;
            final CompletableFuture<Void> dst;
            Object r, s;
            T t;
            U u;
            Throwable ex;
            if ((dst = this.dst) != null && (fn = this.fn) != null && (a = this.src) != null && (r = a.result) != null && (b = this.snd) != null && (s = b.result) != null && compareAndSet(0, 1)) {
                if (r instanceof AltResult) {
                    ex = ((AltResult) r).ex;
                    t = null;
                } else {
                    ex = null;
                    @SuppressWarnings("unchecked")
                    T tr = (T) r;
                    t = tr;
                }
                if (ex != null)
                    u = null;
                else if (s instanceof AltResult) {
                    ex = ((AltResult) s).ex;
                    u = null;
                } else {
                    @SuppressWarnings("unchecked")
                    U us = (U) s;
                    u = us;
                }
                Executor e = executor;
                if (ex == null) {
                    try {
                        if (e != null)
                            e.execute(new AsyncAcceptBoth<T, U>(t, u, fn, dst));
                        else
                            fn.accept(t, u);
                    } catch (Throwable rex) {
                        ex = rex;
                    }
                }
                if (e == null || ex != null)
                    dst.internalComplete(null, ex);
            }
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class RunAfterBoth extends Completion {

        final CompletableFuture<?> src;

        final CompletableFuture<?> snd;

        final Runnable fn;

        final CompletableFuture<Void> dst;

        final Executor executor;

        RunAfterBoth(CompletableFuture<?> src, CompletableFuture<?> snd, Runnable fn, CompletableFuture<Void> dst, Executor executor) {
            this.src = src;
            this.snd = snd;
            this.fn = fn;
            this.dst = dst;
            this.executor = executor;
        }

        public final void run() {
            final CompletableFuture<?> a;
            final CompletableFuture<?> b;
            final Runnable fn;
            final CompletableFuture<Void> dst;
            Object r, s;
            Throwable ex;
            if ((dst = this.dst) != null && (fn = this.fn) != null && (a = this.src) != null && (r = a.result) != null && (b = this.snd) != null && (s = b.result) != null && compareAndSet(0, 1)) {
                if (r instanceof AltResult)
                    ex = ((AltResult) r).ex;
                else
                    ex = null;
                if (ex == null && (s instanceof AltResult))
                    ex = ((AltResult) s).ex;
                Executor e = executor;
                if (ex == null) {
                    try {
                        if (e != null)
                            e.execute(new AsyncRun(fn, dst));
                        else
                            fn.run();
                    } catch (Throwable rex) {
                        ex = rex;
                    }
                }
                if (e == null || ex != null)
                    dst.internalComplete(null, ex);
            }
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class AndCompletion extends Completion {

        final CompletableFuture<?> src;

        final CompletableFuture<?> snd;

        final CompletableFuture<Void> dst;

        AndCompletion(CompletableFuture<?> src, CompletableFuture<?> snd, CompletableFuture<Void> dst) {
            this.src = src;
            this.snd = snd;
            this.dst = dst;
        }

        public final void run() {
            final CompletableFuture<?> a;
            final CompletableFuture<?> b;
            final CompletableFuture<Void> dst;
            Object r, s;
            Throwable ex;
            if ((dst = this.dst) != null && (a = this.src) != null && (r = a.result) != null && (b = this.snd) != null && (s = b.result) != null && compareAndSet(0, 1)) {
                if (r instanceof AltResult)
                    ex = ((AltResult) r).ex;
                else
                    ex = null;
                if (ex == null && (s instanceof AltResult))
                    ex = ((AltResult) s).ex;
                dst.internalComplete(null, ex);
            }
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class ApplyToEither<T, U> extends Completion {

        final CompletableFuture<? extends T> src;

        final CompletableFuture<? extends T> snd;

        final Function<? super T, ? extends U> fn;

        final CompletableFuture<U> dst;

        final Executor executor;

        ApplyToEither(CompletableFuture<? extends T> src, CompletableFuture<? extends T> snd, Function<? super T, ? extends U> fn, CompletableFuture<U> dst, Executor executor) {
            this.src = src;
            this.snd = snd;
            this.fn = fn;
            this.dst = dst;
            this.executor = executor;
        }

        public final void run() {
            final CompletableFuture<? extends T> a;
            final CompletableFuture<? extends T> b;
            final Function<? super T, ? extends U> fn;
            final CompletableFuture<U> dst;
            Object r;
            T t;
            Throwable ex;
            if ((dst = this.dst) != null && (fn = this.fn) != null && (((a = this.src) != null && (r = a.result) != null) || ((b = this.snd) != null && (r = b.result) != null)) && compareAndSet(0, 1)) {
                if (r instanceof AltResult) {
                    ex = ((AltResult) r).ex;
                    t = null;
                } else {
                    ex = null;
                    @SuppressWarnings("unchecked")
                    T tr = (T) r;
                    t = tr;
                }
                Executor e = executor;
                U u = null;
                if (ex == null) {
                    try {
                        if (e != null)
                            e.execute(new AsyncApply<T, U>(t, fn, dst));
                        else
                            u = fn.apply(t);
                    } catch (Throwable rex) {
                        ex = rex;
                    }
                }
                if (e == null || ex != null)
                    dst.internalComplete(u, ex);
            }
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class AcceptEither<T> extends Completion {

        final CompletableFuture<? extends T> src;

        final CompletableFuture<? extends T> snd;

        final Consumer<? super T> fn;

        final CompletableFuture<Void> dst;

        final Executor executor;

        AcceptEither(CompletableFuture<? extends T> src, CompletableFuture<? extends T> snd, Consumer<? super T> fn, CompletableFuture<Void> dst, Executor executor) {
            this.src = src;
            this.snd = snd;
            this.fn = fn;
            this.dst = dst;
            this.executor = executor;
        }

        public final void run() {
            final CompletableFuture<? extends T> a;
            final CompletableFuture<? extends T> b;
            final Consumer<? super T> fn;
            final CompletableFuture<Void> dst;
            Object r;
            T t;
            Throwable ex;
            if ((dst = this.dst) != null && (fn = this.fn) != null && (((a = this.src) != null && (r = a.result) != null) || ((b = this.snd) != null && (r = b.result) != null)) && compareAndSet(0, 1)) {
                if (r instanceof AltResult) {
                    ex = ((AltResult) r).ex;
                    t = null;
                } else {
                    ex = null;
                    @SuppressWarnings("unchecked")
                    T tr = (T) r;
                    t = tr;
                }
                Executor e = executor;
                if (ex == null) {
                    try {
                        if (e != null)
                            e.execute(new AsyncAccept<T>(t, fn, dst));
                        else
                            fn.accept(t);
                    } catch (Throwable rex) {
                        ex = rex;
                    }
                }
                if (e == null || ex != null)
                    dst.internalComplete(null, ex);
            }
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class RunAfterEither extends Completion {

        final CompletableFuture<?> src;

        final CompletableFuture<?> snd;

        final Runnable fn;

        final CompletableFuture<Void> dst;

        final Executor executor;

        RunAfterEither(CompletableFuture<?> src, CompletableFuture<?> snd, Runnable fn, CompletableFuture<Void> dst, Executor executor) {
            this.src = src;
            this.snd = snd;
            this.fn = fn;
            this.dst = dst;
            this.executor = executor;
        }

        public final void run() {
            final CompletableFuture<?> a;
            final CompletableFuture<?> b;
            final Runnable fn;
            final CompletableFuture<Void> dst;
            Object r;
            Throwable ex;
            if ((dst = this.dst) != null && (fn = this.fn) != null && (((a = this.src) != null && (r = a.result) != null) || ((b = this.snd) != null && (r = b.result) != null)) && compareAndSet(0, 1)) {
                if (r instanceof AltResult)
                    ex = ((AltResult) r).ex;
                else
                    ex = null;
                Executor e = executor;
                if (ex == null) {
                    try {
                        if (e != null)
                            e.execute(new AsyncRun(fn, dst));
                        else
                            fn.run();
                    } catch (Throwable rex) {
                        ex = rex;
                    }
                }
                if (e == null || ex != null)
                    dst.internalComplete(null, ex);
            }
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class OrCompletion extends Completion {

        final CompletableFuture<?> src;

        final CompletableFuture<?> snd;

        final CompletableFuture<Object> dst;

        OrCompletion(CompletableFuture<?> src, CompletableFuture<?> snd, CompletableFuture<Object> dst) {
            this.src = src;
            this.snd = snd;
            this.dst = dst;
        }

        public final void run() {
            final CompletableFuture<?> a;
            final CompletableFuture<?> b;
            final CompletableFuture<Object> dst;
            Object r, t;
            Throwable ex;
            if ((dst = this.dst) != null && (((a = this.src) != null && (r = a.result) != null) || ((b = this.snd) != null && (r = b.result) != null)) && compareAndSet(0, 1)) {
                if (r instanceof AltResult) {
                    ex = ((AltResult) r).ex;
                    t = null;
                } else {
                    ex = null;
                    t = r;
                }
                dst.internalComplete(t, ex);
            }
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class ExceptionCompletion<T> extends Completion {

        final CompletableFuture<? extends T> src;

        final Function<? super Throwable, ? extends T> fn;

        final CompletableFuture<T> dst;

        ExceptionCompletion(CompletableFuture<? extends T> src, Function<? super Throwable, ? extends T> fn, CompletableFuture<T> dst) {
            this.src = src;
            this.fn = fn;
            this.dst = dst;
        }

        public final void run() {
            final CompletableFuture<? extends T> a;
            final Function<? super Throwable, ? extends T> fn;
            final CompletableFuture<T> dst;
            Object r;
            T t = null;
            Throwable ex, dx = null;
            if ((dst = this.dst) != null && (fn = this.fn) != null && (a = this.src) != null && (r = a.result) != null && compareAndSet(0, 1)) {
                if ((r instanceof AltResult) && (ex = ((AltResult) r).ex) != null) {
                    try {
                        t = fn.apply(ex);
                    } catch (Throwable rex) {
                        dx = rex;
                    }
                } else {
                    @SuppressWarnings("unchecked")
                    T tr = (T) r;
                    t = tr;
                }
                dst.internalComplete(t, dx);
            }
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class ThenCopy<T> extends Completion {

        final CompletableFuture<?> src;

        final CompletableFuture<T> dst;

        ThenCopy(CompletableFuture<?> src, CompletableFuture<T> dst) {
            this.src = src;
            this.dst = dst;
        }

        public final void run() {
            final CompletableFuture<?> a;
            final CompletableFuture<T> dst;
            Object r;
            T t;
            Throwable ex;
            if ((dst = this.dst) != null && (a = this.src) != null && (r = a.result) != null && compareAndSet(0, 1)) {
                if (r instanceof AltResult) {
                    ex = ((AltResult) r).ex;
                    t = null;
                } else {
                    ex = null;
                    @SuppressWarnings("unchecked")
                    T tr = (T) r;
                    t = tr;
                }
                dst.internalComplete(t, ex);
            }
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class ThenPropagate extends Completion {

        final CompletableFuture<?> src;

        final CompletableFuture<Void> dst;

        ThenPropagate(CompletableFuture<?> src, CompletableFuture<Void> dst) {
            this.src = src;
            this.dst = dst;
        }

        public final void run() {
            final CompletableFuture<?> a;
            final CompletableFuture<Void> dst;
            Object r;
            Throwable ex;
            if ((dst = this.dst) != null && (a = this.src) != null && (r = a.result) != null && compareAndSet(0, 1)) {
                if (r instanceof AltResult)
                    ex = ((AltResult) r).ex;
                else
                    ex = null;
                dst.internalComplete(null, ex);
            }
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class HandleCompletion<T, U> extends Completion {

        final CompletableFuture<? extends T> src;

        final BiFunction<? super T, Throwable, ? extends U> fn;

        final CompletableFuture<U> dst;

        HandleCompletion(CompletableFuture<? extends T> src, BiFunction<? super T, Throwable, ? extends U> fn, CompletableFuture<U> dst) {
            this.src = src;
            this.fn = fn;
            this.dst = dst;
        }

        public final void run() {
            final CompletableFuture<? extends T> a;
            final BiFunction<? super T, Throwable, ? extends U> fn;
            final CompletableFuture<U> dst;
            Object r;
            T t;
            Throwable ex;
            if ((dst = this.dst) != null && (fn = this.fn) != null && (a = this.src) != null && (r = a.result) != null && compareAndSet(0, 1)) {
                if (r instanceof AltResult) {
                    ex = ((AltResult) r).ex;
                    t = null;
                } else {
                    ex = null;
                    @SuppressWarnings("unchecked")
                    T tr = (T) r;
                    t = tr;
                }
                U u = null;
                Throwable dx = null;
                try {
                    u = fn.apply(t, ex);
                } catch (Throwable rex) {
                    dx = rex;
                }
                dst.internalComplete(u, dx);
            }
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class ThenCompose<T, U> extends Completion {

        final CompletableFuture<? extends T> src;

        final Function<? super T, CompletableFuture<U>> fn;

        final CompletableFuture<U> dst;

        final Executor executor;

        ThenCompose(CompletableFuture<? extends T> src, Function<? super T, CompletableFuture<U>> fn, CompletableFuture<U> dst, Executor executor) {
            this.src = src;
            this.fn = fn;
            this.dst = dst;
            this.executor = executor;
        }

        public final void run() {
            final CompletableFuture<? extends T> a;
            final Function<? super T, CompletableFuture<U>> fn;
            final CompletableFuture<U> dst;
            Object r;
            T t;
            Throwable ex;
            Executor e;
            if ((dst = this.dst) != null && (fn = this.fn) != null && (a = this.src) != null && (r = a.result) != null && compareAndSet(0, 1)) {
                if (r instanceof AltResult) {
                    ex = ((AltResult) r).ex;
                    t = null;
                } else {
                    ex = null;
                    @SuppressWarnings("unchecked")
                    T tr = (T) r;
                    t = tr;
                }
                CompletableFuture<U> c = null;
                U u = null;
                boolean complete = false;
                if (ex == null) {
                    if ((e = executor) != null)
                        e.execute(new AsyncCompose<T, U>(t, fn, dst));
                    else {
                        try {
                            if ((c = fn.apply(t)) == null)
                                ex = new NullPointerException();
                        } catch (Throwable rex) {
                            ex = rex;
                        }
                    }
                }
                if (c != null) {
                    ThenCopy<U> d = null;
                    Object s;
                    if ((s = c.result) == null) {
                        CompletionNode p = new CompletionNode(d = new ThenCopy<U>(c, dst));
                        while ((s = c.result) == null) {
                            if (UNSAFE.compareAndSwapObject(c, COMPLETIONS, p.next = c.completions, p))
                                break;
                        }
                    }
                    if (s != null && (d == null || d.compareAndSet(0, 1))) {
                        complete = true;
                        if (s instanceof AltResult) {
                            ex = ((AltResult) s).ex;
                            u = null;
                        } else {
                            @SuppressWarnings("unchecked")
                            U us = (U) s;
                            u = us;
                        }
                    }
                }
                if (complete || ex != null)
                    dst.internalComplete(u, ex);
                if (c != null)
                    c.helpPostComplete();
            }
        }

        private static final long serialVersionUID = 5232453952276885070L;
    }

    public CompletableFuture() {
    }

    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        if (supplier == null)
            throw new NullPointerException();
        CompletableFuture<U> f = new CompletableFuture<U>();
        ForkJoinPool.commonPool().execute((ForkJoinTask<?>) new AsyncSupply<U>(supplier, f));
        return f;
    }

    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor) {
        if (executor == null || supplier == null)
            throw new NullPointerException();
        CompletableFuture<U> f = new CompletableFuture<U>();
        executor.execute(new AsyncSupply<U>(supplier, f));
        return f;
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        if (runnable == null)
            throw new NullPointerException();
        CompletableFuture<Void> f = new CompletableFuture<Void>();
        ForkJoinPool.commonPool().execute((ForkJoinTask<?>) new AsyncRun(runnable, f));
        return f;
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor) {
        if (executor == null || runnable == null)
            throw new NullPointerException();
        CompletableFuture<Void> f = new CompletableFuture<Void>();
        executor.execute(new AsyncRun(runnable, f));
        return f;
    }

    public static <U> CompletableFuture<U> completedFuture(U value) {
        CompletableFuture<U> f = new CompletableFuture<U>();
        f.result = (value == null) ? NIL : value;
        return f;
    }

    public boolean isDone() {
        return result != null;
    }

    public T get() throws InterruptedException, ExecutionException {
        Object r;
        Throwable ex, cause;
        if ((r = result) == null && (r = waitingGet(true)) == null)
            throw new InterruptedException();
        if (!(r instanceof AltResult)) {
            @SuppressWarnings("unchecked")
            T tr = (T) r;
            return tr;
        }
        if ((ex = ((AltResult) r).ex) == null)
            return null;
        if (ex instanceof CancellationException)
            throw (CancellationException) ex;
        if ((ex instanceof CompletionException) && (cause = ex.getCause()) != null)
            ex = cause;
        throw new ExecutionException(ex);
    }

    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Object r;
        Throwable ex, cause;
        long nanos = unit.toNanos(timeout);
        if (Thread.interrupted())
            throw new InterruptedException();
        if ((r = result) == null)
            r = timedAwaitDone(nanos);
        if (!(r instanceof AltResult)) {
            @SuppressWarnings("unchecked")
            T tr = (T) r;
            return tr;
        }
        if ((ex = ((AltResult) r).ex) == null)
            return null;
        if (ex instanceof CancellationException)
            throw (CancellationException) ex;
        if ((ex instanceof CompletionException) && (cause = ex.getCause()) != null)
            ex = cause;
        throw new ExecutionException(ex);
    }

    public T join() {
        Object r;
        Throwable ex;
        if ((r = result) == null)
            r = waitingGet(false);
        if (!(r instanceof AltResult)) {
            @SuppressWarnings("unchecked")
            T tr = (T) r;
            return tr;
        }
        if ((ex = ((AltResult) r).ex) == null)
            return null;
        if (ex instanceof CancellationException)
            throw (CancellationException) ex;
        if (ex instanceof CompletionException)
            throw (CompletionException) ex;
        throw new CompletionException(ex);
    }

    public T getNow(T valueIfAbsent) {
        Object r;
        Throwable ex;
        if ((r = result) == null)
            return valueIfAbsent;
        if (!(r instanceof AltResult)) {
            @SuppressWarnings("unchecked")
            T tr = (T) r;
            return tr;
        }
        if ((ex = ((AltResult) r).ex) == null)
            return null;
        if (ex instanceof CancellationException)
            throw (CancellationException) ex;
        if (ex instanceof CompletionException)
            throw (CompletionException) ex;
        throw new CompletionException(ex);
    }

    public boolean complete(T value) {
        boolean triggered = result == null && UNSAFE.compareAndSwapObject(this, RESULT, null, value == null ? NIL : value);
        postComplete();
        return triggered;
    }

    public boolean completeExceptionally(Throwable ex) {
        if (ex == null)
            throw new NullPointerException();
        boolean triggered = result == null && UNSAFE.compareAndSwapObject(this, RESULT, null, new AltResult(ex));
        postComplete();
        return triggered;
    }

    public <U> CompletableFuture<U> thenApply(Function<? super T, ? extends U> fn) {
        return doThenApply(fn, null);
    }

    public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
        return doThenApply(fn, ForkJoinPool.commonPool());
    }

    public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
        if (executor == null)
            throw new NullPointerException();
        return doThenApply(fn, executor);
    }

    private <U> CompletableFuture<U> doThenApply(Function<? super T, ? extends U> fn, Executor e) {
        if (fn == null)
            throw new NullPointerException();
        CompletableFuture<U> dst = new CompletableFuture<U>();
        ThenApply<T, U> d = null;
        Object r;
        if ((r = result) == null) {
            CompletionNode p = new CompletionNode(d = new ThenApply<T, U>(this, fn, dst, e));
            while ((r = result) == null) {
                if (UNSAFE.compareAndSwapObject(this, COMPLETIONS, p.next = completions, p))
                    break;
            }
        }
        if (r != null && (d == null || d.compareAndSet(0, 1))) {
            T t;
            Throwable ex;
            if (r instanceof AltResult) {
                ex = ((AltResult) r).ex;
                t = null;
            } else {
                ex = null;
                @SuppressWarnings("unchecked")
                T tr = (T) r;
                t = tr;
            }
            U u = null;
            if (ex == null) {
                try {
                    if (e != null)
                        e.execute(new AsyncApply<T, U>(t, fn, dst));
                    else
                        u = fn.apply(t);
                } catch (Throwable rex) {
                    ex = rex;
                }
            }
            if (e == null || ex != null)
                dst.internalComplete(u, ex);
        }
        helpPostComplete();
        return dst;
    }

    public CompletableFuture<Void> thenAccept(Consumer<? super T> block) {
        return doThenAccept(block, null);
    }

    public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> block) {
        return doThenAccept(block, ForkJoinPool.commonPool());
    }

    public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> block, Executor executor) {
        if (executor == null)
            throw new NullPointerException();
        return doThenAccept(block, executor);
    }

    private CompletableFuture<Void> doThenAccept(Consumer<? super T> fn, Executor e) {
        if (fn == null)
            throw new NullPointerException();
        CompletableFuture<Void> dst = new CompletableFuture<Void>();
        ThenAccept<T> d = null;
        Object r;
        if ((r = result) == null) {
            CompletionNode p = new CompletionNode(d = new ThenAccept<T>(this, fn, dst, e));
            while ((r = result) == null) {
                if (UNSAFE.compareAndSwapObject(this, COMPLETIONS, p.next = completions, p))
                    break;
            }
        }
        if (r != null && (d == null || d.compareAndSet(0, 1))) {
            T t;
            Throwable ex;
            if (r instanceof AltResult) {
                ex = ((AltResult) r).ex;
                t = null;
            } else {
                ex = null;
                @SuppressWarnings("unchecked")
                T tr = (T) r;
                t = tr;
            }
            if (ex == null) {
                try {
                    if (e != null)
                        e.execute(new AsyncAccept<T>(t, fn, dst));
                    else
                        fn.accept(t);
                } catch (Throwable rex) {
                    ex = rex;
                }
            }
            if (e == null || ex != null)
                dst.internalComplete(null, ex);
        }
        helpPostComplete();
        return dst;
    }

    public CompletableFuture<Void> thenRun(Runnable action) {
        return doThenRun(action, null);
    }

    public CompletableFuture<Void> thenRunAsync(Runnable action) {
        return doThenRun(action, ForkJoinPool.commonPool());
    }

    public CompletableFuture<Void> thenRunAsync(Runnable action, Executor executor) {
        if (executor == null)
            throw new NullPointerException();
        return doThenRun(action, executor);
    }

    private CompletableFuture<Void> doThenRun(Runnable action, Executor e) {
        if (action == null)
            throw new NullPointerException();
        CompletableFuture<Void> dst = new CompletableFuture<Void>();
        ThenRun d = null;
        Object r;
        if ((r = result) == null) {
            CompletionNode p = new CompletionNode(d = new ThenRun(this, action, dst, e));
            while ((r = result) == null) {
                if (UNSAFE.compareAndSwapObject(this, COMPLETIONS, p.next = completions, p))
                    break;
            }
        }
        if (r != null && (d == null || d.compareAndSet(0, 1))) {
            Throwable ex;
            if (r instanceof AltResult)
                ex = ((AltResult) r).ex;
            else
                ex = null;
            if (ex == null) {
                try {
                    if (e != null)
                        e.execute(new AsyncRun(action, dst));
                    else
                        action.run();
                } catch (Throwable rex) {
                    ex = rex;
                }
            }
            if (e == null || ex != null)
                dst.internalComplete(null, ex);
        }
        helpPostComplete();
        return dst;
    }

    public <U, V> CompletableFuture<V> thenCombine(CompletableFuture<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return doThenCombine(other, fn, null);
    }

    public <U, V> CompletableFuture<V> thenCombineAsync(CompletableFuture<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return doThenCombine(other, fn, ForkJoinPool.commonPool());
    }

    public <U, V> CompletableFuture<V> thenCombineAsync(CompletableFuture<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn, Executor executor) {
        if (executor == null)
            throw new NullPointerException();
        return doThenCombine(other, fn, executor);
    }

    private <U, V> CompletableFuture<V> doThenCombine(CompletableFuture<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn, Executor e) {
        if (other == null || fn == null)
            throw new NullPointerException();
        CompletableFuture<V> dst = new CompletableFuture<V>();
        ThenCombine<T, U, V> d = null;
        Object r, s = null;
        if ((r = result) == null || (s = other.result) == null) {
            d = new ThenCombine<T, U, V>(this, other, fn, dst, e);
            CompletionNode q = null, p = new CompletionNode(d);
            while ((r == null && (r = result) == null) || (s == null && (s = other.result) == null)) {
                if (q != null) {
                    if (s != null || UNSAFE.compareAndSwapObject(other, COMPLETIONS, q.next = other.completions, q))
                        break;
                } else if (r != null || UNSAFE.compareAndSwapObject(this, COMPLETIONS, p.next = completions, p)) {
                    if (s != null)
                        break;
                    q = new CompletionNode(d);
                }
            }
        }
        if (r != null && s != null && (d == null || d.compareAndSet(0, 1))) {
            T t;
            U u;
            Throwable ex;
            if (r instanceof AltResult) {
                ex = ((AltResult) r).ex;
                t = null;
            } else {
                ex = null;
                @SuppressWarnings("unchecked")
                T tr = (T) r;
                t = tr;
            }
            if (ex != null)
                u = null;
            else if (s instanceof AltResult) {
                ex = ((AltResult) s).ex;
                u = null;
            } else {
                @SuppressWarnings("unchecked")
                U us = (U) s;
                u = us;
            }
            V v = null;
            if (ex == null) {
                try {
                    if (e != null)
                        e.execute(new AsyncCombine<T, U, V>(t, u, fn, dst));
                    else
                        v = fn.apply(t, u);
                } catch (Throwable rex) {
                    ex = rex;
                }
            }
            if (e == null || ex != null)
                dst.internalComplete(v, ex);
        }
        helpPostComplete();
        other.helpPostComplete();
        return dst;
    }

    public <U> CompletableFuture<Void> thenAcceptBoth(CompletableFuture<? extends U> other, BiConsumer<? super T, ? super U> block) {
        return doThenAcceptBoth(other, block, null);
    }

    public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletableFuture<? extends U> other, BiConsumer<? super T, ? super U> block) {
        return doThenAcceptBoth(other, block, ForkJoinPool.commonPool());
    }

    public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletableFuture<? extends U> other, BiConsumer<? super T, ? super U> block, Executor executor) {
        if (executor == null)
            throw new NullPointerException();
        return doThenAcceptBoth(other, block, executor);
    }

    private <U> CompletableFuture<Void> doThenAcceptBoth(CompletableFuture<? extends U> other, BiConsumer<? super T, ? super U> fn, Executor e) {
        if (other == null || fn == null)
            throw new NullPointerException();
        CompletableFuture<Void> dst = new CompletableFuture<Void>();
        ThenAcceptBoth<T, U> d = null;
        Object r, s = null;
        if ((r = result) == null || (s = other.result) == null) {
            d = new ThenAcceptBoth<T, U>(this, other, fn, dst, e);
            CompletionNode q = null, p = new CompletionNode(d);
            while ((r == null && (r = result) == null) || (s == null && (s = other.result) == null)) {
                if (q != null) {
                    if (s != null || UNSAFE.compareAndSwapObject(other, COMPLETIONS, q.next = other.completions, q))
                        break;
                } else if (r != null || UNSAFE.compareAndSwapObject(this, COMPLETIONS, p.next = completions, p)) {
                    if (s != null)
                        break;
                    q = new CompletionNode(d);
                }
            }
        }
        if (r != null && s != null && (d == null || d.compareAndSet(0, 1))) {
            T t;
            U u;
            Throwable ex;
            if (r instanceof AltResult) {
                ex = ((AltResult) r).ex;
                t = null;
            } else {
                ex = null;
                @SuppressWarnings("unchecked")
                T tr = (T) r;
                t = tr;
            }
            if (ex != null)
                u = null;
            else if (s instanceof AltResult) {
                ex = ((AltResult) s).ex;
                u = null;
            } else {
                @SuppressWarnings("unchecked")
                U us = (U) s;
                u = us;
            }
            if (ex == null) {
                try {
                    if (e != null)
                        e.execute(new AsyncAcceptBoth<T, U>(t, u, fn, dst));
                    else
                        fn.accept(t, u);
                } catch (Throwable rex) {
                    ex = rex;
                }
            }
            if (e == null || ex != null)
                dst.internalComplete(null, ex);
        }
        helpPostComplete();
        other.helpPostComplete();
        return dst;
    }

    public CompletableFuture<Void> runAfterBoth(CompletableFuture<?> other, Runnable action) {
        return doRunAfterBoth(other, action, null);
    }

    public CompletableFuture<Void> runAfterBothAsync(CompletableFuture<?> other, Runnable action) {
        return doRunAfterBoth(other, action, ForkJoinPool.commonPool());
    }

    public CompletableFuture<Void> runAfterBothAsync(CompletableFuture<?> other, Runnable action, Executor executor) {
        if (executor == null)
            throw new NullPointerException();
        return doRunAfterBoth(other, action, executor);
    }

    private CompletableFuture<Void> doRunAfterBoth(CompletableFuture<?> other, Runnable action, Executor e) {
        if (other == null || action == null)
            throw new NullPointerException();
        CompletableFuture<Void> dst = new CompletableFuture<Void>();
        RunAfterBoth d = null;
        Object r, s = null;
        if ((r = result) == null || (s = other.result) == null) {
            d = new RunAfterBoth(this, other, action, dst, e);
            CompletionNode q = null, p = new CompletionNode(d);
            while ((r == null && (r = result) == null) || (s == null && (s = other.result) == null)) {
                if (q != null) {
                    if (s != null || UNSAFE.compareAndSwapObject(other, COMPLETIONS, q.next = other.completions, q))
                        break;
                } else if (r != null || UNSAFE.compareAndSwapObject(this, COMPLETIONS, p.next = completions, p)) {
                    if (s != null)
                        break;
                    q = new CompletionNode(d);
                }
            }
        }
        if (r != null && s != null && (d == null || d.compareAndSet(0, 1))) {
            Throwable ex;
            if (r instanceof AltResult)
                ex = ((AltResult) r).ex;
            else
                ex = null;
            if (ex == null && (s instanceof AltResult))
                ex = ((AltResult) s).ex;
            if (ex == null) {
                try {
                    if (e != null)
                        e.execute(new AsyncRun(action, dst));
                    else
                        action.run();
                } catch (Throwable rex) {
                    ex = rex;
                }
            }
            if (e == null || ex != null)
                dst.internalComplete(null, ex);
        }
        helpPostComplete();
        other.helpPostComplete();
        return dst;
    }

    public <U> CompletableFuture<U> applyToEither(CompletableFuture<? extends T> other, Function<? super T, U> fn) {
        return doApplyToEither(other, fn, null);
    }

    public <U> CompletableFuture<U> applyToEitherAsync(CompletableFuture<? extends T> other, Function<? super T, U> fn) {
        return doApplyToEither(other, fn, ForkJoinPool.commonPool());
    }

    public <U> CompletableFuture<U> applyToEitherAsync(CompletableFuture<? extends T> other, Function<? super T, U> fn, Executor executor) {
        if (executor == null)
            throw new NullPointerException();
        return doApplyToEither(other, fn, executor);
    }

    private <U> CompletableFuture<U> doApplyToEither(CompletableFuture<? extends T> other, Function<? super T, U> fn, Executor e) {
        if (other == null || fn == null)
            throw new NullPointerException();
        CompletableFuture<U> dst = new CompletableFuture<U>();
        ApplyToEither<T, U> d = null;
        Object r;
        if ((r = result) == null && (r = other.result) == null) {
            d = new ApplyToEither<T, U>(this, other, fn, dst, e);
            CompletionNode q = null, p = new CompletionNode(d);
            while ((r = result) == null && (r = other.result) == null) {
                if (q != null) {
                    if (UNSAFE.compareAndSwapObject(other, COMPLETIONS, q.next = other.completions, q))
                        break;
                } else if (UNSAFE.compareAndSwapObject(this, COMPLETIONS, p.next = completions, p))
                    q = new CompletionNode(d);
            }
        }
        if (r != null && (d == null || d.compareAndSet(0, 1))) {
            T t;
            Throwable ex;
            if (r instanceof AltResult) {
                ex = ((AltResult) r).ex;
                t = null;
            } else {
                ex = null;
                @SuppressWarnings("unchecked")
                T tr = (T) r;
                t = tr;
            }
            U u = null;
            if (ex == null) {
                try {
                    if (e != null)
                        e.execute(new AsyncApply<T, U>(t, fn, dst));
                    else
                        u = fn.apply(t);
                } catch (Throwable rex) {
                    ex = rex;
                }
            }
            if (e == null || ex != null)
                dst.internalComplete(u, ex);
        }
        helpPostComplete();
        other.helpPostComplete();
        return dst;
    }

    public CompletableFuture<Void> acceptEither(CompletableFuture<? extends T> other, Consumer<? super T> block) {
        return doAcceptEither(other, block, null);
    }

    public CompletableFuture<Void> acceptEitherAsync(CompletableFuture<? extends T> other, Consumer<? super T> block) {
        return doAcceptEither(other, block, ForkJoinPool.commonPool());
    }

    public CompletableFuture<Void> acceptEitherAsync(CompletableFuture<? extends T> other, Consumer<? super T> block, Executor executor) {
        if (executor == null)
            throw new NullPointerException();
        return doAcceptEither(other, block, executor);
    }

    private CompletableFuture<Void> doAcceptEither(CompletableFuture<? extends T> other, Consumer<? super T> fn, Executor e) {
        if (other == null || fn == null)
            throw new NullPointerException();
        CompletableFuture<Void> dst = new CompletableFuture<Void>();
        AcceptEither<T> d = null;
        Object r;
        if ((r = result) == null && (r = other.result) == null) {
            d = new AcceptEither<T>(this, other, fn, dst, e);
            CompletionNode q = null, p = new CompletionNode(d);
            while ((r = result) == null && (r = other.result) == null) {
                if (q != null) {
                    if (UNSAFE.compareAndSwapObject(other, COMPLETIONS, q.next = other.completions, q))
                        break;
                } else if (UNSAFE.compareAndSwapObject(this, COMPLETIONS, p.next = completions, p))
                    q = new CompletionNode(d);
            }
        }
        if (r != null && (d == null || d.compareAndSet(0, 1))) {
            T t;
            Throwable ex;
            if (r instanceof AltResult) {
                ex = ((AltResult) r).ex;
                t = null;
            } else {
                ex = null;
                @SuppressWarnings("unchecked")
                T tr = (T) r;
                t = tr;
            }
            if (ex == null) {
                try {
                    if (e != null)
                        e.execute(new AsyncAccept<T>(t, fn, dst));
                    else
                        fn.accept(t);
                } catch (Throwable rex) {
                    ex = rex;
                }
            }
            if (e == null || ex != null)
                dst.internalComplete(null, ex);
        }
        helpPostComplete();
        other.helpPostComplete();
        return dst;
    }

    public CompletableFuture<Void> runAfterEither(CompletableFuture<?> other, Runnable action) {
        return doRunAfterEither(other, action, null);
    }

    public CompletableFuture<Void> runAfterEitherAsync(CompletableFuture<?> other, Runnable action) {
        return doRunAfterEither(other, action, ForkJoinPool.commonPool());
    }

    public CompletableFuture<Void> runAfterEitherAsync(CompletableFuture<?> other, Runnable action, Executor executor) {
        if (executor == null)
            throw new NullPointerException();
        return doRunAfterEither(other, action, executor);
    }

    private CompletableFuture<Void> doRunAfterEither(CompletableFuture<?> other, Runnable action, Executor e) {
        if (other == null || action == null)
            throw new NullPointerException();
        CompletableFuture<Void> dst = new CompletableFuture<Void>();
        RunAfterEither d = null;
        Object r;
        if ((r = result) == null && (r = other.result) == null) {
            d = new RunAfterEither(this, other, action, dst, e);
            CompletionNode q = null, p = new CompletionNode(d);
            while ((r = result) == null && (r = other.result) == null) {
                if (q != null) {
                    if (UNSAFE.compareAndSwapObject(other, COMPLETIONS, q.next = other.completions, q))
                        break;
                } else if (UNSAFE.compareAndSwapObject(this, COMPLETIONS, p.next = completions, p))
                    q = new CompletionNode(d);
            }
        }
        if (r != null && (d == null || d.compareAndSet(0, 1))) {
            Throwable ex;
            if (r instanceof AltResult)
                ex = ((AltResult) r).ex;
            else
                ex = null;
            if (ex == null) {
                try {
                    if (e != null)
                        e.execute(new AsyncRun(action, dst));
                    else
                        action.run();
                } catch (Throwable rex) {
                    ex = rex;
                }
            }
            if (e == null || ex != null)
                dst.internalComplete(null, ex);
        }
        helpPostComplete();
        other.helpPostComplete();
        return dst;
    }

    public <U> CompletableFuture<U> thenCompose(Function<? super T, CompletableFuture<U>> fn) {
        return doThenCompose(fn, null);
    }

    public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, CompletableFuture<U>> fn) {
        return doThenCompose(fn, ForkJoinPool.commonPool());
    }

    public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, CompletableFuture<U>> fn, Executor executor) {
        if (executor == null)
            throw new NullPointerException();
        return doThenCompose(fn, executor);
    }

    private <U> CompletableFuture<U> doThenCompose(Function<? super T, CompletableFuture<U>> fn, Executor e) {
        if (fn == null)
            throw new NullPointerException();
        CompletableFuture<U> dst = null;
        ThenCompose<T, U> d = null;
        Object r;
        if ((r = result) == null) {
            dst = new CompletableFuture<U>();
            CompletionNode p = new CompletionNode(d = new ThenCompose<T, U>(this, fn, dst, e));
            while ((r = result) == null) {
                if (UNSAFE.compareAndSwapObject(this, COMPLETIONS, p.next = completions, p))
                    break;
            }
        }
        if (r != null && (d == null || d.compareAndSet(0, 1))) {
            T t;
            Throwable ex;
            if (r instanceof AltResult) {
                ex = ((AltResult) r).ex;
                t = null;
            } else {
                ex = null;
                @SuppressWarnings("unchecked")
                T tr = (T) r;
                t = tr;
            }
            if (ex == null) {
                if (e != null) {
                    if (dst == null)
                        dst = new CompletableFuture<U>();
                    e.execute(new AsyncCompose<T, U>(t, fn, dst));
                } else {
                    try {
                        if ((dst = fn.apply(t)) == null)
                            ex = new NullPointerException();
                    } catch (Throwable rex) {
                        ex = rex;
                    }
                }
            }
            if (dst == null)
                dst = new CompletableFuture<U>();
            if (e == null || ex != null)
                dst.internalComplete(null, ex);
        }
        helpPostComplete();
        dst.helpPostComplete();
        return dst;
    }

    public CompletableFuture<T> exceptionally(Function<Throwable, ? extends T> fn) {
        if (fn == null)
            throw new NullPointerException();
        CompletableFuture<T> dst = new CompletableFuture<T>();
        ExceptionCompletion<T> d = null;
        Object r;
        if ((r = result) == null) {
            CompletionNode p = new CompletionNode(d = new ExceptionCompletion<T>(this, fn, dst));
            while ((r = result) == null) {
                if (UNSAFE.compareAndSwapObject(this, COMPLETIONS, p.next = completions, p))
                    break;
            }
        }
        if (r != null && (d == null || d.compareAndSet(0, 1))) {
            T t = null;
            Throwable ex, dx = null;
            if (r instanceof AltResult) {
                if ((ex = ((AltResult) r).ex) != null) {
                    try {
                        t = fn.apply(ex);
                    } catch (Throwable rex) {
                        dx = rex;
                    }
                }
            } else {
                @SuppressWarnings("unchecked")
                T tr = (T) r;
                t = tr;
            }
            dst.internalComplete(t, dx);
        }
        helpPostComplete();
        return dst;
    }

    public <U> CompletableFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
        if (fn == null)
            throw new NullPointerException();
        CompletableFuture<U> dst = new CompletableFuture<U>();
        HandleCompletion<T, U> d = null;
        Object r;
        if ((r = result) == null) {
            CompletionNode p = new CompletionNode(d = new HandleCompletion<T, U>(this, fn, dst));
            while ((r = result) == null) {
                if (UNSAFE.compareAndSwapObject(this, COMPLETIONS, p.next = completions, p))
                    break;
            }
        }
        if (r != null && (d == null || d.compareAndSet(0, 1))) {
            T t;
            Throwable ex;
            if (r instanceof AltResult) {
                ex = ((AltResult) r).ex;
                t = null;
            } else {
                ex = null;
                @SuppressWarnings("unchecked")
                T tr = (T) r;
                t = tr;
            }
            U u;
            Throwable dx;
            try {
                u = fn.apply(t, ex);
                dx = null;
            } catch (Throwable rex) {
                dx = rex;
                u = null;
            }
            dst.internalComplete(u, dx);
        }
        helpPostComplete();
        return dst;
    }

    public static CompletableFuture<Void> allOf(CompletableFuture<?>... cfs) {
        int len = cfs.length;
        if (len > 1)
            return allTree(cfs, 0, len - 1);
        else {
            CompletableFuture<Void> dst = new CompletableFuture<Void>();
            CompletableFuture<?> f;
            if (len == 0)
                dst.result = NIL;
            else if ((f = cfs[0]) == null)
                throw new NullPointerException();
            else {
                ThenPropagate d = null;
                CompletionNode p = null;
                Object r;
                while ((r = f.result) == null) {
                    if (d == null)
                        d = new ThenPropagate(f, dst);
                    else if (p == null)
                        p = new CompletionNode(d);
                    else if (UNSAFE.compareAndSwapObject(f, COMPLETIONS, p.next = f.completions, p))
                        break;
                }
                if (r != null && (d == null || d.compareAndSet(0, 1)))
                    dst.internalComplete(null, (r instanceof AltResult) ? ((AltResult) r).ex : null);
                f.helpPostComplete();
            }
            return dst;
        }
    }

    private static CompletableFuture<Void> allTree(CompletableFuture<?>[] cfs, int lo, int hi) {
        CompletableFuture<?> fst, snd;
        int mid = (lo + hi) >>> 1;
        if ((fst = (lo == mid ? cfs[lo] : allTree(cfs, lo, mid))) == null || (snd = (hi == mid + 1 ? cfs[hi] : allTree(cfs, mid + 1, hi))) == null)
            throw new NullPointerException();
        CompletableFuture<Void> dst = new CompletableFuture<Void>();
        AndCompletion d = null;
        CompletionNode p = null, q = null;
        Object r = null, s = null;
        while ((r = fst.result) == null || (s = snd.result) == null) {
            if (d == null)
                d = new AndCompletion(fst, snd, dst);
            else if (p == null)
                p = new CompletionNode(d);
            else if (q == null) {
                if (UNSAFE.compareAndSwapObject(fst, COMPLETIONS, p.next = fst.completions, p))
                    q = new CompletionNode(d);
            } else if (UNSAFE.compareAndSwapObject(snd, COMPLETIONS, q.next = snd.completions, q))
                break;
        }
        if ((r != null || (r = fst.result) != null) && (s != null || (s = snd.result) != null) && (d == null || d.compareAndSet(0, 1))) {
            Throwable ex;
            if (r instanceof AltResult)
                ex = ((AltResult) r).ex;
            else
                ex = null;
            if (ex == null && (s instanceof AltResult))
                ex = ((AltResult) s).ex;
            dst.internalComplete(null, ex);
        }
        fst.helpPostComplete();
        snd.helpPostComplete();
        return dst;
    }

    public static CompletableFuture<Object> anyOf(CompletableFuture<?>... cfs) {
        int len = cfs.length;
        if (len > 1)
            return anyTree(cfs, 0, len - 1);
        else {
            CompletableFuture<Object> dst = new CompletableFuture<Object>();
            CompletableFuture<?> f;
            if (len == 0)
                ;
            else if ((f = cfs[0]) == null)
                throw new NullPointerException();
            else {
                ThenCopy<Object> d = null;
                CompletionNode p = null;
                Object r;
                while ((r = f.result) == null) {
                    if (d == null)
                        d = new ThenCopy<Object>(f, dst);
                    else if (p == null)
                        p = new CompletionNode(d);
                    else if (UNSAFE.compareAndSwapObject(f, COMPLETIONS, p.next = f.completions, p))
                        break;
                }
                if (r != null && (d == null || d.compareAndSet(0, 1))) {
                    Throwable ex;
                    Object t;
                    if (r instanceof AltResult) {
                        ex = ((AltResult) r).ex;
                        t = null;
                    } else {
                        ex = null;
                        t = r;
                    }
                    dst.internalComplete(t, ex);
                }
                f.helpPostComplete();
            }
            return dst;
        }
    }

    private static CompletableFuture<Object> anyTree(CompletableFuture<?>[] cfs, int lo, int hi) {
        CompletableFuture<?> fst, snd;
        int mid = (lo + hi) >>> 1;
        if ((fst = (lo == mid ? cfs[lo] : anyTree(cfs, lo, mid))) == null || (snd = (hi == mid + 1 ? cfs[hi] : anyTree(cfs, mid + 1, hi))) == null)
            throw new NullPointerException();
        CompletableFuture<Object> dst = new CompletableFuture<Object>();
        OrCompletion d = null;
        CompletionNode p = null, q = null;
        Object r;
        while ((r = fst.result) == null && (r = snd.result) == null) {
            if (d == null)
                d = new OrCompletion(fst, snd, dst);
            else if (p == null)
                p = new CompletionNode(d);
            else if (q == null) {
                if (UNSAFE.compareAndSwapObject(fst, COMPLETIONS, p.next = fst.completions, p))
                    q = new CompletionNode(d);
            } else if (UNSAFE.compareAndSwapObject(snd, COMPLETIONS, q.next = snd.completions, q))
                break;
        }
        if ((r != null || (r = fst.result) != null || (r = snd.result) != null) && (d == null || d.compareAndSet(0, 1))) {
            Throwable ex;
            Object t;
            if (r instanceof AltResult) {
                ex = ((AltResult) r).ex;
                t = null;
            } else {
                ex = null;
                t = r;
            }
            dst.internalComplete(t, ex);
        }
        fst.helpPostComplete();
        snd.helpPostComplete();
        return dst;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean cancelled = (result == null) && UNSAFE.compareAndSwapObject(this, RESULT, null, new AltResult(new CancellationException()));
        postComplete();
        return cancelled || isCancelled();
    }

    public boolean isCancelled() {
        Object r;
        return ((r = result) instanceof AltResult) && (((AltResult) r).ex instanceof CancellationException);
    }

    public void obtrudeValue(T value) {
        result = (value == null) ? NIL : value;
        postComplete();
    }

    public void obtrudeException(Throwable ex) {
        if (ex == null)
            throw new NullPointerException();
        result = new AltResult(ex);
        postComplete();
    }

    public int getNumberOfDependents() {
        int count = 0;
        for (CompletionNode p = completions; p != null; p = p.next) ++count;
        return count;
    }

    public String toString() {
        Object r = result;
        int count;
        return super.toString() + ((r == null) ? (((count = getNumberOfDependents()) == 0) ? "[Not completed]" : "[Not completed, " + count + " dependents]") : (((r instanceof AltResult) && ((AltResult) r).ex != null) ? "[Completed exceptionally]" : "[Completed normally]"));
    }

    private static final sun.misc.Unsafe UNSAFE;

    private static final long RESULT;

    private static final long WAITERS;

    private static final long COMPLETIONS;

    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = CompletableFuture.class;
            RESULT = UNSAFE.objectFieldOffset(k.getDeclaredField("result"));
            WAITERS = UNSAFE.objectFieldOffset(k.getDeclaredField("waiters"));
            COMPLETIONS = UNSAFE.objectFieldOffset(k.getDeclaredField("completions"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
