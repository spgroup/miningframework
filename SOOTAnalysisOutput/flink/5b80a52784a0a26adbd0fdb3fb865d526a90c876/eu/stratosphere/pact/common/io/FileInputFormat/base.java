package eu.stratosphere.pact.common.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.nephele.configuration.GlobalConfiguration;
import eu.stratosphere.nephele.fs.BlockLocation;
import eu.stratosphere.nephele.fs.FSDataInputStream;
import eu.stratosphere.nephele.fs.FileInputSplit;
import eu.stratosphere.nephele.fs.FileStatus;
import eu.stratosphere.nephele.fs.FileSystem;
import eu.stratosphere.nephele.fs.Path;
import eu.stratosphere.pact.common.generic.io.InputFormat;
import eu.stratosphere.pact.common.io.statistics.BaseStatistics;
import eu.stratosphere.pact.common.type.PactRecord;
import eu.stratosphere.pact.common.util.PactConfigConstants;

public abstract class FileInputFormat implements InputFormat<PactRecord, FileInputSplit> {

    private static final Log LOG = LogFactory.getLog(FileInputFormat.class);

    static final long DEFAULT_OPENING_TIMEOUT;

    static {
        final long to = GlobalConfiguration.getLong(PactConfigConstants.FS_STREAM_OPENING_TIMEOUT_KEY, PactConfigConstants.DEFAULT_FS_STREAM_OPENING_TIMEOUT);
        if (to < 0) {
            LOG.error("Invalid timeout value for filesystem stream opening: " + to + ". Using default value of " + PactConfigConstants.DEFAULT_FS_STREAM_OPENING_TIMEOUT);
            DEFAULT_OPENING_TIMEOUT = PactConfigConstants.DEFAULT_FS_STREAM_OPENING_TIMEOUT;
        } else if (to == 0) {
            DEFAULT_OPENING_TIMEOUT = Long.MAX_VALUE;
        } else {
            DEFAULT_OPENING_TIMEOUT = to;
        }
    }

    public static final String FILE_PARAMETER_KEY = "pact.input.file.path";

    public static final String DESIRED_NUMBER_OF_SPLITS_PARAMETER_KEY = "pact.input.file.numsplits";

    public static final String MINIMAL_SPLIT_SIZE_PARAMETER_KEY = "pact.input.file.minsplitsize";

    public static final String INPUT_STREAM_OPEN_TIMEOUT = "pact.input.file.timeout";

    private static final float MAX_SPLIT_SIZE_DISCREPANCY = 1.1f;

    protected Path filePath;

    protected FSDataInputStream stream;

    protected long splitStart;

    protected long splitLength;

    protected long minSplitSize;

    protected int numSplits;

    private long openTimeout;

    @Override
    public void configure(Configuration parameters) {
        String filePath = parameters.getString(FILE_PARAMETER_KEY, null);
        if (filePath == null) {
            throw new IllegalArgumentException("Configuration file FileInputFormat does not contain the file path.");
        }
        try {
            this.filePath = new Path(filePath);
        } catch (RuntimeException rex) {
            throw new RuntimeException("Could not create a valid URI from the given file path name: " + rex.getMessage());
        }
        this.numSplits = parameters.getInteger(DESIRED_NUMBER_OF_SPLITS_PARAMETER_KEY, -1);
        if (this.numSplits == 0 || this.numSplits < -1) {
            this.numSplits = -1;
            if (LOG.isWarnEnabled())
                LOG.warn("Ignoring invalid parameter for number of splits: " + this.numSplits);
        }
        this.minSplitSize = parameters.getLong(MINIMAL_SPLIT_SIZE_PARAMETER_KEY, 1);
        if (this.minSplitSize < 1) {
            this.minSplitSize = 1;
            if (LOG.isWarnEnabled())
                LOG.warn("Ignoring invalid parameter for minimal split size (requires a positive value): " + this.numSplits);
        }
        this.openTimeout = parameters.getLong(INPUT_STREAM_OPEN_TIMEOUT, DEFAULT_OPENING_TIMEOUT);
        if (this.openTimeout < 0) {
            this.openTimeout = DEFAULT_OPENING_TIMEOUT;
            if (LOG.isWarnEnabled())
                LOG.warn("Ignoring invalid parameter for stream opening timeout (requires a positive value or zero=infinite): " + this.openTimeout);
        } else if (this.openTimeout == 0) {
            this.openTimeout = Long.MAX_VALUE;
        }
    }

    @Override
    public Class<FileInputSplit> getInputSplitType() {
        return FileInputSplit.class;
    }

    @Override
    public FileInputSplit[] createInputSplits(int minNumSplits) throws IOException {
        minNumSplits = Math.max(minNumSplits, this.numSplits);
        final Path path = this.filePath;
        final List<FileInputSplit> inputSplits = new ArrayList<FileInputSplit>(minNumSplits);
        List<FileStatus> files = new ArrayList<FileStatus>();
        long totalLength = 0;
        final FileSystem fs = path.getFileSystem();
        final FileStatus pathFile = fs.getFileStatus(path);
        if (pathFile.isDir()) {
            final FileStatus[] dir = fs.listStatus(path);
            for (int i = 0; i < dir.length; i++) {
                if (!dir[i].isDir()) {
                    files.add(dir[i]);
                    totalLength += dir[i].getLen();
                }
            }
        } else {
            files.add(pathFile);
            totalLength += pathFile.getLen();
        }
        final long maxSplitSize = (minNumSplits < 1) ? Long.MAX_VALUE : (totalLength / minNumSplits + (totalLength % minNumSplits == 0 ? 0 : 1));
        int splitNum = 0;
        for (final FileStatus file : files) {
            final long len = file.getLen();
            final long blockSize = file.getBlockSize();
            final long minSplitSize;
            if (this.minSplitSize <= blockSize) {
                minSplitSize = this.minSplitSize;
            } else {
                if (LOG.isWarnEnabled())
                    LOG.warn("Minimal split size of " + this.minSplitSize + " is larger than the block size of " + blockSize + ". Decreasing minimal split size to block size.");
                minSplitSize = blockSize;
            }
            final long splitSize = Math.max(minSplitSize, Math.min(maxSplitSize, blockSize));
            final long halfSplit = splitSize >>> 1;
            final long maxBytesForLastSplit = (long) (splitSize * MAX_SPLIT_SIZE_DISCREPANCY);
            if (len > 0) {
                final BlockLocation[] blocks = fs.getFileBlockLocations(file, 0, len);
                Arrays.sort(blocks);
                long bytesUnassigned = len;
                long position = 0;
                int blockIndex = 0;
                while (bytesUnassigned > maxBytesForLastSplit) {
                    blockIndex = getBlockIndexForPosition(blocks, position, halfSplit, blockIndex);
                    FileInputSplit fis = new FileInputSplit(splitNum++, file.getPath(), position, splitSize, blocks[blockIndex].getHosts());
                    inputSplits.add(fis);
                    position += splitSize;
                    bytesUnassigned -= splitSize;
                }
                if (bytesUnassigned > 0) {
                    blockIndex = getBlockIndexForPosition(blocks, position, halfSplit, blockIndex);
                    final FileInputSplit fis = new FileInputSplit(splitNum++, file.getPath(), position, bytesUnassigned, blocks[blockIndex].getHosts());
                    inputSplits.add(fis);
                }
            } else {
                final BlockLocation[] blocks = fs.getFileBlockLocations(file, 0, 0);
                String[] hosts;
                if (blocks.length > 0) {
                    hosts = blocks[0].getHosts();
                } else {
                    hosts = new String[0];
                }
                final FileInputSplit fis = new FileInputSplit(splitNum++, file.getPath(), 0, 0, hosts);
                inputSplits.add(fis);
            }
        }
        return inputSplits.toArray(new FileInputSplit[inputSplits.size()]);
    }

    private final int getBlockIndexForPosition(BlockLocation[] blocks, long offset, long halfSplitSize, int startIndex) {
        for (int i = startIndex; i < blocks.length; i++) {
            long blockStart = blocks[i].getOffset();
            long blockEnd = blockStart + blocks[i].getLength();
            if (offset >= blockStart && offset < blockEnd) {
                if (i < blocks.length - 1 && blockEnd - offset < halfSplitSize) {
                    return i + 1;
                } else {
                    return i;
                }
            }
        }
        throw new IllegalArgumentException("The given offset is not contained in the any block.");
    }

    @Override
    public void open(FileInputSplit split) throws IOException {
        if (!(split instanceof FileInputSplit)) {
            throw new IllegalArgumentException("File Input Formats can only be used with FileInputSplits.");
        }
        final FileInputSplit fileSplit = (FileInputSplit) split;
        this.splitStart = fileSplit.getStart();
        this.splitLength = fileSplit.getLength();
        if (LOG.isDebugEnabled())
            LOG.debug("Opening input split " + fileSplit.getPath() + " [" + this.splitStart + "," + this.splitLength + "]");
        final InputSplitOpenThread isot = new InputSplitOpenThread(fileSplit, this.openTimeout);
        isot.start();
        try {
            this.stream = isot.waitForCompletion();
        } catch (Throwable t) {
            throw new IOException("Error opening the Input Split " + fileSplit.getPath() + " [" + splitStart + "," + splitLength + "]: " + t.getMessage(), t);
        }
        this.stream.seek(this.splitStart);
    }

    @Override
    public void close() throws IOException {
        if (this.stream != null) {
            this.stream.close();
        }
    }

    public String toString() {
        return this.filePath == null ? "File Input (unknown file)" : "File Input (" + this.filePath.toString() + ')';
    }

    public static class FileBaseStatistics implements BaseStatistics {

        protected long fileModTime;

        protected long fileSize;

        protected float avgBytesPerRecord;

        public FileBaseStatistics(long fileModTime, long fileSize, float avgBytesPerRecord) {
            this.fileModTime = fileModTime;
            this.fileSize = fileSize;
            this.avgBytesPerRecord = avgBytesPerRecord;
        }

        public long getLastModificationTime() {
            return fileModTime;
        }

        public void setLastModificationTime(long modificationTime) {
            this.fileModTime = modificationTime;
        }

        @Override
        public long getTotalInputSize() {
            return this.fileSize;
        }

        public void setTotalInputSize(long fileSize) {
            this.fileSize = fileSize;
        }

        @Override
        public long getNumberOfRecords() {
            return (long) Math.ceil(this.fileSize / this.avgBytesPerRecord);
        }

        public void setAverageRecordWidth(float avgBytesPerRecord) {
            this.avgBytesPerRecord = avgBytesPerRecord;
        }

        @Override
        public float getAverageRecordWidth() {
            return this.avgBytesPerRecord;
        }
    }

    public static class InputSplitOpenThread extends Thread {

        private final FileInputSplit split;

        private final long timeout;

        private volatile FSDataInputStream fdis;

        private volatile Throwable error;

        private volatile boolean aborted;

        public InputSplitOpenThread(FileInputSplit split, long timeout) {
            super("Transient InputSplit Opener");
            setDaemon(true);
            this.split = split;
            this.timeout = timeout;
        }

        @Override
        public void run() {
            try {
                final FileSystem fs = FileSystem.get(this.split.getPath().toUri());
                this.fdis = fs.open(this.split.getPath());
                if (this.aborted) {
                    final FSDataInputStream f = this.fdis;
                    this.fdis = null;
                    f.close();
                }
            } catch (Throwable t) {
                this.error = t;
            }
        }

        public FSDataInputStream waitForCompletion() throws Throwable {
            final long start = System.currentTimeMillis();
            long remaining = this.timeout;
            do {
                try {
                    this.join(remaining);
                } catch (InterruptedException iex) {
                    abortWait();
                    throw iex;
                }
            } while (this.error == null && this.fdis == null && (remaining = this.timeout + start - System.currentTimeMillis()) > 0);
            if (this.error != null) {
                throw this.error;
            }
            if (this.fdis != null) {
                return this.fdis;
            } else {
                abortWait();
                final boolean stillAlive = this.isAlive();
                final StringBuilder bld = new StringBuilder(256);
                for (StackTraceElement e : this.getStackTrace()) {
                    bld.append("\tat ").append(e.toString()).append('\n');
                }
                throw new IOException("Input opening request timed out. Opener was " + (stillAlive ? "" : "NOT ") + " alive. Stack:\n" + bld.toString());
            }
        }

        private final void abortWait() {
            this.aborted = true;
            final FSDataInputStream inStream = this.fdis;
            this.fdis = null;
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Throwable t) {
                }
            }
        }
    }
}
