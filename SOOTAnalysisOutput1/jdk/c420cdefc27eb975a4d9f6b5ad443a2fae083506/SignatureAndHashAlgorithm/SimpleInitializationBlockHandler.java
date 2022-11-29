package sun.security.ssl;

import java.security.AlgorithmConstraints;
import java.security.CryptoPrimitive;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.EnumSet;
import java.util.TreeMap;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;

final class SignatureAndHashAlgorithm {

    final static int SUPPORTED_ALG_PRIORITY_MAX_NUM = 0x00F0;

    private final static Set<CryptoPrimitive> SIGNATURE_PRIMITIVE_SET = EnumSet.of(CryptoPrimitive.SIGNATURE);

    private final static Map<Integer, SignatureAndHashAlgorithm> supportedMap;

    private final static Map<Integer, SignatureAndHashAlgorithm> priorityMap;

    private HashAlgorithm hash;

    private SignatureAlgorithm signature;

    private int id;

    private String algorithm;

    private int priority;

    private SignatureAndHashAlgorithm(HashAlgorithm hash, SignatureAlgorithm signature, String algorithm, int priority) {
        this.hash = hash;
        this.signature = signature;
        this.algorithm = algorithm;
        this.id = ((hash.value & 0xFF) << 8) | (signature.value & 0xFF);
        this.priority = priority;
    }

    private SignatureAndHashAlgorithm(String algorithm, int id, int sequence) {
        this.hash = HashAlgorithm.valueOf((id >> 8) & 0xFF);
        this.signature = SignatureAlgorithm.valueOf(id & 0xFF);
        this.algorithm = algorithm;
        this.id = id;
        this.priority = SUPPORTED_ALG_PRIORITY_MAX_NUM + sequence + 1;
    }

    static SignatureAndHashAlgorithm valueOf(int hash, int signature, int sequence) {
        hash &= 0xFF;
        signature &= 0xFF;
        int id = (hash << 8) | signature;
        SignatureAndHashAlgorithm signAlg = supportedMap.get(id);
        if (signAlg == null) {
            signAlg = new SignatureAndHashAlgorithm("Unknown (hash:0x" + Integer.toString(hash, 16) + ", signature:0x" + Integer.toString(signature, 16) + ")", id, sequence);
        }
        return signAlg;
    }

    int getHashValue() {
        return (id >> 8) & 0xFF;
    }

    int getSignatureValue() {
        return id & 0xFF;
    }

    String getAlgorithmName() {
        return algorithm;
    }

    static int sizeInRecord() {
        return 2;
    }

    static Collection<SignatureAndHashAlgorithm> getSupportedAlgorithms(AlgorithmConstraints constraints) {
        Collection<SignatureAndHashAlgorithm> supported = new ArrayList<SignatureAndHashAlgorithm>();
        synchronized (priorityMap) {
            for (SignatureAndHashAlgorithm sigAlg : priorityMap.values()) {
                if (sigAlg.priority <= SUPPORTED_ALG_PRIORITY_MAX_NUM && constraints.permits(SIGNATURE_PRIMITIVE_SET, sigAlg.algorithm, null)) {
                    supported.add(sigAlg);
                }
            }
        }
        return supported;
    }

    static Collection<SignatureAndHashAlgorithm> getSupportedAlgorithms(Collection<SignatureAndHashAlgorithm> algorithms) {
        Collection<SignatureAndHashAlgorithm> supported = new ArrayList<SignatureAndHashAlgorithm>();
        for (SignatureAndHashAlgorithm sigAlg : algorithms) {
            if (sigAlg.priority <= SUPPORTED_ALG_PRIORITY_MAX_NUM) {
                supported.add(sigAlg);
            }
        }
        return supported;
    }

    static String[] getAlgorithmNames(Collection<SignatureAndHashAlgorithm> algorithms) {
        ArrayList<String> algorithmNames = new ArrayList<String>();
        if (algorithms != null) {
            for (SignatureAndHashAlgorithm sigAlg : algorithms) {
                algorithmNames.add(sigAlg.algorithm);
            }
        }
        String[] array = new String[algorithmNames.size()];
        return algorithmNames.toArray(array);
    }

    static Set<String> getHashAlgorithmNames(Collection<SignatureAndHashAlgorithm> algorithms) {
        Set<String> algorithmNames = new HashSet<String>();
        if (algorithms != null) {
            for (SignatureAndHashAlgorithm sigAlg : algorithms) {
                if (sigAlg.hash.value > 0) {
                    algorithmNames.add(sigAlg.hash.standardName);
                }
            }
        }
        return algorithmNames;
    }

    static String getHashAlgorithmName(SignatureAndHashAlgorithm algorithm) {
        return algorithm.hash.standardName;
    }

    private static void supports(HashAlgorithm hash, SignatureAlgorithm signature, String algorithm, int priority) {
        SignatureAndHashAlgorithm pair = new SignatureAndHashAlgorithm(hash, signature, algorithm, priority);
        if (supportedMap.put(pair.id, pair) != null) {
            throw new RuntimeException("Duplicate SignatureAndHashAlgorithm definition, id: " + pair.id);
        }
        if (priorityMap.put(pair.priority, pair) != null) {
            throw new RuntimeException("Duplicate SignatureAndHashAlgorithm definition, priority: " + pair.priority);
        }
    }

    static SignatureAndHashAlgorithm getPreferableAlgorithm(Collection<SignatureAndHashAlgorithm> algorithms, String expected) {
        if (expected == null && !algorithms.isEmpty()) {
            for (SignatureAndHashAlgorithm sigAlg : algorithms) {
                if (sigAlg.priority <= SUPPORTED_ALG_PRIORITY_MAX_NUM) {
                    return sigAlg;
                }
            }
            return null;
        }
        for (SignatureAndHashAlgorithm algorithm : algorithms) {
            int signValue = algorithm.id & 0xFF;
            if ((expected.equalsIgnoreCase("dsa") && signValue == SignatureAlgorithm.DSA.value) || (expected.equalsIgnoreCase("rsa") && signValue == SignatureAlgorithm.RSA.value) || (expected.equalsIgnoreCase("ecdsa") && signValue == SignatureAlgorithm.ECDSA.value) || (expected.equalsIgnoreCase("ec") && signValue == SignatureAlgorithm.ECDSA.value)) {
                return algorithm;
            }
        }
        return null;
    }

    static enum HashAlgorithm {

        UNDEFINED("undefined", "", -1),
        NONE("none", "NONE", 0),
        MD5("md5", "MD5", 1),
        SHA1("sha1", "SHA-1", 2),
        SHA224("sha224", "SHA-224", 3),
        SHA256("sha256", "SHA-256", 4),
        SHA384("sha384", "SHA-384", 5),
        SHA512("sha512", "SHA-512", 6);

        final String name;

        final String standardName;

        final int value;

        private HashAlgorithm(String name, String standardName, int value) {
            this.name = name;
            this.standardName = standardName;
            this.value = value;
        }

        static HashAlgorithm valueOf(int value) {
            HashAlgorithm algorithm = UNDEFINED;
            switch(value) {
                case 0:
                    algorithm = NONE;
                    break;
                case 1:
                    algorithm = MD5;
                    break;
                case 2:
                    algorithm = SHA1;
                    break;
                case 3:
                    algorithm = SHA224;
                    break;
                case 4:
                    algorithm = SHA256;
                    break;
                case 5:
                    algorithm = SHA384;
                    break;
                case 6:
                    algorithm = SHA512;
                    break;
            }
            return algorithm;
        }
    }

    static enum SignatureAlgorithm {

        UNDEFINED("undefined", -1), ANONYMOUS("anonymous", 0), RSA("rsa", 1), DSA("dsa", 2), ECDSA("ecdsa", 3);

        final String name;

        final int value;

        private SignatureAlgorithm(String name, int value) {
            this.name = name;
            this.value = value;
        }

        static SignatureAlgorithm valueOf(int value) {
            SignatureAlgorithm algorithm = UNDEFINED;
            switch(value) {
                case 0:
                    algorithm = ANONYMOUS;
                    break;
                case 1:
                    algorithm = RSA;
                    break;
                case 2:
                    algorithm = DSA;
                    break;
                case 3:
                    algorithm = ECDSA;
                    break;
            }
            return algorithm;
        }
    }

    static {
        supportedMap = Collections.synchronizedSortedMap(new TreeMap<Integer, SignatureAndHashAlgorithm>());
        priorityMap = Collections.synchronizedSortedMap(new TreeMap<Integer, SignatureAndHashAlgorithm>());
        synchronized (supportedMap) {
            int p = SUPPORTED_ALG_PRIORITY_MAX_NUM;
            supports(HashAlgorithm.MD5, SignatureAlgorithm.RSA, "MD5withRSA", --p);
            supports(HashAlgorithm.SHA1, SignatureAlgorithm.DSA, "SHA1withDSA", --p);
            supports(HashAlgorithm.SHA1, SignatureAlgorithm.RSA, "SHA1withRSA", --p);
            supports(HashAlgorithm.SHA1, SignatureAlgorithm.ECDSA, "SHA1withECDSA", --p);
            supports(HashAlgorithm.SHA224, SignatureAlgorithm.RSA, "SHA224withRSA", --p);
            supports(HashAlgorithm.SHA224, SignatureAlgorithm.ECDSA, "SHA224withECDSA", --p);
            supports(HashAlgorithm.SHA256, SignatureAlgorithm.RSA, "SHA256withRSA", --p);
            supports(HashAlgorithm.SHA256, SignatureAlgorithm.ECDSA, "SHA256withECDSA", --p);
            supports(HashAlgorithm.SHA384, SignatureAlgorithm.RSA, "SHA384withRSA", --p);
            supports(HashAlgorithm.SHA384, SignatureAlgorithm.ECDSA, "SHA384withECDSA", --p);
            supports(HashAlgorithm.SHA512, SignatureAlgorithm.RSA, "SHA512withRSA", --p);
            supports(HashAlgorithm.SHA512, SignatureAlgorithm.ECDSA, "SHA512withECDSA", --p);
        }
    }
}