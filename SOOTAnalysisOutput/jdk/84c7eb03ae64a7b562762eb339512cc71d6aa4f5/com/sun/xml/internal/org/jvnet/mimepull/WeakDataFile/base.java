package com.sun.xml.internal.org.jvnet.mimepull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
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
            LOGGER.fine("Initializing clean up executor for MIMEPULL: " + executorFactory.getClass().getName());
            Executor executor = executorFactory.getExecutor();
            executor.execute(new Runnable() {

                public void run() {
                    WeakDataFile weak = null;
                    while (true) {
                        try {
                            weak = (WeakDataFile) refQueue.remove();
                            LOGGER.fine("Cleaning file = " + weak.file + " from reference queue.");
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
        LOGGER.fine("Deleting file = " + file.getName());
        refList.remove(this);
        try {
            raf.close();
            file.delete();
        } catch (IOException ioe) {
            throw new MIMEParsingException(ioe);
        }
    }

    void renameTo(File f) {
        LOGGER.fine("Moving file=" + file + " to=" + f);
        refList.remove(this);
        try {
            raf.close();
            file.renameTo(f);
        } catch (IOException ioe) {
            throw new MIMEParsingException(ioe);
        }
    }

    static void drainRefQueueBounded() {
        WeakDataFile weak = null;
        while ((weak = (WeakDataFile) refQueue.poll()) != null) {
            LOGGER.fine("Cleaning file = " + weak.file + " from reference queue.");
            weak.close();
        }
    }
}
