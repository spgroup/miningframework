package sun.nio.fs;

import java.security.AccessController;
import java.security.PrivilegedAction;

class UnixNativeDispatcher {

    protected UnixNativeDispatcher() {
    }

    private static NativeBuffer copyToNativeBuffer(UnixPath path) {
        byte[] cstr = path.getByteArrayForSysCalls();
        int size = cstr.length + 1;
        NativeBuffer buffer = NativeBuffers.getNativeBufferFromCache(size);
        if (buffer == null) {
            buffer = NativeBuffers.allocNativeBuffer(size);
        } else {
            if (buffer.owner() == path)
                return buffer;
        }
        NativeBuffers.copyCStringToNativeBuffer(cstr, buffer);
        buffer.setOwner(path);
        return buffer;
    }

    static native byte[] getcwd();

    static native int dup(int filedes) throws UnixException;

    static int open(UnixPath path, int flags, int mode) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            return open0(buffer.address(), flags, mode);
        } finally {
            buffer.release();
        }
    }

    private static native int open0(long pathAddress, int flags, int mode) throws UnixException;

    static int openat(int dfd, byte[] path, int flags, int mode) throws UnixException {
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(path);
        try {
            return openat0(dfd, buffer.address(), flags, mode);
        } finally {
            buffer.release();
        }
    }

    private static native int openat0(int dfd, long pathAddress, int flags, int mode) throws UnixException;

    static native void close(int fd);

    static long fopen(UnixPath filename, String mode) throws UnixException {
        NativeBuffer pathBuffer = copyToNativeBuffer(filename);
        NativeBuffer modeBuffer = NativeBuffers.asNativeBuffer(mode.getBytes());
        try {
            return fopen0(pathBuffer.address(), modeBuffer.address());
        } finally {
            modeBuffer.release();
            pathBuffer.release();
        }
    }

    private static native long fopen0(long pathAddress, long modeAddress) throws UnixException;

    static native void fclose(long stream) throws UnixException;

    static void link(UnixPath existing, UnixPath newfile) throws UnixException {
        NativeBuffer existingBuffer = copyToNativeBuffer(existing);
        NativeBuffer newBuffer = copyToNativeBuffer(newfile);
        try {
            link0(existingBuffer.address(), newBuffer.address());
        } finally {
            newBuffer.release();
            existingBuffer.release();
        }
    }

    private static native void link0(long existingAddress, long newAddress) throws UnixException;

    static void unlink(UnixPath path) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            unlink0(buffer.address());
        } finally {
            buffer.release();
        }
    }

    private static native void unlink0(long pathAddress) throws UnixException;

    static void unlinkat(int dfd, byte[] path, int flag) throws UnixException {
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(path);
        try {
            unlinkat0(dfd, buffer.address(), flag);
        } finally {
            buffer.release();
        }
    }

    private static native void unlinkat0(int dfd, long pathAddress, int flag) throws UnixException;

    static void mknod(UnixPath path, int mode, long dev) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            mknod0(buffer.address(), mode, dev);
        } finally {
            buffer.release();
        }
    }

    private static native void mknod0(long pathAddress, int mode, long dev) throws UnixException;

    static void rename(UnixPath from, UnixPath to) throws UnixException {
        NativeBuffer fromBuffer = copyToNativeBuffer(from);
        NativeBuffer toBuffer = copyToNativeBuffer(to);
        try {
            rename0(fromBuffer.address(), toBuffer.address());
        } finally {
            toBuffer.release();
            fromBuffer.release();
        }
    }

    private static native void rename0(long fromAddress, long toAddress) throws UnixException;

    static void renameat(int fromfd, byte[] from, int tofd, byte[] to) throws UnixException {
        NativeBuffer fromBuffer = NativeBuffers.asNativeBuffer(from);
        NativeBuffer toBuffer = NativeBuffers.asNativeBuffer(to);
        try {
            renameat0(fromfd, fromBuffer.address(), tofd, toBuffer.address());
        } finally {
            toBuffer.release();
            fromBuffer.release();
        }
    }

    private static native void renameat0(int fromfd, long fromAddress, int tofd, long toAddress) throws UnixException;

    static void mkdir(UnixPath path, int mode) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            mkdir0(buffer.address(), mode);
        } finally {
            buffer.release();
        }
    }

    private static native void mkdir0(long pathAddress, int mode) throws UnixException;

    static void rmdir(UnixPath path) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            rmdir0(buffer.address());
        } finally {
            buffer.release();
        }
    }

    private static native void rmdir0(long pathAddress) throws UnixException;

    static byte[] readlink(UnixPath path) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            return readlink0(buffer.address());
        } finally {
            buffer.release();
        }
    }

    private static native byte[] readlink0(long pathAddress) throws UnixException;

    static byte[] realpath(UnixPath path) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            return realpath0(buffer.address());
        } finally {
            buffer.release();
        }
    }

    private static native byte[] realpath0(long pathAddress) throws UnixException;

    static void symlink(byte[] name1, UnixPath name2) throws UnixException {
        NativeBuffer targetBuffer = NativeBuffers.asNativeBuffer(name1);
        NativeBuffer linkBuffer = copyToNativeBuffer(name2);
        try {
            symlink0(targetBuffer.address(), linkBuffer.address());
        } finally {
            linkBuffer.release();
            targetBuffer.release();
        }
    }

    private static native void symlink0(long name1, long name2) throws UnixException;

    static void stat(UnixPath path, UnixFileAttributes attrs) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            stat0(buffer.address(), attrs);
        } finally {
            buffer.release();
        }
    }

    private static native void stat0(long pathAddress, UnixFileAttributes attrs) throws UnixException;

    static void lstat(UnixPath path, UnixFileAttributes attrs) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            lstat0(buffer.address(), attrs);
        } finally {
            buffer.release();
        }
    }

    private static native void lstat0(long pathAddress, UnixFileAttributes attrs) throws UnixException;

    static native void fstat(int fd, UnixFileAttributes attrs) throws UnixException;

    static void fstatat(int dfd, byte[] path, int flag, UnixFileAttributes attrs) throws UnixException {
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(path);
        try {
            fstatat0(dfd, buffer.address(), flag, attrs);
        } finally {
            buffer.release();
        }
    }

    private static native void fstatat0(int dfd, long pathAddress, int flag, UnixFileAttributes attrs) throws UnixException;

    static void chown(UnixPath path, int uid, int gid) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            chown0(buffer.address(), uid, gid);
        } finally {
            buffer.release();
        }
    }

    private static native void chown0(long pathAddress, int uid, int gid) throws UnixException;

    static void lchown(UnixPath path, int uid, int gid) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            lchown0(buffer.address(), uid, gid);
        } finally {
            buffer.release();
        }
    }

    private static native void lchown0(long pathAddress, int uid, int gid) throws UnixException;

    static native void fchown(int fd, int uid, int gid) throws UnixException;

    static void chmod(UnixPath path, int mode) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            chmod0(buffer.address(), mode);
        } finally {
            buffer.release();
        }
    }

    private static native void chmod0(long pathAddress, int mode) throws UnixException;

    static native void fchmod(int fd, int mode) throws UnixException;

    static void utimes(UnixPath path, long times0, long times1) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            utimes0(buffer.address(), times0, times1);
        } finally {
            buffer.release();
        }
    }

    private static native void utimes0(long pathAddress, long times0, long times1) throws UnixException;

    static native void futimes(int fd, long times0, long times1) throws UnixException;

    static long opendir(UnixPath path) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            return opendir0(buffer.address());
        } finally {
            buffer.release();
        }
    }

    private static native long opendir0(long pathAddress) throws UnixException;

    static native long fdopendir(int dfd) throws UnixException;

    static native void closedir(long dir) throws UnixException;

    static native byte[] readdir(long dir) throws UnixException;

    static native int read(int fildes, long buf, int nbyte) throws UnixException;

    static native int write(int fildes, long buf, int nbyte) throws UnixException;

    static void access(UnixPath path, int amode) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            access0(buffer.address(), amode);
        } finally {
            buffer.release();
        }
    }

    private static native void access0(long pathAddress, int amode) throws UnixException;

    static native byte[] getpwuid(int uid) throws UnixException;

    static native byte[] getgrgid(int gid) throws UnixException;

    static int getpwnam(String name) throws UnixException {
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(name.getBytes());
        try {
            return getpwnam0(buffer.address());
        } finally {
            buffer.release();
        }
    }

    private static native int getpwnam0(long nameAddress) throws UnixException;

    static int getgrnam(String name) throws UnixException {
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(name.getBytes());
        try {
            return getgrnam0(buffer.address());
        } finally {
            buffer.release();
        }
    }

    private static native int getgrnam0(long nameAddress) throws UnixException;

    static void statvfs(UnixPath path, UnixFileStoreAttributes attrs) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            statvfs0(buffer.address(), attrs);
        } finally {
            buffer.release();
        }
    }

    private static native void statvfs0(long pathAddress, UnixFileStoreAttributes attrs) throws UnixException;

    static long pathconf(UnixPath path, int name) throws UnixException {
        NativeBuffer buffer = copyToNativeBuffer(path);
        try {
            return pathconf0(buffer.address(), name);
        } finally {
            buffer.release();
        }
    }

    private static native long pathconf0(long pathAddress, int name) throws UnixException;

    static native long fpathconf(int filedes, int name) throws UnixException;

    static native byte[] strerror(int errnum);

    private static final boolean hasAtSysCalls;

    static boolean supportsAtSysCalls() {
        return hasAtSysCalls;
    }

    static boolean supportsNoFollowLinks() {
        return UnixConstants.O_NOFOLLOW != 0;
    }

    private static native int init();

    private static final int HAS_AT_SYSCALLS = 0x1;

    static {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

            public Void run() {
                System.loadLibrary("nio");
                return null;
            }
        });
        int flags = init();
        hasAtSysCalls = (flags & HAS_AT_SYSCALLS) > 0;
    }
}
