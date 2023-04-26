package java.util.zip;

import java.io.Closeable;
import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.jar.JarEntry;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import jdk.internal.misc.JavaUtilZipFileAccess;
import jdk.internal.misc.SharedSecrets;
import jdk.internal.misc.VM;
import jdk.internal.perf.PerfCounter;
import static java.util.zip.ZipConstants.*;
import static java.util.zip.ZipConstants64.*;
import static java.util.zip.ZipUtils.*;

public class ZipFile implements ZipConstants, Closeable {

    private final String name;

    private volatile boolean closeRequested;

    private Source zsrc;

    private ZipCoder zc;

    private static final int STORED = ZipEntry.STORED;

    private static final int DEFLATED = ZipEntry.DEFLATED;

    public static final int OPEN_READ = 0x1;

    public static final int OPEN_DELETE = 0x4;

    public ZipFile(String name) throws IOException {
        this(new File(name), OPEN_READ);
    }

    public ZipFile(File file, int mode) throws IOException {
        this(file, mode, StandardCharsets.UTF_8);
    }

    public ZipFile(File file) throws ZipException, IOException {
        this(file, OPEN_READ);
    }

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
        Objects.requireNonNull(charset, "charset");
        this.zc = ZipCoder.get(charset);
        this.name = name;
        long t0 = System.nanoTime();
        this.zsrc = Source.get(file, (mode & OPEN_DELETE) != 0);
        PerfCounter.getZipFileOpenTime().addElapsedTimeFrom(t0);
        PerfCounter.getZipFileCount().increment();
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
            if (zsrc.comment == null) {
                return null;
            }
            return zc.toString(zsrc.comment);
        }
    }

    public ZipEntry getEntry(String name) {
        return getEntry(name, ZipEntry::new);
    }

    private ZipEntry getEntry(String name, Function<String, ? extends ZipEntry> func) {
        Objects.requireNonNull(name, "name");
        synchronized (this) {
            ensureOpen();
            byte[] bname = zc.getBytes(name);
            int pos = zsrc.getEntryPos(bname, true);
            if (pos != -1) {
                return getZipEntry(name, bname, pos, func);
            }
        }
        return null;
    }

    private final Map<InputStream, Inflater> streams = new WeakHashMap<>();

    public InputStream getInputStream(ZipEntry entry) throws IOException {
        Objects.requireNonNull(entry, "entry");
        int pos = -1;
        ZipFileInputStream in = null;
        synchronized (this) {
            ensureOpen();
            if (Objects.equals(lastEntryName, entry.name)) {
                pos = lastEntryPos;
            } else if (!zc.isUTF8() && (entry.flag & EFS) != 0) {
                pos = zsrc.getEntryPos(zc.getBytesUTF8(entry.name), false);
            } else {
                pos = zsrc.getEntryPos(zc.getBytes(entry.name), false);
            }
            if (pos == -1) {
                return null;
            }
            in = new ZipFileInputStream(zsrc.cen, pos);
            switch(CENHOW(zsrc.cen, pos)) {
                case STORED:
                    synchronized (streams) {
                        streams.put(in, null);
                    }
                    return in;
                case DEFLATED:
                    long size = CENLEN(zsrc.cen, pos) + 2;
                    if (size > 65536) {
                        size = 8192;
                    }
                    if (size <= 0) {
                        size = 4096;
                    }
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

        private volatile boolean closeRequested;

        private boolean eof = false;

        ZipFileInflaterInputStream(ZipFileInputStream zfin, Inflater inf, int size) {
            super(zfin, inf, size);
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
            long avail = ((ZipFileInputStream) in).size() - inf.getBytesWritten();
            return (avail > (long) Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) avail);
        }

        @SuppressWarnings("deprecation")
        protected void finalize() throws Throwable {
            close();
        }
    }

    private Inflater getInflater() {
        Inflater inf;
        synchronized (inflaterCache) {
            while ((inf = inflaterCache.poll()) != null) {
                if (!inf.ended()) {
                    return inf;
                }
            }
        }
        return new Inflater(true);
    }

    private void releaseInflater(Inflater inf) {
        if (!inf.ended()) {
            inf.reset();
            synchronized (inflaterCache) {
                inflaterCache.add(inf);
            }
        }
    }

    private final Deque<Inflater> inflaterCache = new ArrayDeque<>();

    public String getName() {
        return name;
    }

    private class ZipEntryIterator<T extends ZipEntry> implements Enumeration<T>, Iterator<T> {

        private int i = 0;

        private final int entryCount;

        private final Function<String, T> gen;

        public ZipEntryIterator(int entryCount, Function<String, T> gen) {
            this.entryCount = entryCount;
            this.gen = gen;
        }

        @Override
        public boolean hasMoreElements() {
            return hasNext();
        }

        @Override
        public boolean hasNext() {
            return i < entryCount;
        }

        @Override
        public T nextElement() {
            return next();
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            synchronized (ZipFile.this) {
                ensureOpen();
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return (T) getZipEntry(null, null, zsrc.getEntryPos(i++ * 3), gen);
            }
        }

        @Override
        public Iterator<T> asIterator() {
            return this;
        }
    }

    public Enumeration<? extends ZipEntry> entries() {
        synchronized (this) {
            ensureOpen();
            return new ZipEntryIterator<ZipEntry>(zsrc.total, ZipEntry::new);
        }
    }

    private Enumeration<JarEntry> entries(Function<String, JarEntry> func) {
        synchronized (this) {
            ensureOpen();
            return new ZipEntryIterator<JarEntry>(zsrc.total, func);
        }
    }

    private class EntrySpliterator<T> extends Spliterators.AbstractSpliterator<T> {

        private int index;

        private final int fence;

        private final IntFunction<T> gen;

        EntrySpliterator(int index, int fence, IntFunction<T> gen) {
            super((long) fence, Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL);
            this.index = index;
            this.fence = fence;
            this.gen = gen;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (action == null)
                throw new NullPointerException();
            if (index >= 0 && index < fence) {
                synchronized (ZipFile.this) {
                    ensureOpen();
                    action.accept(gen.apply(zsrc.getEntryPos(index++ * 3)));
                }
                return true;
            }
            return false;
        }
    }

    public Stream<? extends ZipEntry> stream() {
        synchronized (this) {
            ensureOpen();
            return StreamSupport.stream(new EntrySpliterator<>(0, zsrc.total, pos -> getZipEntry(null, null, pos, ZipEntry::new)), false);
        }
    }

    private String getEntryName(int pos) {
        byte[] cen = zsrc.cen;
        int nlen = CENNAM(cen, pos);
        int clen = CENCOM(cen, pos);
        int flag = CENFLG(cen, pos);
        if (!zc.isUTF8() && (flag & EFS) != 0) {
            return zc.toStringUTF8(cen, pos + CENHDR, nlen);
        } else {
            return zc.toString(cen, pos + CENHDR, nlen);
        }
    }

    private Stream<String> entryNameStream() {
        synchronized (this) {
            ensureOpen();
            return StreamSupport.stream(new EntrySpliterator<>(0, zsrc.total, this::getEntryName), false);
        }
    }

    private Stream<JarEntry> stream(Function<String, JarEntry> func) {
        synchronized (this) {
            ensureOpen();
            return StreamSupport.stream(new EntrySpliterator<>(0, zsrc.total, pos -> (JarEntry) getZipEntry(null, null, pos, func)), false);
        }
    }

    private String lastEntryName;

    private int lastEntryPos;

    private ZipEntry getZipEntry(String name, byte[] bname, int pos, Function<String, ? extends ZipEntry> func) {
        byte[] cen = zsrc.cen;
        int nlen = CENNAM(cen, pos);
        int elen = CENEXT(cen, pos);
        int clen = CENCOM(cen, pos);
        int flag = CENFLG(cen, pos);
        if (name == null || bname.length != nlen) {
            if (!zc.isUTF8() && (flag & EFS) != 0) {
                name = zc.toStringUTF8(cen, pos + CENHDR, nlen);
            } else {
                name = zc.toString(cen, pos + CENHDR, nlen);
            }
        }
        ZipEntry e = func.apply(name);
        e.flag = flag;
        e.xdostime = CENTIM(cen, pos);
        e.crc = CENCRC(cen, pos);
        e.size = CENLEN(cen, pos);
        e.csize = CENSIZ(cen, pos);
        e.method = CENHOW(cen, pos);
        if (elen != 0) {
            int start = pos + CENHDR + nlen;
            e.setExtra0(Arrays.copyOfRange(cen, start, start + elen), true);
        }
        if (clen != 0) {
            int start = pos + CENHDR + nlen + elen;
            if (!zc.isUTF8() && (flag & EFS) != 0) {
                e.comment = zc.toStringUTF8(cen, start, clen);
            } else {
                e.comment = zc.toString(cen, start, clen);
            }
        }
        lastEntryName = e.name;
        lastEntryPos = pos;
        return e;
    }

    public int size() {
        synchronized (this) {
            ensureOpen();
            return zsrc.total;
        }
    }

    public void close() throws IOException {
        if (closeRequested) {
            return;
        }
        closeRequested = true;
        synchronized (this) {
            synchronized (streams) {
                if (!streams.isEmpty()) {
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
            synchronized (inflaterCache) {
                Inflater inf;
                while ((inf = inflaterCache.poll()) != null) {
                    inf.end();
                }
            }
            if (zsrc != null) {
                Source.close(zsrc);
                zsrc = null;
            }
        }
    }

    @Deprecated(since = "9")
    protected void finalize() throws IOException {
        close();
    }

    private void ensureOpen() {
        if (closeRequested) {
            throw new IllegalStateException("zip file closed");
        }
        if (zsrc == null) {
            throw new IllegalStateException("The object is not initialized.");
        }
    }

    private void ensureOpenOrZipException() throws IOException {
        if (closeRequested) {
            throw new ZipException("ZipFile closed");
        }
    }

    private class ZipFileInputStream extends InputStream {

        private volatile boolean closeRequested;

        private long pos;

        protected long rem;

        protected long size;

        ZipFileInputStream(byte[] cen, int cenpos) throws IOException {
            rem = CENSIZ(cen, cenpos);
            size = CENLEN(cen, cenpos);
            pos = CENOFF(cen, cenpos);
            if (rem == ZIP64_MAGICVAL || size == ZIP64_MAGICVAL || pos == ZIP64_MAGICVAL) {
                checkZIP64(cen, cenpos);
            }
            pos = -(pos + ZipFile.this.zsrc.locpos);
        }

        private void checkZIP64(byte[] cen, int cenpos) throws IOException {
            int off = cenpos + CENHDR + CENNAM(cen, cenpos);
            int end = off + CENEXT(cen, cenpos);
            while (off + 4 < end) {
                int tag = get16(cen, off);
                int sz = get16(cen, off + 2);
                off += 4;
                if (off + sz > end)
                    break;
                if (tag == EXTID_ZIP64) {
                    if (size == ZIP64_MAGICVAL) {
                        if (sz < 8 || (off + 8) > end)
                            break;
                        size = get64(cen, off);
                        sz -= 8;
                        off += 8;
                    }
                    if (rem == ZIP64_MAGICVAL) {
                        if (sz < 8 || (off + 8) > end)
                            break;
                        rem = get64(cen, off);
                        sz -= 8;
                        off += 8;
                    }
                    if (pos == ZIP64_MAGICVAL) {
                        if (sz < 8 || (off + 8) > end)
                            break;
                        pos = get64(cen, off);
                        sz -= 8;
                        off += 8;
                    }
                    break;
                }
                off += sz;
            }
        }

        private long initDataOffset() throws IOException {
            if (pos <= 0) {
                byte[] loc = new byte[LOCHDR];
                pos = -pos;
                int len = ZipFile.this.zsrc.readFullyAt(loc, 0, loc.length, pos);
                if (len != LOCHDR) {
                    throw new ZipException("ZipFile error reading zip file");
                }
                if (LOCSIG(loc) != LOCSIG) {
                    throw new ZipException("ZipFile invalid LOC header (bad signature)");
                }
                pos += LOCHDR + LOCNAM(loc) + LOCEXT(loc);
            }
            return pos;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            synchronized (ZipFile.this) {
                ensureOpenOrZipException();
                initDataOffset();
                if (rem == 0) {
                    return -1;
                }
                if (len > rem) {
                    len = (int) rem;
                }
                if (len <= 0) {
                    return 0;
                }
                len = ZipFile.this.zsrc.readAt(b, off, len, pos);
                if (len > 0) {
                    pos += len;
                    rem -= len;
                }
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

        public long skip(long n) throws IOException {
            synchronized (ZipFile.this) {
                initDataOffset();
                if (n > rem) {
                    n = rem;
                }
                pos += n;
                rem -= n;
            }
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
            if (closeRequested) {
                return;
            }
            closeRequested = true;
            rem = 0;
            synchronized (streams) {
                streams.remove(this);
            }
        }

        @SuppressWarnings("deprecation")
        protected void finalize() {
            close();
        }
    }

    private String[] getMetaInfEntryNames() {
        synchronized (this) {
            ensureOpen();
            if (zsrc.metanames == null) {
                return null;
            }
            String[] names = new String[zsrc.metanames.length];
            byte[] cen = zsrc.cen;
            for (int i = 0; i < names.length; i++) {
                int pos = zsrc.metanames[i];
                names[i] = new String(cen, pos + CENHDR, CENNAM(cen, pos), StandardCharsets.UTF_8);
            }
            return names;
        }
    }

    private static boolean isWindows;

    static {
        SharedSecrets.setJavaUtilZipFileAccess(new JavaUtilZipFileAccess() {

            @Override
            public boolean startsWithLocHeader(ZipFile zip) {
                return zip.zsrc.startsWithLoc;
            }

            @Override
            public String[] getMetaInfEntryNames(ZipFile zip) {
                return zip.getMetaInfEntryNames();
            }

            @Override
            public JarEntry getEntry(ZipFile zip, String name, Function<String, JarEntry> func) {
                return (JarEntry) zip.getEntry(name, func);
            }

            @Override
            public Enumeration<JarEntry> entries(ZipFile zip, Function<String, JarEntry> func) {
                return zip.entries(func);
            }

            @Override
            public Stream<JarEntry> stream(ZipFile zip, Function<String, JarEntry> func) {
                return zip.stream(func);
            }

            @Override
            public Stream<String> entryNameStream(ZipFile zip) {
                return zip.entryNameStream();
            }
        });
        isWindows = VM.getSavedProperty("os.name").contains("Windows");
    }

    private static class Source {

        private final Key key;

        private int refs = 1;

        private RandomAccessFile zfile;

        private byte[] cen;

        private long locpos;

        private byte[] comment;

        private int[] metanames;

        private final boolean startsWithLoc;

        private int[] entries;

        private int addEntry(int index, int hash, int next, int pos) {
            entries[index++] = hash;
            entries[index++] = next;
            entries[index++] = pos;
            return index;
        }

        private int getEntryHash(int index) {
            return entries[index];
        }

        private int getEntryNext(int index) {
            return entries[index + 1];
        }

        private int getEntryPos(int index) {
            return entries[index + 2];
        }

        private static final int ZIP_ENDCHAIN = -1;

        private int total;

        private int[] table;

        private int tablelen;

        private static class Key {

            BasicFileAttributes attrs;

            File file;

            public Key(File file, BasicFileAttributes attrs) {
                this.attrs = attrs;
                this.file = file;
            }

            public int hashCode() {
                long t = attrs.lastModifiedTime().toMillis();
                return ((int) (t ^ (t >>> 32))) + file.hashCode();
            }

            public boolean equals(Object obj) {
                if (obj instanceof Key) {
                    Key key = (Key) obj;
                    if (!attrs.lastModifiedTime().equals(key.attrs.lastModifiedTime())) {
                        return false;
                    }
                    Object fk = attrs.fileKey();
                    if (fk != null) {
                        return fk.equals(key.attrs.fileKey());
                    } else {
                        return file.equals(key.file);
                    }
                }
                return false;
            }
        }

        private static final HashMap<Key, Source> files = new HashMap<>();

        public static Source get(File file, boolean toDelete) throws IOException {
            Key key = new Key(file, Files.readAttributes(file.toPath(), BasicFileAttributes.class));
            Source src = null;
            synchronized (files) {
                src = files.get(key);
                if (src != null) {
                    src.refs++;
                    return src;
                }
            }
            src = new Source(key, toDelete);
            synchronized (files) {
                if (files.containsKey(key)) {
                    src.close();
                    src = files.get(key);
                    src.refs++;
                    return src;
                }
                files.put(key, src);
                return src;
            }
        }

        private static void close(Source src) throws IOException {
            synchronized (files) {
                if (--src.refs == 0) {
                    files.remove(src.key);
                    src.close();
                }
            }
        }

        private Source(Key key, boolean toDelete) throws IOException {
            this.key = key;
            if (toDelete) {
                if (isWindows) {
                    this.zfile = SharedSecrets.getJavaIORandomAccessFileAccess().openAndDelete(key.file, "r");
                } else {
                    this.zfile = new RandomAccessFile(key.file, "r");
                    key.file.delete();
                }
            } else {
                this.zfile = new RandomAccessFile(key.file, "r");
            }
            try {
                initCEN(-1);
                byte[] buf = new byte[4];
                readFullyAt(buf, 0, 4, 0);
                this.startsWithLoc = (LOCSIG(buf) == LOCSIG);
            } catch (IOException x) {
                try {
                    this.zfile.close();
                } catch (IOException xx) {
                }
                throw x;
            }
        }

        private void close() throws IOException {
            zfile.close();
            zfile = null;
            cen = null;
            entries = null;
            table = null;
            metanames = null;
        }

        private static final int BUF_SIZE = 8192;

        private final int readFullyAt(byte[] buf, int off, int len, long pos) throws IOException {
            synchronized (zfile) {
                zfile.seek(pos);
                int N = len;
                while (N > 0) {
                    int n = Math.min(BUF_SIZE, N);
                    zfile.readFully(buf, off, n);
                    off += n;
                    N -= n;
                }
                return len;
            }
        }

        private final int readAt(byte[] buf, int off, int len, long pos) throws IOException {
            synchronized (zfile) {
                zfile.seek(pos);
                return zfile.read(buf, off, len);
            }
        }

        private static final int hashN(byte[] a, int off, int len) {
            int h = 1;
            while (len-- > 0) {
                h = 31 * h + a[off++];
            }
            return h;
        }

        private static final int hash_append(int hash, byte b) {
            return hash * 31 + b;
        }

        private static class End {

            int centot;

            long cenlen;

            long cenoff;

            long endpos;
        }

        private End findEND() throws IOException {
            long ziplen = zfile.length();
            if (ziplen <= 0)
                zerror("zip file is empty");
            End end = new End();
            byte[] buf = new byte[READBLOCKSZ];
            long minHDR = (ziplen - END_MAXLEN) > 0 ? ziplen - END_MAXLEN : 0;
            long minPos = minHDR - (buf.length - ENDHDR);
            for (long pos = ziplen - buf.length; pos >= minPos; pos -= (buf.length - ENDHDR)) {
                int off = 0;
                if (pos < 0) {
                    off = (int) -pos;
                    Arrays.fill(buf, 0, off, (byte) 0);
                }
                int len = buf.length - off;
                if (readFullyAt(buf, off, len, pos + off) != len) {
                    zerror("zip END header not found");
                }
                for (int i = buf.length - ENDHDR; i >= 0; i--) {
                    if (buf[i + 0] == (byte) 'P' && buf[i + 1] == (byte) 'K' && buf[i + 2] == (byte) '\005' && buf[i + 3] == (byte) '\006') {
                        byte[] endbuf = Arrays.copyOfRange(buf, i, i + ENDHDR);
                        end.centot = ENDTOT(endbuf);
                        end.cenlen = ENDSIZ(endbuf);
                        end.cenoff = ENDOFF(endbuf);
                        end.endpos = pos + i;
                        int comlen = ENDCOM(endbuf);
                        if (end.endpos + ENDHDR + comlen != ziplen) {
                            byte[] sbuf = new byte[4];
                            long cenpos = end.endpos - end.cenlen;
                            long locpos = cenpos - end.cenoff;
                            if (cenpos < 0 || locpos < 0 || readFullyAt(sbuf, 0, sbuf.length, cenpos) != 4 || GETSIG(sbuf) != CENSIG || readFullyAt(sbuf, 0, sbuf.length, locpos) != 4 || GETSIG(sbuf) != LOCSIG) {
                                continue;
                            }
                        }
                        if (comlen > 0) {
                            comment = new byte[comlen];
                            if (readFullyAt(comment, 0, comlen, end.endpos + ENDHDR) != comlen) {
                                zerror("zip comment read failed");
                            }
                        }
                        try {
                            byte[] loc64 = new byte[ZIP64_LOCHDR];
                            if (end.endpos < ZIP64_LOCHDR || readFullyAt(loc64, 0, loc64.length, end.endpos - ZIP64_LOCHDR) != loc64.length || GETSIG(loc64) != ZIP64_LOCSIG) {
                                return end;
                            }
                            long end64pos = ZIP64_LOCOFF(loc64);
                            byte[] end64buf = new byte[ZIP64_ENDHDR];
                            if (readFullyAt(end64buf, 0, end64buf.length, end64pos) != end64buf.length || GETSIG(end64buf) != ZIP64_ENDSIG) {
                                return end;
                            }
                            long cenlen64 = ZIP64_ENDSIZ(end64buf);
                            long cenoff64 = ZIP64_ENDOFF(end64buf);
                            long centot64 = ZIP64_ENDTOT(end64buf);
                            if (cenlen64 != end.cenlen && end.cenlen != ZIP64_MAGICVAL || cenoff64 != end.cenoff && end.cenoff != ZIP64_MAGICVAL || centot64 != end.centot && end.centot != ZIP64_MAGICCOUNT) {
                                return end;
                            }
                            end.cenlen = cenlen64;
                            end.cenoff = cenoff64;
                            end.centot = (int) centot64;
                            end.endpos = end64pos;
                        } catch (IOException x) {
                        }
                        return end;
                    }
                }
            }
            zerror("zip END header not found");
            return null;
        }

        private void initCEN(int knownTotal) throws IOException {
            if (knownTotal == -1) {
                End end = findEND();
                if (end.endpos == 0) {
                    locpos = 0;
                    total = 0;
                    entries = new int[0];
                    cen = null;
                    return;
                }
                if (end.cenlen > end.endpos)
                    zerror("invalid END header (bad central directory size)");
                long cenpos = end.endpos - end.cenlen;
                locpos = cenpos - end.cenoff;
                if (locpos < 0) {
                    zerror("invalid END header (bad central directory offset)");
                }
                cen = new byte[(int) (end.cenlen + ENDHDR)];
                if (readFullyAt(cen, 0, cen.length, cenpos) != end.cenlen + ENDHDR) {
                    zerror("read CEN tables failed");
                }
                total = end.centot;
            } else {
                total = knownTotal;
            }
            entries = new int[total * 3];
            tablelen = ((total / 2) | 1);
            table = new int[tablelen];
            Arrays.fill(table, ZIP_ENDCHAIN);
            int idx = 0;
            int hash = 0;
            int next = -1;
            ArrayList<Integer> metanamesList = null;
            int i = 0;
            int hsh = 0;
            int pos = 0;
            int limit = cen.length - ENDHDR;
            while (pos + CENHDR <= limit) {
                if (i >= total) {
                    initCEN(countCENHeaders(cen, limit));
                    return;
                }
                if (CENSIG(cen, pos) != CENSIG)
                    zerror("invalid CEN header (bad signature)");
                int method = CENHOW(cen, pos);
                int nlen = CENNAM(cen, pos);
                int elen = CENEXT(cen, pos);
                int clen = CENCOM(cen, pos);
                if ((CENFLG(cen, pos) & 1) != 0)
                    zerror("invalid CEN header (encrypted entry)");
                if (method != STORED && method != DEFLATED)
                    zerror("invalid CEN header (bad compression method: " + method + ")");
                if (pos + CENHDR + nlen > limit)
                    zerror("invalid CEN header (bad header size)");
                hash = hashN(cen, pos + CENHDR, nlen);
                hsh = (hash & 0x7fffffff) % tablelen;
                next = table[hsh];
                table[hsh] = idx;
                idx = addEntry(idx, hash, next, pos);
                if (isMetaName(cen, pos + CENHDR, nlen)) {
                    if (metanamesList == null)
                        metanamesList = new ArrayList<>(4);
                    metanamesList.add(pos);
                }
                pos += (CENHDR + nlen + elen + clen);
                i++;
            }
            total = i;
            if (metanamesList != null) {
                metanames = new int[metanamesList.size()];
                for (int j = 0, len = metanames.length; j < len; j++) {
                    metanames[j] = metanamesList.get(j);
                }
            }
            if (pos + ENDHDR != cen.length) {
                zerror("invalid CEN header (bad header size)");
            }
        }

        private static void zerror(String msg) throws ZipException {
            throw new ZipException(msg);
        }

        private int getEntryPos(byte[] name, boolean addSlash) {
            if (total == 0) {
                return -1;
            }
            int hsh = hashN(name, 0, name.length);
            int idx = table[(hsh & 0x7fffffff) % tablelen];
            while (true) {
                while (idx != ZIP_ENDCHAIN) {
                    if (getEntryHash(idx) == hsh) {
                        int pos = getEntryPos(idx);
                        if (name.length == CENNAM(cen, pos)) {
                            boolean matched = true;
                            int nameoff = pos + CENHDR;
                            for (int i = 0; i < name.length; i++) {
                                if (name[i] != cen[nameoff++]) {
                                    matched = false;
                                    break;
                                }
                            }
                            if (matched) {
                                return pos;
                            }
                        }
                    }
                    idx = getEntryNext(idx);
                }
                if (!addSlash || name.length == 0 || name[name.length - 1] == '/') {
                    return -1;
                }
                name = Arrays.copyOf(name, name.length + 1);
                name[name.length - 1] = '/';
                hsh = hash_append(hsh, (byte) '/');
                idx = table[(hsh & 0x7fffffff) % tablelen];
                addSlash = false;
            }
        }

        private static boolean isMetaName(byte[] name, int off, int len) {
            return len > 9 && name[off + len - 1] != '/' && (name[off++] | 0x20) == 'm' && (name[off++] | 0x20) == 'e' && (name[off++] | 0x20) == 't' && (name[off++] | 0x20) == 'a' && (name[off++]) == '-' && (name[off++] | 0x20) == 'i' && (name[off++] | 0x20) == 'n' && (name[off++] | 0x20) == 'f' && (name[off]) == '/';
        }

        private static int countCENHeaders(byte[] cen, int size) {
            int count = 0;
            for (int p = 0; p + CENHDR <= size; p += CENHDR + CENNAM(cen, p) + CENEXT(cen, p) + CENCOM(cen, p)) count++;
            return count;
        }
    }
}
