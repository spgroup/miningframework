package java.util.zip;

import java.io.Closeable;
import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;
import java.security.AccessController;
import sun.security.action.GetPropertyAction;
import static java.util.zip.ZipConstants64.*;

public class ZipFile implements ZipConstants, Closeable {

    private long jzfile;

    private final String name;

    private final int total;

    private final boolean locsig;

    private volatile boolean closeRequested = false;

    private static final int STORED = ZipEntry.STORED;

    private static final int DEFLATED = ZipEntry.DEFLATED;

    public static final int OPEN_READ = 0x1;

    public static final int OPEN_DELETE = 0x4;

    static {
        initIDs();
    }

    private static native void initIDs();

    private static final boolean usemmap;

    static {
        String prop = sun.misc.VM.getSavedProperty("sun.zip.disableMemoryMapping");
        usemmap = (prop == null || !(prop.length() == 0 || prop.equalsIgnoreCase("true")));
    }

    public ZipFile(String name) throws IOException {
        this(new File(name), OPEN_READ);
    }

    public ZipFile(File file, int mode) throws IOException {
        this(file, mode, StandardCharsets.UTF_8);
    }

    public ZipFile(File file) throws ZipException, IOException {
        this(file, OPEN_READ);
    }

    private ZipCoder zc;

    public ZipFile(File file, int mode, Charset charset) throws IOException {
        if (((mode & OPEN_READ) == 0) || ((mode & ~(OPEN_READ | OPEN_DELETE)) != 0)) {
            throw new IllegalArgumentException("Illegal mode: 0x" + Integer.toHexString(mode));
        }
        String name = file.getPath();
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkRead(name);
            if ((mode & OPEN_DELETE) != 0) {
                sm.checkDelete(name);
            }
        }
        if (charset == null)
            throw new NullPointerException("charset is null");
        this.zc = ZipCoder.get(charset);
        long t0 = System.nanoTime();
        jzfile = open(name, mode, file.lastModified(), usemmap);
        sun.misc.PerfCounter.getZipFileOpenTime().addElapsedTimeFrom(t0);
        sun.misc.PerfCounter.getZipFileCount().increment();
        this.name = name;
        this.total = getTotal(jzfile);
        this.locsig = startsWithLOC(jzfile);
    }

    public ZipFile(String name, Charset charset) throws IOException {
        this(new File(name), OPEN_READ, charset);
    }

    public ZipFile(File file, Charset charset) throws IOException {
        this(file, OPEN_READ, charset);
    }

    public String getComment() {
        synchronized (this) {
            ensureOpen();
            byte[] bcomm = getCommentBytes(jzfile);
            if (bcomm == null)
                return null;
            return zc.toString(bcomm, bcomm.length);
        }
    }

    public ZipEntry getEntry(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        long jzentry = 0;
        synchronized (this) {
            ensureOpen();
            jzentry = getEntry(jzfile, zc.getBytes(name), true);
            if (jzentry != 0) {
                ZipEntry ze = getZipEntry(name, jzentry);
                freeEntry(jzfile, jzentry);
                return ze;
            }
        }
        return null;
    }

    private static native long getEntry(long jzfile, byte[] name, boolean addSlash);

    private static native void freeEntry(long jzfile, long jzentry);

    private final Map<InputStream, Inflater> streams = new WeakHashMap<>();

    public InputStream getInputStream(ZipEntry entry) throws IOException {
        if (entry == null) {
            throw new NullPointerException("entry");
        }
        long jzentry = 0;
        ZipFileInputStream in = null;
        synchronized (this) {
            ensureOpen();
            if (!zc.isUTF8() && (entry.flag & EFS) != 0) {
                jzentry = getEntry(jzfile, zc.getBytesUTF8(entry.name), false);
            } else {
                jzentry = getEntry(jzfile, zc.getBytes(entry.name), false);
            }
            if (jzentry == 0) {
                return null;
            }
            in = new ZipFileInputStream(jzentry);
            switch(getEntryMethod(jzentry)) {
                case STORED:
                    synchronized (streams) {
                        streams.put(in, null);
                    }
                    return in;
                case DEFLATED:
                    long size = getEntrySize(jzentry) + 2;
                    if (size > 65536)
                        size = 8192;
                    if (size <= 0)
                        size = 4096;
                    Inflater inf = getInflater();
                    InputStream is = new ZipFileInflaterInputStream(in, inf, (int) size);
                    synchronized (streams) {
                        streams.put(is, inf);
                    }
                    return is;
                default:
                    throw new ZipException("invalid compression method");
            }
        }
    }

    private class ZipFileInflaterInputStream extends InflaterInputStream {

        private volatile boolean closeRequested = false;

        private boolean eof = false;

        private final ZipFileInputStream zfin;

        ZipFileInflaterInputStream(ZipFileInputStream zfin, Inflater inf, int size) {
            super(zfin, inf, size);
            this.zfin = zfin;
        }

        public void close() throws IOException {
            if (closeRequested)
                return;
            closeRequested = true;
            super.close();
            Inflater inf;
            synchronized (streams) {
                inf = streams.remove(this);
            }
            if (inf != null) {
                releaseInflater(inf);
            }
        }

        protected void fill() throws IOException {
            if (eof) {
                throw new EOFException("Unexpected end of ZLIB input stream");
            }
            len = in.read(buf, 0, buf.length);
            if (len == -1) {
                buf[0] = 0;
                len = 1;
                eof = true;
            }
            inf.setInput(buf, 0, len);
        }

        public int available() throws IOException {
            if (closeRequested)
                return 0;
            long avail = zfin.size() - inf.getBytesWritten();
            return (avail > (long) Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) avail);
        }

        protected void finalize() throws Throwable {
            close();
        }
    }

    private Inflater getInflater() {
        Inflater inf;
        synchronized (inflaterCache) {
            while (null != (inf = inflaterCache.poll())) {
                if (false == inf.ended()) {
                    return inf;
                }
            }
        }
        return new Inflater(true);
    }

    private void releaseInflater(Inflater inf) {
        if (false == inf.ended()) {
            inf.reset();
            synchronized (inflaterCache) {
                inflaterCache.add(inf);
            }
        }
    }

    private Deque<Inflater> inflaterCache = new ArrayDeque<>();

    public String getName() {
        return name;
    }

    public Enumeration<? extends ZipEntry> entries() {
        ensureOpen();
        return new Enumeration<ZipEntry>() {

            private int i = 0;

            public boolean hasMoreElements() {
                synchronized (ZipFile.this) {
                    ensureOpen();
                    return i < total;
                }
            }

            public ZipEntry nextElement() throws NoSuchElementException {
                synchronized (ZipFile.this) {
                    ensureOpen();
                    if (i >= total) {
                        throw new NoSuchElementException();
                    }
                    long jzentry = getNextEntry(jzfile, i++);
                    if (jzentry == 0) {
                        String message;
                        if (closeRequested) {
                            message = "ZipFile concurrently closed";
                        } else {
                            message = getZipMessage(ZipFile.this.jzfile);
                        }
                        throw new ZipError("jzentry == 0" + ",\n jzfile = " + ZipFile.this.jzfile + ",\n total = " + ZipFile.this.total + ",\n name = " + ZipFile.this.name + ",\n i = " + i + ",\n message = " + message);
                    }
                    ZipEntry ze = getZipEntry(null, jzentry);
                    freeEntry(jzfile, jzentry);
                    return ze;
                }
            }
        };
    }

    private ZipEntry getZipEntry(String name, long jzentry) {
        ZipEntry e = new ZipEntry();
        e.flag = getEntryFlag(jzentry);
        if (name != null) {
            e.name = name;
        } else {
            byte[] bname = getEntryBytes(jzentry, JZENTRY_NAME);
            if (!zc.isUTF8() && (e.flag & EFS) != 0) {
                e.name = zc.toStringUTF8(bname, bname.length);
            } else {
                e.name = zc.toString(bname, bname.length);
            }
        }
        e.time = getEntryTime(jzentry);
        e.crc = getEntryCrc(jzentry);
        e.size = getEntrySize(jzentry);
        e.csize = getEntryCSize(jzentry);
        e.method = getEntryMethod(jzentry);
        e.extra = getEntryBytes(jzentry, JZENTRY_EXTRA);
        byte[] bcomm = getEntryBytes(jzentry, JZENTRY_COMMENT);
        if (bcomm == null) {
            e.comment = null;
        } else {
            if (!zc.isUTF8() && (e.flag & EFS) != 0) {
                e.comment = zc.toStringUTF8(bcomm, bcomm.length);
            } else {
                e.comment = zc.toString(bcomm, bcomm.length);
            }
        }
        return e;
    }

    private static native long getNextEntry(long jzfile, int i);

    public int size() {
        ensureOpen();
        return total;
    }

    public void close() throws IOException {
        if (closeRequested)
            return;
        closeRequested = true;
        synchronized (this) {
            synchronized (streams) {
                if (false == streams.isEmpty()) {
                    Map<InputStream, Inflater> copy = new HashMap<>(streams);
                    streams.clear();
                    for (Map.Entry<InputStream, Inflater> e : copy.entrySet()) {
                        e.getKey().close();
                        Inflater inf = e.getValue();
                        if (inf != null) {
                            inf.end();
                        }
                    }
                }
            }
            Inflater inf;
            synchronized (inflaterCache) {
                while (null != (inf = inflaterCache.poll())) {
                    inf.end();
                }
            }
            if (jzfile != 0) {
                long zf = this.jzfile;
                jzfile = 0;
                close(zf);
            }
        }
    }

    protected void finalize() throws IOException {
        close();
    }

    private static native void close(long jzfile);

    private void ensureOpen() {
        if (closeRequested) {
            throw new IllegalStateException("zip file closed");
        }
        if (jzfile == 0) {
            throw new IllegalStateException("The object is not initialized.");
        }
    }

    private void ensureOpenOrZipException() throws IOException {
        if (closeRequested) {
            throw new ZipException("ZipFile closed");
        }
    }

    private class ZipFileInputStream extends InputStream {

        private volatile boolean closeRequested = false;

        protected long jzentry;

        private long pos;

        protected long rem;

        protected long size;

        ZipFileInputStream(long jzentry) {
            pos = 0;
            rem = getEntryCSize(jzentry);
            size = getEntrySize(jzentry);
            this.jzentry = jzentry;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (rem == 0) {
                return -1;
            }
            if (len <= 0) {
                return 0;
            }
            if (len > rem) {
                len = (int) rem;
            }
            synchronized (ZipFile.this) {
                ensureOpenOrZipException();
                len = ZipFile.read(ZipFile.this.jzfile, jzentry, pos, b, off, len);
            }
            if (len > 0) {
                pos += len;
                rem -= len;
            }
            if (rem == 0) {
                close();
            }
            return len;
        }

        public int read() throws IOException {
            byte[] b = new byte[1];
            if (read(b, 0, 1) == 1) {
                return b[0] & 0xff;
            } else {
                return -1;
            }
        }

        public long skip(long n) {
            if (n > rem)
                n = rem;
            pos += n;
            rem -= n;
            if (rem == 0) {
                close();
            }
            return n;
        }

        public int available() {
            return rem > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) rem;
        }

        public long size() {
            return size;
        }

        public void close() {
            if (closeRequested)
                return;
            closeRequested = true;
            rem = 0;
            synchronized (ZipFile.this) {
                if (jzentry != 0 && ZipFile.this.jzfile != 0) {
                    freeEntry(ZipFile.this.jzfile, jzentry);
                    jzentry = 0;
                }
            }
            synchronized (streams) {
                streams.remove(this);
            }
        }

        protected void finalize() {
            close();
        }
    }

    static {
        sun.misc.SharedSecrets.setJavaUtilZipFileAccess(new sun.misc.JavaUtilZipFileAccess() {

            public boolean startsWithLocHeader(ZipFile zip) {
                return zip.startsWithLocHeader();
            }
        });
    }

    private boolean startsWithLocHeader() {
        return locsig;
    }

    private static native long open(String name, int mode, long lastModified, boolean usemmap) throws IOException;

    private static native int getTotal(long jzfile);

    private static native boolean startsWithLOC(long jzfile);

    private static native int read(long jzfile, long jzentry, long pos, byte[] b, int off, int len);

    private static native long getEntryTime(long jzentry);

    private static native long getEntryCrc(long jzentry);

    private static native long getEntryCSize(long jzentry);

    private static native long getEntrySize(long jzentry);

    private static native int getEntryMethod(long jzentry);

    private static native int getEntryFlag(long jzentry);

    private static native byte[] getCommentBytes(long jzfile);

    private static final int JZENTRY_NAME = 0;

    private static final int JZENTRY_EXTRA = 1;

    private static final int JZENTRY_COMMENT = 2;

    private static native byte[] getEntryBytes(long jzentry, int type);

    private static native String getZipMessage(long jzfile);
}
