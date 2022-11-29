package sun.nio.ch;

import java.io.IOException;
import java.security.AccessController;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import sun.security.action.GetIntegerAction;

class EPollArrayWrapper {

    private static final int EPOLLIN = 0x001;

    private static final int EPOLL_CTL_ADD = 1;

    private static final int EPOLL_CTL_DEL = 2;

    private static final int EPOLL_CTL_MOD = 3;

    private static final int SIZE_EPOLLEVENT = sizeofEPollEvent();

    private static final int EVENT_OFFSET = 0;

    private static final int DATA_OFFSET = offsetofData();

    private static final int FD_OFFSET = DATA_OFFSET;

    private static final int OPEN_MAX = IOUtil.fdLimit();

    private static final int NUM_EPOLLEVENTS = Math.min(OPEN_MAX, 8192);

    private static final byte KILLED = (byte) -1;

    private static final int INITIAL_PENDING_UPDATE_SIZE = 64;

    private static final int MAX_UPDATE_ARRAY_SIZE = AccessController.doPrivileged(new GetIntegerAction("sun.nio.ch.maxUpdateArraySize", Math.min(OPEN_MAX, 64 * 1024)));

    private final int epfd;

    private final AllocatedNativeObject pollArray;

    private final long pollArrayAddress;

    private int outgoingInterruptFD;

    private int incomingInterruptFD;

    private int interruptedIndex;

    int updated;

    private final Object updateLock = new Object();

    private int updateCount;

    private int[] updateDescriptors = new int[INITIAL_PENDING_UPDATE_SIZE];

    private final byte[] eventsLow = new byte[MAX_UPDATE_ARRAY_SIZE];

    private final Map<Integer, Byte> eventsHigh = new HashMap<>();

    private final BitSet registered = new BitSet();

    EPollArrayWrapper() throws IOException {
        epfd = epollCreate();
        int allocationSize = NUM_EPOLLEVENTS * SIZE_EPOLLEVENT;
        pollArray = new AllocatedNativeObject(allocationSize, true);
        pollArrayAddress = pollArray.address();
    }

    void initInterrupt(int fd0, int fd1) {
        outgoingInterruptFD = fd1;
        incomingInterruptFD = fd0;
        epollCtl(epfd, EPOLL_CTL_ADD, fd0, EPOLLIN);
    }

    void putEventOps(int i, int event) {
        int offset = SIZE_EPOLLEVENT * i + EVENT_OFFSET;
        pollArray.putInt(offset, event);
    }

    void putDescriptor(int i, int fd) {
        int offset = SIZE_EPOLLEVENT * i + FD_OFFSET;
        pollArray.putInt(offset, fd);
    }

    int getEventOps(int i) {
        int offset = SIZE_EPOLLEVENT * i + EVENT_OFFSET;
        return pollArray.getInt(offset);
    }

    int getDescriptor(int i) {
        int offset = SIZE_EPOLLEVENT * i + FD_OFFSET;
        return pollArray.getInt(offset);
    }

    private boolean isEventsHighKilled(Integer key) {
        assert key >= MAX_UPDATE_ARRAY_SIZE;
        Byte value = eventsHigh.get(key);
        return (value != null && value == KILLED);
    }

    private void setUpdateEvents(int fd, byte events, boolean force) {
        if (fd < MAX_UPDATE_ARRAY_SIZE) {
            if ((eventsLow[fd] != KILLED) || force) {
                eventsLow[fd] = events;
            }
        } else {
            Integer key = Integer.valueOf(fd);
            if (!isEventsHighKilled(key) || force) {
                eventsHigh.put(key, Byte.valueOf(events));
            }
        }
    }

    private byte getUpdateEvents(int fd) {
        if (fd < MAX_UPDATE_ARRAY_SIZE) {
            return eventsLow[fd];
        } else {
            Byte result = eventsHigh.get(Integer.valueOf(fd));
            return result.byteValue();
        }
    }

    void setInterest(int fd, int mask) {
        synchronized (updateLock) {
            int oldCapacity = updateDescriptors.length;
            if (updateCount == oldCapacity) {
                int newCapacity = oldCapacity + INITIAL_PENDING_UPDATE_SIZE;
                int[] newDescriptors = new int[newCapacity];
                System.arraycopy(updateDescriptors, 0, newDescriptors, 0, oldCapacity);
                updateDescriptors = newDescriptors;
            }
            updateDescriptors[updateCount++] = fd;
            byte b = (byte) mask;
            assert (b == mask) && (b != KILLED);
            setUpdateEvents(fd, b, false);
        }
    }

    void add(int fd) {
        synchronized (updateLock) {
            assert !registered.get(fd);
            setUpdateEvents(fd, (byte) 0, true);
        }
    }

    void remove(int fd) {
        synchronized (updateLock) {
            setUpdateEvents(fd, KILLED, false);
            if (registered.get(fd)) {
                epollCtl(epfd, EPOLL_CTL_DEL, fd, 0);
                registered.clear(fd);
            }
        }
    }

    void closeEPollFD() throws IOException {
        FileDispatcherImpl.closeIntFD(epfd);
        pollArray.free();
    }

    int poll(long timeout) throws IOException {
        updateRegistrations();
        updated = epollWait(pollArrayAddress, NUM_EPOLLEVENTS, timeout, epfd);
        for (int i = 0; i < updated; i++) {
            if (getDescriptor(i) == incomingInterruptFD) {
                interruptedIndex = i;
                interrupted = true;
                break;
            }
        }
        return updated;
    }

    private void updateRegistrations() {
        synchronized (updateLock) {
            int j = 0;
            while (j < updateCount) {
                int fd = updateDescriptors[j];
                short events = getUpdateEvents(fd);
                boolean isRegistered = registered.get(fd);
                int opcode = 0;
                if (events != KILLED) {
                    if (isRegistered) {
                        opcode = (events != 0) ? EPOLL_CTL_MOD : EPOLL_CTL_DEL;
                    } else {
                        opcode = (events != 0) ? EPOLL_CTL_ADD : 0;
                    }
                    if (opcode != 0) {
                        epollCtl(epfd, opcode, fd, events);
                        if (opcode == EPOLL_CTL_ADD) {
                            registered.set(fd);
                        } else if (opcode == EPOLL_CTL_DEL) {
                            registered.clear(fd);
                        }
                    }
                }
                j++;
            }
            updateCount = 0;
        }
    }

    private boolean interrupted = false;

    public void interrupt() {
        interrupt(outgoingInterruptFD);
    }

    public int interruptedIndex() {
        return interruptedIndex;
    }

    boolean interrupted() {
        return interrupted;
    }

    void clearInterrupted() {
        interrupted = false;
    }

    static {
        IOUtil.load();
        init();
    }

    private native int epollCreate();

    private native void epollCtl(int epfd, int opcode, int fd, int events);

    private native int epollWait(long pollAddress, int numfds, long timeout, int epfd) throws IOException;

    private static native int sizeofEPollEvent();

    private static native int offsetofData();

    private static native void interrupt(int fd);

    private static native void init();
}
