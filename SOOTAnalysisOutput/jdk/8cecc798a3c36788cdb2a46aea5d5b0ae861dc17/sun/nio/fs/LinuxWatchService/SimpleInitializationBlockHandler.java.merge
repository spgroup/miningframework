package sun.nio.fs;

import java.nio.file.*;
import java.util.*;
import java.io.IOException;
import jdk.internal.misc.Unsafe;
import static sun.nio.fs.UnixNativeDispatcher.*;
import static sun.nio.fs.UnixConstants.*;

class LinuxWatchService extends AbstractWatchService {

    private static final Unsafe unsafe = Unsafe.getUnsafe();

    private final Poller poller;

    LinuxWatchService(UnixFileSystem fs) throws IOException {
        int ifd = -1;
        try {
            ifd = inotifyInit();
        } catch (UnixException x) {
            String msg = (x.errno() == EMFILE) ? "User limit of inotify instances reached or too many open files" : x.errorString();
            throw new IOException(msg);
        }
        int[] sp = new int[2];
        try {
            configureBlocking(ifd, false);
            socketpair(sp);
            configureBlocking(sp[0], false);
        } catch (UnixException x) {
            UnixNativeDispatcher.close(ifd);
            throw new IOException(x.errorString());
        }
        this.poller = new Poller(fs, this, ifd, sp);
        this.poller.start();
    }

    @Override
    WatchKey register(Path dir, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        return poller.register(dir, events, modifiers);
    }

    @Override
    void implClose() throws IOException {
        poller.close();
    }

    private static class LinuxWatchKey extends AbstractWatchKey {

        private final int ifd;

        private volatile int wd;

        LinuxWatchKey(UnixPath dir, LinuxWatchService watcher, int ifd, int wd) {
            super(dir, watcher);
            this.ifd = ifd;
            this.wd = wd;
        }

        int descriptor() {
            return wd;
        }

        void invalidate(boolean remove) {
            if (remove) {
                try {
                    inotifyRmWatch(ifd, wd);
                } catch (UnixException x) {
                }
            }
            wd = -1;
        }

        @Override
        public boolean isValid() {
            return (wd != -1);
        }

        @Override
        public void cancel() {
            if (isValid()) {
                ((LinuxWatchService) watcher()).poller.cancel(this);
            }
        }
    }

    private static class Poller extends AbstractPoller {

        private static final int SIZEOF_INOTIFY_EVENT = eventSize();

        private static final int[] offsets = eventOffsets();

        private static final int OFFSETOF_WD = offsets[0];

        private static final int OFFSETOF_MASK = offsets[1];

        private static final int OFFSETOF_LEN = offsets[3];

        private static final int OFFSETOF_NAME = offsets[4];

        private static final int IN_MODIFY = 0x00000002;

        private static final int IN_ATTRIB = 0x00000004;

        private static final int IN_MOVED_FROM = 0x00000040;

        private static final int IN_MOVED_TO = 0x00000080;

        private static final int IN_CREATE = 0x00000100;

        private static final int IN_DELETE = 0x00000200;

        private static final int IN_UNMOUNT = 0x00002000;

        private static final int IN_Q_OVERFLOW = 0x00004000;

        private static final int IN_IGNORED = 0x00008000;

        private static final int BUFFER_SIZE = 8192;

        private final UnixFileSystem fs;

        private final LinuxWatchService watcher;

        private final int ifd;

        private final int[] socketpair;

        private final Map<Integer, LinuxWatchKey> wdToKey;

        private final long address;

        Poller(UnixFileSystem fs, LinuxWatchService watcher, int ifd, int[] sp) {
            this.fs = fs;
            this.watcher = watcher;
            this.ifd = ifd;
            this.socketpair = sp;
            this.wdToKey = new HashMap<>();
            this.address = unsafe.allocateMemory(BUFFER_SIZE);
        }

        @Override
        void wakeup() throws IOException {
            try {
                write(socketpair[1], address, 1);
            } catch (UnixException x) {
                throw new IOException(x.errorString());
            }
        }

        @Override
        Object implRegister(Path obj, Set<? extends WatchEvent.Kind<?>> events, WatchEvent.Modifier... modifiers) {
            UnixPath dir = (UnixPath) obj;
            int mask = 0;
            for (WatchEvent.Kind<?> event : events) {
                if (event == StandardWatchEventKinds.ENTRY_CREATE) {
                    mask |= IN_CREATE | IN_MOVED_TO;
                    continue;
                }
                if (event == StandardWatchEventKinds.ENTRY_DELETE) {
                    mask |= IN_DELETE | IN_MOVED_FROM;
                    continue;
                }
                if (event == StandardWatchEventKinds.ENTRY_MODIFY) {
                    mask |= IN_MODIFY | IN_ATTRIB;
                    continue;
                }
            }
            if (modifiers.length > 0) {
                for (WatchEvent.Modifier modifier : modifiers) {
                    if (modifier == null)
                        return new NullPointerException();
                    if (!ExtendedOptions.SENSITIVITY_HIGH.matches(modifier) && !ExtendedOptions.SENSITIVITY_MEDIUM.matches(modifier) && !ExtendedOptions.SENSITIVITY_LOW.matches(modifier)) {
                        return new UnsupportedOperationException("Modifier not supported");
                    }
                }
            }
            UnixFileAttributes attrs = null;
            try {
                attrs = UnixFileAttributes.get(dir, true);
            } catch (UnixException x) {
                return x.asIOException(dir);
            }
            if (!attrs.isDirectory()) {
                return new NotDirectoryException(dir.getPathForExceptionMessage());
            }
            int wd = -1;
            try {
                NativeBuffer buffer = NativeBuffers.asNativeBuffer(dir.getByteArrayForSysCalls());
                try {
                    wd = inotifyAddWatch(ifd, buffer.address(), mask);
                } finally {
                    buffer.release();
                }
            } catch (UnixException x) {
                if (x.errno() == ENOSPC) {
                    return new IOException("User limit of inotify watches reached");
                }
                return x.asIOException(dir);
            }
            LinuxWatchKey key = wdToKey.get(wd);
            if (key == null) {
                key = new LinuxWatchKey(dir, watcher, ifd, wd);
                wdToKey.put(wd, key);
            }
            return key;
        }

        @Override
        void implCancelKey(WatchKey obj) {
            LinuxWatchKey key = (LinuxWatchKey) obj;
            if (key.isValid()) {
                wdToKey.remove(key.descriptor());
                key.invalidate(true);
            }
        }

        @Override
        void implCloseAll() {
            for (Map.Entry<Integer, LinuxWatchKey> entry : wdToKey.entrySet()) {
                entry.getValue().invalidate(true);
            }
            wdToKey.clear();
            unsafe.freeMemory(address);
            UnixNativeDispatcher.close(socketpair[0]);
            UnixNativeDispatcher.close(socketpair[1]);
            UnixNativeDispatcher.close(ifd);
        }

        @Override
        public void run() {
            try {
                for (; ; ) {
                    int nReady, bytesRead;
                    nReady = poll(ifd, socketpair[0]);
                    try {
                        bytesRead = read(ifd, address, BUFFER_SIZE);
                    } catch (UnixException x) {
                        if (x.errno() != EAGAIN && x.errno() != EWOULDBLOCK)
                            throw x;
                        bytesRead = 0;
                    }
                    int offset = 0;
                    while (offset < bytesRead) {
                        long event = address + offset;
                        int wd = unsafe.getInt(event + OFFSETOF_WD);
                        int mask = unsafe.getInt(event + OFFSETOF_MASK);
                        int len = unsafe.getInt(event + OFFSETOF_LEN);
                        UnixPath name = null;
                        if (len > 0) {
                            int actual = len;
                            while (actual > 0) {
                                long last = event + OFFSETOF_NAME + actual - 1;
                                if (unsafe.getByte(last) != 0)
                                    break;
                                actual--;
                            }
                            if (actual > 0) {
                                byte[] buf = new byte[actual];
                                unsafe.copyMemory(null, event + OFFSETOF_NAME, buf, Unsafe.ARRAY_BYTE_BASE_OFFSET, actual);
                                name = new UnixPath(fs, buf);
                            }
                        }
                        processEvent(wd, mask, name);
                        offset += (SIZEOF_INOTIFY_EVENT + len);
                    }
                    if ((nReady > 1) || (nReady == 1 && bytesRead == 0)) {
                        try {
                            read(socketpair[0], address, BUFFER_SIZE);
                            boolean shutdown = processRequests();
                            if (shutdown)
                                break;
                        } catch (UnixException x) {
                            if (x.errno() != EAGAIN && x.errno() != EWOULDBLOCK)
                                throw x;
                        }
                    }
                }
            } catch (UnixException x) {
                x.printStackTrace();
            }
        }

        private WatchEvent.Kind<?> maskToEventKind(int mask) {
            if ((mask & IN_MODIFY) > 0)
                return StandardWatchEventKinds.ENTRY_MODIFY;
            if ((mask & IN_ATTRIB) > 0)
                return StandardWatchEventKinds.ENTRY_MODIFY;
            if ((mask & IN_CREATE) > 0)
                return StandardWatchEventKinds.ENTRY_CREATE;
            if ((mask & IN_MOVED_TO) > 0)
                return StandardWatchEventKinds.ENTRY_CREATE;
            if ((mask & IN_DELETE) > 0)
                return StandardWatchEventKinds.ENTRY_DELETE;
            if ((mask & IN_MOVED_FROM) > 0)
                return StandardWatchEventKinds.ENTRY_DELETE;
            return null;
        }

        private void processEvent(int wd, int mask, final UnixPath name) {
            if ((mask & IN_Q_OVERFLOW) > 0) {
                for (Map.Entry<Integer, LinuxWatchKey> entry : wdToKey.entrySet()) {
                    entry.getValue().signalEvent(StandardWatchEventKinds.OVERFLOW, null);
                }
                return;
            }
            LinuxWatchKey key = wdToKey.get(wd);
            if (key == null)
                return;
            if ((mask & IN_IGNORED) > 0) {
                wdToKey.remove(wd);
                key.invalidate(false);
                key.signal();
                return;
            }
            if (name == null)
                return;
            WatchEvent.Kind<?> kind = maskToEventKind(mask);
            if (kind != null) {
                key.signalEvent(kind, name);
            }
        }
    }

    private static native int eventSize();

    private static native int[] eventOffsets();

    private static native int inotifyInit() throws UnixException;

    private static native int inotifyAddWatch(int fd, long pathAddress, int mask) throws UnixException;

    private static native void inotifyRmWatch(int fd, int wd) throws UnixException;

    private static native void configureBlocking(int fd, boolean blocking) throws UnixException;

    private static native void socketpair(int[] sv) throws UnixException;

    private static native int poll(int fd1, int fd2) throws UnixException;

    static {
        jdk.internal.loader.BootLoader.loadLibrary("nio");
    }
}