package eu.stratosphere.nephele.fs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import eu.stratosphere.nephele.util.ClassUtils;
import eu.stratosphere.nephele.util.StringUtils;

public abstract class FileSystem {

    private static final String DISTRIBUTED_FILESYSTEM_CLASS = "eu.stratosphere.nephele.fs.hdfs.DistributedFileSystem";

    private static final String LOCAL_FILESYSTEM_CLASS = "eu.stratosphere.nephele.fs.file.LocalFileSystem";

    private static final Object synchronizationObject = new Object();

    public static class FSKey {

        private String scheme;

        private String authority;

        public FSKey(String scheme, String authority) {
            this.scheme = scheme;
            this.authority = authority;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof FSKey) {
                final FSKey key = (FSKey) obj;
                if (!this.scheme.equals(key.scheme)) {
                    return false;
                }
                if ((this.authority == null) || (key.authority == null)) {
                    if (this.authority == null && key.authority == null) {
                        return true;
                    }
                    return false;
                }
                if (!this.authority.equals(key.authority)) {
                    return false;
                }
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            if (this.scheme != null) {
                return this.scheme.hashCode();
            }
            if (this.authority != null) {
                return this.authority.hashCode();
            }
            return super.hashCode();
        }
    }

    private final static Map<FSKey, FileSystem> CACHE = new HashMap<FSKey, FileSystem>();

    private final static Map<String, String> FSDIRECTORY = new HashMap<String, String>();

    static {
        FSDIRECTORY.put("hdfs", DISTRIBUTED_FILESYSTEM_CLASS);
        FSDIRECTORY.put("file", LOCAL_FILESYSTEM_CLASS);
    }

    public static FileSystem getLocalFileSystem() throws IOException {
        URI localUri;
        try {
            localUri = new URI("file:///");
        } catch (URISyntaxException e) {
            throw new IOException("Cannot create URI for local file system");
        }
        return get(localUri);
    }

    public static FileSystem get(URI uri) throws IOException {
        FileSystem fs = null;
        synchronized (synchronizationObject) {
            if (uri.getScheme() == null) {
                throw new IOException("FileSystem: Scheme is null");
            }
            final FSKey key = new FSKey(uri.getScheme(), uri.getAuthority());
            if (CACHE.containsKey(key)) {
                return CACHE.get(key);
            }
            if (!FSDIRECTORY.containsKey(uri.getScheme())) {
                throw new IOException("No file system found with scheme " + uri.getScheme());
            }
            Class<? extends FileSystem> fsClass = null;
            try {
                fsClass = ClassUtils.getFileSystemByName(FSDIRECTORY.get(uri.getScheme()));
            } catch (ClassNotFoundException e1) {
                throw new IOException(StringUtils.stringifyException(e1));
            }
            try {
                fs = fsClass.newInstance();
            } catch (InstantiationException e) {
                throw new IOException(StringUtils.stringifyException(e));
            } catch (IllegalAccessException e) {
                throw new IOException(StringUtils.stringifyException(e));
            }
            fs.initialize(uri);
            CACHE.put(key, fs);
        }
        return fs;
    }

    public abstract Path getWorkingDirectory();

    public abstract URI getUri();

    public abstract void initialize(URI name) throws IOException;

    public abstract FileStatus getFileStatus(Path f) throws IOException;

    public abstract BlockLocation[] getFileBlockLocations(FileStatus file, long start, long len) throws IOException;

    public abstract FSDataInputStream open(Path f, int bufferSize) throws IOException;

    public abstract FSDataInputStream open(Path f) throws IOException;

    public long getDefaultBlockSize() {
        return 32 * 1024 * 1024;
    }

    public abstract FileStatus[] listStatus(Path f) throws IOException;

    public boolean exists(Path f) throws IOException {
        try {
            return (getFileStatus(f) != null);
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    public abstract boolean delete(Path f, boolean recursive) throws IOException;

    public abstract boolean mkdirs(Path f) throws IOException;

    public abstract FSDataOutputStream create(Path f, boolean overwrite, int bufferSize, short replication, long blockSize) throws IOException;

    public abstract FSDataOutputStream create(Path f, boolean overwrite) throws IOException;

    public int getNumberOfBlocks(FileStatus file) throws IOException {
        int numberOfBlocks = 0;
        if (file == null) {
            return 0;
        }
        if (!file.isDir()) {
            return getNumberOfBlocks(file.getLen(), file.getBlockSize());
        }
        FileStatus[] files = this.listStatus(file.getPath());
        for (int i = 0; i < files.length; i++) {
            if (!files[i].isDir()) {
                numberOfBlocks += getNumberOfBlocks(files[i].getLen(), files[i].getBlockSize());
            }
        }
        return numberOfBlocks;
    }

    private int getNumberOfBlocks(long length, long blocksize) {
        if (blocksize != 0) {
            int numberOfBlocks;
            numberOfBlocks = (int) (length / blocksize);
            if ((length % blocksize) != 0) {
                numberOfBlocks++;
            }
            return numberOfBlocks;
        } else {
            return 1;
        }
    }
}
