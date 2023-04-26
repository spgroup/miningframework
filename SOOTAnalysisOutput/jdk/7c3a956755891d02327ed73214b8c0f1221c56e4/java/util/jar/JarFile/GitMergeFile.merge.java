package java.util.jar;

import jdk.internal.misc.SharedSecrets;
import jdk.internal.misc.JavaUtilZipFileAccess;
import sun.security.action.GetPropertyAction;
import sun.security.util.ManifestEntryVerifier;
import sun.security.util.SignatureFileVerifier;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
<<<<<<< MINE
import java.util.function.Function;
=======
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collector;
>>>>>>> YOURS
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class JarFile extends ZipFile {

    private final static Runtime.Version BASE_VERSION;

    private final static int BASE_VERSION_MAJOR;

    private final static Runtime.Version RUNTIME_VERSION;

    private final static boolean MULTI_RELEASE_ENABLED;

    private final static boolean MULTI_RELEASE_FORCED;

    private SoftReference<Manifest> manRef;

    private JarEntry manEntry;

    private JarVerifier jv;

    private boolean jvInitialized;

    private boolean verify;

    private final Runtime.Version version;

    private final int versionMajor;

    private boolean isMultiRelease;

    private boolean hasClassPathAttribute;

    private volatile boolean hasCheckedSpecialAttributes;

    private static final JavaUtilZipFileAccess JUZFA;

    static {
        SharedSecrets.setJavaUtilJarAccess(new JavaUtilJarAccessImpl());
        JUZFA = jdk.internal.misc.SharedSecrets.getJavaUtilZipFileAccess();
        BASE_VERSION = Runtime.Version.parse(Integer.toString(8));
        BASE_VERSION_MAJOR = BASE_VERSION.major();
        String jarVersion = GetPropertyAction.privilegedGetProperty("jdk.util.jar.version");
        int runtimeVersion = Runtime.version().major();
        if (jarVersion != null) {
            int jarVer = Integer.parseInt(jarVersion);
            runtimeVersion = (jarVer > runtimeVersion) ? runtimeVersion : Math.max(jarVer, BASE_VERSION_MAJOR);
        }
        RUNTIME_VERSION = Runtime.Version.parse(Integer.toString(runtimeVersion));
        String enableMultiRelease = GetPropertyAction.privilegedGetProperty("jdk.util.jar.enableMultiRelease", "true");
        switch(enableMultiRelease) {
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

    private static final String META_INF = "META-INF/";

    private static final String META_INF_VERSIONS = META_INF + "versions/";

    public static final String MANIFEST_NAME = META_INF + "MANIFEST.MF";

    public static Runtime.Version baseVersion() {
        return BASE_VERSION;
    }

    public static Runtime.Version runtimeVersion() {
        return RUNTIME_VERSION;
    }

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
        this(file, verify, mode, BASE_VERSION);
    }

    public JarFile(File file, boolean verify, int mode, Runtime.Version version) throws IOException {
        super(file, mode);
        this.verify = verify;
        Objects.requireNonNull(version);
        if (MULTI_RELEASE_FORCED || version.major() == RUNTIME_VERSION.major()) {
            this.version = RUNTIME_VERSION;
        } else if (version.major() <= BASE_VERSION_MAJOR) {
            this.version = BASE_VERSION;
        } else {
            this.version = Runtime.Version.parse(Integer.toString(version.major()));
        }
        this.versionMajor = this.version.major();
    }

    public final Runtime.Version getVersion() {
        return isMultiRelease() ? this.version : BASE_VERSION;
    }

    public final boolean isMultiRelease() {
        if (isMultiRelease) {
            return true;
        }
        if (MULTI_RELEASE_ENABLED) {
            try {
                checkForSpecialAttributes();
            } catch (IOException io) {
                isMultiRelease = false;
            }
        }
        return isMultiRelease;
    }

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
        return JUZFA.getMetaInfEntryNames((ZipFile) this);
    }

    public JarEntry getJarEntry(String name) {
        return (JarEntry) getEntry(name);
    }

    public ZipEntry getEntry(String name) {
        JarFileEntry je = getEntry0(name);
        if (isMultiRelease()) {
            return getVersionedEntry(name, je);
        }
        return je;
    }

    public Enumeration<JarEntry> entries() {
        return JUZFA.entries(this, JarFileEntry::new);
    }
<<<<<<< MINE

    public Stream<JarEntry> stream() {
        return JUZFA.stream(this, JarFileEntry::new);
    }

    public Stream<JarEntry> versionedStream() {
        if (isMultiRelease()) {
            return JUZFA.entryNameStream(this).map(this::getBasename).filter(Objects::nonNull).distinct().map(this::getJarEntry);
        }
        return stream();
    }

    private JarFileEntry getEntry0(String name) {
        Function<String, JarEntry> newJarFileEntryFn = new Function<>() {

            @Override
            public JarEntry apply(String name) {
                return new JarFileEntry(name);
            }
        };
        return (JarFileEntry) JUZFA.getEntry(this, name, newJarFileEntryFn);
=======

    public Stream<JarEntry> stream() {
        return JUZFA.stream(this, JarFileEntry::new);
    }

    public Stream<JarEntry> versionedStream() {
        if (isMultiRelease()) {
            return JUZFA.entryNameStream(this).map(this::getBasename).filter(Objects::nonNull).distinct().map(this::getJarEntry);
        }
        return stream();
    }

    private JarFileEntry getEntry0(String name) {
        return (JarFileEntry) JUZFA.getEntry(this, name, JarFileEntry::new);
>>>>>>> YOURS
    }

    private String getBasename(String name) {
        if (name.startsWith(META_INF_VERSIONS)) {
            int off = META_INF_VERSIONS.length();
            int index = name.indexOf('/', off);
            try {
                if (index == -1 || index == (name.length() - 1) || Integer.parseInt(name, off, index, 10) > versionMajor) {
                    return null;
                }
            } catch (NumberFormatException x) {
                return null;
            }
            return name.substring(index + 1);
        }
        return name;
    }

    private JarEntry getVersionedEntry(String name, JarEntry je) {
        if (BASE_VERSION_MAJOR < versionMajor) {
            if (!name.startsWith(META_INF)) {
                int v = versionMajor;
                while (v > BASE_VERSION_MAJOR) {
                    JarFileEntry vje = getEntry0(META_INF_VERSIONS + v + "/" + name);
                    if (vje != null) {
                        return vje.withBasename(name);
                    }
                    v--;
                }
            }
        }
        return je;
    }

    String getRealName(JarEntry entry) {
        return entry.getRealName();
    }

    private class JarFileEntry extends JarEntry {

        private String basename;

        JarFileEntry(String name) {
            super(name);
            this.basename = name;
        }

        JarFileEntry(String name, ZipEntry vze) {
            super(vze);
            this.basename = name;
        }

        @Override
        public Attributes getAttributes() throws IOException {
            Manifest man = JarFile.this.getManifest();
            if (man != null) {
                return man.getAttributes(super.getName());
            } else {
                return null;
            }
        }

        @Override
        public Certificate[] getCertificates() {
            try {
                maybeInstantiateVerifier();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (certs == null && jv != null) {
                certs = jv.getCerts(JarFile.this, realEntry());
            }
            return certs == null ? null : certs.clone();
        }

        @Override
        public CodeSigner[] getCodeSigners() {
            try {
                maybeInstantiateVerifier();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (signers == null && jv != null) {
                signers = jv.getCodeSigners(JarFile.this, realEntry());
            }
            return signers == null ? null : signers.clone();
        }

        @Override
        public String getRealName() {
            return super.getName();
        }

        @Override
        public String getName() {
            return basename;
        }

        JarFileEntry realEntry() {
            if (isMultiRelease() && versionMajor != BASE_VERSION_MAJOR) {
                String entryName = super.getName();
                return entryName == basename || entryName.equals(basename) ? this : new JarFileEntry(entryName, this);
            }
            return this;
        }

        JarFileEntry withBasename(String name) {
            basename = name;
            return this;
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
            return ((JarFileEntry) ze).realEntry();
        }
        ze = getJarEntry(ze.getName());
        if (ze instanceof JarFileEntry) {
            return ((JarFileEntry) ze).realEntry();
        }
        return (JarEntry) ze;
    }

    private static final byte[] CLASSPATH_CHARS = { 'C', 'L', 'A', 'S', 'S', '-', 'P', 'A', 'T', 'H', ':', ' ' };

    private static final byte[] CLASSPATH_LASTOCC;

    private static final byte[] CLASSPATH_OPTOSFT;

    private static final byte[] MULTIRELEASE_CHARS = { 'M', 'U', 'L', 'T', 'I', '-', 'R', 'E', 'L', 'E', 'A', 'S', 'E', ':', ' ', 'T', 'R', 'U', 'E' };

    private static final byte[] MULTIRELEASE_LASTOCC;

    private static final byte[] MULTIRELEASE_OPTOSFT;

    static {
        CLASSPATH_LASTOCC = new byte[65];
        CLASSPATH_OPTOSFT = new byte[12];
        CLASSPATH_LASTOCC[(int) 'C' - 32] = 1;
        CLASSPATH_LASTOCC[(int) 'L' - 32] = 2;
        CLASSPATH_LASTOCC[(int) 'S' - 32] = 5;
        CLASSPATH_LASTOCC[(int) '-' - 32] = 6;
        CLASSPATH_LASTOCC[(int) 'P' - 32] = 7;
        CLASSPATH_LASTOCC[(int) 'A' - 32] = 8;
        CLASSPATH_LASTOCC[(int) 'T' - 32] = 9;
        CLASSPATH_LASTOCC[(int) 'H' - 32] = 10;
        CLASSPATH_LASTOCC[(int) ':' - 32] = 11;
        CLASSPATH_LASTOCC[(int) ' ' - 32] = 12;
        for (int i = 0; i < 11; i++) {
            CLASSPATH_OPTOSFT[i] = 12;
        }
        CLASSPATH_OPTOSFT[11] = 1;
        MULTIRELEASE_LASTOCC = new byte[65];
        MULTIRELEASE_OPTOSFT = new byte[19];
        MULTIRELEASE_LASTOCC[(int) 'M' - 32] = 1;
        MULTIRELEASE_LASTOCC[(int) 'I' - 32] = 5;
        MULTIRELEASE_LASTOCC[(int) '-' - 32] = 6;
        MULTIRELEASE_LASTOCC[(int) 'L' - 32] = 9;
        MULTIRELEASE_LASTOCC[(int) 'A' - 32] = 11;
        MULTIRELEASE_LASTOCC[(int) 'S' - 32] = 12;
        MULTIRELEASE_LASTOCC[(int) ':' - 32] = 14;
        MULTIRELEASE_LASTOCC[(int) ' ' - 32] = 15;
        MULTIRELEASE_LASTOCC[(int) 'T' - 32] = 16;
        MULTIRELEASE_LASTOCC[(int) 'R' - 32] = 17;
        MULTIRELEASE_LASTOCC[(int) 'U' - 32] = 18;
        MULTIRELEASE_LASTOCC[(int) 'E' - 32] = 19;
        for (int i = 0; i < 17; i++) {
            MULTIRELEASE_OPTOSFT[i] = 19;
        }
        MULTIRELEASE_OPTOSFT[17] = 6;
        MULTIRELEASE_OPTOSFT[18] = 1;
    }

    private JarEntry getManEntry() {
        if (manEntry == null) {
            JarEntry manEntry = getEntry0(MANIFEST_NAME);
            if (manEntry == null) {
                String[] names = getMetaInfEntryNames();
                if (names != null) {
                    for (String name : names) {
                        if (MANIFEST_NAME.equals(name.toUpperCase(Locale.ENGLISH))) {
                            manEntry = getEntry0(name);
                            break;
                        }
                    }
                }
            }
            this.manEntry = manEntry;
        }
        return manEntry;
    }

    boolean hasClassPathAttribute() throws IOException {
        checkForSpecialAttributes();
        return hasClassPathAttribute;
    }

    private int match(byte[] src, byte[] b, byte[] lastOcc, byte[] optoSft) {
        int len = src.length;
        int last = b.length - len;
        int i = 0;
        next: while (i <= last) {
            for (int j = (len - 1); j >= 0; j--) {
                byte c = b[i + j];
                if (c >= ' ' && c <= 'z') {
                    if (c >= 'a')
                        c -= 32;
                    if (c != src[j]) {
                        int badShift = lastOcc[c - 32];
                        i += Math.max(j + 1 - badShift, optoSft[j]);
                        continue next;
                    }
                } else {
                    i += len;
                    continue next;
                }
            }
            return i;
        }
        return -1;
    }

    private void checkForSpecialAttributes() throws IOException {
        if (hasCheckedSpecialAttributes) {
            return;
        }
        synchronized (this) {
            if (hasCheckedSpecialAttributes) {
                return;
            }
            JarEntry manEntry = getManEntry();
            if (manEntry != null) {
                byte[] b = getBytes(manEntry);
                hasClassPathAttribute = match(CLASSPATH_CHARS, b, CLASSPATH_LASTOCC, CLASSPATH_OPTOSFT) != -1;
                if (MULTI_RELEASE_ENABLED) {
                    int i = match(MULTIRELEASE_CHARS, b, MULTIRELEASE_LASTOCC, MULTIRELEASE_OPTOSFT);
                    if (i != -1) {
                        i += MULTIRELEASE_CHARS.length;
                        if (i < b.length) {
                            byte c = b[i++];
                            if (c == '\n' && (i == b.length || b[i] != ' ')) {
                                isMultiRelease = true;
                            } else if (c == '\r') {
                                if (i == b.length) {
                                    isMultiRelease = true;
                                } else {
                                    c = b[i++];
                                    if (c == '\n') {
                                        if (i == b.length || b[i] != ' ') {
                                            isMultiRelease = true;
                                        }
                                    } else if (c != ' ') {
                                        isMultiRelease = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            hasCheckedSpecialAttributes = true;
        }
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

    JarEntry newEntry(JarEntry je) {
        if (isMultiRelease()) {
            return getVersionedEntry(je.getName(), je);
        }
        return je;
    }

    JarEntry newEntry(String name) {
        if (isMultiRelease()) {
            JarEntry vje = getVersionedEntry(name, (JarEntry) null);
            if (vje != null) {
                return vje;
            }
        }
        return new JarFileEntry(name);
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
            return jv.entries2(this, JUZFA.entries(JarFile.this, JarFileEntry::new));
        }
        final var unfilteredEntries = JUZFA.entries(JarFile.this, JarFileEntry::new);
        return new Enumeration<>() {

            JarEntry entry;

            public boolean hasMoreElements() {
                if (entry != null) {
                    return true;
                }
                while (unfilteredEntries.hasMoreElements()) {
                    JarEntry je = unfilteredEntries.nextElement();
                    if (JarVerifier.isSigningRelated(je.getName())) {
                        continue;
                    }
                    entry = je;
                    return true;
                }
                return false;
            }

            public JarEntry nextElement() {
                if (hasMoreElements()) {
                    JarEntry je = entry;
                    entry = null;
                    return newEntry(je);
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
