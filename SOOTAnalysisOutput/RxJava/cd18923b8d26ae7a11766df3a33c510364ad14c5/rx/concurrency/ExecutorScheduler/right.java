package rx.concurrency;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import rx.Scheduler;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Func2;

public class ExecutorScheduler extends Scheduler {

    private final Executor executor;

    public ExecutorScheduler(Executor executor) {
        this.executor = executor;
    }

    public ExecutorScheduler(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public <T> Subscription schedule(final T state, final Func2<Scheduler, T, Subscription> action, long delayTime, TimeUnit unit) {
        final DiscardableAction<T> discardableAction = new DiscardableAction<T>(state, action);
        final Scheduler _scheduler = this;
        final CompositeSubscription subscription = new CompositeSubscription(discardableAction);
        if (executor instanceof ScheduledExecutorService) {
            ScheduledFuture<?> f = ((ScheduledExecutorService) executor).schedule(new Runnable() {

                @Override
                public void run() {
                    Subscription s = discardableAction.call(_scheduler);
                    subscription.add(s);
                }
            }, delayTime, unit);
            subscription.add(Subscriptions.create(f));
        } else {
            if (delayTime == 0) {
                Subscription s = schedule(state, action);
                subscription.add(s);
            } else {
                ScheduledFuture<?> f = GenericScheduledExecutorService.getInstance().schedule(new Runnable() {

                    @Override
                    public void run() {
                        Subscription s = _scheduler.schedule(state, action);
                        subscription.add(s);
                    }
                }, delayTime, unit);
                subscription.add(Subscriptions.create(f));
            }
        }
        return subscription;
    }

    @Override
    public <T> Subscription schedule(T state, Func2<Scheduler, T, Subscription> action) {
        final DiscardableAction<T> discardableAction = new DiscardableAction<T>(state, action);
        final Scheduler _scheduler = this;
        final CompositeSubscription subscription = new CompositeSubscription(discardableAction);
        Runnable r = new Runnable() {

            @Override
            public void run() {
                Subscription s = discardableAction.call(_scheduler);
                subscription.add(s);
            }
        };
        if (executor instanceof ExecutorService) {
            Future<?> f = ((ExecutorService) executor).submit(r);
            subscription.add(Subscriptions.create(f));
        } else {
            executor.execute(r);
        }
        return subscription;
    }
}
