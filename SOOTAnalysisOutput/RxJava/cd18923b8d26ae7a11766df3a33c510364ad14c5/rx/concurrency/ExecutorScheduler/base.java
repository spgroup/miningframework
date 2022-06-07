package rx.concurrency;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import rx.Scheduler;
import rx.Subscription;
import rx.util.functions.Func0;

public class ExecutorScheduler extends AbstractScheduler {

    private final Executor executor;

    private final static ScheduledExecutorService SYSTEM_SCHEDULED_EXECUTOR;

    static {
        int count = Runtime.getRuntime().availableProcessors();
        if (count > 8) {
            count = count / 2;
        }
        if (count > 8) {
            count = 8;
        }
        SYSTEM_SCHEDULED_EXECUTOR = Executors.newScheduledThreadPool(count, new ThreadFactory() {

            final AtomicInteger counter = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "RxScheduledExecutorPool-" + counter.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        });
    }

    public ExecutorScheduler(Executor executor) {
        this.executor = executor;
    }

    public ExecutorScheduler(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public Subscription schedule(Func0<Subscription> action, long dueTime, TimeUnit unit) {
        final DiscardableAction discardableAction = new DiscardableAction(action);
        if (executor instanceof ScheduledExecutorService) {
            ((ScheduledExecutorService) executor).schedule(new Runnable() {

                @Override
                public void run() {
                    discardableAction.call();
                }
            }, dueTime, unit);
        } else {
            if (dueTime == 0) {
                return (schedule(action));
            } else {
                SYSTEM_SCHEDULED_EXECUTOR.schedule(new Runnable() {

                    @Override
                    public void run() {
                        executor.execute(new Runnable() {

                            @Override
                            public void run() {
                                discardableAction.call();
                            }
                        });
                    }
                }, dueTime, unit);
            }
        }
        return discardableAction;
    }

    @Override
    public Subscription schedule(Func0<Subscription> action) {
        final DiscardableAction discardableAction = new DiscardableAction(action);
        executor.execute(new Runnable() {

            @Override
            public void run() {
                discardableAction.call();
            }
        });
        return discardableAction;
    }
}
