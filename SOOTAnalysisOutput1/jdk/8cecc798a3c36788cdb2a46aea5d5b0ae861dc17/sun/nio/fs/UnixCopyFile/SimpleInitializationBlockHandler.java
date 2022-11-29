package sun.nio.fs;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.LinkOption;
import java.nio.file.LinkPermission;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import static sun.nio.fs.UnixNativeDispatcher.*;
import static sun.nio.fs.UnixConstants.*;

class UnixCopyFile {

    private UnixCopyFile() {
    }

    private static class Flags {

        boolean replaceExisting;

        boolean atomicMove;

        boolean followLinks;

        boolean interruptible;

        boolean copyBasicAttributes;

        boolean copyPosixAttributes;

        boolean copyNonPosixAttributes;

        boolean failIfUnableToCopyBasic;

        boolean failIfUnableToCopyPosix;

        boolean failIfUnableToCopyNonPosix;

        static Flags fromCopyOptions(CopyOption... options) {
            Flags flags = new Flags();
            flags.followLinks = true;
            for (CopyOption option : options) {
                if (option == StandardCopyOption.REPLACE_EXISTING) {
                    flags.replaceExisting = true;
                    continue;
                }
                if (option == LinkOption.NOFOLLOW_LINKS) {
                    flags.followLinks = false;
                    continue;
                }
                if (option == StandardCopyOption.COPY_ATTRIBUTES) {
                    flags.copyBasicAttributes = true;
                    flags.copyPosixAttributes = true;
                    flags.copyNonPosixAttributes = true;
                    flags.failIfUnableToCopyBasic = true;
                    continue;
                }
                if (ExtendedOptions.INTERRUPTIBLE.matches(option)) {
                    flags.interruptible = true;
                    continue;
                }
                if (option == null)
                    throw new NullPointerException();
                throw new UnsupportedOperationException("Unsupported copy option");
            }
            return flags;
        }

        static Flags fromMoveOptions(CopyOption... options) {
            Flags flags = new Flags();
            for (CopyOption option : options) {
                if (option == StandardCopyOption.ATOMIC_MOVE) {
                    flags.atomicMove = true;
                    continue;
                }
                if (option == StandardCopyOption.REPLACE_EXISTING) {
                    flags.replaceExisting = true;
                    continue;
                }
                if (option == LinkOption.NOFOLLOW_LINKS) {
                    continue;
                }
                if (option == null)
                    throw new NullPointerException();
                throw new UnsupportedOperationException("Unsupported copy option");
            }
            flags.copyBasicAttributes = true;
            flags.copyPosixAttributes = true;
            flags.copyNonPosixAttributes = true;
            flags.failIfUnableToCopyBasic = true;
            return flags;
        }
    }

    private static void copyDirectory(UnixPath source, UnixFileAttributes attrs, UnixPath target, Flags flags) throws IOException {
        try {
            mkdir(target, attrs.mode());
        } catch (UnixException x) {
            x.rethrowAsIOException(target);
        }
        if (!flags.copyBasicAttributes && !flags.copyPosixAttributes && !flags.copyNonPosixAttributes)
            return;
        int dfd = -1;
        try {
            dfd = open(target, O_RDONLY, 0);
        } catch (UnixException x) {
            if (flags.copyNonPosixAttributes && flags.failIfUnableToCopyNonPosix) {
                try {
                    rmdir(target);
                } catch (UnixException ignore) {
                }
                x.rethrowAsIOException(target);
            }
        }
        boolean done = false;
        try {
            if (flags.copyPosixAttributes) {
                try {
                    if (dfd >= 0) {
                        fchown(dfd, attrs.uid(), attrs.gid());
                        fchmod(dfd, attrs.mode());
                    } else {
                        chown(target, attrs.uid(), attrs.gid());
                        chmod(target, attrs.mode());
                    }
                } catch (UnixException x) {
                    if (flags.failIfUnableToCopyPosix)
                        x.rethrowAsIOException(target);
                }
            }
            if (flags.copyNonPosixAttributes && (dfd >= 0)) {
                int sfd = -1;
                try {
                    sfd = open(source, O_RDONLY, 0);
                } catch (UnixException x) {
                    if (flags.failIfUnableToCopyNonPosix)
                        x.rethrowAsIOException(source);
                }
                if (sfd >= 0) {
                    source.getFileSystem().copyNonPosixAttributes(sfd, dfd);
                    close(sfd);
                }
            }
            if (flags.copyBasicAttributes) {
                try {
                    if (dfd >= 0 && futimesSupported()) {
                        futimes(dfd, attrs.lastAccessTime().to(TimeUnit.MICROSECONDS), attrs.lastModifiedTime().to(TimeUnit.MICROSECONDS));
                    } else {
                        utimes(target, attrs.lastAccessTime().to(TimeUnit.MICROSECONDS), attrs.lastModifiedTime().to(TimeUnit.MICROSECONDS));
                    }
                } catch (UnixException x) {
                    if (flags.failIfUnableToCopyBasic)
                        x.rethrowAsIOException(target);
                }
            }
            done = true;
        } finally {
            if (dfd >= 0)
                close(dfd);
            if (!done) {
                try {
                    rmdir(target);
                } catch (UnixException ignore) {
                }
            }
        }
    }

    private static void copyFile(UnixPath source, UnixFileAttributes attrs, UnixPath target, Flags flags, long addressToPollForCancel) throws IOException {
        int fi = -1;
        try {
            fi = open(source, O_RDONLY, 0);
        } catch (UnixException x) {
            x.rethrowAsIOException(source);
        }
        try {
            int fo = -1;
            try {
                fo = open(target, (O_WRONLY | O_CREAT | O_EXCL), attrs.mode());
            } catch (UnixException x) {
                x.rethrowAsIOException(target);
            }
            boolean complete = false;
            try {
                try {
                    transfer(fo, fi, addressToPollForCancel);
                } catch (UnixException x) {
                    x.rethrowAsIOException(source, target);
                }
                if (flags.copyPosixAttributes) {
                    try {
                        fchown(fo, attrs.uid(), attrs.gid());
                        fchmod(fo, attrs.mode());
                    } catch (UnixException x) {
                        if (flags.failIfUnableToCopyPosix)
                            x.rethrowAsIOException(target);
                    }
                }
                if (flags.copyNonPosixAttributes) {
                    source.getFileSystem().copyNonPosixAttributes(fi, fo);
                }
                if (flags.copyBasicAttributes) {
                    try {
                        if (futimesSupported()) {
                            futimes(fo, attrs.lastAccessTime().to(TimeUnit.MICROSECONDS), attrs.lastModifiedTime().to(TimeUnit.MICROSECONDS));
                        } else {
                            utimes(target, attrs.lastAccessTime().to(TimeUnit.MICROSECONDS), attrs.lastModifiedTime().to(TimeUnit.MICROSECONDS));
                        }
                    } catch (UnixException x) {
                        if (flags.failIfUnableToCopyBasic)
                            x.rethrowAsIOException(target);
                    }
                }
                complete = true;
            } finally {
                close(fo);
                if (!complete) {
                    try {
                        unlink(target);
                    } catch (UnixException ignore) {
                    }
                }
            }
        } finally {
            close(fi);
        }
    }

    private static void copyLink(UnixPath source, UnixFileAttributes attrs, UnixPath target, Flags flags) throws IOException {
        byte[] linktarget = null;
        try {
            linktarget = readlink(source);
        } catch (UnixException x) {
            x.rethrowAsIOException(source);
        }
        try {
            symlink(linktarget, target);
            if (flags.copyPosixAttributes) {
                try {
                    lchown(target, attrs.uid(), attrs.gid());
                } catch (UnixException x) {
                }
            }
        } catch (UnixException x) {
            x.rethrowAsIOException(target);
        }
    }

    private static void copySpecial(UnixPath source, UnixFileAttributes attrs, UnixPath target, Flags flags) throws IOException {
        try {
            mknod(target, attrs.mode(), attrs.rdev());
        } catch (UnixException x) {
            x.rethrowAsIOException(target);
        }
        boolean done = false;
        try {
            if (flags.copyPosixAttributes) {
                try {
                    chown(target, attrs.uid(), attrs.gid());
                    chmod(target, attrs.mode());
                } catch (UnixException x) {
                    if (flags.failIfUnableToCopyPosix)
                        x.rethrowAsIOException(target);
                }
            }
            if (flags.copyBasicAttributes) {
                try {
                    utimes(target, attrs.lastAccessTime().to(TimeUnit.MICROSECONDS), attrs.lastModifiedTime().to(TimeUnit.MICROSECONDS));
                } catch (UnixException x) {
                    if (flags.failIfUnableToCopyBasic)
                        x.rethrowAsIOException(target);
                }
            }
            done = true;
        } finally {
            if (!done) {
                try {
                    unlink(target);
                } catch (UnixException ignore) {
                }
            }
        }
    }

    static void ensureEmptyDir(UnixPath dir) throws IOException {
        try {
            long ptr = opendir(dir);
            try (UnixDirectoryStream stream = new UnixDirectoryStream(dir, ptr, e -> true)) {
                if (stream.iterator().hasNext()) {
                    throw new DirectoryNotEmptyException(dir.getPathForExceptionMessage());
                }
            }
        } catch (UnixException e) {
            e.rethrowAsIOException(dir);
        }
    }

    static void move(UnixPath source, UnixPath target, CopyOption... options) throws IOException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            source.checkWrite();
            target.checkWrite();
        }
        Flags flags = Flags.fromMoveOptions(options);
        if (flags.atomicMove) {
            try {
                rename(source, target);
            } catch (UnixException x) {
                if (x.errno() == EXDEV) {
                    throw new AtomicMoveNotSupportedException(source.getPathForExceptionMessage(), target.getPathForExceptionMessage(), x.errorString());
                }
                x.rethrowAsIOException(source, target);
            }
            return;
        }
        UnixFileAttributes sourceAttrs = null;
        UnixFileAttributes targetAttrs = null;
        try {
            sourceAttrs = UnixFileAttributes.get(source, false);
        } catch (UnixException x) {
            x.rethrowAsIOException(source);
        }
        try {
            targetAttrs = UnixFileAttributes.get(target, false);
        } catch (UnixException x) {
        }
        boolean targetExists = (targetAttrs != null);
        if (targetExists) {
            if (sourceAttrs.isSameFile(targetAttrs))
                return;
            if (!flags.replaceExisting) {
                throw new FileAlreadyExistsException(target.getPathForExceptionMessage());
            }
            try {
                if (targetAttrs.isDirectory()) {
                    rmdir(target);
                } else {
                    unlink(target);
                }
            } catch (UnixException x) {
                if (targetAttrs.isDirectory() && (x.errno() == EEXIST || x.errno() == ENOTEMPTY)) {
                    throw new DirectoryNotEmptyException(target.getPathForExceptionMessage());
                }
                x.rethrowAsIOException(target);
            }
        }
        try {
            rename(source, target);
            return;
        } catch (UnixException x) {
            if (x.errno() != EXDEV && x.errno() != EISDIR) {
                x.rethrowAsIOException(source, target);
            }
        }
        if (sourceAttrs.isDirectory()) {
            ensureEmptyDir(source);
            copyDirectory(source, sourceAttrs, target, flags);
        } else {
            if (sourceAttrs.isSymbolicLink()) {
                copyLink(source, sourceAttrs, target, flags);
            } else {
                if (sourceAttrs.isDevice()) {
                    copySpecial(source, sourceAttrs, target, flags);
                } else {
                    copyFile(source, sourceAttrs, target, flags, 0L);
                }
            }
        }
        try {
            if (sourceAttrs.isDirectory()) {
                rmdir(source);
            } else {
                unlink(source);
            }
        } catch (UnixException x) {
            try {
                if (sourceAttrs.isDirectory()) {
                    rmdir(target);
                } else {
                    unlink(target);
                }
            } catch (UnixException ignore) {
            }
            if (sourceAttrs.isDirectory() && (x.errno() == EEXIST || x.errno() == ENOTEMPTY)) {
                throw new DirectoryNotEmptyException(source.getPathForExceptionMessage());
            }
            x.rethrowAsIOException(source);
        }
    }

    static void copy(final UnixPath source, final UnixPath target, CopyOption... options) throws IOException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            source.checkRead();
            target.checkWrite();
        }
        final Flags flags = Flags.fromCopyOptions(options);
        UnixFileAttributes sourceAttrs = null;
        UnixFileAttributes targetAttrs = null;
        try {
            sourceAttrs = UnixFileAttributes.get(source, flags.followLinks);
        } catch (UnixException x) {
            x.rethrowAsIOException(source);
        }
        if (sm != null && sourceAttrs.isSymbolicLink()) {
            sm.checkPermission(new LinkPermission("symbolic"));
        }
        try {
            targetAttrs = UnixFileAttributes.get(target, false);
        } catch (UnixException x) {
        }
        boolean targetExists = (targetAttrs != null);
        if (targetExists) {
            if (sourceAttrs.isSameFile(targetAttrs))
                return;
            if (!flags.replaceExisting)
                throw new FileAlreadyExistsException(target.getPathForExceptionMessage());
            try {
                if (targetAttrs.isDirectory()) {
                    rmdir(target);
                } else {
                    unlink(target);
                }
            } catch (UnixException x) {
                if (targetAttrs.isDirectory() && (x.errno() == EEXIST || x.errno() == ENOTEMPTY)) {
                    throw new DirectoryNotEmptyException(target.getPathForExceptionMessage());
                }
                x.rethrowAsIOException(target);
            }
        }
        if (sourceAttrs.isDirectory()) {
            copyDirectory(source, sourceAttrs, target, flags);
            return;
        }
        if (sourceAttrs.isSymbolicLink()) {
            copyLink(source, sourceAttrs, target, flags);
            return;
        }
        if (!flags.interruptible) {
            copyFile(source, sourceAttrs, target, flags, 0L);
            return;
        }
        final UnixFileAttributes attrsToCopy = sourceAttrs;
        Cancellable copyTask = new Cancellable() {

            @Override
            public void implRun() throws IOException {
                copyFile(source, attrsToCopy, target, flags, addressToPollForCancel());
            }
        };
        try {
            Cancellable.runInterruptibly(copyTask);
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof IOException)
                throw (IOException) t;
            throw new IOException(t);
        }
    }

    static native void transfer(int dst, int src, long addressToPollForCancel) throws UnixException;

    static {
        jdk.internal.loader.BootLoader.loadLibrary("nio");
    }
}