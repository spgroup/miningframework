package sun.nio.ch;

import java.nio.channels.spi.AsynchronousChannelProvider;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import static sun.nio.ch.EPoll.*;

final class EPollPort extends Port {

    private static final int MAX_EPOLL_EVENTS = 512;

    private static final int ENOENT = 2;

    private final int epfd;

    private boolean closed;

    private final int[] sp;

    private final AtomicInteger wakeupCount = new AtomicInteger();

    private final long address;

    static class Event {

        final PollableChannel channel;

        final int events;

        Event(PollableChannel channel, int events) {
            this.channel = channel;
            this.events = events;
        }

        PollableChannel channel() {
            return channel;
        }

        int events() {
            return events;
        }
    }

    private final ArrayBlockingQueue<Event> queue;

    private final Event NEED_TO_POLL = new Event(null, 0);

    private final Event EXECUTE_TASK_OR_SHUTDOWN = new Event(null, 0);

    EPollPort(AsynchronousChannelProvider provider, ThreadPool pool) throws IOException {
        super(provider, pool);
        this.epfd = epollCreate();
        int[] sv = new int[2];
        try {
            socketpair(sv);
            epollCtl(epfd, EPOLL_CTL_ADD, sv[0], Net.POLLIN);
        } catch (IOException x) {
            close0(epfd);
            throw x;
        }
        this.sp = sv;
        this.address = allocatePollArray(MAX_EPOLL_EVENTS);
        this.queue = new ArrayBlockingQueue<>(MAX_EPOLL_EVENTS);
        this.queue.offer(NEED_TO_POLL);
    }

    EPollPort start() {
        startThreads(new EventHandlerTask());
        return this;
    }

    private void implClose() {
        synchronized (this) {
            if (closed)
                return;
            closed = true;
        }
        freePollArray(address);
        close0(sp[0]);
        close0(sp[1]);
        close0(epfd);
    }

    private void wakeup() {
        if (wakeupCount.incrementAndGet() == 1) {
            try {
                interrupt(sp[1]);
            } catch (IOException x) {
                throw new AssertionError(x);
            }
        }
    }

    @Override
    void executeOnHandlerTask(Runnable task) {
        synchronized (this) {
            if (closed)
                throw new RejectedExecutionException();
            offerTask(task);
            wakeup();
        }
    }

    @Override
    void shutdownHandlerTasks() {
        int nThreads = threadCount();
        if (nThreads == 0) {
            implClose();
        } else {
            while (nThreads-- > 0) {
                wakeup();
            }
        }
    }

    @Override
    void startPoll(int fd, int events) {
        int err = epollCtl(epfd, EPOLL_CTL_MOD, fd, (events | EPOLLONESHOT));
        if (err == ENOENT)
            err = epollCtl(epfd, EPOLL_CTL_ADD, fd, (events | EPOLLONESHOT));
        if (err != 0)
            throw new AssertionError();
    }

    private class EventHandlerTask implements Runnable {

        private Event poll() throws IOException {
            try {
                for (; ; ) {
                    int n = epollWait(epfd, address, MAX_EPOLL_EVENTS);
                    fdToChannelLock.readLock().lock();
                    try {
                        while (n-- > 0) {
                            long eventAddress = getEvent(address, n);
                            int fd = getDescriptor(eventAddress);
                            if (fd == sp[0]) {
                                if (wakeupCount.decrementAndGet() == 0) {
                                    drain1(sp[0]);
                                }
                                if (n > 0) {
                                    queue.offer(EXECUTE_TASK_OR_SHUTDOWN);
                                    continue;
                                }
                                return EXECUTE_TASK_OR_SHUTDOWN;
                            }
                            PollableChannel channel = fdToChannel.get(fd);
                            if (channel != null) {
                                int events = getEvents(eventAddress);
                                Event ev = new Event(channel, events);
                                if (n > 0) {
                                    queue.offer(ev);
                                } else {
                                    return ev;
                                }
                            }
                        }
                    } finally {
                        fdToChannelLock.readLock().unlock();
                    }
                }
            } finally {
                queue.offer(NEED_TO_POLL);
            }
        }

        public void run() {
            Invoker.GroupAndInvokeCount myGroupAndInvokeCount = Invoker.getGroupAndInvokeCount();
            final boolean isPooledThread = (myGroupAndInvokeCount != null);
            boolean replaceMe = false;
            Event ev;
            try {
                for (; ; ) {
                    if (isPooledThread)
                        myGroupAndInvokeCount.resetInvokeCount();
                    try {
                        replaceMe = false;
                        ev = queue.take();
                        if (ev == NEED_TO_POLL) {
                            try {
                                ev = poll();
                            } catch (IOException x) {
                                x.printStackTrace();
                                return;
                            }
                        }
                    } catch (InterruptedException x) {
                        continue;
                    }
                    if (ev == EXECUTE_TASK_OR_SHUTDOWN) {
                        Runnable task = pollTask();
                        if (task == null) {
                            return;
                        }
                        replaceMe = true;
                        task.run();
                        continue;
                    }
                    try {
                        ev.channel().onEvent(ev.events(), isPooledThread);
                    } catch (Error x) {
                        replaceMe = true;
                        throw x;
                    } catch (RuntimeException x) {
                        replaceMe = true;
                        throw x;
                    }
                }
            } finally {
                int remaining = threadExit(this, replaceMe);
                if (remaining == 0 && isShutdown()) {
                    implClose();
                }
            }
        }
    }

    private static native void socketpair(int[] sv) throws IOException;

    private static native void interrupt(int fd) throws IOException;

    private static native void drain1(int fd) throws IOException;

    private static native void close0(int fd);

    static {
        IOUtil.load();
    }
}
