package org.apache.accumulo.start.classloader.vfs;

import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.apache.commons.vfs2.impl.VFSClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccumuloReloadingVFSClassLoader implements FileListener, ReloadingClassLoader {

    private static final Logger log = LoggerFactory.getLogger(AccumuloReloadingVFSClassLoader.class);

    private static final int DEFAULT_TIMEOUT = 5 * 60 * 1000;

    private FileObject[] files;

    private VFSClassLoader cl;

    private final ReloadingClassLoader parent;

    private final String uris;

    private final DefaultFileMonitor monitor;

    private final boolean preDelegate;

    private final ThreadPoolExecutor executor;

    {
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(2);
        ThreadFactory factory = new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        };
        executor = new ThreadPoolExecutor(1, 1, 1, SECONDS, queue, factory);
    }

    private final Runnable refresher = new Runnable() {

        @Override
        public void run() {
            while (!executor.isTerminating()) {
                try {
                    FileSystemManager vfs = AccumuloVFSClassLoader.generateVfs();
                    FileObject[] files = AccumuloVFSClassLoader.resolve(vfs, uris);
                    log.debug("Rebuilding dynamic classloader using files- " + stringify(files));
                    VFSClassLoader cl;
                    if (preDelegate)
                        cl = new VFSClassLoader(files, vfs, parent.getClassLoader());
                    else
                        cl = new PostDelegatingVFSClassLoader(files, vfs, parent.getClassLoader());
                    updateClassloader(files, cl);
                    return;
                } catch (Exception e) {
                    log.error("{}", e.getMessage(), e);
                    try {
                        Thread.sleep(DEFAULT_TIMEOUT);
                    } catch (InterruptedException ie) {
                        log.error("{}", e.getMessage(), ie);
                    }
                }
            }
        }
    };

    public String stringify(FileObject[] files) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        String delim = "";
        for (FileObject file : files) {
            sb.append(delim);
            delim = ", ";
            sb.append(file.getName());
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public synchronized ClassLoader getClassLoader() {
        if (cl.getParent() != parent.getClassLoader()) {
            scheduleRefresh();
        }
        return cl;
    }

    private void scheduleRefresh() {
        try {
            executor.execute(refresher);
        } catch (RejectedExecutionException e) {
            log.trace("Ignoring refresh request (already refreshing)");
        }
    }

    private synchronized void updateClassloader(FileObject[] files, VFSClassLoader cl) {
        this.files = files;
        this.cl = cl;
    }

    public AccumuloReloadingVFSClassLoader(String uris, FileSystemManager vfs, ReloadingClassLoader parent, long monitorDelay, boolean preDelegate) throws FileSystemException {
        this.uris = uris;
        this.parent = parent;
        this.preDelegate = preDelegate;
        ArrayList<FileObject> pathsToMonitor = new ArrayList<FileObject>();
        files = AccumuloVFSClassLoader.resolve(vfs, uris, pathsToMonitor);
        if (preDelegate)
            cl = new VFSClassLoader(files, vfs, parent.getClassLoader());
        else
            cl = new PostDelegatingVFSClassLoader(files, vfs, parent.getClassLoader());
        monitor = new DefaultFileMonitor(this);
        monitor.setDelay(monitorDelay);
        monitor.setRecursive(false);
        for (FileObject file : pathsToMonitor) {
            monitor.addFile(file);
            log.debug("monitoring " + file);
        }
        monitor.start();
    }

    public AccumuloReloadingVFSClassLoader(String uris, FileSystemManager vfs, final ReloadingClassLoader parent, boolean preDelegate) throws FileSystemException {
        this(uris, vfs, parent, DEFAULT_TIMEOUT, preDelegate);
    }

    synchronized public FileObject[] getFiles() {
        return Arrays.copyOf(this.files, this.files.length);
    }

    public void close() {
        executor.shutdownNow();
        monitor.stop();
    }

    @Override
    public void fileCreated(FileChangeEvent event) throws Exception {
        if (log.isDebugEnabled())
            log.debug(event.getFile().getURL().toString() + " created, recreating classloader");
        scheduleRefresh();
    }

    @Override
    public void fileDeleted(FileChangeEvent event) throws Exception {
        if (log.isDebugEnabled())
            log.debug(event.getFile().getURL().toString() + " deleted, recreating classloader");
        scheduleRefresh();
    }

    @Override
    public void fileChanged(FileChangeEvent event) throws Exception {
        if (log.isDebugEnabled())
            log.debug(event.getFile().getURL().toString() + " changed, recreating classloader");
        scheduleRefresh();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (FileObject f : files) {
            try {
                buf.append("\t").append(f.getURL().toString()).append("\n");
            } catch (FileSystemException e) {
                log.error("Error getting URL for file", e);
            }
        }
        return buf.toString();
    }
}
