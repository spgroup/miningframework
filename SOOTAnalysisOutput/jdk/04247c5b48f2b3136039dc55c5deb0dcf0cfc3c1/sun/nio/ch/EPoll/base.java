package sun.nio.ch;

import java.io.IOException;
import sun.misc.Unsafe;

class EPoll {

    private EPoll() {
    }

    private static final Unsafe unsafe = Unsafe.getUnsafe();

    private static final int SIZEOF_EPOLLEVENT = eventSize();

    private static final int OFFSETOF_EVENTS = eventsOffset();

    private static final int OFFSETOF_FD = dataOffset();

    static final int EPOLL_CTL_ADD = 1;

    static final int EPOLL_CTL_DEL = 2;

    static final int EPOLL_CTL_MOD = 3;

    static final int EPOLLONESHOT = (1 << 30);

    static long allocatePollArray(int count) {
        return unsafe.allocateMemory(count * SIZEOF_EPOLLEVENT);
    }

    static void freePollArray(long address) {
        unsafe.freeMemory(address);
    }

    static long getEvent(long address, int i) {
        return address + (SIZEOF_EPOLLEVENT * i);
    }

    static int getDescriptor(long eventAddress) {
        return unsafe.getInt(eventAddress + OFFSETOF_FD);
    }

    static int getEvents(long eventAddress) {
        return unsafe.getInt(eventAddress + OFFSETOF_EVENTS);
    }

    private static native void init();

    private static native int eventSize();

    private static native int eventsOffset();

    private static native int dataOffset();

    static native int epollCreate() throws IOException;

    static native int epollCtl(int epfd, int opcode, int fd, int events);

    static native int epollWait(int epfd, long pollAddress, int numfds) throws IOException;

    static {
        Util.load();
        init();
    }
}
