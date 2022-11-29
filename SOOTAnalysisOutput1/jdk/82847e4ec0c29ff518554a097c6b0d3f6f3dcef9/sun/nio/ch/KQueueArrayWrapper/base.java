package sun.nio.ch;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import sun.security.action.GetPropertyAction;

class KQueueArrayWrapper {

    static short EVFILT_READ;

    static short EVFILT_WRITE;

    static short SIZEOF_KEVENT;

    static short FD_OFFSET;

    static short FILTER_OFFSET;

    static final int NUM_KEVENTS = 128;

    static boolean is64bit = false;

    private AllocatedNativeObject keventArray = null;

    private long keventArrayAddress;

    private int kq = -1;

    private int outgoingInterruptFD;

    private int incomingInterruptFD;

    static {
        IOUtil.load();
        initStructSizes();
        String datamodel = GetPropertyAction.privilegedGetProperty("sun.arch.data.model");
        is64bit = "64".equals(datamodel);
    }

    KQueueArrayWrapper() {
        int allocationSize = SIZEOF_KEVENT * NUM_KEVENTS;
        keventArray = new AllocatedNativeObject(allocationSize, true);
        keventArrayAddress = keventArray.address();
        kq = init();
    }

    private static class Update {

        SelChImpl channel;

        int events;

        Update(SelChImpl channel, int events) {
            this.channel = channel;
            this.events = events;
        }
    }

    private LinkedList<Update> updateList = new LinkedList<Update>();

    void initInterrupt(int fd0, int fd1) {
        outgoingInterruptFD = fd1;
        incomingInterruptFD = fd0;
        register0(kq, fd0, 1, 0);
    }

    int getReventOps(int index) {
        int result = 0;
        int offset = SIZEOF_KEVENT * index + FILTER_OFFSET;
        short filter = keventArray.getShort(offset);
        if (filter == EVFILT_READ) {
            result |= Net.POLLIN;
        } else if (filter == EVFILT_WRITE) {
            result |= Net.POLLOUT;
        }
        return result;
    }

    int getDescriptor(int index) {
        int offset = SIZEOF_KEVENT * index + FD_OFFSET;
        if (is64bit) {
            long fd = keventArray.getLong(offset);
            assert fd <= Integer.MAX_VALUE;
            return (int) fd;
        } else {
            return keventArray.getInt(offset);
        }
    }

    void setInterest(SelChImpl channel, int events) {
        synchronized (updateList) {
            updateList.add(new Update(channel, events));
        }
    }

    void release(SelChImpl channel) {
        synchronized (updateList) {
            for (Iterator<Update> it = updateList.iterator(); it.hasNext(); ) {
                if (it.next().channel == channel) {
                    it.remove();
                }
            }
            register0(kq, channel.getFDVal(), 0, 0);
        }
    }

    void updateRegistrations() {
        synchronized (updateList) {
            Update u = null;
            while ((u = updateList.poll()) != null) {
                SelChImpl ch = u.channel;
                if (!ch.isOpen())
                    continue;
                register0(kq, ch.getFDVal(), u.events & Net.POLLIN, u.events & Net.POLLOUT);
            }
        }
    }

    void close() throws IOException {
        if (keventArray != null) {
            keventArray.free();
            keventArray = null;
        }
        if (kq >= 0) {
            FileDispatcherImpl.closeIntFD(kq);
            kq = -1;
        }
    }

    int poll(long timeout) {
        updateRegistrations();
        int updated = kevent0(kq, keventArrayAddress, NUM_KEVENTS, timeout);
        return updated;
    }

    void interrupt() {
        interrupt(outgoingInterruptFD);
    }

    private native int init();

    private static native void initStructSizes();

    private native void register0(int kq, int fd, int read, int write);

    private native int kevent0(int kq, long keventAddress, int keventCount, long timeout);

    private static native void interrupt(int fd);
}
