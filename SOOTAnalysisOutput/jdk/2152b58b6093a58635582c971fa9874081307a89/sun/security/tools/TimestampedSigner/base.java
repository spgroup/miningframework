package sun.security.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import com.sun.jarsigner.*;
import java.util.Arrays;
import sun.security.pkcs.*;
import sun.security.timestamp.*;
import sun.security.util.*;
import sun.security.x509.*;

public final class TimestampedSigner extends ContentSigner {

    private static final SecureRandom RANDOM;

    static {
        SecureRandom tmp = null;
        try {
            tmp = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
        }
        RANDOM = tmp;
    }

    private static final String SUBJECT_INFO_ACCESS_OID = "1.3.6.1.5.5.7.1.11";

    private static final String KP_TIMESTAMPING_OID = "1.3.6.1.5.5.7.3.8";

    private static final String EXTENDED_KEY_USAGE_OID = "2.5.29.37";

    private static final ObjectIdentifier AD_TIMESTAMPING_Id;

    static {
        ObjectIdentifier tmp = null;
        try {
            tmp = new ObjectIdentifier("1.3.6.1.5.5.7.48.3");
        } catch (IOException e) {
        }
        AD_TIMESTAMPING_Id = tmp;
    }

    private String tsaUrl = null;

    private X509Certificate tsaCertificate = null;

    private MessageDigest messageDigest = null;

    private boolean tsRequestCertificate = true;

    public TimestampedSigner() {
    }

    public byte[] generateSignedData(ContentSignerParameters parameters, boolean omitContent, boolean applyTimestamp) throws NoSuchAlgorithmException, CertificateException, IOException {
        if (parameters == null) {
            throw new NullPointerException();
        }
        String signatureAlgorithm = parameters.getSignatureAlgorithm();
        String keyAlgorithm = AlgorithmId.getEncAlgFromSigAlg(signatureAlgorithm);
        String digestAlgorithm = AlgorithmId.getDigAlgFromSigAlg(signatureAlgorithm);
        AlgorithmId digestAlgorithmId = AlgorithmId.get(digestAlgorithm);
        X509Certificate[] signerCertificateChain = parameters.getSignerCertificateChain();
        Principal issuerName = signerCertificateChain[0].getIssuerDN();
        if (!(issuerName instanceof X500Name)) {
            X509CertInfo tbsCert = new X509CertInfo(signerCertificateChain[0].getTBSCertificate());
            issuerName = (Principal) tbsCert.get(CertificateIssuerName.NAME + "." + CertificateIssuerName.DN_NAME);
        }
        BigInteger serialNumber = signerCertificateChain[0].getSerialNumber();
        byte[] content = parameters.getContent();
        ContentInfo contentInfo;
        if (omitContent) {
            contentInfo = new ContentInfo(ContentInfo.DATA_OID, null);
        } else {
            contentInfo = new ContentInfo(content);
        }
        byte[] signature = parameters.getSignature();
        SignerInfo signerInfo = null;
        if (applyTimestamp) {
            tsaCertificate = parameters.getTimestampingAuthorityCertificate();
            URI tsaUri = parameters.getTimestampingAuthority();
            if (tsaUri != null) {
                tsaUrl = tsaUri.toString();
            } else {
                String certUrl = getTimestampingUrl(tsaCertificate);
                if (certUrl == null) {
                    throw new CertificateException("Subject Information Access extension not found");
                }
                tsaUrl = certUrl;
            }
            byte[] tsToken = generateTimestampToken(signature);
            PKCS9Attributes unsignedAttrs = new PKCS9Attributes(new PKCS9Attribute[] { new PKCS9Attribute(PKCS9Attribute.SIGNATURE_TIMESTAMP_TOKEN_STR, tsToken) });
            signerInfo = new SignerInfo((X500Name) issuerName, serialNumber, digestAlgorithmId, null, AlgorithmId.get(keyAlgorithm), signature, unsignedAttrs);
        } else {
            signerInfo = new SignerInfo((X500Name) issuerName, serialNumber, digestAlgorithmId, AlgorithmId.get(keyAlgorithm), signature);
        }
        SignerInfo[] signerInfos = { signerInfo };
        AlgorithmId[] algorithms = { digestAlgorithmId };
        PKCS7 p7 = new PKCS7(algorithms, contentInfo, signerCertificateChain, null, signerInfos);
        ByteArrayOutputStream p7out = new ByteArrayOutputStream();
        p7.encodeSignedData(p7out);
        return p7out.toByteArray();
    }

    public static String getTimestampingUrl(X509Certificate tsaCertificate) {
        if (tsaCertificate == null) {
            return null;
        }
        try {
            byte[] extensionValue = tsaCertificate.getExtensionValue(SUBJECT_INFO_ACCESS_OID);
            if (extensionValue == null) {
                return null;
            }
            DerInputStream der = new DerInputStream(extensionValue);
            der = new DerInputStream(der.getOctetString());
            DerValue[] derValue = der.getSequence(5);
            AccessDescription description;
            GeneralName location;
            URIName uri;
            for (int i = 0; i < derValue.length; i++) {
                description = new AccessDescription(derValue[i]);
                if (description.getAccessMethod().equals((Object) AD_TIMESTAMPING_Id)) {
                    location = description.getAccessLocation();
                    if (location.getType() == GeneralNameInterface.NAME_URI) {
                        uri = (URIName) location.getName();
                        if (uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https")) {
                            return uri.getName();
                        }
                    }
                }
            }
        } catch (IOException ioe) {
        }
        return null;
    }

    private byte[] generateTimestampToken(byte[] toBeTimestamped) throws CertificateException, IOException {
        if (messageDigest == null) {
            try {
                messageDigest = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
            }
        }
        byte[] digest = messageDigest.digest(toBeTimestamped);
        TSRequest tsQuery = new TSRequest(digest, "SHA-1");
        BigInteger nonce = null;
        if (RANDOM != null) {
            nonce = new BigInteger(64, RANDOM);
            tsQuery.setNonce(nonce);
        }
        tsQuery.requestCertificate(tsRequestCertificate);
        Timestamper tsa = new HttpTimestamper(tsaUrl);
        TSResponse tsReply = tsa.generateTimestamp(tsQuery);
        int status = tsReply.getStatusCode();
        if (status != 0 && status != 1) {
            int failureCode = tsReply.getFailureCode();
            if (failureCode == -1) {
                throw new IOException("Error generating timestamp: " + tsReply.getStatusCodeAsText());
            } else {
                throw new IOException("Error generating timestamp: " + tsReply.getStatusCodeAsText() + " " + tsReply.getFailureCodeAsText());
            }
        }
        PKCS7 tsToken = tsReply.getToken();
        TimestampToken tst = new TimestampToken(tsToken.getContentInfo().getData());
        if (!tst.getHashAlgorithm().equals(new AlgorithmId(new ObjectIdentifier("1.3.14.3.2.26")))) {
            throw new IOException("Digest algorithm not SHA-1 in timestamp token");
        }
        if (!Arrays.equals(tst.getHashedMessage(), digest)) {
            throw new IOException("Digest octets changed in timestamp token");
        }
        BigInteger replyNonce = tst.getNonce();
        if (replyNonce == null && nonce != null) {
            throw new IOException("Nonce missing in timestamp token");
        }
        if (replyNonce != null && !replyNonce.equals(nonce)) {
            throw new IOException("Nonce changed in timestamp token");
        }
        for (SignerInfo si : tsToken.getSignerInfos()) {
            X509Certificate cert = si.getCertificate(tsToken);
            if (cert == null) {
                throw new CertificateException("Certificate not included in timestamp token");
            } else {
                if (!cert.getCriticalExtensionOIDs().contains(EXTENDED_KEY_USAGE_OID)) {
                    throw new CertificateException("Certificate is not valid for timestamping");
                }
                List<String> keyPurposes = cert.getExtendedKeyUsage();
                if (keyPurposes == null || !keyPurposes.contains(KP_TIMESTAMPING_OID)) {
                    throw new CertificateException("Certificate is not valid for timestamping");
                }
            }
        }
        return tsReply.getEncodedToken();
    }
}
