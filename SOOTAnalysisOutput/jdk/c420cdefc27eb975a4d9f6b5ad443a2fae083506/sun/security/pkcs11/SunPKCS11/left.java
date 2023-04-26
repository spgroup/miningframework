package sun.security.pkcs11;

import java.io.*;
import java.util.*;
import java.security.*;
import java.security.interfaces.*;
import javax.crypto.interfaces.*;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import sun.security.util.Debug;
import sun.security.util.ResourcesMgr;
import sun.security.pkcs11.Secmod.*;
import sun.security.pkcs11.wrapper.*;
import static sun.security.pkcs11.wrapper.PKCS11Constants.*;

public final class SunPKCS11 extends AuthProvider {

    private static final long serialVersionUID = -1354835039035306505L;

    static final Debug debug = Debug.getInstance("sunpkcs11");

    private static int dummyConfigId;

    final PKCS11 p11;

    private final String configName;

    final Config config;

    final long slotID;

    private CallbackHandler pHandler;

    private final Object LOCK_HANDLER = new Object();

    final boolean removable;

    final Module nssModule;

    final boolean nssUseSecmodTrust;

    private volatile Token token;

    private TokenPoller poller;

    Token getToken() {
        return token;
    }

    public SunPKCS11() {
        super("SunPKCS11-Dummy", 1.7d, "SunPKCS11-Dummy");
        throw new ProviderException("SunPKCS11 requires configuration file argument");
    }

    public SunPKCS11(String configName) {
        this(checkNull(configName), null);
    }

    public SunPKCS11(InputStream configStream) {
        this(getDummyConfigName(), checkNull(configStream));
    }

    private static <T> T checkNull(T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }

    private static synchronized String getDummyConfigName() {
        int id = ++dummyConfigId;
        return "---DummyConfig-" + id + "---";
    }

    @Deprecated
    public SunPKCS11(String configName, InputStream configStream) {
        super("SunPKCS11-" + Config.getConfig(configName, configStream).getName(), 1.7d, Config.getConfig(configName, configStream).getDescription());
        this.configName = configName;
        this.config = Config.removeConfig(configName);
        if (debug != null) {
            System.out.println("SunPKCS11 loading " + configName);
        }
        String library = config.getLibrary();
        String functionList = config.getFunctionList();
        long slotID = config.getSlotID();
        int slotListIndex = config.getSlotListIndex();
        boolean useSecmod = config.getNssUseSecmod();
        boolean nssUseSecmodTrust = config.getNssUseSecmodTrust();
        Module nssModule = null;
        if (useSecmod) {
            Secmod secmod = Secmod.getInstance();
            DbMode nssDbMode = config.getNssDbMode();
            try {
                String nssLibraryDirectory = config.getNssLibraryDirectory();
                String nssSecmodDirectory = config.getNssSecmodDirectory();
                if (secmod.isInitialized()) {
                    if (nssSecmodDirectory != null) {
                        String s = secmod.getConfigDir();
                        if ((s != null) && (s.equals(nssSecmodDirectory) == false)) {
                            throw new ProviderException("Secmod directory " + nssSecmodDirectory + " invalid, NSS already initialized with " + s);
                        }
                    }
                    if (nssLibraryDirectory != null) {
                        String s = secmod.getLibDir();
                        if ((s != null) && (s.equals(nssLibraryDirectory) == false)) {
                            throw new ProviderException("NSS library directory " + nssLibraryDirectory + " invalid, NSS already initialized with " + s);
                        }
                    }
                } else {
                    if (nssDbMode != DbMode.NO_DB) {
                        if (nssSecmodDirectory == null) {
                            throw new ProviderException("Secmod not initialized and " + "nssSecmodDirectory not specified");
                        }
                    } else {
                        if (nssSecmodDirectory != null) {
                            throw new ProviderException("nssSecmodDirectory must not be " + "specified in noDb mode");
                        }
                    }
                    secmod.initialize(nssDbMode, nssSecmodDirectory, nssLibraryDirectory);
                }
            } catch (IOException e) {
                throw new ProviderException("Could not initialize NSS", e);
            }
            List<Module> modules = secmod.getModules();
            if (config.getShowInfo()) {
                System.out.println("NSS modules: " + modules);
            }
            String moduleName = config.getNssModule();
            if (moduleName == null) {
                nssModule = secmod.getModule(ModuleType.FIPS);
                if (nssModule != null) {
                    moduleName = "fips";
                } else {
                    moduleName = (nssDbMode == DbMode.NO_DB) ? "crypto" : "keystore";
                }
            }
            if (moduleName.equals("fips")) {
                nssModule = secmod.getModule(ModuleType.FIPS);
                nssUseSecmodTrust = true;
                functionList = "FC_GetFunctionList";
            } else if (moduleName.equals("keystore")) {
                nssModule = secmod.getModule(ModuleType.KEYSTORE);
                nssUseSecmodTrust = true;
            } else if (moduleName.equals("crypto")) {
                nssModule = secmod.getModule(ModuleType.CRYPTO);
            } else if (moduleName.equals("trustanchors")) {
                nssModule = secmod.getModule(ModuleType.TRUSTANCHOR);
                nssUseSecmodTrust = true;
            } else if (moduleName.startsWith("external-")) {
                int moduleIndex;
                try {
                    moduleIndex = Integer.parseInt(moduleName.substring("external-".length()));
                } catch (NumberFormatException e) {
                    moduleIndex = -1;
                }
                if (moduleIndex < 1) {
                    throw new ProviderException("Invalid external module: " + moduleName);
                }
                int k = 0;
                for (Module module : modules) {
                    if (module.getType() == ModuleType.EXTERNAL) {
                        if (++k == moduleIndex) {
                            nssModule = module;
                            break;
                        }
                    }
                }
                if (nssModule == null) {
                    throw new ProviderException("Invalid module " + moduleName + ": only " + k + " external NSS modules available");
                }
            } else {
                throw new ProviderException("Unknown NSS module: " + moduleName);
            }
            if (nssModule == null) {
                throw new ProviderException("NSS module not available: " + moduleName);
            }
            if (nssModule.hasInitializedProvider()) {
                throw new ProviderException("Secmod module already configured");
            }
            library = nssModule.libraryName;
            slotListIndex = nssModule.slot;
        }
        this.nssUseSecmodTrust = nssUseSecmodTrust;
        this.nssModule = nssModule;
        File libraryFile = new File(library);
        if (libraryFile.getName().equals(library) == false) {
            if (new File(library).isFile() == false) {
                String msg = "Library " + library + " does not exist";
                if (config.getHandleStartupErrors() == Config.ERR_HALT) {
                    throw new ProviderException(msg);
                } else {
                    throw new UnsupportedOperationException(msg);
                }
            }
        }
        try {
            if (debug != null) {
                debug.println("Initializing PKCS#11 library " + library);
            }
            CK_C_INITIALIZE_ARGS initArgs = new CK_C_INITIALIZE_ARGS();
            String nssArgs = config.getNssArgs();
            if (nssArgs != null) {
                initArgs.pReserved = nssArgs;
            }
            initArgs.flags = CKF_OS_LOCKING_OK;
            PKCS11 tmpPKCS11;
            try {
                tmpPKCS11 = PKCS11.getInstance(library, functionList, initArgs, config.getOmitInitialize());
            } catch (PKCS11Exception e) {
                if (debug != null) {
                    debug.println("Multi-threaded initialization failed: " + e);
                }
                if (config.getAllowSingleThreadedModules() == false) {
                    throw e;
                }
                if (nssArgs == null) {
                    initArgs = null;
                } else {
                    initArgs.flags = 0;
                }
                tmpPKCS11 = PKCS11.getInstance(library, functionList, initArgs, config.getOmitInitialize());
            }
            p11 = tmpPKCS11;
            CK_INFO p11Info = p11.C_GetInfo();
            if (p11Info.cryptokiVersion.major < 2) {
                throw new ProviderException("Only PKCS#11 v2.0 and later " + "supported, library version is v" + p11Info.cryptokiVersion);
            }
            boolean showInfo = config.getShowInfo();
            if (showInfo) {
                System.out.println("Information for provider " + getName());
                System.out.println("Library info:");
                System.out.println(p11Info);
            }
            if ((slotID < 0) || showInfo) {
                long[] slots = p11.C_GetSlotList(false);
                if (showInfo) {
                    System.out.println("All slots: " + toString(slots));
                    slots = p11.C_GetSlotList(true);
                    System.out.println("Slots with tokens: " + toString(slots));
                }
                if (slotID < 0) {
                    if ((slotListIndex < 0) || (slotListIndex >= slots.length)) {
                        throw new ProviderException("slotListIndex is " + slotListIndex + " but token only has " + slots.length + " slots");
                    }
                    slotID = slots[slotListIndex];
                }
            }
            this.slotID = slotID;
            CK_SLOT_INFO slotInfo = p11.C_GetSlotInfo(slotID);
            removable = (slotInfo.flags & CKF_REMOVABLE_DEVICE) != 0;
            initToken(slotInfo);
            if (nssModule != null) {
                nssModule.setProvider(this);
            }
        } catch (Exception e) {
            if (config.getHandleStartupErrors() == Config.ERR_IGNORE_ALL) {
                throw new UnsupportedOperationException("Initialization failed", e);
            } else {
                throw new ProviderException("Initialization failed", e);
            }
        }
    }

    private static String toString(long[] longs) {
        if (longs.length == 0) {
            return "(none)";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(longs[0]);
        for (int i = 1; i < longs.length; i++) {
            sb.append(", ");
            sb.append(longs[i]);
        }
        return sb.toString();
    }

    public boolean equals(Object obj) {
        return this == obj;
    }

    public int hashCode() {
        return System.identityHashCode(this);
    }

    private static String[] s(String s1) {
        return new String[] { s1 };
    }

    private static String[] s(String s1, String s2) {
        return new String[] { s1, s2 };
    }

    private static final class Descriptor {

        final String type;

        final String algorithm;

        final String className;

        final String[] aliases;

        final int[] mechanisms;

        private Descriptor(String type, String algorithm, String className, String[] aliases, int[] mechanisms) {
            this.type = type;
            this.algorithm = algorithm;
            this.className = className;
            this.aliases = aliases;
            this.mechanisms = mechanisms;
        }

        private P11Service service(Token token, int mechanism) {
            return new P11Service(token, type, algorithm, className, aliases, mechanism);
        }

        public String toString() {
            return type + "." + algorithm;
        }
    }

    private final static Map<Integer, List<Descriptor>> descriptors = new HashMap<Integer, List<Descriptor>>();

    private static int[] m(long m1) {
        return new int[] { (int) m1 };
    }

    private static int[] m(long m1, long m2) {
        return new int[] { (int) m1, (int) m2 };
    }

    private static int[] m(long m1, long m2, long m3) {
        return new int[] { (int) m1, (int) m2, (int) m3 };
    }

    private static int[] m(long m1, long m2, long m3, long m4) {
        return new int[] { (int) m1, (int) m2, (int) m3, (int) m4 };
    }

    private static void d(String type, String algorithm, String className, int[] m) {
        register(new Descriptor(type, algorithm, className, null, m));
    }

    private static void d(String type, String algorithm, String className, String[] aliases, int[] m) {
        register(new Descriptor(type, algorithm, className, aliases, m));
    }

    private static void register(Descriptor d) {
        for (int i = 0; i < d.mechanisms.length; i++) {
            int m = d.mechanisms[i];
            Integer key = Integer.valueOf(m);
            List<Descriptor> list = descriptors.get(key);
            if (list == null) {
                list = new ArrayList<Descriptor>();
                descriptors.put(key, list);
            }
            list.add(d);
        }
    }

    private final static String MD = "MessageDigest";

    private final static String SIG = "Signature";

    private final static String KPG = "KeyPairGenerator";

    private final static String KG = "KeyGenerator";

    private final static String AGP = "AlgorithmParameters";

    private final static String KF = "KeyFactory";

    private final static String SKF = "SecretKeyFactory";

    private final static String CIP = "Cipher";

    private final static String MAC = "Mac";

    private final static String KA = "KeyAgreement";

    private final static String KS = "KeyStore";

    private final static String SR = "SecureRandom";

    static {
        String P11Digest = "sun.security.pkcs11.P11Digest";
        String P11MAC = "sun.security.pkcs11.P11MAC";
        String P11KeyPairGenerator = "sun.security.pkcs11.P11KeyPairGenerator";
        String P11KeyGenerator = "sun.security.pkcs11.P11KeyGenerator";
        String P11RSAKeyFactory = "sun.security.pkcs11.P11RSAKeyFactory";
        String P11DSAKeyFactory = "sun.security.pkcs11.P11DSAKeyFactory";
        String P11DHKeyFactory = "sun.security.pkcs11.P11DHKeyFactory";
        String P11KeyAgreement = "sun.security.pkcs11.P11KeyAgreement";
        String P11SecretKeyFactory = "sun.security.pkcs11.P11SecretKeyFactory";
        String P11Cipher = "sun.security.pkcs11.P11Cipher";
        String P11RSACipher = "sun.security.pkcs11.P11RSACipher";
        String P11Signature = "sun.security.pkcs11.P11Signature";
        d(MD, "MD2", P11Digest, m(CKM_MD2));
        d(MD, "MD5", P11Digest, m(CKM_MD5));
        d(MD, "SHA1", P11Digest, s("SHA", "SHA-1"), m(CKM_SHA_1));
        d(MD, "SHA-256", P11Digest, m(CKM_SHA256));
        d(MD, "SHA-384", P11Digest, m(CKM_SHA384));
        d(MD, "SHA-512", P11Digest, m(CKM_SHA512));
        d(MAC, "HmacMD5", P11MAC, m(CKM_MD5_HMAC));
        d(MAC, "HmacSHA1", P11MAC, m(CKM_SHA_1_HMAC));
        d(MAC, "HmacSHA256", P11MAC, m(CKM_SHA256_HMAC));
        d(MAC, "HmacSHA384", P11MAC, m(CKM_SHA384_HMAC));
        d(MAC, "HmacSHA512", P11MAC, m(CKM_SHA512_HMAC));
        d(MAC, "SslMacMD5", P11MAC, m(CKM_SSL3_MD5_MAC));
        d(MAC, "SslMacSHA1", P11MAC, m(CKM_SSL3_SHA1_MAC));
        d(KPG, "RSA", P11KeyPairGenerator, m(CKM_RSA_PKCS_KEY_PAIR_GEN));
        d(KPG, "DSA", P11KeyPairGenerator, m(CKM_DSA_KEY_PAIR_GEN));
        d(KPG, "DH", P11KeyPairGenerator, s("DiffieHellman"), m(CKM_DH_PKCS_KEY_PAIR_GEN));
        d(KPG, "EC", P11KeyPairGenerator, m(CKM_EC_KEY_PAIR_GEN));
        d(KG, "ARCFOUR", P11KeyGenerator, s("RC4"), m(CKM_RC4_KEY_GEN));
        d(KG, "DES", P11KeyGenerator, m(CKM_DES_KEY_GEN));
        d(KG, "DESede", P11KeyGenerator, m(CKM_DES3_KEY_GEN, CKM_DES2_KEY_GEN));
        d(KG, "AES", P11KeyGenerator, m(CKM_AES_KEY_GEN));
        d(KG, "Blowfish", P11KeyGenerator, m(CKM_BLOWFISH_KEY_GEN));
        d(KF, "RSA", P11RSAKeyFactory, m(CKM_RSA_PKCS_KEY_PAIR_GEN, CKM_RSA_PKCS, CKM_RSA_X_509));
        d(KF, "DSA", P11DSAKeyFactory, m(CKM_DSA_KEY_PAIR_GEN, CKM_DSA, CKM_DSA_SHA1));
        d(KF, "DH", P11DHKeyFactory, s("DiffieHellman"), m(CKM_DH_PKCS_KEY_PAIR_GEN, CKM_DH_PKCS_DERIVE));
        d(KF, "EC", P11DHKeyFactory, m(CKM_EC_KEY_PAIR_GEN, CKM_ECDH1_DERIVE, CKM_ECDSA, CKM_ECDSA_SHA1));
        d(AGP, "EC", "sun.security.ec.ECParameters", s("1.2.840.10045.2.1"), m(CKM_EC_KEY_PAIR_GEN, CKM_ECDH1_DERIVE, CKM_ECDSA, CKM_ECDSA_SHA1));
        d(KA, "DH", P11KeyAgreement, s("DiffieHellman"), m(CKM_DH_PKCS_DERIVE));
        d(KA, "ECDH", "sun.security.pkcs11.P11ECDHKeyAgreement", m(CKM_ECDH1_DERIVE));
        d(SKF, "ARCFOUR", P11SecretKeyFactory, s("RC4"), m(CKM_RC4));
        d(SKF, "DES", P11SecretKeyFactory, m(CKM_DES_CBC));
        d(SKF, "DESede", P11SecretKeyFactory, m(CKM_DES3_CBC));
        d(SKF, "AES", P11SecretKeyFactory, m(CKM_AES_CBC));
        d(SKF, "Blowfish", P11SecretKeyFactory, m(CKM_BLOWFISH_CBC));
        d(CIP, "ARCFOUR", P11Cipher, s("RC4"), m(CKM_RC4));
        d(CIP, "DES/CBC/NoPadding", P11Cipher, m(CKM_DES_CBC));
        d(CIP, "DES/CBC/PKCS5Padding", P11Cipher, m(CKM_DES_CBC_PAD, CKM_DES_CBC));
        d(CIP, "DES/ECB", P11Cipher, s("DES"), m(CKM_DES_ECB));
        d(CIP, "DESede/CBC/NoPadding", P11Cipher, m(CKM_DES3_CBC));
        d(CIP, "DESede/CBC/PKCS5Padding", P11Cipher, m(CKM_DES3_CBC_PAD, CKM_DES3_CBC));
        d(CIP, "DESede/ECB", P11Cipher, s("DESede"), m(CKM_DES3_ECB));
        d(CIP, "AES/CBC/NoPadding", P11Cipher, m(CKM_AES_CBC));
        d(CIP, "AES/CBC/PKCS5Padding", P11Cipher, m(CKM_AES_CBC_PAD, CKM_AES_CBC));
        d(CIP, "AES/ECB", P11Cipher, s("AES"), m(CKM_AES_ECB));
        d(CIP, "Blowfish/CBC", P11Cipher, m(CKM_BLOWFISH_CBC));
        d(CIP, "RSA/ECB/PKCS1Padding", P11RSACipher, m(CKM_RSA_PKCS));
        d(SIG, "RawDSA", P11Signature, s("NONEwithDSA"), m(CKM_DSA));
        d(SIG, "DSA", P11Signature, s("SHA1withDSA"), m(CKM_DSA_SHA1, CKM_DSA));
        d(SIG, "NONEwithECDSA", P11Signature, m(CKM_ECDSA));
        d(SIG, "SHA1withECDSA", P11Signature, s("ECDSA"), m(CKM_ECDSA_SHA1, CKM_ECDSA));
        d(SIG, "SHA256withECDSA", P11Signature, m(CKM_ECDSA));
        d(SIG, "SHA384withECDSA", P11Signature, m(CKM_ECDSA));
        d(SIG, "SHA512withECDSA", P11Signature, m(CKM_ECDSA));
        d(SIG, "MD2withRSA", P11Signature, m(CKM_MD2_RSA_PKCS, CKM_RSA_PKCS, CKM_RSA_X_509));
        d(SIG, "MD5withRSA", P11Signature, m(CKM_MD5_RSA_PKCS, CKM_RSA_PKCS, CKM_RSA_X_509));
        d(SIG, "SHA1withRSA", P11Signature, m(CKM_SHA1_RSA_PKCS, CKM_RSA_PKCS, CKM_RSA_X_509));
        d(SIG, "SHA256withRSA", P11Signature, m(CKM_SHA256_RSA_PKCS, CKM_RSA_PKCS, CKM_RSA_X_509));
        d(SIG, "SHA384withRSA", P11Signature, m(CKM_SHA384_RSA_PKCS, CKM_RSA_PKCS, CKM_RSA_X_509));
        d(SIG, "SHA512withRSA", P11Signature, m(CKM_SHA512_RSA_PKCS, CKM_RSA_PKCS, CKM_RSA_X_509));
        d(KG, "SunTlsRsaPremasterSecret", "sun.security.pkcs11.P11TlsRsaPremasterSecretGenerator", m(CKM_SSL3_PRE_MASTER_KEY_GEN, CKM_TLS_PRE_MASTER_KEY_GEN));
        d(KG, "SunTlsMasterSecret", "sun.security.pkcs11.P11TlsMasterSecretGenerator", m(CKM_SSL3_MASTER_KEY_DERIVE, CKM_TLS_MASTER_KEY_DERIVE, CKM_SSL3_MASTER_KEY_DERIVE_DH, CKM_TLS_MASTER_KEY_DERIVE_DH));
        d(KG, "SunTlsKeyMaterial", "sun.security.pkcs11.P11TlsKeyMaterialGenerator", m(CKM_SSL3_KEY_AND_MAC_DERIVE, CKM_TLS_KEY_AND_MAC_DERIVE));
        d(KG, "SunTlsPrf", "sun.security.pkcs11.P11TlsPrfGenerator", m(CKM_TLS_PRF, CKM_NSS_TLS_PRF_GENERAL));
    }

    private static class TokenPoller implements Runnable {

        private final SunPKCS11 provider;

        private volatile boolean enabled;

        private TokenPoller(SunPKCS11 provider) {
            this.provider = provider;
            enabled = true;
        }

        public void run() {
            int interval = provider.config.getInsertionCheckInterval();
            while (enabled) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    break;
                }
                if (enabled == false) {
                    break;
                }
                try {
                    provider.initToken(null);
                } catch (PKCS11Exception e) {
                }
            }
        }

        void disable() {
            enabled = false;
        }
    }

    private void createPoller() {
        if (poller != null) {
            return;
        }
        TokenPoller poller = new TokenPoller(this);
        Thread t = new Thread(poller, "Poller " + getName());
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
        this.poller = poller;
    }

    private void destroyPoller() {
        if (poller != null) {
            poller.disable();
            poller = null;
        }
    }

    private boolean hasValidToken() {
        Token token = this.token;
        return (token != null) && token.isValid();
    }

    synchronized void uninitToken(Token token) {
        if (this.token != token) {
            return;
        }
        destroyPoller();
        this.token = null;
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            public Object run() {
                clear();
                return null;
            }
        });
        createPoller();
    }

    private void initToken(CK_SLOT_INFO slotInfo) throws PKCS11Exception {
        if (slotInfo == null) {
            slotInfo = p11.C_GetSlotInfo(slotID);
        }
        if (removable && (slotInfo.flags & CKF_TOKEN_PRESENT) == 0) {
            createPoller();
            return;
        }
        destroyPoller();
        boolean showInfo = config.getShowInfo();
        if (showInfo) {
            System.out.println("Slot info for slot " + slotID + ":");
            System.out.println(slotInfo);
        }
        final Token token = new Token(this);
        if (showInfo) {
            System.out.println("Token info for token in slot " + slotID + ":");
            System.out.println(token.tokenInfo);
        }
        long[] supportedMechanisms = p11.C_GetMechanismList(slotID);
        final Map<Descriptor, Integer> supportedAlgs = new HashMap<Descriptor, Integer>();
        for (int i = 0; i < supportedMechanisms.length; i++) {
            long longMech = supportedMechanisms[i];
            boolean isEnabled = config.isEnabled(longMech);
            if (showInfo) {
                CK_MECHANISM_INFO mechInfo = p11.C_GetMechanismInfo(slotID, longMech);
                System.out.println("Mechanism " + Functions.getMechanismName(longMech) + ":");
                if (isEnabled == false) {
                    System.out.println("DISABLED in configuration");
                }
                System.out.println(mechInfo);
            }
            if (isEnabled == false) {
                continue;
            }
            if (longMech >>> 32 != 0) {
                continue;
            }
            int mech = (int) longMech;
            Integer integerMech = Integer.valueOf(mech);
            List<Descriptor> ds = descriptors.get(integerMech);
            if (ds == null) {
                continue;
            }
            for (Descriptor d : ds) {
                Integer oldMech = supportedAlgs.get(d);
                if (oldMech == null) {
                    supportedAlgs.put(d, integerMech);
                    continue;
                }
                int intOldMech = oldMech.intValue();
                for (int j = 0; j < d.mechanisms.length; j++) {
                    int nextMech = d.mechanisms[j];
                    if (mech == nextMech) {
                        supportedAlgs.put(d, integerMech);
                        break;
                    } else if (intOldMech == nextMech) {
                        break;
                    }
                }
            }
        }
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            public Object run() {
                for (Map.Entry<Descriptor, Integer> entry : supportedAlgs.entrySet()) {
                    Descriptor d = entry.getKey();
                    int mechanism = entry.getValue().intValue();
                    Service s = d.service(token, mechanism);
                    putService(s);
                }
                if (((token.tokenInfo.flags & CKF_RNG) != 0) && config.isEnabled(PCKM_SECURERANDOM) && !token.sessionManager.lowMaxSessions()) {
                    putService(new P11Service(token, SR, "PKCS11", "sun.security.pkcs11.P11SecureRandom", null, PCKM_SECURERANDOM));
                }
                if (config.isEnabled(PCKM_KEYSTORE)) {
                    putService(new P11Service(token, KS, "PKCS11", "sun.security.pkcs11.P11KeyStore", s("PKCS11-" + config.getName()), PCKM_KEYSTORE));
                }
                return null;
            }
        });
        this.token = token;
    }

    private static final class P11Service extends Service {

        private final Token token;

        private final long mechanism;

        P11Service(Token token, String type, String algorithm, String className, String[] al, long mechanism) {
            super(token.provider, type, algorithm, className, toList(al), null);
            this.token = token;
            this.mechanism = mechanism & 0xFFFFFFFFL;
        }

        private static List<String> toList(String[] aliases) {
            return (aliases == null) ? null : Arrays.asList(aliases);
        }

        public Object newInstance(Object param) throws NoSuchAlgorithmException {
            if (token.isValid() == false) {
                throw new NoSuchAlgorithmException("Token has been removed");
            }
            try {
                return newInstance0(param);
            } catch (PKCS11Exception e) {
                throw new NoSuchAlgorithmException(e);
            }
        }

        public Object newInstance0(Object param) throws PKCS11Exception, NoSuchAlgorithmException {
            String algorithm = getAlgorithm();
            String type = getType();
            if (type == MD) {
                return new P11Digest(token, algorithm, mechanism);
            } else if (type == CIP) {
                if (algorithm.startsWith("RSA")) {
                    return new P11RSACipher(token, algorithm, mechanism);
                } else {
                    return new P11Cipher(token, algorithm, mechanism);
                }
            } else if (type == SIG) {
                return new P11Signature(token, algorithm, mechanism);
            } else if (type == MAC) {
                return new P11Mac(token, algorithm, mechanism);
            } else if (type == KPG) {
                return new P11KeyPairGenerator(token, algorithm, mechanism);
            } else if (type == KA) {
                if (algorithm.equals("ECDH")) {
                    return new P11ECDHKeyAgreement(token, algorithm, mechanism);
                } else {
                    return new P11KeyAgreement(token, algorithm, mechanism);
                }
            } else if (type == KF) {
                return token.getKeyFactory(algorithm);
            } else if (type == SKF) {
                return new P11SecretKeyFactory(token, algorithm);
            } else if (type == KG) {
                if (algorithm == "SunTlsRsaPremasterSecret") {
                    return new P11TlsRsaPremasterSecretGenerator(token, algorithm, mechanism);
                } else if (algorithm == "SunTlsMasterSecret") {
                    return new P11TlsMasterSecretGenerator(token, algorithm, mechanism);
                } else if (algorithm == "SunTlsKeyMaterial") {
                    return new P11TlsKeyMaterialGenerator(token, algorithm, mechanism);
                } else if (algorithm == "SunTlsPrf") {
                    return new P11TlsPrfGenerator(token, algorithm, mechanism);
                } else {
                    return new P11KeyGenerator(token, algorithm, mechanism);
                }
            } else if (type == SR) {
                return token.getRandom();
            } else if (type == KS) {
                return token.getKeyStore();
            } else if (type == AGP) {
                return new sun.security.ec.ECParameters();
            } else {
                throw new NoSuchAlgorithmException("Unknown type: " + type);
            }
        }

        public boolean supportsParameter(Object param) {
            if ((param == null) || (token.isValid() == false)) {
                return false;
            }
            if (param instanceof Key == false) {
                throw new InvalidParameterException("Parameter must be a Key");
            }
            String algorithm = getAlgorithm();
            String type = getType();
            Key key = (Key) param;
            String keyAlgorithm = key.getAlgorithm();
            if (((type == CIP) && algorithm.startsWith("RSA")) || (type == SIG) && algorithm.endsWith("RSA")) {
                if (keyAlgorithm.equals("RSA") == false) {
                    return false;
                }
                return isLocalKey(key) || (key instanceof RSAPrivateKey) || (key instanceof RSAPublicKey);
            }
            if (((type == KA) && algorithm.equals("ECDH")) || ((type == SIG) && algorithm.endsWith("ECDSA"))) {
                if (keyAlgorithm.equals("EC") == false) {
                    return false;
                }
                return isLocalKey(key) || (key instanceof ECPrivateKey) || (key instanceof ECPublicKey);
            }
            if ((type == SIG) && algorithm.endsWith("DSA")) {
                if (keyAlgorithm.equals("DSA") == false) {
                    return false;
                }
                return isLocalKey(key) || (key instanceof DSAPrivateKey) || (key instanceof DSAPublicKey);
            }
            if ((type == CIP) || (type == MAC)) {
                return isLocalKey(key) || "RAW".equals(key.getFormat());
            }
            if (type == KA) {
                if (keyAlgorithm.equals("DH") == false) {
                    return false;
                }
                return isLocalKey(key) || (key instanceof DHPrivateKey) || (key instanceof DHPublicKey);
            }
            throw new AssertionError("SunPKCS11 error: " + type + ", " + algorithm);
        }

        private boolean isLocalKey(Key key) {
            return (key instanceof P11Key) && (((P11Key) key).token == token);
        }

        public String toString() {
            return super.toString() + " (" + Functions.getMechanismName(mechanism) + ")";
        }
    }

    public void login(Subject subject, CallbackHandler handler) throws LoginException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (debug != null) {
                debug.println("checking login permission");
            }
            sm.checkPermission(new SecurityPermission("authProvider." + this.getName()));
        }
        if (hasValidToken() == false) {
            throw new LoginException("No token present");
        }
        if ((token.tokenInfo.flags & CKF_LOGIN_REQUIRED) == 0) {
            if (debug != null) {
                debug.println("login operation not required for token - " + "ignoring login request");
            }
            return;
        }
        try {
            if (token.isLoggedInNow(null)) {
                if (debug != null) {
                    debug.println("user already logged in");
                }
                return;
            }
        } catch (PKCS11Exception e) {
        }
        char[] pin = null;
        if ((token.tokenInfo.flags & CKF_PROTECTED_AUTHENTICATION_PATH) == 0) {
            CallbackHandler myHandler = getCallbackHandler(handler);
            if (myHandler == null) {
                throw new LoginException("no password provided, and no callback handler " + "available for retrieving password");
            }
            java.text.MessageFormat form = new java.text.MessageFormat(ResourcesMgr.getString("PKCS11.Token.providerName.Password."));
            Object[] source = { getName() };
            PasswordCallback pcall = new PasswordCallback(form.format(source), false);
            Callback[] callbacks = { pcall };
            try {
                myHandler.handle(callbacks);
            } catch (Exception e) {
                LoginException le = new LoginException("Unable to perform password callback");
                le.initCause(e);
                throw le;
            }
            pin = pcall.getPassword();
            pcall.clearPassword();
            if (pin == null) {
                if (debug != null) {
                    debug.println("caller passed NULL pin");
                }
            }
        }
        Session session = null;
        try {
            session = token.getOpSession();
            p11.C_Login(session.id(), CKU_USER, pin);
            if (debug != null) {
                debug.println("login succeeded");
            }
        } catch (PKCS11Exception pe) {
            if (pe.getErrorCode() == CKR_USER_ALREADY_LOGGED_IN) {
                if (debug != null) {
                    debug.println("user already logged in");
                }
                return;
            } else if (pe.getErrorCode() == CKR_PIN_INCORRECT) {
                FailedLoginException fle = new FailedLoginException();
                fle.initCause(pe);
                throw fle;
            } else {
                LoginException le = new LoginException();
                le.initCause(pe);
                throw le;
            }
        } finally {
            token.releaseSession(session);
            if (pin != null) {
                Arrays.fill(pin, ' ');
            }
        }
    }

    public void logout() throws LoginException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SecurityPermission("authProvider." + this.getName()));
        }
        if (hasValidToken() == false) {
            return;
        }
        if ((token.tokenInfo.flags & CKF_LOGIN_REQUIRED) == 0) {
            if (debug != null) {
                debug.println("logout operation not required for token - " + "ignoring logout request");
            }
            return;
        }
        try {
            if (token.isLoggedInNow(null) == false) {
                if (debug != null) {
                    debug.println("user not logged in");
                }
                return;
            }
        } catch (PKCS11Exception e) {
        }
        Session session = null;
        try {
            session = token.getOpSession();
            p11.C_Logout(session.id());
            if (debug != null) {
                debug.println("logout succeeded");
            }
        } catch (PKCS11Exception pe) {
            if (pe.getErrorCode() == CKR_USER_NOT_LOGGED_IN) {
                if (debug != null) {
                    debug.println("user not logged in");
                }
                return;
            }
            LoginException le = new LoginException();
            le.initCause(pe);
            throw le;
        } finally {
            token.releaseSession(session);
        }
    }

    public void setCallbackHandler(CallbackHandler handler) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SecurityPermission("authProvider." + this.getName()));
        }
        synchronized (LOCK_HANDLER) {
            pHandler = handler;
        }
    }

    private CallbackHandler getCallbackHandler(CallbackHandler handler) {
        if (handler != null) {
            return handler;
        }
        if (debug != null) {
            debug.println("getting provider callback handler");
        }
        synchronized (LOCK_HANDLER) {
            if (pHandler != null) {
                return pHandler;
            }
            try {
                if (debug != null) {
                    debug.println("getting default callback handler");
                }
                CallbackHandler myHandler = AccessController.doPrivileged(new PrivilegedExceptionAction<CallbackHandler>() {

                    public CallbackHandler run() throws Exception {
                        String defaultHandler = java.security.Security.getProperty("auth.login.defaultCallbackHandler");
                        if (defaultHandler == null || defaultHandler.length() == 0) {
                            if (debug != null) {
                                debug.println("no default handler set");
                            }
                            return null;
                        }
                        Class c = Class.forName(defaultHandler, true, Thread.currentThread().getContextClassLoader());
                        return (CallbackHandler) c.newInstance();
                    }
                });
                pHandler = myHandler;
                return myHandler;
            } catch (PrivilegedActionException pae) {
                if (debug != null) {
                    debug.println("Unable to load default callback handler");
                    pae.printStackTrace();
                }
            }
        }
        return null;
    }

    private Object writeReplace() throws ObjectStreamException {
        return new SunPKCS11Rep(this);
    }

    private static class SunPKCS11Rep implements Serializable {

        static final long serialVersionUID = -2896606995897745419L;

        private final String providerName;

        private final String configName;

        SunPKCS11Rep(SunPKCS11 provider) throws NotSerializableException {
            providerName = provider.getName();
            configName = provider.configName;
            if (Security.getProvider(providerName) != provider) {
                throw new NotSerializableException("Only SunPKCS11 providers " + "installed in java.security.Security can be serialized");
            }
        }

        private Object readResolve() throws ObjectStreamException {
            SunPKCS11 p = (SunPKCS11) Security.getProvider(providerName);
            if ((p == null) || (p.configName.equals(configName) == false)) {
                throw new NotSerializableException("Could not find " + providerName + " in installed providers");
            }
            return p;
        }
    }
}
