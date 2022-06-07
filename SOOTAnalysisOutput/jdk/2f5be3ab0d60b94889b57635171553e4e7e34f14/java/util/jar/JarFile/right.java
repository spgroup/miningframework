package java.util.jar;

import java.io.*;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.*;
import java.util.zip.*;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.security.AccessController;
import java.security.CodeSource;
import sun.misc.IOUtils;
import sun.security.action.GetPropertyAction;
import sun.security.util.ManifestEntryVerifier;
import sun.misc.SharedSecrets;

public class JarFile extends ZipFile {

    private SoftReference<Manifest> manRef;

    private JarEntry manEntry;

    private JarVerifier jv;

    private boolean jvInitialized;

    private boolean verify;

    private boolean computedHasClassPathAttribute;

    private boolean hasClassPathAttribute;

    static {
        SharedSecrets.setJavaUtilJarAccess(new JavaUtilJarAccessImpl());
    }

    public static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";

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
        super(file, mode);
        this.verify = verify;
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

    private native String[] getMetaInfEntryNames();

    public JarEntry getJarEntry(String name) {
        return (JarEntry) getEntry(name);
    }

    public ZipEntry getEntry(String name) {
        ZipEntry ze = super.getEntry(name);
        if (ze != null) {
            return new JarFileEntry(ze);
        }
        return null;
    }

    public Enumeration<JarEntry> entries() {
        final Enumeration<? extends ZipEntry> enum_ = super.entries();
        return new Enumeration<JarEntry>() {

            public boolean hasMoreElements() {
                return enum_.hasMoreElements();
            }

            public JarFileEntry nextElement() {
                ZipEntry ze = enum_.nextElement();
                return new JarFileEntry(ze);
            }
        };
    }

    private class JarFileEntry extends JarEntry {

        JarFileEntry(ZipEntry ze) {
            super(ze);
        }

        public Attributes getAttributes() throws IOException {
            Manifest man = JarFile.this.getManifest();
            if (man != null) {
                return man.getAttributes(getName());
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
                certs = jv.getCerts(JarFile.this, this);
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
                signers = jv.getCodeSigners(JarFile.this, this);
            }
            return signers == null ? null : signers.clone();
        }
    }

    private void maybeInstantiateVerifier() throws IOException {
        if (jv != null) {
            return;
        }
        if (verify) {
            String[] names = getMetaInfEntryNames();
            if (names != null) {
                for (int i = 0; i < names.length; i++) {
                    String name = names[i].toUpperCase(Locale.ENGLISH);
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
                for (int i = 0; i < names.length; i++) {
                    JarEntry e = getJarEntry(names[i]);
                    if (e == null) {
                        throw new JarException("corrupted jar file");
                    }
                    if (!e.isDirectory()) {
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
            return IOUtils.readFully(is, (int) ze.getSize(), true);
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
        return new JarVerifier.VerifierStream(getManifestFromReference(), ze instanceof JarFileEntry ? (JarEntry) ze : getJarEntry(ze.getName()), super.getInputStream(ze), jv);
    }

    private static int[] lastOcc;

    private static int[] optoSft;

    private static char[] src = { 'c', 'l', 'a', 's', 's', '-', 'p', 'a', 't', 'h' };

    static {
        lastOcc = new int[128];
        optoSft = new int[10];
        lastOcc[(int) 'c'] = 1;
        lastOcc[(int) 'l'] = 2;
        lastOcc[(int) 's'] = 5;
        lastOcc[(int) '-'] = 6;
        lastOcc[(int) 'p'] = 7;
        lastOcc[(int) 'a'] = 8;
        lastOcc[(int) 't'] = 9;
        lastOcc[(int) 'h'] = 10;
        for (int i = 0; i < 9; i++) optoSft[i] = 10;
        optoSft[9] = 1;
    }

    private JarEntry getManEntry() {
        if (manEntry == null) {
            manEntry = getJarEntry(MANIFEST_NAME);
            if (manEntry == null) {
                String[] names = getMetaInfEntryNames();
                if (names != null) {
                    for (int i = 0; i < names.length; i++) {
                        if (MANIFEST_NAME.equals(names[i].toUpperCase(Locale.ENGLISH))) {
                            manEntry = getJarEntry(names[i]);
                            break;
                        }
                    }
                }
            }
        }
        return manEntry;
    }

    boolean hasClassPathAttribute() throws IOException {
        if (computedHasClassPathAttribute) {
            return hasClassPathAttribute;
        }
        hasClassPathAttribute = false;
        if (!isKnownToNotHaveClassPathAttribute()) {
            JarEntry manEntry = getManEntry();
            if (manEntry != null) {
                byte[] b = getBytes(manEntry);
                int last = b.length - src.length;
                int i = 0;
                next: while (i <= last) {
                    for (int j = 9; j >= 0; j--) {
                        char c = (char) b[i + j];
                        c = (((c - 'A') | ('Z' - c)) >= 0) ? (char) (c + 32) : c;
                        if (c != src[j]) {
                            i += Math.max(j + 1 - lastOcc[c & 0x7F], optoSft[j]);
                            continue next;
                        }
                    }
                    hasClassPathAttribute = true;
                    break;
                }
            }
        }
        computedHasClassPathAttribute = true;
        return hasClassPathAttribute;
    }

    private static String javaHome;

    private static String[] jarNames;

    private boolean isKnownToNotHaveClassPathAttribute() {
        if (javaHome == null) {
            javaHome = AccessController.doPrivileged(new GetPropertyAction("java.home"));
        }
        if (jarNames == null) {
            String[] names = new String[10];
            String fileSep = File.separator;
            int i = 0;
            names[i++] = fileSep + "rt.jar";
            names[i++] = fileSep + "sunrsasign.jar";
            names[i++] = fileSep + "jsse.jar";
            names[i++] = fileSep + "jce.jar";
            names[i++] = fileSep + "charsets.jar";
            names[i++] = fileSep + "dnsns.jar";
            names[i++] = fileSep + "ldapsec.jar";
            names[i++] = fileSep + "localedata.jar";
            names[i++] = fileSep + "sunjce_provider.jar";
            names[i++] = fileSep + "sunpkcs11.jar";
            jarNames = names;
        }
        String name = getName();
        String localJavaHome = javaHome;
        if (name.startsWith(localJavaHome)) {
            String[] names = jarNames;
            for (int i = 0; i < names.length; i++) {
                if (name.endsWith(names[i])) {
                    return true;
                }
            }
        }
        return false;
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
        for (int i = 0; i < cs.length; i++) {
            if (cs[i].getCodeSigners() == null) {
                includeUnsigned = true;
                break;
            }
        }
        if (includeUnsigned) {
            return unsignedEntryNames();
        } else {
            return new Enumeration<String>() {

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
        return new Enumeration<JarEntry>() {

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
        return new Enumeration<String>() {

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
        return new ArrayList<Object>();
    }
}
