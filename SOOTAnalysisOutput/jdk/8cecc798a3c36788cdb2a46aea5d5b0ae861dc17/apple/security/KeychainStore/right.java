package apple.security;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.spec.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.security.auth.x500.*;
import sun.security.pkcs.*;
import sun.security.pkcs.EncryptedPrivateKeyInfo;
import sun.security.util.*;
import sun.security.x509.*;

public final class KeychainStore extends KeyStoreSpi {

    class KeyEntry {

        Date date;

        byte[] protectedPrivKey;

        char[] password;

        long keyRef;

        Certificate[] chain;

        long[] chainRefs;
    }

    class TrustedCertEntry {

        Date date;

        Certificate cert;

        long certRef;
    }

    private Hashtable<String, Object> deletedEntries = new Hashtable<>();

    private Hashtable<String, Object> addedEntries = new Hashtable<>();

    private Hashtable<String, Object> entries = new Hashtable<>();

    private static final int[] keyBag = { 1, 2, 840, 113549, 1, 12, 10, 1, 2 };

    private static final int[] pbeWithSHAAnd3KeyTripleDESCBC = { 1, 2, 840, 113549, 1, 12, 1, 3 };

    private static ObjectIdentifier PKCS8ShroudedKeyBag_OID;

    private static ObjectIdentifier pbeWithSHAAnd3KeyTripleDESCBC_OID;

    private static final int iterationCount = 1024;

    private static final int SALT_LEN = 20;

    private static final Debug debug = Debug.getInstance("keystore");

    static {
        jdk.internal.loader.BootLoader.loadLibrary("osxsecurity");
        try {
            PKCS8ShroudedKeyBag_OID = new ObjectIdentifier(keyBag);
            pbeWithSHAAnd3KeyTripleDESCBC_OID = new ObjectIdentifier(pbeWithSHAAnd3KeyTripleDESCBC);
        } catch (IOException ioe) {
        }
    }

    private static void permissionCheck() {
        SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            sec.checkPermission(new RuntimePermission("useKeychainStore"));
        }
    }

    public KeychainStore() {
    }

    public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
        permissionCheck();
        if (password == null || password.length == 0) {
            if (random == null) {
                random = new SecureRandom();
            }
            password = Long.toString(random.nextLong()).toCharArray();
        }
        Object entry = entries.get(alias.toLowerCase());
        if (entry == null || !(entry instanceof KeyEntry)) {
            return null;
        }
        byte[] exportedKeyInfo = _getEncodedKeyData(((KeyEntry) entry).keyRef, password);
        if (exportedKeyInfo == null) {
            return null;
        }
        PrivateKey returnValue = null;
        try {
            byte[] pkcs8KeyData = fetchPrivateKeyFromBag(exportedKeyInfo);
            byte[] encryptedKey;
            AlgorithmParameters algParams;
            ObjectIdentifier algOid;
            try {
                EncryptedPrivateKeyInfo encrInfo = new EncryptedPrivateKeyInfo(pkcs8KeyData);
                encryptedKey = encrInfo.getEncryptedData();
                DerValue val = new DerValue(encrInfo.getAlgorithm().encode());
                DerInputStream in = val.toDerInputStream();
                algOid = in.getOID();
                algParams = parseAlgParameters(in);
            } catch (IOException ioe) {
                UnrecoverableKeyException uke = new UnrecoverableKeyException("Private key not stored as " + "PKCS#8 EncryptedPrivateKeyInfo: " + ioe);
                uke.initCause(ioe);
                throw uke;
            }
            SecretKey skey = getPBEKey(password);
            Cipher cipher = Cipher.getInstance(algOid.toString());
            cipher.init(Cipher.DECRYPT_MODE, skey, algParams);
            byte[] decryptedPrivateKey = cipher.doFinal(encryptedKey);
            PKCS8EncodedKeySpec kspec = new PKCS8EncodedKeySpec(decryptedPrivateKey);
            DerValue val = new DerValue(decryptedPrivateKey);
            DerInputStream in = val.toDerInputStream();
            int i = in.getInteger();
            DerValue[] value = in.getSequence(2);
            AlgorithmId algId = new AlgorithmId(value[0].getOID());
            String algName = algId.getName();
            KeyFactory kfac = KeyFactory.getInstance(algName);
            returnValue = kfac.generatePrivate(kspec);
        } catch (Exception e) {
            UnrecoverableKeyException uke = new UnrecoverableKeyException("Get Key failed: " + e.getMessage());
            uke.initCause(e);
            throw uke;
        }
        return returnValue;
    }

    private native byte[] _getEncodedKeyData(long secKeyRef, char[] password);

    public Certificate[] engineGetCertificateChain(String alias) {
        permissionCheck();
        Object entry = entries.get(alias.toLowerCase());
        if (entry != null && entry instanceof KeyEntry) {
            if (((KeyEntry) entry).chain == null) {
                return null;
            } else {
                return ((KeyEntry) entry).chain.clone();
            }
        } else {
            return null;
        }
    }

    public Certificate engineGetCertificate(String alias) {
        permissionCheck();
        Object entry = entries.get(alias.toLowerCase());
        if (entry != null) {
            if (entry instanceof TrustedCertEntry) {
                return ((TrustedCertEntry) entry).cert;
            } else {
                KeyEntry ke = (KeyEntry) entry;
                if (ke.chain == null || ke.chain.length == 0) {
                    return null;
                }
                return ke.chain[0];
            }
        } else {
            return null;
        }
    }

    public Date engineGetCreationDate(String alias) {
        permissionCheck();
        Object entry = entries.get(alias.toLowerCase());
        if (entry != null) {
            if (entry instanceof TrustedCertEntry) {
                return new Date(((TrustedCertEntry) entry).date.getTime());
            } else {
                return new Date(((KeyEntry) entry).date.getTime());
            }
        } else {
            return null;
        }
    }

    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
        permissionCheck();
        synchronized (entries) {
            try {
                KeyEntry entry = new KeyEntry();
                entry.date = new Date();
                if (key instanceof PrivateKey) {
                    if ((key.getFormat().equals("PKCS#8")) || (key.getFormat().equals("PKCS8"))) {
                        entry.protectedPrivKey = encryptPrivateKey(key.getEncoded(), password);
                        entry.password = password.clone();
                    } else {
                        throw new KeyStoreException("Private key is not encoded as PKCS#8");
                    }
                } else {
                    throw new KeyStoreException("Key is not a PrivateKey");
                }
                if (chain != null) {
                    if ((chain.length > 1) && !validateChain(chain)) {
                        throw new KeyStoreException("Certificate chain does not validate");
                    }
                    entry.chain = chain.clone();
                    entry.chainRefs = new long[entry.chain.length];
                }
                String lowerAlias = alias.toLowerCase();
                if (entries.get(lowerAlias) != null) {
                    deletedEntries.put(lowerAlias, entries.get(lowerAlias));
                }
                entries.put(lowerAlias, entry);
                addedEntries.put(lowerAlias, entry);
            } catch (Exception nsae) {
                KeyStoreException ke = new KeyStoreException("Key protection algorithm not found: " + nsae);
                ke.initCause(nsae);
                throw ke;
            }
        }
    }

    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
        permissionCheck();
        synchronized (entries) {
            KeyEntry entry = new KeyEntry();
            try {
                EncryptedPrivateKeyInfo privateKey = new EncryptedPrivateKeyInfo(key);
                entry.protectedPrivKey = privateKey.getEncoded();
            } catch (IOException ioe) {
                throw new KeyStoreException("key is not encoded as " + "EncryptedPrivateKeyInfo");
            }
            entry.date = new Date();
            if ((chain != null) && (chain.length != 0)) {
                entry.chain = chain.clone();
                entry.chainRefs = new long[entry.chain.length];
            }
            String lowerAlias = alias.toLowerCase();
            if (entries.get(lowerAlias) != null) {
                deletedEntries.put(lowerAlias, entries.get(alias));
            }
            entries.put(lowerAlias, entry);
            addedEntries.put(lowerAlias, entry);
        }
    }

    public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
        permissionCheck();
        synchronized (entries) {
            Object entry = entries.get(alias.toLowerCase());
            if ((entry != null) && (entry instanceof KeyEntry)) {
                throw new KeyStoreException("Cannot overwrite key entry with certificate");
            }
            Collection<Object> allValues = entries.values();
            for (Object value : allValues) {
                if (value instanceof TrustedCertEntry) {
                    TrustedCertEntry tce = (TrustedCertEntry) value;
                    if (tce.cert.equals(cert)) {
                        throw new KeyStoreException("Keychain does not support mulitple copies of same certificate.");
                    }
                }
            }
            TrustedCertEntry trustedCertEntry = new TrustedCertEntry();
            trustedCertEntry.cert = cert;
            trustedCertEntry.date = new Date();
            String lowerAlias = alias.toLowerCase();
            if (entries.get(lowerAlias) != null) {
                deletedEntries.put(lowerAlias, entries.get(lowerAlias));
            }
            entries.put(lowerAlias, trustedCertEntry);
            addedEntries.put(lowerAlias, trustedCertEntry);
        }
    }

    public void engineDeleteEntry(String alias) throws KeyStoreException {
        permissionCheck();
        synchronized (entries) {
            Object entry = entries.remove(alias.toLowerCase());
            deletedEntries.put(alias.toLowerCase(), entry);
        }
    }

    public Enumeration<String> engineAliases() {
        permissionCheck();
        return entries.keys();
    }

    public boolean engineContainsAlias(String alias) {
        permissionCheck();
        return entries.containsKey(alias.toLowerCase());
    }

    public int engineSize() {
        permissionCheck();
        return entries.size();
    }

    public boolean engineIsKeyEntry(String alias) {
        permissionCheck();
        Object entry = entries.get(alias.toLowerCase());
        if ((entry != null) && (entry instanceof KeyEntry)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean engineIsCertificateEntry(String alias) {
        permissionCheck();
        Object entry = entries.get(alias.toLowerCase());
        if ((entry != null) && (entry instanceof TrustedCertEntry)) {
            return true;
        } else {
            return false;
        }
    }

    public String engineGetCertificateAlias(Certificate cert) {
        permissionCheck();
        Certificate certElem;
        for (Enumeration<String> e = entries.keys(); e.hasMoreElements(); ) {
            String alias = e.nextElement();
            Object entry = entries.get(alias);
            if (entry instanceof TrustedCertEntry) {
                certElem = ((TrustedCertEntry) entry).cert;
            } else {
                KeyEntry ke = (KeyEntry) entry;
                if (ke.chain == null || ke.chain.length == 0) {
                    continue;
                }
                certElem = ke.chain[0];
            }
            if (certElem.equals(cert)) {
                return alias;
            }
        }
        return null;
    }

    public void engineStore(OutputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        permissionCheck();
        for (Enumeration<String> e = deletedEntries.keys(); e.hasMoreElements(); ) {
            String alias = e.nextElement();
            Object entry = deletedEntries.get(alias);
            if (entry instanceof TrustedCertEntry) {
                if (((TrustedCertEntry) entry).certRef != 0) {
                    _removeItemFromKeychain(((TrustedCertEntry) entry).certRef);
                    _releaseKeychainItemRef(((TrustedCertEntry) entry).certRef);
                }
            } else {
                Certificate certElem;
                KeyEntry keyEntry = (KeyEntry) entry;
                if (keyEntry.chain != null) {
                    for (int i = 0; i < keyEntry.chain.length; i++) {
                        if (keyEntry.chainRefs[i] != 0) {
                            _removeItemFromKeychain(keyEntry.chainRefs[i]);
                            _releaseKeychainItemRef(keyEntry.chainRefs[i]);
                        }
                    }
                    if (keyEntry.keyRef != 0) {
                        _removeItemFromKeychain(keyEntry.keyRef);
                        _releaseKeychainItemRef(keyEntry.keyRef);
                    }
                }
            }
        }
        for (Enumeration<String> e = addedEntries.keys(); e.hasMoreElements(); ) {
            String alias = e.nextElement();
            Object entry = addedEntries.get(alias);
            if (entry instanceof TrustedCertEntry) {
                TrustedCertEntry tce = (TrustedCertEntry) entry;
                Certificate certElem;
                certElem = tce.cert;
                tce.certRef = addCertificateToKeychain(alias, certElem);
            } else {
                KeyEntry keyEntry = (KeyEntry) entry;
                if (keyEntry.chain != null) {
                    for (int i = 0; i < keyEntry.chain.length; i++) {
                        keyEntry.chainRefs[i] = addCertificateToKeychain(alias, keyEntry.chain[i]);
                    }
                    keyEntry.keyRef = _addItemToKeychain(alias, false, keyEntry.protectedPrivKey, keyEntry.password);
                }
            }
        }
        deletedEntries.clear();
        addedEntries.clear();
    }

    private long addCertificateToKeychain(String alias, Certificate cert) {
        byte[] certblob = null;
        long returnValue = 0;
        try {
            certblob = cert.getEncoded();
            returnValue = _addItemToKeychain(alias, true, certblob, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    private native long _addItemToKeychain(String alias, boolean isCertificate, byte[] datablob, char[] password);

    private native int _removeItemFromKeychain(long certRef);

    private native void _releaseKeychainItemRef(long keychainItemRef);

    public void engineLoad(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
        permissionCheck();
        synchronized (entries) {
            for (Enumeration<String> e = entries.keys(); e.hasMoreElements(); ) {
                String alias = e.nextElement();
                Object entry = entries.get(alias);
                if (entry instanceof TrustedCertEntry) {
                    if (((TrustedCertEntry) entry).certRef != 0) {
                        _releaseKeychainItemRef(((TrustedCertEntry) entry).certRef);
                    }
                } else {
                    KeyEntry keyEntry = (KeyEntry) entry;
                    if (keyEntry.chain != null) {
                        for (int i = 0; i < keyEntry.chain.length; i++) {
                            if (keyEntry.chainRefs[i] != 0) {
                                _releaseKeychainItemRef(keyEntry.chainRefs[i]);
                            }
                        }
                        if (keyEntry.keyRef != 0) {
                            _releaseKeychainItemRef(keyEntry.keyRef);
                        }
                    }
                }
            }
            entries.clear();
            _scanKeychain();
            if (debug != null) {
                debug.println("KeychainStore load entry count: " + entries.size());
            }
        }
    }

    private native void _scanKeychain();

    private void createTrustedCertEntry(String alias, long keychainItemRef, long creationDate, byte[] derStream) {
        TrustedCertEntry tce = new TrustedCertEntry();
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream input = new ByteArrayInputStream(derStream);
            X509Certificate cert = (X509Certificate) cf.generateCertificate(input);
            input.close();
            tce.cert = cert;
            tce.certRef = keychainItemRef;
            if (creationDate != 0)
                tce.date = new Date(creationDate);
            else
                tce.date = new Date();
            int uniqueVal = 1;
            String originalAlias = alias;
            while (entries.containsKey(alias.toLowerCase())) {
                alias = originalAlias + " " + uniqueVal;
                uniqueVal++;
            }
            entries.put(alias.toLowerCase(), tce);
        } catch (Exception e) {
            System.err.println("KeychainStore Ignored Exception: " + e);
        }
    }

    private void createKeyEntry(String alias, long creationDate, long secKeyRef, long[] secCertificateRefs, byte[][] rawCertData) throws IOException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyEntry ke = new KeyEntry();
        ke.protectedPrivKey = null;
        ke.keyRef = secKeyRef;
        if (creationDate != 0)
            ke.date = new Date(creationDate);
        else
            ke.date = new Date();
        List<CertKeychainItemPair> createdCerts = new ArrayList<>();
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            for (int i = 0; i < rawCertData.length; i++) {
                try {
                    InputStream input = new ByteArrayInputStream(rawCertData[i]);
                    X509Certificate cert = (X509Certificate) cf.generateCertificate(input);
                    input.close();
                    createdCerts.add(new CertKeychainItemPair(secCertificateRefs[i], cert));
                } catch (CertificateException e) {
                    System.err.println("KeychainStore Ignored Exception: " + e);
                }
            }
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        CertKeychainItemPair[] objArray = createdCerts.toArray(new CertKeychainItemPair[0]);
        Certificate[] certArray = new Certificate[objArray.length];
        long[] certRefArray = new long[objArray.length];
        for (int i = 0; i < objArray.length; i++) {
            CertKeychainItemPair addedItem = objArray[i];
            certArray[i] = addedItem.mCert;
            certRefArray[i] = addedItem.mCertificateRef;
        }
        ke.chain = certArray;
        ke.chainRefs = certRefArray;
        int uniqueVal = 1;
        String originalAlias = alias;
        while (entries.containsKey(alias.toLowerCase())) {
            alias = originalAlias + " " + uniqueVal;
            uniqueVal++;
        }
        entries.put(alias.toLowerCase(), ke);
    }

    private class CertKeychainItemPair {

        long mCertificateRef;

        Certificate mCert;

        CertKeychainItemPair(long inCertRef, Certificate cert) {
            mCertificateRef = inCertRef;
            mCert = cert;
        }
    }

    private boolean validateChain(Certificate[] certChain) {
        for (int i = 0; i < certChain.length - 1; i++) {
            X500Principal issuerDN = ((X509Certificate) certChain[i]).getIssuerX500Principal();
            X500Principal subjectDN = ((X509Certificate) certChain[i + 1]).getSubjectX500Principal();
            if (!(issuerDN.equals(subjectDN)))
                return false;
        }
        return true;
    }

    private byte[] fetchPrivateKeyFromBag(byte[] privateKeyInfo) throws IOException, NoSuchAlgorithmException, CertificateException {
        byte[] returnValue = null;
        DerValue val = new DerValue(new ByteArrayInputStream(privateKeyInfo));
        DerInputStream s = val.toDerInputStream();
        int version = s.getInteger();
        if (version != 3) {
            throw new IOException("PKCS12 keystore not in version 3 format");
        }
        byte[] authSafeData;
        ContentInfo authSafe = new ContentInfo(s);
        ObjectIdentifier contentType = authSafe.getContentType();
        if (contentType.equals(ContentInfo.DATA_OID)) {
            authSafeData = authSafe.getData();
        } else {
            throw new IOException("public key protected PKCS12 not supported");
        }
        DerInputStream as = new DerInputStream(authSafeData);
        DerValue[] safeContentsArray = as.getSequence(2);
        int count = safeContentsArray.length;
        for (int i = 0; i < count; i++) {
            byte[] safeContentsData;
            ContentInfo safeContents;
            DerInputStream sci;
            byte[] eAlgId = null;
            sci = new DerInputStream(safeContentsArray[i].toByteArray());
            safeContents = new ContentInfo(sci);
            contentType = safeContents.getContentType();
            safeContentsData = null;
            if (contentType.equals(ContentInfo.DATA_OID)) {
                safeContentsData = safeContents.getData();
            } else if (contentType.equals(ContentInfo.ENCRYPTED_DATA_OID)) {
                continue;
            } else {
                throw new IOException("public key protected PKCS12" + " not supported");
            }
            DerInputStream sc = new DerInputStream(safeContentsData);
            returnValue = extractKeyData(sc);
        }
        return returnValue;
    }

    private byte[] extractKeyData(DerInputStream stream) throws IOException, NoSuchAlgorithmException, CertificateException {
        byte[] returnValue = null;
        DerValue[] safeBags = stream.getSequence(2);
        int count = safeBags.length;
        for (int i = 0; i < count; i++) {
            ObjectIdentifier bagId;
            DerInputStream sbi;
            DerValue bagValue;
            Object bagItem = null;
            sbi = safeBags[i].toDerInputStream();
            bagId = sbi.getOID();
            bagValue = sbi.getDerValue();
            if (!bagValue.isContextSpecific((byte) 0)) {
                throw new IOException("unsupported PKCS12 bag value type " + bagValue.tag);
            }
            bagValue = bagValue.data.getDerValue();
            if (bagId.equals(PKCS8ShroudedKeyBag_OID)) {
                returnValue = bagValue.toByteArray();
            } else {
                System.out.println("Unsupported bag type '" + bagId + "'");
            }
        }
        return returnValue;
    }

    private AlgorithmParameters getAlgorithmParameters(String algorithm) throws IOException {
        AlgorithmParameters algParams = null;
        PBEParameterSpec paramSpec = new PBEParameterSpec(getSalt(), iterationCount);
        try {
            algParams = AlgorithmParameters.getInstance(algorithm);
            algParams.init(paramSpec);
        } catch (Exception e) {
            IOException ioe = new IOException("getAlgorithmParameters failed: " + e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
        return algParams;
    }

    private SecureRandom random;

    private byte[] getSalt() {
        byte[] salt = new byte[SALT_LEN];
        if (random == null) {
            random = new SecureRandom();
        }
        random.nextBytes(salt);
        return salt;
    }

    private AlgorithmParameters parseAlgParameters(DerInputStream in) throws IOException {
        AlgorithmParameters algParams = null;
        try {
            DerValue params;
            if (in.available() == 0) {
                params = null;
            } else {
                params = in.getDerValue();
                if (params.tag == DerValue.tag_Null) {
                    params = null;
                }
            }
            if (params != null) {
                algParams = AlgorithmParameters.getInstance("PBE");
                algParams.init(params.toByteArray());
            }
        } catch (Exception e) {
            IOException ioe = new IOException("parseAlgParameters failed: " + e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
        return algParams;
    }

    private SecretKey getPBEKey(char[] password) throws IOException {
        SecretKey skey = null;
        try {
            PBEKeySpec keySpec = new PBEKeySpec(password);
            SecretKeyFactory skFac = SecretKeyFactory.getInstance("PBE");
            skey = skFac.generateSecret(keySpec);
        } catch (Exception e) {
            IOException ioe = new IOException("getSecretKey failed: " + e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
        return skey;
    }

    private byte[] encryptPrivateKey(byte[] data, char[] password) throws IOException, NoSuchAlgorithmException, UnrecoverableKeyException {
        byte[] key = null;
        try {
            AlgorithmParameters algParams = getAlgorithmParameters("PBEWithSHA1AndDESede");
            SecretKey skey = getPBEKey(password);
            Cipher cipher = Cipher.getInstance("PBEWithSHA1AndDESede");
            cipher.init(Cipher.ENCRYPT_MODE, skey, algParams);
            byte[] encryptedKey = cipher.doFinal(data);
            AlgorithmId algid = new AlgorithmId(pbeWithSHAAnd3KeyTripleDESCBC_OID, algParams);
            EncryptedPrivateKeyInfo encrInfo = new EncryptedPrivateKeyInfo(algid, encryptedKey);
            key = encrInfo.getEncoded();
        } catch (Exception e) {
            UnrecoverableKeyException uke = new UnrecoverableKeyException("Encrypt Private Key failed: " + e.getMessage());
            uke.initCause(e);
            throw uke;
        }
        return key;
    }
}
