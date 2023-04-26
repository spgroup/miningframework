package sun.security.tools;

import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import com.sun.jarsigner.*;
import sun.security.pkcs.PKCS7;
import sun.security.util.*;
import sun.security.x509.*;

public final class TimestampedSigner extends ContentSigner {

    private static final String SUBJECT_INFO_ACCESS_OID = "1.3.6.1.5.5.7.1.11";

    private static final ObjectIdentifier AD_TIMESTAMPING_Id;

    static {
        ObjectIdentifier tmp = null;
        try {
            tmp = new ObjectIdentifier("1.3.6.1.5.5.7.48.3");
        } catch (IOException e) {
        }
        AD_TIMESTAMPING_Id = tmp;
    }

    public TimestampedSigner() {
    }

    public byte[] generateSignedData(ContentSignerParameters params, boolean omitContent, boolean applyTimestamp) throws NoSuchAlgorithmException, CertificateException, IOException {
        if (params == null) {
            throw new NullPointerException();
        }
        String signatureAlgorithm = params.getSignatureAlgorithm();
        X509Certificate[] signerChain = params.getSignerCertificateChain();
        byte[] signature = params.getSignature();
        byte[] content = (omitContent == true) ? null : params.getContent();
        URI tsaURI = null;
        if (applyTimestamp) {
            tsaURI = params.getTimestampingAuthority();
            if (tsaURI == null) {
                tsaURI = getTimestampingURI(params.getTimestampingAuthorityCertificate());
                if (tsaURI == null) {
                    throw new CertificateException("Subject Information Access extension not found");
                }
            }
        }
        return PKCS7.generateSignedData(signature, signerChain, content, params.getSignatureAlgorithm(), tsaURI);
    }

    public static URI getTimestampingURI(X509Certificate tsaCertificate) {
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
                            return uri.getURI();
                        }
                    }
                }
            }
        } catch (IOException ioe) {
        }
        return null;
    }
}
