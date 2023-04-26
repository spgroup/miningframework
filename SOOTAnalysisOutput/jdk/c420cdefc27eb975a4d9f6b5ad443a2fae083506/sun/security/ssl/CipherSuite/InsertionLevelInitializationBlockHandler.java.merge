package sun.security.ssl;

import java.util.*;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import sun.security.ssl.CipherSuite.*;
import static sun.security.ssl.CipherSuite.KeyExchange.*;
import static sun.security.ssl.CipherSuite.PRF.*;
import static sun.security.ssl.JsseJce.*;

final class CipherSuite implements Comparable {

    final static int SUPPORTED_SUITES_PRIORITY = 1;

    final static int DEFAULT_SUITES_PRIORITY = 300;

    final static boolean DYNAMIC_AVAILABILITY = true;

    private final static boolean ALLOW_ECC = Debug.getBooleanProperty("com.sun.net.ssl.enableECC", true);

    private final static Map<Integer, CipherSuite> idMap;

    private final static Map<String, CipherSuite> nameMap;

    final String name;

    final int id;

    final int priority;

    final KeyExchange keyExchange;

    final BulkCipher cipher;

    final MacAlg macAlg;

    final PRF prfAlg;

    final boolean exportable;

    final boolean allowed;

    final int obsoleted;

    final int supported;

    private CipherSuite(String name, int id, int priority, KeyExchange keyExchange, BulkCipher cipher, boolean allowed, int obsoleted, int supported, PRF prfAlg) {
        this.name = name;
        this.id = id;
        this.priority = priority;
        this.keyExchange = keyExchange;
        this.cipher = cipher;
        this.exportable = cipher.exportable;
        if (name.endsWith("_MD5")) {
            macAlg = M_MD5;
        } else if (name.endsWith("_SHA")) {
            macAlg = M_SHA;
        } else if (name.endsWith("_SHA256")) {
            macAlg = M_SHA256;
        } else if (name.endsWith("_SHA384")) {
            macAlg = M_SHA384;
        } else if (name.endsWith("_NULL")) {
            macAlg = M_NULL;
        } else if (name.endsWith("_SCSV")) {
            macAlg = M_NULL;
        } else {
            throw new IllegalArgumentException("Unknown MAC algorithm for ciphersuite " + name);
        }
        allowed &= keyExchange.allowed;
        allowed &= cipher.allowed;
        this.allowed = allowed;
        this.obsoleted = obsoleted;
        this.supported = supported;
        this.prfAlg = prfAlg;
    }

    private CipherSuite(String name, int id) {
        this.name = name;
        this.id = id;
        this.allowed = false;
        this.priority = 0;
        this.keyExchange = null;
        this.cipher = null;
        this.macAlg = null;
        this.exportable = false;
        this.obsoleted = ProtocolVersion.LIMIT_MAX_VALUE;
        this.supported = ProtocolVersion.LIMIT_MIN_VALUE;
        this.prfAlg = P_NONE;
    }

    boolean isAvailable() {
        return allowed && keyExchange.isAvailable() && cipher.isAvailable();
    }

    boolean isNegotiable() {
        return this != C_SCSV && isAvailable();
    }

    public int compareTo(Object o) {
        return ((CipherSuite) o).priority - priority;
    }

    public String toString() {
        return name;
    }

    static CipherSuite valueOf(String s) {
        if (s == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        CipherSuite c = nameMap.get(s);
        if ((c == null) || (c.allowed == false)) {
            throw new IllegalArgumentException("Unsupported ciphersuite " + s);
        }
        return c;
    }

    static CipherSuite valueOf(int id1, int id2) {
        id1 &= 0xff;
        id2 &= 0xff;
        int id = (id1 << 8) | id2;
        CipherSuite c = idMap.get(id);
        if (c == null) {
            String h1 = Integer.toString(id1, 16);
            String h2 = Integer.toString(id2, 16);
            c = new CipherSuite("Unknown 0x" + h1 + ":0x" + h2, id);
        }
        return c;
    }

    static Collection<CipherSuite> allowedCipherSuites() {
        return nameMap.values();
    }

    private static void add(String name, int id, int priority, KeyExchange keyExchange, BulkCipher cipher, boolean allowed, int obsoleted, int supported, PRF prf) {
        CipherSuite c = new CipherSuite(name, id, priority, keyExchange, cipher, allowed, obsoleted, supported, prf);
        if (idMap.put(id, c) != null) {
            throw new RuntimeException("Duplicate ciphersuite definition: " + id + ", " + name);
        }
        if (c.allowed) {
            if (nameMap.put(name, c) != null) {
                throw new RuntimeException("Duplicate ciphersuite definition: " + id + ", " + name);
            }
        }
    }

    private static void add(String name, int id, int priority, KeyExchange keyExchange, BulkCipher cipher, boolean allowed, int obsoleted) {
        PRF prf = P_SHA256;
        if (obsoleted < ProtocolVersion.TLS12.v) {
            prf = P_NONE;
        }
        add(name, id, priority, keyExchange, cipher, allowed, obsoleted, ProtocolVersion.LIMIT_MIN_VALUE, prf);
    }

    private static void add(String name, int id, int priority, KeyExchange keyExchange, BulkCipher cipher, boolean allowed) {
        add(name, id, priority, keyExchange, cipher, allowed, ProtocolVersion.LIMIT_MAX_VALUE);
    }

    private static void add(String name, int id) {
        CipherSuite c = new CipherSuite(name, id);
        if (idMap.put(id, c) != null) {
            throw new RuntimeException("Duplicate ciphersuite definition: " + id + ", " + name);
        }
    }

    static enum KeyExchange {

        K_NULL("NULL", false),
        K_RSA("RSA", true),
        K_RSA_EXPORT("RSA_EXPORT", true),
        K_DH_RSA("DH_RSA", false),
        K_DH_DSS("DH_DSS", false),
        K_DHE_DSS("DHE_DSS", true),
        K_DHE_RSA("DHE_RSA", true),
        K_DH_ANON("DH_anon", true),
        K_ECDH_ECDSA("ECDH_ECDSA", ALLOW_ECC),
        K_ECDH_RSA("ECDH_RSA", ALLOW_ECC),
        K_ECDHE_ECDSA("ECDHE_ECDSA", ALLOW_ECC),
        K_ECDHE_RSA("ECDHE_RSA", ALLOW_ECC),
        K_ECDH_ANON("ECDH_anon", ALLOW_ECC),
        K_KRB5("KRB5", true),
        K_KRB5_EXPORT("KRB5_EXPORT", true),
        K_SCSV("SCSV", true);

        final String name;

        final boolean allowed;

        private final boolean alwaysAvailable;

        KeyExchange(String name, boolean allowed) {
            this.name = name;
            this.allowed = allowed;
            this.alwaysAvailable = allowed && (!name.startsWith("EC")) && (!name.startsWith("KRB"));
        }

        boolean isAvailable() {
            if (alwaysAvailable) {
                return true;
            }
            if (name.startsWith("EC")) {
                return (allowed && JsseJce.isEcAvailable());
            } else if (name.startsWith("KRB")) {
                return (allowed && JsseJce.isKerberosAvailable());
            } else {
                return allowed;
            }
        }

        public String toString() {
            return name;
        }
    }

    final static class BulkCipher {

        private final static Map<BulkCipher, Boolean> availableCache = new HashMap<BulkCipher, Boolean>(8);

        final String description;

        final String transformation;

        final String algorithm;

        final boolean allowed;

        final int keySize;

        final int expandedKeySize;

        final int ivSize;

        final boolean exportable;

        BulkCipher(String transformation, int keySize, int expandedKeySize, int ivSize, boolean allowed) {
            this.transformation = transformation;
            this.algorithm = transformation.split("/")[0];
            this.description = this.algorithm + "/" + (keySize << 3);
            this.keySize = keySize;
            this.ivSize = ivSize;
            this.allowed = allowed;
            this.expandedKeySize = expandedKeySize;
            this.exportable = true;
        }

        BulkCipher(String transformation, int keySize, int ivSize, boolean allowed) {
            this.transformation = transformation;
            this.algorithm = transformation.split("/")[0];
            this.description = this.algorithm + "/" + (keySize << 3);
            this.keySize = keySize;
            this.ivSize = ivSize;
            this.allowed = allowed;
            this.expandedKeySize = keySize;
            this.exportable = false;
        }

        CipherBox newCipher(ProtocolVersion version, SecretKey key, IvParameterSpec iv, SecureRandom random, boolean encrypt) throws NoSuchAlgorithmException {
            return CipherBox.newCipherBox(version, this, key, iv, random, encrypt);
        }

        boolean isAvailable() {
            if (allowed == false) {
                return false;
            }
            if (this == B_AES_256) {
                return isAvailable(this);
            }
            return true;
        }

        static synchronized void clearAvailableCache() {
            if (DYNAMIC_AVAILABILITY) {
                availableCache.clear();
            }
        }

        private static synchronized boolean isAvailable(BulkCipher cipher) {
            Boolean b = availableCache.get(cipher);
            if (b == null) {
                try {
                    SecretKey key = new SecretKeySpec(new byte[cipher.expandedKeySize], cipher.algorithm);
                    IvParameterSpec iv = new IvParameterSpec(new byte[cipher.ivSize]);
                    cipher.newCipher(ProtocolVersion.DEFAULT, key, iv, null, true);
                    b = Boolean.TRUE;
                } catch (NoSuchAlgorithmException e) {
                    b = Boolean.FALSE;
                }
                availableCache.put(cipher, b);
            }
            return b.booleanValue();
        }

        public String toString() {
            return description;
        }
    }

    final static class MacAlg {

        final String name;

        final int size;

        MacAlg(String name, int size) {
            this.name = name;
            this.size = size;
        }

        MAC newMac(ProtocolVersion protocolVersion, SecretKey secret) throws NoSuchAlgorithmException, InvalidKeyException {
            return new MAC(this, protocolVersion, secret);
        }

        public String toString() {
            return name;
        }
    }

    final static BulkCipher B_NULL = new BulkCipher("NULL", 0, 0, 0, true);

    final static BulkCipher B_RC4_40 = new BulkCipher(CIPHER_RC4, 5, 16, 0, true);

    final static BulkCipher B_RC2_40 = new BulkCipher("RC2", 5, 16, 8, false);

    final static BulkCipher B_DES_40 = new BulkCipher(CIPHER_DES, 5, 8, 8, true);

    final static BulkCipher B_RC4_128 = new BulkCipher(CIPHER_RC4, 16, 0, true);

    final static BulkCipher B_DES = new BulkCipher(CIPHER_DES, 8, 8, true);

    final static BulkCipher B_3DES = new BulkCipher(CIPHER_3DES, 24, 8, true);

    final static BulkCipher B_IDEA = new BulkCipher("IDEA", 16, 8, false);

    final static BulkCipher B_AES_128 = new BulkCipher(CIPHER_AES, 16, 16, true);

    final static BulkCipher B_AES_256 = new BulkCipher(CIPHER_AES, 32, 16, true);

    final static MacAlg M_NULL = new MacAlg("NULL", 0);

    final static MacAlg M_MD5 = new MacAlg("MD5", 16);

    final static MacAlg M_SHA = new MacAlg("SHA", 20);

    final static MacAlg M_SHA256 = new MacAlg("SHA256", 32);

    final static MacAlg M_SHA384 = new MacAlg("SHA384", 48);

    static enum PRF {

        P_NONE("NONE", 0, 0), P_SHA256("SHA-256", 32, 64), P_SHA384("SHA-384", 48, 128), P_SHA512("SHA-512", 64, 128);

        private final String prfHashAlg;

        private final int prfHashLength;

        private final int prfBlockSize;

        PRF(String prfHashAlg, int prfHashLength, int prfBlockSize) {
            this.prfHashAlg = prfHashAlg;
            this.prfHashLength = prfHashLength;
            this.prfBlockSize = prfBlockSize;
        }

        String getPRFHashAlg() {
            return prfHashAlg;
        }

        int getPRFHashLength() {
            return prfHashLength;
        }

        int getPRFBlockSize() {
            return prfBlockSize;
        }
    }

    static {
        idMap = new HashMap<Integer, CipherSuite>();
        nameMap = new HashMap<String, CipherSuite>();
        final boolean F = false;
        final boolean T = true;
        final boolean N = (SunJSSE.isFIPS() == false);
        add("SSL_NULL_WITH_NULL_NULL", 0x0000, 1, K_NULL, B_NULL, F);
        int p = DEFAULT_SUITES_PRIORITY * 2;
        int max = ProtocolVersion.LIMIT_MAX_VALUE;
        int tls11 = ProtocolVersion.TLS11.v;
        int tls12 = ProtocolVersion.TLS12.v;
        add("TLS_RSA_WITH_AES_128_CBC_SHA256", 0x003c, --p, K_RSA, B_AES_128, T, max, tls12, P_SHA256);
        add("TLS_RSA_WITH_AES_256_CBC_SHA256", 0x003d, --p, K_RSA, B_AES_256, T, max, tls12, P_SHA256);
        add("TLS_DHE_DSS_WITH_AES_128_CBC_SHA256", 0x0040, --p, K_DHE_DSS, B_AES_128, T, max, tls12, P_SHA256);
        add("TLS_DHE_RSA_WITH_AES_128_CBC_SHA256", 0x0067, --p, K_DHE_RSA, B_AES_128, T, max, tls12, P_SHA256);
        add("TLS_DHE_DSS_WITH_AES_256_CBC_SHA256", 0x006a, --p, K_DHE_DSS, B_AES_256, T, max, tls12, P_SHA256);
        add("TLS_DHE_RSA_WITH_AES_256_CBC_SHA256", 0x006b, --p, K_DHE_RSA, B_AES_256, T, max, tls12, P_SHA256);
        add("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256", 0xc023, --p, K_ECDHE_ECDSA, B_AES_128, T, max, tls12, P_SHA256);
        add("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384", 0xc024, --p, K_ECDHE_ECDSA, B_AES_256, T, max, tls12, P_SHA384);
        add("TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256", 0xc025, --p, K_ECDH_ECDSA, B_AES_128, T, max, tls12, P_SHA256);
        add("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384", 0xc026, --p, K_ECDH_ECDSA, B_AES_256, T, max, tls12, P_SHA384);
        add("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", 0xc027, --p, K_ECDHE_RSA, B_AES_128, T, max, tls12, P_SHA256);
        add("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384", 0xc028, --p, K_ECDHE_RSA, B_AES_256, T, max, tls12, P_SHA384);
        add("TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256", 0xc029, --p, K_ECDH_RSA, B_AES_128, T, max, tls12, P_SHA256);
        add("TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384", 0xc02a, --p, K_ECDH_RSA, B_AES_256, T, max, tls12, P_SHA384);
        add("SSL_RSA_WITH_RC4_128_MD5", 0x0004, --p, K_RSA, B_RC4_128, N);
        add("SSL_RSA_WITH_RC4_128_SHA", 0x0005, --p, K_RSA, B_RC4_128, N);
        add("TLS_RSA_WITH_AES_128_CBC_SHA", 0x002f, --p, K_RSA, B_AES_128, T);
        add("TLS_RSA_WITH_AES_256_CBC_SHA", 0x0035, --p, K_RSA, B_AES_256, T);
        add("TLS_ECDH_ECDSA_WITH_RC4_128_SHA", 0xC002, --p, K_ECDH_ECDSA, B_RC4_128, N);
        add("TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA", 0xC004, --p, K_ECDH_ECDSA, B_AES_128, T);
        add("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA", 0xC005, --p, K_ECDH_ECDSA, B_AES_256, T);
        add("TLS_ECDH_RSA_WITH_RC4_128_SHA", 0xC00C, --p, K_ECDH_RSA, B_RC4_128, N);
        add("TLS_ECDH_RSA_WITH_AES_128_CBC_SHA", 0xC00E, --p, K_ECDH_RSA, B_AES_128, T);
        add("TLS_ECDH_RSA_WITH_AES_256_CBC_SHA", 0xC00F, --p, K_ECDH_RSA, B_AES_256, T);
        add("TLS_ECDHE_ECDSA_WITH_RC4_128_SHA", 0xC007, --p, K_ECDHE_ECDSA, B_RC4_128, N);
        add("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA", 0xC009, --p, K_ECDHE_ECDSA, B_AES_128, T);
        add("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA", 0xC00A, --p, K_ECDHE_ECDSA, B_AES_256, T);
        add("TLS_ECDHE_RSA_WITH_RC4_128_SHA", 0xC011, --p, K_ECDHE_RSA, B_RC4_128, N);
        add("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", 0xC013, --p, K_ECDHE_RSA, B_AES_128, T);
        add("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", 0xC014, --p, K_ECDHE_RSA, B_AES_256, T);
        add("TLS_DHE_RSA_WITH_AES_128_CBC_SHA", 0x0033, --p, K_DHE_RSA, B_AES_128, T);
        add("TLS_DHE_RSA_WITH_AES_256_CBC_SHA", 0x0039, --p, K_DHE_RSA, B_AES_256, T);
        add("TLS_DHE_DSS_WITH_AES_128_CBC_SHA", 0x0032, --p, K_DHE_DSS, B_AES_128, T);
        add("TLS_DHE_DSS_WITH_AES_256_CBC_SHA", 0x0038, --p, K_DHE_DSS, B_AES_256, T);
        add("SSL_RSA_WITH_3DES_EDE_CBC_SHA", 0x000a, --p, K_RSA, B_3DES, T);
        add("TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA", 0xC003, --p, K_ECDH_ECDSA, B_3DES, T);
        add("TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA", 0xC00D, --p, K_ECDH_RSA, B_3DES, T);
        add("TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA", 0xC008, --p, K_ECDHE_ECDSA, B_3DES, T);
        add("TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA", 0xC012, --p, K_ECDHE_RSA, B_3DES, T);
        add("SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", 0x0016, --p, K_DHE_RSA, B_3DES, T);
        add("SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", 0x0013, --p, K_DHE_DSS, B_3DES, N);
        add("TLS_EMPTY_RENEGOTIATION_INFO_SCSV", 0x00ff, --p, K_SCSV, B_NULL, T);
        p = DEFAULT_SUITES_PRIORITY;
        add("SSL_RSA_WITH_DES_CBC_SHA", 0x0009, --p, K_RSA, B_DES, N, tls12);
        add("SSL_DHE_RSA_WITH_DES_CBC_SHA", 0x0015, --p, K_DHE_RSA, B_DES, N, tls12);
        add("SSL_DHE_DSS_WITH_DES_CBC_SHA", 0x0012, --p, K_DHE_DSS, B_DES, N, tls12);
        add("SSL_RSA_WITH_NULL_MD5", 0x0001, --p, K_RSA, B_NULL, N);
        add("SSL_RSA_WITH_NULL_SHA", 0x0002, --p, K_RSA, B_NULL, N);
        add("TLS_RSA_WITH_NULL_SHA256", 0x003b, --p, K_RSA, B_NULL, N, max, tls12, P_SHA256);
        add("TLS_ECDH_ECDSA_WITH_NULL_SHA", 0xC001, --p, K_ECDH_ECDSA, B_NULL, N);
        add("TLS_ECDH_RSA_WITH_NULL_SHA", 0xC00B, --p, K_ECDH_RSA, B_NULL, N);
        add("TLS_ECDHE_ECDSA_WITH_NULL_SHA", 0xC006, --p, K_ECDHE_ECDSA, B_NULL, N);
        add("TLS_ECDHE_RSA_WITH_NULL_SHA", 0xC010, --p, K_ECDHE_RSA, B_NULL, N);
        add("SSL_DH_anon_WITH_RC4_128_MD5", 0x0018, --p, K_DH_ANON, B_RC4_128, N);
        add("TLS_DH_anon_WITH_AES_128_CBC_SHA", 0x0034, --p, K_DH_ANON, B_AES_128, N);
        add("TLS_DH_anon_WITH_AES_256_CBC_SHA", 0x003a, --p, K_DH_ANON, B_AES_256, N);
        add("SSL_DH_anon_WITH_3DES_EDE_CBC_SHA", 0x001b, --p, K_DH_ANON, B_3DES, N);
        add("SSL_DH_anon_WITH_DES_CBC_SHA", 0x001a, --p, K_DH_ANON, B_DES, N, tls12);
        add("TLS_DH_anon_WITH_AES_128_CBC_SHA256", 0x006c, --p, K_DH_ANON, B_AES_128, N, max, tls12, P_SHA256);
        add("TLS_DH_anon_WITH_AES_256_CBC_SHA256", 0x006d, --p, K_DH_ANON, B_AES_256, N, max, tls12, P_SHA256);
        add("TLS_ECDH_anon_WITH_RC4_128_SHA", 0xC016, --p, K_ECDH_ANON, B_RC4_128, N);
        add("TLS_ECDH_anon_WITH_AES_128_CBC_SHA", 0xC018, --p, K_ECDH_ANON, B_AES_128, T);
        add("TLS_ECDH_anon_WITH_AES_256_CBC_SHA", 0xC019, --p, K_ECDH_ANON, B_AES_256, T);
        add("TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA", 0xC017, --p, K_ECDH_ANON, B_3DES, T);
        add("SSL_DH_anon_EXPORT_WITH_RC4_40_MD5", 0x0017, --p, K_DH_ANON, B_RC4_40, N, tls11);
        add("SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA", 0x0019, --p, K_DH_ANON, B_DES_40, N, tls11);
        add("TLS_ECDH_anon_WITH_NULL_SHA", 0xC015, --p, K_ECDH_ANON, B_NULL, N);
        add("SSL_RSA_EXPORT_WITH_RC4_40_MD5", 0x0003, --p, K_RSA_EXPORT, B_RC4_40, N, tls11);
        add("SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", 0x0008, --p, K_RSA_EXPORT, B_DES_40, N, tls11);
        add("SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", 0x0014, --p, K_DHE_RSA, B_DES_40, N, tls11);
        add("SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", 0x0011, --p, K_DHE_DSS, B_DES_40, N, tls11);
        add("TLS_KRB5_WITH_RC4_128_SHA", 0x0020, --p, K_KRB5, B_RC4_128, N);
        add("TLS_KRB5_WITH_RC4_128_MD5", 0x0024, --p, K_KRB5, B_RC4_128, N);
        add("TLS_KRB5_WITH_3DES_EDE_CBC_SHA", 0x001f, --p, K_KRB5, B_3DES, N);
        add("TLS_KRB5_WITH_3DES_EDE_CBC_MD5", 0x0023, --p, K_KRB5, B_3DES, N);
        add("TLS_KRB5_WITH_DES_CBC_SHA", 0x001e, --p, K_KRB5, B_DES, N, tls12);
        add("TLS_KRB5_WITH_DES_CBC_MD5", 0x0022, --p, K_KRB5, B_DES, N, tls12);
        add("TLS_KRB5_EXPORT_WITH_RC4_40_SHA", 0x0028, --p, K_KRB5_EXPORT, B_RC4_40, N, tls11);
        add("TLS_KRB5_EXPORT_WITH_RC4_40_MD5", 0x002b, --p, K_KRB5_EXPORT, B_RC4_40, N, tls11);
        add("TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA", 0x0026, --p, K_KRB5_EXPORT, B_DES_40, N, tls11);
        add("TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5", 0x0029, --p, K_KRB5_EXPORT, B_DES_40, N, tls11);
        add("SSL_RSA_EXPORT_WITH_RC2_CBC_40_MD5", 0x0006);
        add("SSL_RSA_WITH_IDEA_CBC_SHA", 0x0007);
        add("SSL_DH_DSS_EXPORT_WITH_DES40_CBC_SHA", 0x000b);
        add("SSL_DH_DSS_WITH_DES_CBC_SHA", 0x000c);
        add("SSL_DH_DSS_WITH_3DES_EDE_CBC_SHA", 0x000d);
        add("SSL_DH_RSA_EXPORT_WITH_DES40_CBC_SHA", 0x000e);
        add("SSL_DH_RSA_WITH_DES_CBC_SHA", 0x000f);
        add("SSL_DH_RSA_WITH_3DES_EDE_CBC_SHA", 0x0010);
        add("SSL_FORTEZZA_DMS_WITH_NULL_SHA", 0x001c);
        add("SSL_FORTEZZA_DMS_WITH_FORTEZZA_CBC_SHA", 0x001d);
        add("SSL_RSA_EXPORT1024_WITH_DES_CBC_SHA", 0x0062);
        add("SSL_DHE_DSS_EXPORT1024_WITH_DES_CBC_SHA", 0x0063);
        add("SSL_RSA_EXPORT1024_WITH_RC4_56_SHA", 0x0064);
        add("SSL_DHE_DSS_EXPORT1024_WITH_RC4_56_SHA", 0x0065);
        add("SSL_DHE_DSS_WITH_RC4_128_SHA", 0x0066);
        add("NETSCAPE_RSA_FIPS_WITH_3DES_EDE_CBC_SHA", 0xffe0);
        add("NETSCAPE_RSA_FIPS_WITH_DES_CBC_SHA", 0xffe1);
        add("SSL_RSA_FIPS_WITH_DES_CBC_SHA", 0xfefe);
        add("SSL_RSA_FIPS_WITH_3DES_EDE_CBC_SHA", 0xfeff);
        add("TLS_KRB5_WITH_IDEA_CBC_SHA", 0x0021);
        add("TLS_KRB5_WITH_IDEA_CBC_MD5", 0x0025);
        add("TLS_KRB5_EXPORT_WITH_RC2_CBC_40_SHA", 0x0027);
        add("TLS_KRB5_EXPORT_WITH_RC2_CBC_40_MD5", 0x002a);
        add("TLS_RSA_WITH_SEED_CBC_SHA", 0x0096);
        add("TLS_DH_DSS_WITH_SEED_CBC_SHA", 0x0097);
        add("TLS_DH_RSA_WITH_SEED_CBC_SHA", 0x0098);
        add("TLS_DHE_DSS_WITH_SEED_CBC_SHA", 0x0099);
        add("TLS_DHE_RSA_WITH_SEED_CBC_SHA", 0x009a);
        add("TLS_DH_anon_WITH_SEED_CBC_SHA", 0x009b);
        add("TLS_PSK_WITH_RC4_128_SHA", 0x008a);
        add("TLS_PSK_WITH_3DES_EDE_CBC_SHA", 0x008b);
        add("TLS_PSK_WITH_AES_128_CBC_SHA", 0x008c);
        add("TLS_PSK_WITH_AES_256_CBC_SHA", 0x008d);
        add("TLS_DHE_PSK_WITH_RC4_128_SHA", 0x008e);
        add("TLS_DHE_PSK_WITH_3DES_EDE_CBC_SHA", 0x008f);
        add("TLS_DHE_PSK_WITH_AES_128_CBC_SHA", 0x0090);
        add("TLS_DHE_PSK_WITH_AES_256_CBC_SHA", 0x0091);
        add("TLS_RSA_PSK_WITH_RC4_128_SHA", 0x0092);
        add("TLS_RSA_PSK_WITH_3DES_EDE_CBC_SHA", 0x0093);
        add("TLS_RSA_PSK_WITH_AES_128_CBC_SHA", 0x0094);
        add("TLS_RSA_PSK_WITH_AES_256_CBC_SHA", 0x0095);
        add("TLS_PSK_WITH_NULL_SHA", 0x002c);
        add("TLS_DHE_PSK_WITH_NULL_SHA", 0x002d);
        add("TLS_RSA_PSK_WITH_NULL_SHA", 0x002e);
        add("TLS_DH_DSS_WITH_AES_128_CBC_SHA", 0x0030);
        add("TLS_DH_RSA_WITH_AES_128_CBC_SHA", 0x0031);
        add("TLS_DH_DSS_WITH_AES_256_CBC_SHA", 0x0036);
        add("TLS_DH_RSA_WITH_AES_256_CBC_SHA", 0x0037);
        add("TLS_DH_DSS_WITH_AES_128_CBC_SHA256", 0x003e);
        add("TLS_DH_RSA_WITH_AES_128_CBC_SHA256", 0x003f);
        add("TLS_DH_DSS_WITH_AES_256_CBC_SHA256", 0x0068);
        add("TLS_DH_RSA_WITH_AES_256_CBC_SHA256", 0x0069);
        add("TLS_RSA_WITH_AES_128_GCM_SHA256", 0x009c);
        add("TLS_RSA_WITH_AES_256_GCM_SHA384", 0x009d);
        add("TLS_DHE_RSA_WITH_AES_128_GCM_SHA256", 0x009e);
        add("TLS_DHE_RSA_WITH_AES_256_GCM_SHA384", 0x009f);
        add("TLS_DH_RSA_WITH_AES_128_GCM_SHA256", 0x00a0);
        add("TLS_DH_RSA_WITH_AES_256_GCM_SHA384", 0x00a1);
        add("TLS_DHE_DSS_WITH_AES_128_GCM_SHA256", 0x00a2);
        add("TLS_DHE_DSS_WITH_AES_256_GCM_SHA384", 0x00a3);
        add("TLS_DH_DSS_WITH_AES_128_GCM_SHA256", 0x00a4);
        add("TLS_DH_DSS_WITH_AES_256_GCM_SHA384", 0x00a5);
        add("TLS_DH_anon_WITH_AES_128_GCM_SHA256", 0x00a6);
        add("TLS_DH_anon_WITH_AES_256_GCM_SHA384", 0x00a7);
        add("TLS_PSK_WITH_AES_128_GCM_SHA256", 0x00a8);
        add("TLS_PSK_WITH_AES_256_GCM_SHA384", 0x00a9);
        add("TLS_DHE_PSK_WITH_AES_128_GCM_SHA256", 0x00aa);
        add("TLS_DHE_PSK_WITH_AES_256_GCM_SHA384", 0x00ab);
        add("TLS_RSA_PSK_WITH_AES_128_GCM_SHA256", 0x00ac);
        add("TLS_RSA_PSK_WITH_AES_256_GCM_SHA384", 0x00ad);
        add("TLS_PSK_WITH_AES_128_CBC_SHA256", 0x00ae);
        add("TLS_PSK_WITH_AES_256_CBC_SHA384", 0x00af);
        add("TLS_PSK_WITH_NULL_SHA256", 0x00b0);
        add("TLS_PSK_WITH_NULL_SHA384", 0x00b1);
        add("TLS_DHE_PSK_WITH_AES_128_CBC_SHA256", 0x00b2);
        add("TLS_DHE_PSK_WITH_AES_256_CBC_SHA384", 0x00b3);
        add("TLS_DHE_PSK_WITH_NULL_SHA256", 0x00b4);
        add("TLS_DHE_PSK_WITH_NULL_SHA384", 0x00b5);
        add("TLS_RSA_PSK_WITH_AES_128_CBC_SHA256", 0x00b6);
        add("TLS_RSA_PSK_WITH_AES_256_CBC_SHA384", 0x00b7);
        add("TLS_RSA_PSK_WITH_NULL_SHA256", 0x00b8);
        add("TLS_RSA_PSK_WITH_NULL_SHA384", 0x00b9);
        add("TLS_RSA_WITH_CAMELLIA_128_CBC_SHA", 0x0041);
        add("TLS_DH_DSS_WITH_CAMELLIA_128_CBC_SHA", 0x0042);
        add("TLS_DH_RSA_WITH_CAMELLIA_128_CBC_SHA", 0x0043);
        add("TLS_DHE_DSS_WITH_CAMELLIA_128_CBC_SHA", 0x0044);
        add("TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA", 0x0045);
        add("TLS_DH_anon_WITH_CAMELLIA_128_CBC_SHA", 0x0046);
        add("TLS_RSA_WITH_CAMELLIA_256_CBC_SHA", 0x0084);
        add("TLS_DH_DSS_WITH_CAMELLIA_256_CBC_SHA", 0x0085);
        add("TLS_DH_RSA_WITH_CAMELLIA_256_CBC_SHA", 0x0086);
        add("TLS_DHE_DSS_WITH_CAMELLIA_256_CBC_SHA", 0x0087);
        add("TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA", 0x0088);
        add("TLS_DH_anon_WITH_CAMELLIA_256_CBC_SHA", 0x0089);
        add("TLS_RSA_WITH_CAMELLIA_128_CBC_SHA256", 0x00ba);
        add("TLS_DH_DSS_WITH_CAMELLIA_128_CBC_SHA256", 0x00bb);
        add("TLS_DH_RSA_WITH_CAMELLIA_128_CBC_SHA256", 0x00bc);
        add("TLS_DHE_DSS_WITH_CAMELLIA_128_CBC_SHA256", 0x00bd);
        add("TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA256", 0x00be);
        add("TLS_DH_anon_WITH_CAMELLIA_128_CBC_SHA256", 0x00bf);
        add("TLS_RSA_WITH_CAMELLIA_256_CBC_SHA256", 0x00c0);
        add("TLS_DH_DSS_WITH_CAMELLIA_256_CBC_SHA256", 0x00c1);
        add("TLS_DH_RSA_WITH_CAMELLIA_256_CBC_SHA256", 0x00c2);
        add("TLS_DHE_DSS_WITH_CAMELLIA_256_CBC_SHA256", 0x00c3);
        add("TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA256", 0x00c4);
        add("TLS_DH_anon_WITH_CAMELLIA_256_CBC_SHA256", 0x00c5);
        add("TLS_SRP_SHA_WITH_3DES_EDE_CBC_SHA", 0xc01a);
        add("TLS_SRP_SHA_RSA_WITH_3DES_EDE_CBC_SHA", 0xc01b);
        add("TLS_SRP_SHA_DSS_WITH_3DES_EDE_CBC_SHA", 0xc01c);
        add("TLS_SRP_SHA_WITH_AES_128_CBC_SHA", 0xc01d);
        add("TLS_SRP_SHA_RSA_WITH_AES_128_CBC_SHA", 0xc01e);
        add("TLS_SRP_SHA_DSS_WITH_AES_128_CBC_SHA", 0xc01f);
        add("TLS_SRP_SHA_WITH_AES_256_CBC_SHA", 0xc020);
        add("TLS_SRP_SHA_RSA_WITH_AES_256_CBC_SHA", 0xc021);
        add("TLS_SRP_SHA_DSS_WITH_AES_256_CBC_SHA", 0xc022);
        add("TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", 0xc02b);
        add("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", 0xc02c);
        add("TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256", 0xc02d);
        add("TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384", 0xc02e);
        add("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", 0xc02f);
        add("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", 0xc030);
        add("TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256", 0xc031);
        add("TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384", 0xc032);
        add("TLS_ECDHE_PSK_WITH_RC4_128_SHA", 0xc033);
        add("TLS_ECDHE_PSK_WITH_3DES_EDE_CBC_SHA", 0xc034);
        add("TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA", 0xc035);
        add("TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA", 0xc036);
        add("TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA256", 0xc037);
        add("TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA384", 0xc038);
        add("TLS_ECDHE_PSK_WITH_NULL_SHA", 0xc039);
        add("TLS_ECDHE_PSK_WITH_NULL_SHA256", 0xc03a);
        add("TLS_ECDHE_PSK_WITH_NULL_SHA384", 0xc03b);
    }

    final static CipherSuite C_NULL = CipherSuite.valueOf(0, 0);

    final static CipherSuite C_SCSV = CipherSuite.valueOf(0x00, 0xff);
}