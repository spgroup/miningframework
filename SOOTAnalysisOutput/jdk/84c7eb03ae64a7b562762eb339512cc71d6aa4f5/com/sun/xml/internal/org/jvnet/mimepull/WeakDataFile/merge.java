package com.sun.xml.internal.org.jvnet.mimepull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

final class WeakDataFile extends WeakReference<DataFile> {

    private static final Logger LOGGER = Logger.getLogger(WeakDataFile.class.getName());

    private static ReferenceQueue<DataFile> refQueue = new ReferenceQueue<DataFile>();

    private static List<WeakDataFile> refList = new ArrayList<WeakDataFile>();

    private final File file;

    private final RandomAccessFile raf;

    private static boolean hasCleanUpExecutor = false;

    static {
        CleanUpExecutorFactory executorFactory = CleanUpExecutorFactory.newInstance();
        if (executorFactory != null) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Initializing clean up executor for MIMEPULL: {0}", executorFactory.getClass().getName());
            }
            Executor executor = executorFactory.getExecutor();
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    WeakDataFile weak;
                    while (true) {
                        try {
                            weak = (WeakDataFile) refQueue.remove();
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.log(Level.FINE, "Cleaning file = {0} from reference queue.", weak.file);
                            }
                            weak.close();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            });
            hasCleanUpExecutor = true;
        }
    }

    WeakDataFile(DataFile df, File file) {
        super(df, refQueue);
        refList.add(this);
        this.file = file;
        try {
            raf = new RandomAccessFile(file, "rw");
        } catch (IOException ioe) {
            throw new MIMEParsingException(ioe);
        }
        if (!hasCleanUpExecutor) {
            drainRefQueueBounded();
        }
    }

    synchronized void read(long pointer, byte[] buf, int offset, int length) {
        try {
            raf.seek(pointer);
            raf.readFully(buf, offset, length);
        } catch (IOException ioe) {
            throw new MIMEParsingException(ioe);
        }
    }

    synchronized long writeTo(long pointer, byte[] data, int offset, int length) {
        try {
            raf.seek(pointer);
            raf.write(data, offset, length);
            return raf.getFilePointer();
        } catch (IOException ioe) {
            throw new MIMEParsingException(ioe);
        }
    }

    void close() {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Deleting file = {0}", file.getName());
        }
        refList.remove(this);
        try {
            raf.close();
            boolean deleted = file.delete();
            if (!deleted) {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.log(Level.INFO, "File {0} was not deleted", file.getAbsolutePath());
                }
            }
        } catch (IOException ioe) {
            throw new MIMEParsingException(ioe);
        }
    }

    void renameTo(File f) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Moving file={0} to={1}", new Object[] { file, f });
        }
        refList.remove(this);
        try {
            raf.close();
            boolean renamed = file.renameTo(f);
            if (!renamed) {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.log(Level.INFO, "File {0} was not moved to {1}", new Object[] { file.getAbsolutePath(), f.getAbsolutePath() });
                }
            }
        } catch (IOException ioe) {
            throw new MIMEParsingException(ioe);
        }
    }

    static void drainRefQueueBounded() {
        WeakDataFile weak;
        while ((weak = (WeakDataFile) refQueue.poll()) != null) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Cleaning file = {0} from reference queue.", weak.file);
            }
            weak.close();
        }
    }
}
