package sun.nio.ch;

import java.nio.channels.spi.AsynchronousChannelProvider;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import static sun.nio.ch.KQueue.EVFILT_READ;
import static sun.nio.ch.KQueue.EVFILT_WRITE;
import static sun.nio.ch.KQueue.EV_ADD;
import static sun.nio.ch.KQueue.EV_ONESHOT;

final class KQueuePort extends Port {

    private static final int MAX_KEVENTS_TO_POLL = 512;

    private final int kqfd;

    private final long address;

    private boolean closed;

    private final int[] sp;

    private final AtomicInteger wakeupCount = new AtomicInteger();

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

    KQueuePort(AsynchronousChannelProvider provider, ThreadPool pool) throws IOException {
        super(provider, pool);
        this.kqfd = KQueue.create();
        this.address = KQueue.allocatePollArray(MAX_KEVENTS_TO_POLL);
        try {
            long fds = IOUtil.makePipe(true);
            this.sp = new int[] { (int) (fds >>> 32), (int) fds };
        } catch (IOException ioe) {
            KQueue.freePollArray(address);
            FileDispatcherImpl.closeIntFD(kqfd);
            throw ioe;
        }
        KQueue.register(kqfd, sp[0], EVFILT_READ, EV_ADD);
        this.queue = new ArrayBlockingQueue<>(MAX_KEVENTS_TO_POLL);
        this.queue.offer(NEED_TO_POLL);
    }

    KQueuePort start() {
        startThreads(new EventHandlerTask());
        return this;
    }

    private void implClose() {
        synchronized (this) {
            if (closed)
                return;
            closed = true;
        }
        try {
            FileDispatcherImpl.closeIntFD(kqfd);
        } catch (IOException ioe) {
        }
        try {
            FileDispatcherImpl.closeIntFD(sp[0]);
        } catch (IOException ioe) {
        }
        try {
            FileDispatcherImpl.closeIntFD(sp[1]);
        } catch (IOException ioe) {
        }
        KQueue.freePollArray(address);
    }

    private void wakeup() {
        if (wakeupCount.incrementAndGet() == 1) {
            try {
                IOUtil.write1(sp[1], (byte) 0);
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
        int err = 0;
        int flags = (EV_ADD | EV_ONESHOT);
        if ((events & Net.POLLIN) > 0)
            err = KQueue.register(kqfd, fd, EVFILT_READ, flags);
        if (err == 0 && (events & Net.POLLOUT) > 0)
            err = KQueue.register(kqfd, fd, EVFILT_WRITE, flags);
        if (err != 0)
            throw new InternalError("kevent failed: " + err);
    }

    private class EventHandlerTask implements Runnable {

        private Event poll() throws IOException {
            try {
                for (; ; ) {
                    int n;
                    do {
                        n = KQueue.poll(kqfd, address, MAX_KEVENTS_TO_POLL, -1L);
                    } while (n == IOStatus.INTERRUPTED);
                    fdToChannelLock.readLock().lock();
                    try {
                        while (n-- > 0) {
                            long keventAddress = KQueue.getEvent(address, n);
                            int fd = KQueue.getDescriptor(keventAddress);
                            if (fd == sp[0]) {
                                if (wakeupCount.decrementAndGet() == 0) {
<<<<<<< MINE
                                    IOUtil.drain(sp[0]);
=======
                                    int nread;
                                    do {
                                        nread = IOUtil.drain1(sp[0]);
                                    } while (nread == IOStatus.INTERRUPTED);
>>>>>>> YOURS
                                }
                                if (n > 0) {
                                    queue.offer(EXECUTE_TASK_OR_SHUTDOWN);
                                    continue;
                                }
                                return EXECUTE_TASK_OR_SHUTDOWN;
                            }
                            PollableChannel channel = fdToChannel.get(fd);
                            if (channel != null) {
                                int filter = KQueue.getFilter(keventAddress);
                                int events = 0;
                                if (filter == EVFILT_READ)
                                    events = Net.POLLIN;
                                else if (filter == EVFILT_WRITE)
                                    events = Net.POLLOUT;
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
}
