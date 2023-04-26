package sun.security.ssl;

import java.util.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import javax.crypto.*;
import java.security.Provider;
import sun.security.jca.Providers;
import sun.security.jca.ProviderList;
import sun.security.util.ECUtil;
import static sun.security.ssl.SunJSSE.cryptoProvider;

final class JsseJce {

    private static final ProviderList fipsProviderList;

    private static Boolean ecAvailable;

    private static final boolean kerberosAvailable;

    static {
        ClientKeyExchangeService p = ClientKeyExchangeService.find("KRB5");
        kerberosAvailable = (p != null);
    }

    static {
        if (SunJSSE.isFIPS() == false) {
            fipsProviderList = null;
        } else {
            Provider sun = Security.getProvider("SUN");
            if (sun == null) {
                throw new RuntimeException("FIPS mode: SUN provider must be installed");
            }
            Provider sunCerts = new SunCertificates(sun);
            fipsProviderList = ProviderList.newList(cryptoProvider, sunCerts);
        }
    }

    private static final class SunCertificates extends Provider {

        private static final long serialVersionUID = -3284138292032213752L;

        SunCertificates(final Provider p) {
            super("SunCertificates", 1.9d, "SunJSSE internal");
            AccessController.doPrivileged(new PrivilegedAction<Object>() {

                @Override
                public Object run() {
                    for (Map.Entry<Object, Object> entry : p.entrySet()) {
                        String key = (String) entry.getKey();
                        if (key.startsWith("CertPathValidator.") || key.startsWith("CertPathBuilder.") || key.startsWith("CertStore.") || key.startsWith("CertificateFactory.")) {
                            put(key, entry.getValue());
                        }
                    }
                    return null;
                }
            });
        }
    }

    static final String CIPHER_RSA_PKCS1 = "RSA/ECB/PKCS1Padding";

    static final String CIPHER_RC4 = "RC4";

    static final String CIPHER_DES = "DES/CBC/NoPadding";

    static final String CIPHER_3DES = "DESede/CBC/NoPadding";

    static final String CIPHER_AES = "AES/CBC/NoPadding";

    static final String CIPHER_AES_GCM = "AES/GCM/NoPadding";

    static final String SIGNATURE_DSA = "DSA";

    static final String SIGNATURE_ECDSA = "SHA1withECDSA";

    static final String SIGNATURE_RAWDSA = "RawDSA";

    static final String SIGNATURE_RAWECDSA = "NONEwithECDSA";

    static final String SIGNATURE_RAWRSA = "NONEwithRSA";

    static final String SIGNATURE_SSLRSA = "MD5andSHA1withRSA";

    private JsseJce() {
    }

    static synchronized boolean isEcAvailable() {
        if (ecAvailable == null) {
            try {
                JsseJce.getSignature(SIGNATURE_ECDSA);
                JsseJce.getSignature(SIGNATURE_RAWECDSA);
                JsseJce.getKeyAgreement("ECDH");
                JsseJce.getKeyFactory("EC");
                JsseJce.getKeyPairGenerator("EC");
                ecAvailable = true;
            } catch (Exception e) {
                ecAvailable = false;
            }
        }
        return ecAvailable;
    }

    static synchronized void clearEcAvailable() {
        ecAvailable = null;
    }

    static boolean isKerberosAvailable() {
        return kerberosAvailable;
    }

    static Cipher getCipher(String transformation) throws NoSuchAlgorithmException {
        try {
            if (cryptoProvider == null) {
                return Cipher.getInstance(transformation);
            } else {
                return Cipher.getInstance(transformation, cryptoProvider);
            }
        } catch (NoSuchPaddingException e) {
            throw new NoSuchAlgorithmException(e);
        }
    }

    static Signature getSignature(String algorithm) throws NoSuchAlgorithmException {
        if (cryptoProvider == null) {
            return Signature.getInstance(algorithm);
        } else {
            if (algorithm == SIGNATURE_SSLRSA) {
                if (cryptoProvider.getService("Signature", algorithm) == null) {
                    try {
                        return Signature.getInstance(algorithm, "SunJSSE");
                    } catch (NoSuchProviderException e) {
                        throw new NoSuchAlgorithmException(e);
                    }
                }
            }
            return Signature.getInstance(algorithm, cryptoProvider);
        }
    }

    static KeyGenerator getKeyGenerator(String algorithm) throws NoSuchAlgorithmException {
        if (cryptoProvider == null) {
            return KeyGenerator.getInstance(algorithm);
        } else {
            return KeyGenerator.getInstance(algorithm, cryptoProvider);
        }
    }

    static KeyPairGenerator getKeyPairGenerator(String algorithm) throws NoSuchAlgorithmException {
        if (cryptoProvider == null) {
            return KeyPairGenerator.getInstance(algorithm);
        } else {
            return KeyPairGenerator.getInstance(algorithm, cryptoProvider);
        }
    }

    static KeyAgreement getKeyAgreement(String algorithm) throws NoSuchAlgorithmException {
        if (cryptoProvider == null) {
            return KeyAgreement.getInstance(algorithm);
        } else {
            return KeyAgreement.getInstance(algorithm, cryptoProvider);
        }
    }

    static Mac getMac(String algorithm) throws NoSuchAlgorithmException {
        if (cryptoProvider == null) {
            return Mac.getInstance(algorithm);
        } else {
            return Mac.getInstance(algorithm, cryptoProvider);
        }
    }

    static KeyFactory getKeyFactory(String algorithm) throws NoSuchAlgorithmException {
        if (cryptoProvider == null) {
            return KeyFactory.getInstance(algorithm);
        } else {
            return KeyFactory.getInstance(algorithm, cryptoProvider);
        }
    }

    static SecureRandom getSecureRandom() throws KeyManagementException {
        if (cryptoProvider == null) {
            return new SecureRandom();
        }
        try {
            return SecureRandom.getInstance("PKCS11", cryptoProvider);
        } catch (NoSuchAlgorithmException e) {
        }
        for (Provider.Service s : cryptoProvider.getServices()) {
            if (s.getType().equals("SecureRandom")) {
                try {
                    return SecureRandom.getInstance(s.getAlgorithm(), cryptoProvider);
                } catch (NoSuchAlgorithmException ee) {
                }
            }
        }
        throw new KeyManagementException("FIPS mode: no SecureRandom " + " implementation found in provider " + cryptoProvider.getName());
    }

    static MessageDigest getMD5() {
        return getMessageDigest("MD5");
    }

    static MessageDigest getSHA() {
        return getMessageDigest("SHA");
    }

    static MessageDigest getMessageDigest(String algorithm) {
        try {
            if (cryptoProvider == null) {
                return MessageDigest.getInstance(algorithm);
            } else {
                return MessageDigest.getInstance(algorithm, cryptoProvider);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm " + algorithm + " not available", e);
        }
    }

    static int getRSAKeyLength(PublicKey key) {
        BigInteger modulus;
        if (key instanceof RSAPublicKey) {
            modulus = ((RSAPublicKey) key).getModulus();
        } else {
            RSAPublicKeySpec spec = getRSAPublicKeySpec(key);
            modulus = spec.getModulus();
        }
        return modulus.bitLength();
    }

    static RSAPublicKeySpec getRSAPublicKeySpec(PublicKey key) {
        if (key instanceof RSAPublicKey) {
            RSAPublicKey rsaKey = (RSAPublicKey) key;
            return new RSAPublicKeySpec(rsaKey.getModulus(), rsaKey.getPublicExponent());
        }
        try {
            KeyFactory factory = JsseJce.getKeyFactory("RSA");
            return factory.getKeySpec(key, RSAPublicKeySpec.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static ECParameterSpec getECParameterSpec(String namedCurveOid) {
        return ECUtil.getECParameterSpec(cryptoProvider, namedCurveOid);
    }

    static String getNamedCurveOid(ECParameterSpec params) {
        return ECUtil.getCurveName(cryptoProvider, params);
    }

    static ECPoint decodePoint(byte[] encoded, EllipticCurve curve) throws java.io.IOException {
        return ECUtil.decodePoint(encoded, curve);
    }

    static byte[] encodePoint(ECPoint point, EllipticCurve curve) {
        return ECUtil.encodePoint(point, curve);
    }

    static Object beginFipsProvider() {
        if (fipsProviderList == null) {
            return null;
        } else {
            return Providers.beginThreadProviderList(fipsProviderList);
        }
    }

    static void endFipsProvider(Object o) {
        if (fipsProviderList != null) {
            Providers.endThreadProviderList((ProviderList) o);
        }
    }
}
