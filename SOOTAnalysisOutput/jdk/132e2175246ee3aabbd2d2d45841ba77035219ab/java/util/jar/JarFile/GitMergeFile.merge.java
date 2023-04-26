package java.util.jar;

import java.io.*;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.*;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.security.AccessController;
import java.security.CodeSource;
import jdk.internal.misc.SharedSecrets;
import sun.security.util.ManifestEntryVerifier;
import sun.security.util.SignatureFileVerifier;
import static java.util.jar.Attributes.Name.MULTI_RELEASE;

public class JarFile extends ZipFile {

    private final static int BASE_VERSION;

    private final static int RUNTIME_VERSION;

    private final static boolean MULTI_RELEASE_ENABLED;

    private final static boolean MULTI_RELEASE_FORCED;

    private SoftReference<Manifest> manRef;

    private JarEntry manEntry;

    private JarVerifier jv;

    private boolean jvInitialized;

    private boolean verify;

    private final int version;

    private boolean notVersioned;

    private final boolean runtimeVersioned;

    private boolean hasClassPathAttribute;

    private volatile boolean hasCheckedSpecialAttributes;

    static {
        SharedSecrets.setJavaUtilJarAccess(new JavaUtilJarAccessImpl());
        BASE_VERSION = 8;
        RUNTIME_VERSION = AccessController.doPrivileged(new PrivilegedAction<Integer>() {

            public Integer run() {
                Integer v = jdk.Version.current().major();
                Integer i = Integer.getInteger("jdk.util.jar.version", v);
                i = i < 0 ? 0 : i;
                return i > v ? v : i;
            }
        });
        String multi_release = AccessController.doPrivileged(new PrivilegedAction<String>() {

            public String run() {
                return System.getProperty("jdk.util.jar.enableMultiRelease", "true");
            }
        });
        switch(multi_release) {
            case "true":
            default:
                MULTI_RELEASE_ENABLED = true;
                MULTI_RELEASE_FORCED = false;
                break;
            case "false":
                MULTI_RELEASE_ENABLED = false;
                MULTI_RELEASE_FORCED = false;
                break;
            case "force":
                MULTI_RELEASE_ENABLED = true;
                MULTI_RELEASE_FORCED = true;
                break;
        }
    }

    public enum Release {

        BASE(BASE_VERSION), VERSION_9(9), RUNTIME(RUNTIME_VERSION);

        Release(int version) {
            this.version = version;
        }

        private static Release valueOf(int version) {
            return version <= BASE.value() ? BASE : valueOf("VERSION_" + version);
        }

        private final int version;

        private int value() {
            return this.version;
        }
    }

    private static final String META_INF = "META-INF/";

    private static final String META_INF_VERSIONS = META_INF + "versions/";

    public static final String MANIFEST_NAME = META_INF + "MANIFEST.MF";

    public JarFile(String name) throws IOException {
        this(new File(name), true, ZipFile.OPEN_READ);
    }

    public JarFile(String name, boolean verify) throws IOException {
        this(new File(name), verify, ZipFile.OPEN_READ);
    }

    public JarFile(File file) throws IOException {
        this(file, true, ZipFile.OPEN_READ);
    }

    public JarFile(File file, boolean verify) throws IOException {
        this(file, verify, ZipFile.OPEN_READ);
    }

    public JarFile(File file, boolean verify, int mode) throws IOException {
        this(file, verify, mode, Release.BASE);
        this.notVersioned = true;
    }

    public JarFile(File file, boolean verify, int mode, Release version) throws IOException {
        super(file, mode);
        Objects.requireNonNull(version);
        this.verify = verify;
        this.version = MULTI_RELEASE_FORCED ? RUNTIME_VERSION : version.value();
        this.runtimeVersioned = version == Release.RUNTIME;
        assert runtimeVersionExists();
    }

    private boolean runtimeVersionExists() {
        int version = jdk.Version.current().major();
        try {
            Release.valueOf(version);
            return true;
        } catch (IllegalArgumentException x) {
            System.err.println("No JarFile.Release object for release " + version);
            return false;
        }
    }

    public final Release getVersion() {
        if (isMultiRelease()) {
            return runtimeVersioned ? Release.RUNTIME : Release.valueOf(version);
        } else {
            return Release.BASE;
        }
    }

    public final boolean isMultiRelease() {
        if (MULTI_RELEASE_ENABLED) {
            Boolean result = isMultiRelease;
            if (result == null) {
                synchronized (this) {
                    result = isMultiRelease;
                    if (result == null) {
                        Manifest man = null;
                        try {
                            man = getManifest();
                        } catch (IOException e) {
                        }
                        isMultiRelease = result = (man != null) && man.getMainAttributes().containsKey(MULTI_RELEASE) ? Boolean.TRUE : Boolean.FALSE;
                    }
                }
            }
            return result == Boolean.TRUE;
        } else {
            return false;
        }
    }

    private volatile Boolean isMultiRelease;

    public Manifest getManifest() throws IOException {
        return getManifestFromReference();
    }

    private Manifest getManifestFromReference() throws IOException {
        Manifest man = manRef != null ? manRef.get() : null;
        if (man == null) {
            JarEntry manEntry = getManEntry();
            if (manEntry != null) {
                if (verify) {
                    byte[] b = getBytes(manEntry);
                    man = new Manifest(new ByteArrayInputStream(b));
                    if (!jvInitialized) {
                        jv = new JarVerifier(b);
                    }
                } else {
                    man = new Manifest(super.getInputStream(manEntry));
                }
                manRef = new SoftReference<>(man);
            }
        }
        return man;
    }

    private String[] getMetaInfEntryNames() {
        return jdk.internal.misc.SharedSecrets.getJavaUtilZipFileAccess().getMetaInfEntryNames((ZipFile) this);
    }

    public JarEntry getJarEntry(String name) {
        return (JarEntry) getEntry(name);
    }

    public ZipEntry getEntry(String name) {
        ZipEntry ze = super.getEntry(name);
        if (ze != null) {
            return new JarFileEntry(ze);
        }
        if (isMultiRelease()) {
            ze = new ZipEntry(name);
            ZipEntry vze = getVersionedEntry(ze);
            if (ze != vze) {
                return new JarFileEntry(name, vze);
            }
        }
        return null;
    }

    private class JarEntryIterator implements Enumeration<JarEntry>, Iterator<JarEntry> {

        final Enumeration<? extends ZipEntry> e = JarFile.super.entries();

        ZipEntry ze;

        public boolean hasNext() {
            if (notVersioned) {
                return e.hasMoreElements();
            }
            if (ze != null) {
                return true;
            }
            return findNext();
        }

        private boolean findNext() {
            while (e.hasMoreElements()) {
                ZipEntry ze2 = e.nextElement();
                if (!ze2.getName().startsWith(META_INF_VERSIONS)) {
                    ze = ze2;
                    return true;
                }
            }
            return false;
        }

        public JarEntry next() {
            ZipEntry ze2;
            if (notVersioned) {
                ze2 = e.nextElement();
                return new JarFileEntry(ze2.getName(), ze2);
            }
            if (ze != null || findNext()) {
                ze2 = ze;
                ze = null;
                return new JarFileEntry(ze2);
            }
            throw new NoSuchElementException();
        }

        public boolean hasMoreElements() {
            return hasNext();
        }

        public JarEntry nextElement() {
            return next();
        }

        public Iterator<JarEntry> asIterator() {
            return this;
        }
    }

    public Enumeration<JarEntry> entries() {
        return new JarEntryIterator();
    }

    public Stream<JarEntry> stream() {
        return StreamSupport.stream(Spliterators.spliterator(new JarEntryIterator(), size(), Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL), false);
    }

    private ZipEntry searchForVersionedEntry(final int version, String name) {
        ZipEntry vze = null;
        String sname = "/" + name;
        int i = version;
        while (i > BASE_VERSION) {
            vze = super.getEntry(META_INF_VERSIONS + i + sname);
            if (vze != null)
                break;
            i--;
        }
        return vze;
    }

    private ZipEntry getVersionedEntry(ZipEntry ze) {
        ZipEntry vze = null;
        if (version > BASE_VERSION && !ze.isDirectory()) {
            String name = ze.getName();
            if (!name.startsWith(META_INF)) {
                vze = searchForVersionedEntry(version, name);
            }
        }
        return vze == null ? ze : vze;
    }

    private class JarFileEntry extends JarEntry {

        final private String name;

        JarFileEntry(ZipEntry ze) {
            super(isMultiRelease() ? getVersionedEntry(ze) : ze);
            this.name = ze.getName();
        }

        JarFileEntry(String name, ZipEntry vze) {
            super(vze);
            this.name = name;
        }

        public Attributes getAttributes() throws IOException {
            Manifest man = JarFile.this.getManifest();
            if (man != null) {
                return man.getAttributes(super.getName());
            } else {
                return null;
            }
        }

        public Certificate[] getCertificates() {
            try {
                maybeInstantiateVerifier();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (certs == null && jv != null) {
                certs = jv.getCerts(JarFile.this, reifiedEntry());
            }
            return certs == null ? null : certs.clone();
        }

        public CodeSigner[] getCodeSigners() {
            try {
                maybeInstantiateVerifier();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (signers == null && jv != null) {
                signers = jv.getCodeSigners(JarFile.this, reifiedEntry());
            }
            return signers == null ? null : signers.clone();
        }

        JarFileEntry reifiedEntry() {
            if (isMultiRelease()) {
                String entryName = super.getName();
                return entryName.equals(this.name) ? this : new JarFileEntry(entryName, this);
            }
            return this;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private void maybeInstantiateVerifier() throws IOException {
        if (jv != null) {
            return;
        }
        if (verify) {
            String[] names = getMetaInfEntryNames();
            if (names != null) {
                for (String nameLower : names) {
                    String name = nameLower.toUpperCase(Locale.ENGLISH);
                    if (name.endsWith(".DSA") || name.endsWith(".RSA") || name.endsWith(".EC") || name.endsWith(".SF")) {
                        getManifest();
                        return;
                    }
                }
            }
            verify = false;
        }
    }

    private void initializeVerifier() {
        ManifestEntryVerifier mev = null;
        try {
            String[] names = getMetaInfEntryNames();
            if (names != null) {
                for (String name : names) {
                    String uname = name.toUpperCase(Locale.ENGLISH);
                    if (MANIFEST_NAME.equals(uname) || SignatureFileVerifier.isBlockOrSF(uname)) {
                        JarEntry e = getJarEntry(name);
                        if (e == null) {
                            throw new JarException("corrupted jar file");
                        }
                        if (mev == null) {
                            mev = new ManifestEntryVerifier(getManifestFromReference());
                        }
                        byte[] b = getBytes(e);
                        if (b != null && b.length > 0) {
                            jv.beginEntry(e, mev);
                            jv.update(b.length, b, 0, b.length, mev);
                            jv.update(-1, null, 0, 0, mev);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            jv = null;
            verify = false;
            if (JarVerifier.debug != null) {
                JarVerifier.debug.println("jarfile parsing error!");
                ex.printStackTrace();
            }
        }
        if (jv != null) {
            jv.doneWithMeta();
            if (JarVerifier.debug != null) {
                JarVerifier.debug.println("done with meta!");
            }
            if (jv.nothingToVerify()) {
                if (JarVerifier.debug != null) {
                    JarVerifier.debug.println("nothing to verify!");
                }
                jv = null;
                verify = false;
            }
        }
    }

    private byte[] getBytes(ZipEntry ze) throws IOException {
        try (InputStream is = super.getInputStream(ze)) {
            int len = (int) ze.getSize();
            int bytesRead;
            byte[] b;
            if (len != -1 && len <= 65535) {
                b = new byte[len];
                bytesRead = is.readNBytes(b, 0, len);
            } else {
                b = is.readAllBytes();
                bytesRead = b.length;
            }
            if (len != -1 && len != bytesRead) {
                throw new EOFException("Expected:" + len + ", read:" + bytesRead);
            }
            return b;
        }
    }

    public synchronized InputStream getInputStream(ZipEntry ze) throws IOException {
        maybeInstantiateVerifier();
        if (jv == null) {
            return super.getInputStream(ze);
        }
        if (!jvInitialized) {
            initializeVerifier();
            jvInitialized = true;
            if (jv == null)
                return super.getInputStream(ze);
        }
        return new JarVerifier.VerifierStream(getManifestFromReference(), verifiableEntry(ze), super.getInputStream(ze), jv);
    }

    private JarEntry verifiableEntry(ZipEntry ze) {
        if (ze instanceof JarFileEntry) {
            return ((JarFileEntry) ze).reifiedEntry();
        }
        ze = getJarEntry(ze.getName());
        if (ze instanceof JarFileEntry) {
            return ((JarFileEntry) ze).reifiedEntry();
        }
        return (JarEntry) ze;
    }

    private static final char[] CLASSPATH_CHARS = { 'c', 'l', 'a', 's', 's', '-', 'p', 'a', 't', 'h' };

    private static final int[] CLASSPATH_LASTOCC;

    private static final int[] CLASSPATH_OPTOSFT;

    static {
        CLASSPATH_LASTOCC = new int[128];
        CLASSPATH_OPTOSFT = new int[10];
        CLASSPATH_LASTOCC[(int) 'c'] = 1;
        CLASSPATH_LASTOCC[(int) 'l'] = 2;
        CLASSPATH_LASTOCC[(int) 's'] = 5;
        CLASSPATH_LASTOCC[(int) '-'] = 6;
        CLASSPATH_LASTOCC[(int) 'p'] = 7;
        CLASSPATH_LASTOCC[(int) 'a'] = 8;
        CLASSPATH_LASTOCC[(int) 't'] = 9;
        CLASSPATH_LASTOCC[(int) 'h'] = 10;
        for (int i = 0; i < 9; i++) CLASSPATH_OPTOSFT[i] = 10;
        CLASSPATH_OPTOSFT[9] = 1;
    }

    private JarEntry getManEntry() {
        if (manEntry == null) {
            ZipEntry manEntry = super.getEntry(MANIFEST_NAME);
            if (manEntry == null) {
                String[] names = getMetaInfEntryNames();
                if (names != null) {
                    for (String name : names) {
                        if (MANIFEST_NAME.equals(name.toUpperCase(Locale.ENGLISH))) {
                            manEntry = super.getEntry(name);
                            break;
                        }
                    }
                }
            }
            this.manEntry = (manEntry == null) ? null : new JarFileEntry(manEntry.getName(), manEntry);
        }
        return manEntry;
    }

    boolean hasClassPathAttribute() throws IOException {
        checkForSpecialAttributes();
        return hasClassPathAttribute;
    }

    private boolean match(char[] src, byte[] b, int[] lastOcc, int[] optoSft) {
        int len = src.length;
        int last = b.length - len;
        int i = 0;
        next: while (i <= last) {
            for (int j = (len - 1); j >= 0; j--) {
                char c = (char) b[i + j];
                c = (((c - 'A') | ('Z' - c)) >= 0) ? (char) (c + 32) : c;
                if (c != src[j]) {
                    i += Math.max(j + 1 - lastOcc[c & 0x7F], optoSft[j]);
                    continue next;
                }
            }
            return true;
        }
        return false;
    }

    private void checkForSpecialAttributes() throws IOException {
        if (hasCheckedSpecialAttributes)
            return;
        JarEntry manEntry = getManEntry();
        if (manEntry != null) {
            byte[] b = getBytes(manEntry);
            if (match(CLASSPATH_CHARS, b, CLASSPATH_LASTOCC, CLASSPATH_OPTOSFT))
                hasClassPathAttribute = true;
        }
        hasCheckedSpecialAttributes = true;
    }

    private synchronized void ensureInitialization() {
        try {
            maybeInstantiateVerifier();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (jv != null && !jvInitialized) {
            initializeVerifier();
            jvInitialized = true;
        }
    }

    JarEntry newEntry(ZipEntry ze) {
        return new JarFileEntry(ze);
    }

    Enumeration<String> entryNames(CodeSource[] cs) {
        ensureInitialization();
        if (jv != null) {
            return jv.entryNames(this, cs);
        }
        boolean includeUnsigned = false;
        for (CodeSource c : cs) {
            if (c.getCodeSigners() == null) {
                includeUnsigned = true;
                break;
            }
        }
        if (includeUnsigned) {
            return unsignedEntryNames();
        } else {
            return new Enumeration<>() {

                public boolean hasMoreElements() {
                    return false;
                }

                public String nextElement() {
                    throw new NoSuchElementException();
                }
            };
        }
    }

    Enumeration<JarEntry> entries2() {
        ensureInitialization();
        if (jv != null) {
            return jv.entries2(this, super.entries());
        }
        final Enumeration<? extends ZipEntry> enum_ = super.entries();
        return new Enumeration<>() {

            ZipEntry entry;

            public boolean hasMoreElements() {
                if (entry != null) {
                    return true;
                }
                while (enum_.hasMoreElements()) {
                    ZipEntry ze = enum_.nextElement();
                    if (JarVerifier.isSigningRelated(ze.getName())) {
                        continue;
                    }
                    entry = ze;
                    return true;
                }
                return false;
            }

            public JarFileEntry nextElement() {
                if (hasMoreElements()) {
                    ZipEntry ze = entry;
                    entry = null;
                    return new JarFileEntry(ze);
                }
                throw new NoSuchElementException();
            }
        };
    }

    CodeSource[] getCodeSources(URL url) {
        ensureInitialization();
        if (jv != null) {
            return jv.getCodeSources(this, url);
        }
        Enumeration<String> unsigned = unsignedEntryNames();
        if (unsigned.hasMoreElements()) {
            return new CodeSource[] { JarVerifier.getUnsignedCS(url) };
        } else {
            return null;
        }
    }

    private Enumeration<String> unsignedEntryNames() {
        final Enumeration<JarEntry> entries = entries();
        return new Enumeration<>() {

            String name;

            public boolean hasMoreElements() {
                if (name != null) {
                    return true;
                }
                while (entries.hasMoreElements()) {
                    String value;
                    ZipEntry e = entries.nextElement();
                    value = e.getName();
                    if (e.isDirectory() || JarVerifier.isSigningRelated(value)) {
                        continue;
                    }
                    name = value;
                    return true;
                }
                return false;
            }

            public String nextElement() {
                if (hasMoreElements()) {
                    String value = name;
                    name = null;
                    return value;
                }
                throw new NoSuchElementException();
            }
        };
    }

    CodeSource getCodeSource(URL url, String name) {
        ensureInitialization();
        if (jv != null) {
            if (jv.eagerValidation) {
                CodeSource cs = null;
                JarEntry je = getJarEntry(name);
                if (je != null) {
                    cs = jv.getCodeSource(url, this, je);
                } else {
                    cs = jv.getCodeSource(url, name);
                }
                return cs;
            } else {
                return jv.getCodeSource(url, name);
            }
        }
        return JarVerifier.getUnsignedCS(url);
    }

    void setEagerValidation(boolean eager) {
        try {
            maybeInstantiateVerifier();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (jv != null) {
            jv.setEagerValidation(eager);
        }
    }

    List<Object> getManifestDigests() {
        ensureInitialization();
        if (jv != null) {
            return jv.getManifestDigests();
        }
        return new ArrayList<>();
    }
}
