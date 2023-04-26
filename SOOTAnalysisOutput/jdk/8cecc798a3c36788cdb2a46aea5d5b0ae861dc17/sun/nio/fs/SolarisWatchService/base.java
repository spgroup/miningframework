package sun.nio.fs;

import java.nio.file.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.io.IOException;
import jdk.internal.misc.Unsafe;
import static sun.nio.fs.UnixConstants.*;

class SolarisWatchService extends AbstractWatchService {

    private static final Unsafe unsafe = Unsafe.getUnsafe();

    private static int addressSize = unsafe.addressSize();

    private static int dependsArch(int value32, int value64) {
        return (addressSize == 4) ? value32 : value64;
    }

    private static final int SIZEOF_PORT_EVENT = dependsArch(16, 24);

    private static final int OFFSETOF_EVENTS = 0;

    private static final int OFFSETOF_SOURCE = 4;

    private static final int OFFSETOF_OBJECT = 8;

    private static final int SIZEOF_FILEOBJ = dependsArch(40, 80);

    private static final int OFFSET_FO_NAME = dependsArch(36, 72);

    private static final short PORT_SOURCE_USER = 3;

    private static final short PORT_SOURCE_FILE = 7;

    private static final int FILE_MODIFIED = 0x00000002;

    private static final int FILE_ATTRIB = 0x00000004;

    private static final int FILE_NOFOLLOW = 0x10000000;

    private static final int FILE_DELETE = 0x00000010;

    private static final int FILE_RENAME_TO = 0x00000020;

    private static final int FILE_RENAME_FROM = 0x00000040;

    private static final int UNMOUNTED = 0x20000000;

    private static final int MOUNTEDOVER = 0x40000000;

    private final Poller poller;

    SolarisWatchService(UnixFileSystem fs) throws IOException {
        int port = -1;
        try {
            port = portCreate();
        } catch (UnixException x) {
            throw new IOException(x.errorString());
        }
        this.poller = new Poller(fs, this, port);
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

    private class SolarisWatchKey extends AbstractWatchKey implements DirectoryNode {

        private final UnixFileKey fileKey;

        private final long object;

        private volatile Set<? extends WatchEvent.Kind<?>> events;

        private Map<Path, EntryNode> children = new HashMap<>();

        SolarisWatchKey(SolarisWatchService watcher, UnixPath dir, UnixFileKey fileKey, long object, Set<? extends WatchEvent.Kind<?>> events) {
            super(dir, watcher);
            this.fileKey = fileKey;
            this.object = object;
            this.events = events;
        }

        UnixPath getDirectory() {
            return (UnixPath) watchable();
        }

        UnixFileKey getFileKey() {
            return fileKey;
        }

        @Override
        public long object() {
            return object;
        }

        void invalidate() {
            events = null;
        }

        Set<? extends WatchEvent.Kind<?>> events() {
            return events;
        }

        void setEvents(Set<? extends WatchEvent.Kind<?>> events) {
            this.events = events;
        }

        Map<Path, EntryNode> children() {
            return children;
        }

        @Override
        public boolean isValid() {
            return events != null;
        }

        @Override
        public void cancel() {
            if (isValid()) {
                poller.cancel(this);
            }
        }

        @Override
        public void addChild(Path name, EntryNode node) {
            children.put(name, node);
        }

        @Override
        public void removeChild(Path name) {
            children.remove(name);
        }

        @Override
        public EntryNode getChild(Path name) {
            return children.get(name);
        }
    }

    private class Poller extends AbstractPoller {

        private static final int MAX_EVENT_COUNT = 128;

        private static final int FILE_REMOVED = (FILE_DELETE | FILE_RENAME_TO | FILE_RENAME_FROM);

        private static final int FILE_EXCEPTION = (FILE_REMOVED | UNMOUNTED | MOUNTEDOVER);

        private final long bufferAddress;

        private final SolarisWatchService watcher;

        private final int port;

        private final Map<UnixFileKey, SolarisWatchKey> fileKey2WatchKey;

        private final Map<Long, Node> object2Node;

        Poller(UnixFileSystem fs, SolarisWatchService watcher, int port) {
            this.watcher = watcher;
            this.port = port;
            this.bufferAddress = unsafe.allocateMemory(SIZEOF_PORT_EVENT * MAX_EVENT_COUNT);
            this.fileKey2WatchKey = new HashMap<UnixFileKey, SolarisWatchKey>();
            this.object2Node = new HashMap<Long, Node>();
        }

        @Override
        void wakeup() throws IOException {
            try {
                portSend(port, 0);
            } catch (UnixException x) {
                throw new IOException(x.errorString());
            }
        }

        @Override
        Object implRegister(Path obj, Set<? extends WatchEvent.Kind<?>> events, WatchEvent.Modifier... modifiers) {
            if (modifiers.length > 0) {
                for (WatchEvent.Modifier modifier : modifiers) {
                    if (modifier == null)
                        return new NullPointerException();
                    if (!ExtendedOptions.SENSITIVITY_HIGH.matches(modifier) && !ExtendedOptions.SENSITIVITY_MEDIUM.matches(modifier) && !ExtendedOptions.SENSITIVITY_LOW.matches(modifier)) {
                        return new UnsupportedOperationException("Modifier not supported");
                    }
                }
            }
            UnixPath dir = (UnixPath) obj;
            UnixFileAttributes attrs = null;
            try {
                attrs = UnixFileAttributes.get(dir, true);
            } catch (UnixException x) {
                return x.asIOException(dir);
            }
            if (!attrs.isDirectory()) {
                return new NotDirectoryException(dir.getPathForExceptionMessage());
            }
            UnixFileKey fileKey = attrs.fileKey();
            SolarisWatchKey watchKey = fileKey2WatchKey.get(fileKey);
            if (watchKey != null) {
                try {
                    updateEvents(watchKey, events);
                } catch (UnixException x) {
                    return x.asIOException(dir);
                }
                return watchKey;
            }
            long object = 0L;
            try {
                object = registerImpl(dir, (FILE_MODIFIED | FILE_ATTRIB));
            } catch (UnixException x) {
                return x.asIOException(dir);
            }
            watchKey = new SolarisWatchKey(watcher, dir, fileKey, object, events);
            object2Node.put(object, watchKey);
            fileKey2WatchKey.put(fileKey, watchKey);
            registerChildren(dir, watchKey, false, false);
            return watchKey;
        }

        void releaseChild(EntryNode node) {
            long object = node.object();
            if (object != 0L) {
                object2Node.remove(object);
                releaseObject(object, true);
                node.setObject(0L);
            }
        }

        void releaseChildren(SolarisWatchKey key) {
            for (EntryNode node : key.children().values()) {
                releaseChild(node);
            }
        }

        @Override
        void implCancelKey(WatchKey obj) {
            SolarisWatchKey key = (SolarisWatchKey) obj;
            if (key.isValid()) {
                fileKey2WatchKey.remove(key.getFileKey());
                releaseChildren(key);
                long object = key.object();
                object2Node.remove(object);
                releaseObject(object, true);
                key.invalidate();
            }
        }

        @Override
        void implCloseAll() {
            for (Long object : object2Node.keySet()) {
                releaseObject(object, true);
            }
            for (Map.Entry<UnixFileKey, SolarisWatchKey> entry : fileKey2WatchKey.entrySet()) {
                entry.getValue().invalidate();
            }
            object2Node.clear();
            fileKey2WatchKey.clear();
            unsafe.freeMemory(bufferAddress);
            UnixNativeDispatcher.close(port);
        }

        @Override
        public void run() {
            try {
                for (; ; ) {
                    int n = portGetn(port, bufferAddress, MAX_EVENT_COUNT);
                    assert n > 0;
                    long address = bufferAddress;
                    for (int i = 0; i < n; i++) {
                        boolean shutdown = processEvent(address);
                        if (shutdown)
                            return;
                        address += SIZEOF_PORT_EVENT;
                    }
                }
            } catch (UnixException x) {
                x.printStackTrace();
            }
        }

        boolean processEvent(long address) {
            short source = unsafe.getShort(address + OFFSETOF_SOURCE);
            long object = unsafe.getAddress(address + OFFSETOF_OBJECT);
            int events = unsafe.getInt(address + OFFSETOF_EVENTS);
            if (source != PORT_SOURCE_FILE) {
                if (source == PORT_SOURCE_USER) {
                    boolean shutdown = processRequests();
                    if (shutdown)
                        return true;
                }
                return false;
            }
            Node node = object2Node.get(object);
            if (node == null) {
                return false;
            }
            boolean reregister = true;
            boolean isDirectory = (node instanceof SolarisWatchKey);
            if (isDirectory) {
                processDirectoryEvents((SolarisWatchKey) node, events);
            } else {
                boolean ignore = processEntryEvents((EntryNode) node, events);
                if (ignore)
                    reregister = false;
            }
            if (reregister) {
                try {
                    events = FILE_MODIFIED | FILE_ATTRIB;
                    if (!isDirectory)
                        events |= FILE_NOFOLLOW;
                    portAssociate(port, PORT_SOURCE_FILE, object, events);
                } catch (UnixException x) {
                    reregister = false;
                }
            }
            if (!reregister) {
                object2Node.remove(object);
                releaseObject(object, false);
                if (isDirectory) {
                    SolarisWatchKey key = (SolarisWatchKey) node;
                    fileKey2WatchKey.remove(key.getFileKey());
                    key.invalidate();
                    key.signal();
                } else {
                    EntryNode entry = (EntryNode) node;
                    SolarisWatchKey key = (SolarisWatchKey) entry.parent();
                    key.removeChild(entry.name());
                }
            }
            return false;
        }

        void processDirectoryEvents(SolarisWatchKey key, int mask) {
            if ((mask & (FILE_MODIFIED | FILE_ATTRIB)) != 0) {
                registerChildren(key.getDirectory(), key, key.events().contains(StandardWatchEventKinds.ENTRY_CREATE), key.events().contains(StandardWatchEventKinds.ENTRY_DELETE));
            }
        }

        boolean processEntryEvents(EntryNode node, int mask) {
            SolarisWatchKey key = (SolarisWatchKey) node.parent();
            Set<? extends WatchEvent.Kind<?>> events = key.events();
            if (events == null) {
                return true;
            }
            if (((mask & (FILE_MODIFIED | FILE_ATTRIB)) != 0) && events.contains(StandardWatchEventKinds.ENTRY_MODIFY)) {
                key.signalEvent(StandardWatchEventKinds.ENTRY_MODIFY, node.name());
            }
            return false;
        }

        void registerChildren(UnixPath dir, SolarisWatchKey parent, boolean sendCreateEvents, boolean sendDeleteEvents) {
            boolean isModifyEnabled = parent.events().contains(StandardWatchEventKinds.ENTRY_MODIFY);
            for (EntryNode node : parent.children().values()) {
                node.setVisited(false);
            }
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path entry : stream) {
                    Path name = entry.getFileName();
                    EntryNode node = parent.getChild(name);
                    if (node != null) {
                        node.setVisited(true);
                        continue;
                    }
                    long object = 0L;
                    int errno = 0;
                    boolean addNode = false;
                    if (isModifyEnabled) {
                        try {
                            UnixPath path = (UnixPath) entry;
                            int events = (FILE_NOFOLLOW | FILE_MODIFIED | FILE_ATTRIB);
                            object = registerImpl(path, events);
                            addNode = true;
                        } catch (UnixException x) {
                            errno = x.errno();
                        }
                    } else {
                        addNode = true;
                    }
                    if (addNode) {
                        node = new EntryNode(object, (UnixPath) entry.getFileName(), parent);
                        node.setVisited(true);
                        parent.addChild(entry.getFileName(), node);
                        if (object != 0L)
                            object2Node.put(object, node);
                    }
                    boolean deleted = (errno == ENOENT);
                    if (sendCreateEvents && (addNode || deleted))
                        parent.signalEvent(StandardWatchEventKinds.ENTRY_CREATE, name);
                    if (sendDeleteEvents && deleted)
                        parent.signalEvent(StandardWatchEventKinds.ENTRY_DELETE, name);
                }
            } catch (DirectoryIteratorException | IOException x) {
                parent.signalEvent(StandardWatchEventKinds.OVERFLOW, null);
                return;
            }
            Iterator<Map.Entry<Path, EntryNode>> iterator = parent.children().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Path, EntryNode> entry = iterator.next();
                EntryNode node = entry.getValue();
                if (!node.isVisited()) {
                    long object = node.object();
                    if (object != 0L) {
                        object2Node.remove(object);
                        releaseObject(object, true);
                    }
                    if (sendDeleteEvents)
                        parent.signalEvent(StandardWatchEventKinds.ENTRY_DELETE, node.name());
                    iterator.remove();
                }
            }
        }

        void updateEvents(SolarisWatchKey key, Set<? extends WatchEvent.Kind<?>> events) throws UnixException {
            boolean oldModifyEnabled = key.events().contains(StandardWatchEventKinds.ENTRY_MODIFY);
            key.setEvents(events);
            boolean newModifyEnabled = events.contains(StandardWatchEventKinds.ENTRY_MODIFY);
            if (newModifyEnabled != oldModifyEnabled) {
                UnixException ex = null;
                for (EntryNode node : key.children().values()) {
                    if (newModifyEnabled) {
                        UnixPath path = key.getDirectory().resolve(node.name());
                        int ev = (FILE_NOFOLLOW | FILE_MODIFIED | FILE_ATTRIB);
                        try {
                            long object = registerImpl(path, ev);
                            object2Node.put(object, node);
                            node.setObject(object);
                        } catch (UnixException x) {
                            if (x.errno() != ENOENT) {
                                ex = x;
                                break;
                            }
                        }
                    } else {
                        releaseChild(node);
                    }
                }
                if (ex != null) {
                    releaseChildren(key);
                    throw ex;
                }
            }
        }

        long registerImpl(UnixPath dir, int events) throws UnixException {
            byte[] path = dir.getByteArrayForSysCalls();
            int len = path.length;
            long name = unsafe.allocateMemory(len + 1);
            unsafe.copyMemory(path, Unsafe.ARRAY_BYTE_BASE_OFFSET, null, name, (long) len);
            unsafe.putByte(name + len, (byte) 0);
            long object = unsafe.allocateMemory(SIZEOF_FILEOBJ);
            unsafe.setMemory(null, object, SIZEOF_FILEOBJ, (byte) 0);
            unsafe.putAddress(object + OFFSET_FO_NAME, name);
            try {
                portAssociate(port, PORT_SOURCE_FILE, object, events);
            } catch (UnixException x) {
                if (x.errno() == EAGAIN) {
                    System.err.println("The maximum number of objects associated " + "with the port has been reached");
                }
                unsafe.freeMemory(name);
                unsafe.freeMemory(object);
                throw x;
            }
            return object;
        }

        void releaseObject(long object, boolean dissociate) {
            if (dissociate) {
                try {
                    portDissociate(port, PORT_SOURCE_FILE, object);
                } catch (UnixException x) {
                }
            }
            long name = unsafe.getAddress(object + OFFSET_FO_NAME);
            unsafe.freeMemory(name);
            unsafe.freeMemory(object);
        }
    }

    private static interface Node {

        long object();
    }

    private static interface DirectoryNode extends Node {

        void addChild(Path name, EntryNode node);

        void removeChild(Path name);

        EntryNode getChild(Path name);
    }

    private static class EntryNode implements Node {

        private long object;

        private final UnixPath name;

        private final DirectoryNode parent;

        private boolean visited;

        EntryNode(long object, UnixPath name, DirectoryNode parent) {
            this.object = object;
            this.name = name;
            this.parent = parent;
        }

        @Override
        public long object() {
            return object;
        }

        void setObject(long ptr) {
            this.object = ptr;
        }

        UnixPath name() {
            return name;
        }

        DirectoryNode parent() {
            return parent;
        }

        boolean isVisited() {
            return visited;
        }

        void setVisited(boolean v) {
            this.visited = v;
        }
    }

    private static native void init();

    private static native int portCreate() throws UnixException;

    private static native void portAssociate(int port, int source, long object, int events) throws UnixException;

    private static native void portDissociate(int port, int source, long object) throws UnixException;

    private static native void portSend(int port, int events) throws UnixException;

    private static native int portGetn(int port, long address, int max) throws UnixException;

    static {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

            public Void run() {
                System.loadLibrary("nio");
                return null;
            }
        });
        init();
    }
}
