package org.apache.cassandra.io.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.ch.DirectBuffer;
import org.apache.cassandra.concurrent.ScheduledExecutors;
import org.apache.cassandra.io.FSError;
import org.apache.cassandra.io.FSErrorHandler;
import org.apache.cassandra.io.FSReadError;
import org.apache.cassandra.io.FSWriteError;
import org.apache.cassandra.io.sstable.CorruptSSTableException;
import org.apache.cassandra.utils.JVMStabilityInspector;
import static com.google.common.base.Throwables.propagate;
import static org.apache.cassandra.utils.Throwables.maybeFail;
import static org.apache.cassandra.utils.Throwables.merge;

public final class FileUtils {

    public static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static final long ONE_KB = 1024;

    public static final long ONE_MB = 1024 * ONE_KB;

    public static final long ONE_GB = 1024 * ONE_MB;

    public static final long ONE_TB = 1024 * ONE_GB;

    private static final DecimalFormat df = new DecimalFormat("#.##");

    public static final boolean isCleanerAvailable;

    private static final AtomicReference<Optional<FSErrorHandler>> fsErrorHandler = new AtomicReference<>(Optional.empty());

    static {
        boolean canClean = false;
        try {
            ByteBuffer buf = ByteBuffer.allocateDirect(1);
            ((DirectBuffer) buf).cleaner().clean();
            canClean = true;
        } catch (Throwable t) {
            logger.error("Cannot initialize un-mmaper.  (Are you using a non-Oracle JVM?)  Compacted data files will not be removed promptly.  Consider using an Oracle JVM or using standard disk access mode", t);
            JVMStabilityInspector.inspectThrowable(t);
        }
        isCleanerAvailable = canClean;
    }

    public static void createHardLink(String from, String to) {
        createHardLink(new File(from), new File(to));
    }

    public static void createHardLink(File from, File to) {
        if (to.exists())
            throw new RuntimeException("Tried to create duplicate hard link to " + to);
        if (!from.exists())
            throw new RuntimeException("Tried to hard link to file that does not exist " + from);
        try {
            Files.createLink(to.toPath(), from.toPath());
        } catch (IOException e) {
            throw new FSWriteError(e, to);
        }
    }

    public static File createTempFile(String prefix, String suffix, File directory) {
        try {
            return File.createTempFile(prefix, suffix, directory);
        } catch (IOException e) {
            throw new FSWriteError(e, directory);
        }
    }

    public static File createTempFile(String prefix, String suffix) {
        return createTempFile(prefix, suffix, new File(System.getProperty("java.io.tmpdir")));
    }

    public static Throwable deleteWithConfirm(String filePath, boolean expect, Throwable accumulate) {
        return deleteWithConfirm(new File(filePath), expect, accumulate);
    }

    public static Throwable deleteWithConfirm(File file, boolean expect, Throwable accumulate) {
        boolean exists = file.exists();
        assert exists || !expect : "attempted to delete non-existing file " + file.getName();
        try {
            if (exists)
                Files.delete(file.toPath());
        } catch (Throwable t) {
            try {
                throw new FSWriteError(t, file);
            } catch (Throwable t2) {
                accumulate = merge(accumulate, t2);
            }
        }
        return accumulate;
    }

    public static void deleteWithConfirm(String file) {
        deleteWithConfirm(new File(file));
    }

    public static void deleteWithConfirm(File file) {
        maybeFail(deleteWithConfirm(file, true, null));
    }

    public static void renameWithOutConfirm(String from, String to) {
        try {
            atomicMoveWithFallback(new File(from).toPath(), new File(to).toPath());
        } catch (IOException e) {
            if (logger.isTraceEnabled())
                logger.trace("Could not move file " + from + " to " + to, e);
        }
    }

    public static void renameWithConfirm(String from, String to) {
        renameWithConfirm(new File(from), new File(to));
    }

    public static void renameWithConfirm(File from, File to) {
        assert from.exists();
        if (logger.isTraceEnabled())
            logger.trace("Renaming {} to {}", from.getPath(), to.getPath());
        try {
            atomicMoveWithFallback(from.toPath(), to.toPath());
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to rename %s to %s", from.getPath(), to.getPath()), e);
        }
    }

    private static void atomicMoveWithFallback(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            logger.trace("Could not do an atomic move", e);
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void truncate(String path, long size) {
        try (FileChannel channel = FileChannel.open(Paths.get(path), StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            channel.truncate(size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeQuietly(Closeable c) {
        try {
            if (c != null)
                c.close();
        } catch (Exception e) {
            logger.warn("Failed closing {}", c, e);
        }
    }

    public static void closeQuietly(AutoCloseable c) {
        try {
            if (c != null)
                c.close();
        } catch (Exception e) {
            logger.warn("Failed closing {}", c, e);
        }
    }

    public static void close(Closeable... cs) throws IOException {
        close(Arrays.asList(cs));
    }

    public static void close(Iterable<? extends Closeable> cs) throws IOException {
        Throwable e = null;
        for (Closeable c : cs) {
            try {
                if (c != null)
                    c.close();
            } catch (Throwable ex) {
                if (e == null)
                    e = ex;
                else
                    e.addSuppressed(ex);
                logger.warn("Failed closing stream {}", c, ex);
            }
        }
        maybeFail(e, IOException.class);
    }

    public static void closeQuietly(Iterable<? extends AutoCloseable> cs) {
        for (AutoCloseable c : cs) {
            try {
                if (c != null)
                    c.close();
            } catch (Exception ex) {
                logger.warn("Failed closing {}", c, ex);
            }
        }
    }

    public static String getCanonicalPath(String filename) {
        try {
            return new File(filename).getCanonicalPath();
        } catch (IOException e) {
            throw new FSReadError(e, filename);
        }
    }

    public static String getCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            throw new FSReadError(e, file);
        }
    }

    public static boolean isContained(File folder, File file) {
        Path folderPath = Paths.get(getCanonicalPath(folder));
        Path filePath = Paths.get(getCanonicalPath(file));
        return filePath.startsWith(folderPath);
    }

    public static String getRelativePath(String basePath, String path) {
        try {
            return Paths.get(basePath).relativize(Paths.get(path)).toString();
        } catch (Exception ex) {
            String absDataPath = FileUtils.getCanonicalPath(basePath);
            return Paths.get(absDataPath).relativize(Paths.get(path)).toString();
        }
    }

    public static void clean(ByteBuffer buffer) {
        if (buffer == null)
            return;
        if (isCleanerAvailable && buffer.isDirect()) {
            DirectBuffer db = (DirectBuffer) buffer;
            if (db.cleaner() != null)
                db.cleaner().clean();
        }
    }

    public static void createDirectory(String directory) {
        createDirectory(new File(directory));
    }

    public static void createDirectory(File directory) {
        if (!directory.exists()) {
            if (!directory.mkdirs())
                throw new FSWriteError(new IOException("Failed to mkdirs " + directory), directory);
        }
    }

    public static boolean delete(String file) {
        File f = new File(file);
        return f.delete();
    }

    public static void delete(File... files) {
        if (files == null) {
            logger.debug("Received null list of files to delete");
            return;
        }
        for (File file : files) {
            file.delete();
        }
    }

    public static void deleteAsync(final String file) {
        Runnable runnable = new Runnable() {

            public void run() {
                deleteWithConfirm(new File(file));
            }
        };
        ScheduledExecutors.nonPeriodicTasks.execute(runnable);
    }

    public static void visitDirectory(Path dir, Predicate<? super File> filter, Consumer<? super File> consumer) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            StreamSupport.stream(stream.spliterator(), false).map(Path::toFile).filter(f -> f.exists() && (filter == null || filter.test(f))).forEach(consumer);
        } catch (IOException | DirectoryIteratorException ex) {
            logger.error("Failed to list files in {} with exception: {}", dir, ex.getMessage(), ex);
        }
    }

    public static String stringifyFileSize(double value) {
        double d;
        if (value >= ONE_TB) {
            d = value / ONE_TB;
            String val = df.format(d);
            return val + " TiB";
        } else if (value >= ONE_GB) {
            d = value / ONE_GB;
            String val = df.format(d);
            return val + " GiB";
        } else if (value >= ONE_MB) {
            d = value / ONE_MB;
            String val = df.format(d);
            return val + " MiB";
        } else if (value >= ONE_KB) {
            d = value / ONE_KB;
            String val = df.format(d);
            return val + " KiB";
        } else {
            String val = df.format(value);
            return val + " bytes";
        }
    }

    public static void deleteRecursive(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) deleteRecursive(new File(dir, child));
        }
        deleteWithConfirm(dir);
    }

    public static void deleteRecursiveOnExit(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) deleteRecursiveOnExit(new File(dir, child));
        }
        logger.trace("Scheduling deferred deletion of file: {}", dir);
        dir.deleteOnExit();
    }

    public static void handleCorruptSSTable(CorruptSSTableException e) {
        fsErrorHandler.get().ifPresent(handler -> handler.handleCorruptSSTable(e));
    }

    public static void handleFSError(FSError e) {
        fsErrorHandler.get().ifPresent(handler -> handler.handleFSError(e));
    }

    public static void handleFSErrorAndPropagate(FSError e) {
        JVMStabilityInspector.inspectThrowable(e);
        throw propagate(e);
    }

    public static long folderSize(File folder) {
        final long[] sizeArr = { 0L };
        try {
            Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    sizeArr[0] += attrs.size();
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.error("Error while getting {} folder size. {}", folder, e);
        }
        return sizeArr[0];
    }

    public static void copyTo(DataInput in, OutputStream out, int length) throws IOException {
        byte[] buffer = new byte[64 * 1024];
        int copiedBytes = 0;
        while (copiedBytes + buffer.length < length) {
            in.readFully(buffer);
            out.write(buffer);
            copiedBytes += buffer.length;
        }
        if (copiedBytes < length) {
            int left = length - copiedBytes;
            in.readFully(buffer, 0, left);
            out.write(buffer, 0, left);
        }
    }

    public static boolean isSubDirectory(File parent, File child) throws IOException {
        parent = parent.getCanonicalFile();
        child = child.getCanonicalFile();
        File toCheck = child;
        while (toCheck != null) {
            if (parent.equals(toCheck))
                return true;
            toCheck = toCheck.getParentFile();
        }
        return false;
    }

    public static void append(File file, String... lines) {
        if (file.exists())
            write(file, Arrays.asList(lines), StandardOpenOption.APPEND);
        else
            write(file, Arrays.asList(lines), StandardOpenOption.CREATE);
    }

    public static void appendAndSync(File file, String... lines) {
        if (file.exists())
            write(file, Arrays.asList(lines), StandardOpenOption.APPEND, StandardOpenOption.SYNC);
        else
            write(file, Arrays.asList(lines), StandardOpenOption.CREATE, StandardOpenOption.SYNC);
    }

    public static void replace(File file, String... lines) {
        write(file, Arrays.asList(lines), StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void write(File file, List<String> lines, StandardOpenOption... options) {
        try {
            Files.write(file.toPath(), lines, CHARSET, options);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static List<String> readLines(File file) {
        try {
            return Files.readAllLines(file.toPath(), CHARSET);
        } catch (IOException ex) {
            if (ex instanceof NoSuchFileException)
                return Collections.emptyList();
            throw new RuntimeException(ex);
        }
    }

    public static void setFSErrorHandler(FSErrorHandler handler) {
        fsErrorHandler.getAndSet(Optional.ofNullable(handler));
    }

    public static long getTotalSpace(File file) {
        return handleLargeFileSystem(file.getTotalSpace());
    }

    public static long getFreeSpace(File file) {
        return handleLargeFileSystem(file.getFreeSpace());
    }

    public static long getUsableSpace(File file) {
        return handleLargeFileSystem(file.getUsableSpace());
    }

    public static FileStore getFileStore(Path path) throws IOException {
        return new SafeFileStore(Files.getFileStore(path));
    }

    private static long handleLargeFileSystem(long size) {
        return size < 0 ? Long.MAX_VALUE : size;
    }

    private FileUtils() {
    }

    private static final class SafeFileStore extends FileStore {

        private final FileStore fileStore;

        public SafeFileStore(FileStore fileStore) {
            this.fileStore = fileStore;
        }

        @Override
        public String name() {
            return fileStore.name();
        }

        @Override
        public String type() {
            return fileStore.type();
        }

        @Override
        public boolean isReadOnly() {
            return fileStore.isReadOnly();
        }

        @Override
        public long getTotalSpace() throws IOException {
            return handleLargeFileSystem(fileStore.getTotalSpace());
        }

        @Override
        public long getUsableSpace() throws IOException {
            return handleLargeFileSystem(fileStore.getUsableSpace());
        }

        @Override
        public long getUnallocatedSpace() throws IOException {
            return handleLargeFileSystem(fileStore.getUnallocatedSpace());
        }

        @Override
        public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
            return fileStore.supportsFileAttributeView(type);
        }

        @Override
        public boolean supportsFileAttributeView(String name) {
            return fileStore.supportsFileAttributeView(name);
        }

        @Override
        public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
            return fileStore.getFileStoreAttributeView(type);
        }

        @Override
        public Object getAttribute(String attribute) throws IOException {
            return fileStore.getAttribute(attribute);
        }
    }
}
