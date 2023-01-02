import java.lang.reflect.*;
import java.io.*;
import java.net.*;

public class Poller {

    public final static short POLLERR = 0x08;

    public final static short POLLHUP = 0x10;

    public final static short POLLNVAL = 0x20;

    public final static short POLLIN = 1;

    public final static short POLLPRI = 2;

    public final static short POLLOUT = 4;

    public final static short POLLRDNORM = 0x40;

    public final static short POLLWRNORM = POLLOUT;

    public final static short POLLRDBAND = 0x80;

    public final static short POLLWRBAND = 0x100;

    public final static short POLLNORM = POLLRDNORM;

    private final static Object globalSync = new Object();

    private int handle;

    public Poller() throws Exception {
        synchronized (globalSync) {
            this.handle = nativeCreatePoller(-1);
        }
    }

    public Poller(int maxFd) throws Exception {
        synchronized (globalSync) {
            this.handle = nativeCreatePoller(maxFd);
        }
    }

    protected void finalize() throws Throwable {
        synchronized (globalSync) {
            nativeDestroyPoller(handle);
            super.finalize();
        }
    }

    public void reset(int maxFd) throws Exception {
        synchronized (globalSync) {
            nativeDestroyPoller(handle);
            this.handle = nativeCreatePoller(maxFd);
        }
    }

    public void reset() throws Exception {
        synchronized (globalSync) {
            nativeDestroyPoller(handle);
            this.handle = nativeCreatePoller(-1);
        }
    }

    public synchronized int add(Object fdObj, short event) throws Exception {
        return nativeAddFd(handle, findfd(fdObj), event);
    }

    public synchronized boolean remove(Object fdObj) throws Exception {
        return (nativeRemoveFd(handle, findfd(fdObj)) == 1);
    }

    public synchronized boolean isMember(Object fdObj) throws Exception {
        return (nativeIsMember(handle, findfd(fdObj)) == 1);
    }

    public synchronized int waitMultiple(int maxRet, int[] fds, short[] revents, long timeout) throws Exception {
        if ((revents == null) || (fds == null)) {
            if (maxRet > 0) {
                throw new NullPointerException("fds or revents is null");
            }
        } else if ((maxRet < 0) || (maxRet > revents.length) || (maxRet > fds.length)) {
            throw new IllegalArgumentException("maxRet out of range");
        }
        int ret = nativeWait(handle, maxRet, fds, revents, timeout);
        if (ret < 0) {
            throw new InterruptedIOException();
        }
        return ret;
    }

    public int waitMultiple(int maxRet, int[] fds, short[] revents) throws Exception {
        return waitMultiple(maxRet, fds, revents, -1L);
    }

    public synchronized int waitMultiple(int[] fds, short[] revents, long timeout) throws Exception {
        if ((revents == null) && (fds == null)) {
            return nativeWait(handle, 0, null, null, timeout);
        } else if ((revents == null) || (fds == null)) {
            throw new NullPointerException("revents or fds is null");
        } else if (fds.length == revents.length) {
            return nativeWait(handle, fds.length, fds, revents, timeout);
        }
        throw new IllegalArgumentException("fds.length != revents.length");
    }

    public int waitMultiple(int[] fds, short[] revents) throws Exception {
        if ((revents == null) || (fds == null)) {
            throw new NullPointerException("fds or revents is null");
        } else if (fds.length == revents.length) {
            return waitMultiple(revents.length, fds, revents, -1L);
        }
        throw new IllegalArgumentException("fds.length != revents.length");
    }

    private int findfd(Object fdObj) throws Exception {
        Class cl;
        Field f;
        Object val, implVal;
        if ((fdObj instanceof java.net.Socket) || (fdObj instanceof java.net.ServerSocket)) {
            cl = fdObj.getClass();
            f = cl.getDeclaredField("impl");
            f.setAccessible(true);
            val = f.get(fdObj);
            cl = f.getType();
            f = cl.getDeclaredField("fd");
            f.setAccessible(true);
            implVal = f.get(val);
            cl = f.getType();
            f = cl.getDeclaredField("fd");
            f.setAccessible(true);
            return ((Integer) f.get(implVal)).intValue();
        } else if (fdObj instanceof java.io.FileDescriptor) {
            cl = fdObj.getClass();
            f = cl.getDeclaredField("fd");
            f.setAccessible(true);
            return ((Integer) f.get(fdObj)).intValue();
        } else {
            throw new IllegalArgumentException("Illegal Object type.");
        }
    }

    private static native int nativeInit();

    private native int nativeCreatePoller(int maxFd) throws Exception;

    private native void nativeDestroyPoller(int handle) throws Exception;

    private native int nativeAddFd(int handle, int fd, short events) throws Exception;

    private native int nativeRemoveFd(int handle, int fd) throws Exception;

    private native int nativeRemoveIndex(int handle, int index) throws Exception;

    private native int nativeIsMember(int handle, int fd) throws Exception;

    private native int nativeWait(int handle, int maxRet, int[] fds, short[] events, long timeout) throws Exception;

    public static native int getNumCPUs();

    static {
        System.loadLibrary("poller");
        nativeInit();
    }
}
