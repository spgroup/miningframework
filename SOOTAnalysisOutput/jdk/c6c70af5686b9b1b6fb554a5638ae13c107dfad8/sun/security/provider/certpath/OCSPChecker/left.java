package sun.security.provider.certpath;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.Security;
import java.security.cert.*;
import java.security.cert.CertPathValidatorException.BasicReason;
import java.net.*;
import javax.security.auth.x500.X500Principal;
import sun.misc.IOUtils;
import sun.security.util.*;
import sun.security.x509.*;

class OCSPChecker extends PKIXCertPathChecker {

    public static final String OCSP_ENABLE_PROP = "ocsp.enable";

    public static final String OCSP_URL_PROP = "ocsp.responderURL";

    public static final String OCSP_CERT_SUBJECT_PROP = "ocsp.responderCertSubjectName";

    public static final String OCSP_CERT_ISSUER_PROP = "ocsp.responderCertIssuerName";

    public static final String OCSP_CERT_NUMBER_PROP = "ocsp.responderCertSerialNumber";

    private static final String HEX_DIGITS = "0123456789ABCDEFabcdef";

    private static final Debug DEBUG = Debug.getInstance("certpath");

    private static final boolean dump = false;

    private static final int[] OCSP_NONCE_DATA = { 1, 3, 6, 1, 5, 5, 7, 48, 1, 2 };

    private static final ObjectIdentifier OCSP_NONCE_OID;

    static {
        OCSP_NONCE_OID = ObjectIdentifier.newInternal(OCSP_NONCE_DATA);
    }

    private int remainingCerts;

    private X509Certificate[] certs;

    private CertPath cp;

    private PKIXParameters pkixParams;

    OCSPChecker(CertPath certPath, PKIXParameters pkixParams) throws CertPathValidatorException {
        this.cp = certPath;
        this.pkixParams = pkixParams;
        List<? extends Certificate> tmp = cp.getCertificates();
        certs = tmp.toArray(new X509Certificate[tmp.size()]);
        init(false);
    }

    public void init(boolean forward) throws CertPathValidatorException {
        if (!forward) {
            remainingCerts = certs.length + 1;
        } else {
            throw new CertPathValidatorException("Forward checking not supported");
        }
    }

    public boolean isForwardCheckingSupported() {
        return false;
    }

    public Set<String> getSupportedExtensions() {
        return Collections.<String>emptySet();
    }

    public void check(Certificate cert, Collection<String> unresolvedCritExts) throws CertPathValidatorException {
        InputStream in = null;
        OutputStream out = null;
        remainingCerts--;
        try {
            X509Certificate responderCert = null;
            boolean seekResponderCert = false;
            X500Principal responderSubjectName = null;
            X500Principal responderIssuerName = null;
            BigInteger responderSerialNumber = null;
            boolean seekIssuerCert = true;
            X509CertImpl issuerCertImpl = null;
            X509CertImpl currCertImpl = X509CertImpl.toImpl((X509Certificate) cert);
            String[] properties = getOCSPProperties();
            URL url = getOCSPServerURL(currCertImpl, properties);
            if (properties[1] != null) {
                responderSubjectName = new X500Principal(properties[1]);
            } else if (properties[2] != null && properties[3] != null) {
                responderIssuerName = new X500Principal(properties[2]);
                String value = stripOutSeparators(properties[3]);
                responderSerialNumber = new BigInteger(value, 16);
            } else if (properties[2] != null || properties[3] != null) {
                throw new CertPathValidatorException("Must specify both ocsp.responderCertIssuerName and " + "ocsp.responderCertSerialNumber properties");
            }
            if (responderSubjectName != null || responderIssuerName != null) {
                seekResponderCert = true;
            }
            if (remainingCerts < certs.length) {
                issuerCertImpl = X509CertImpl.toImpl(certs[remainingCerts]);
                seekIssuerCert = false;
                if (!seekResponderCert) {
                    responderCert = certs[remainingCerts];
                    if (DEBUG != null) {
                        DEBUG.println("Responder's certificate is the same " + "as the issuer of the certificate being validated");
                    }
                }
            }
            if (seekIssuerCert || seekResponderCert) {
                if (DEBUG != null && seekResponderCert) {
                    DEBUG.println("Searching trust anchors for responder's " + "certificate");
                }
                Iterator anchors = pkixParams.getTrustAnchors().iterator();
                if (!anchors.hasNext()) {
                    throw new CertPathValidatorException("Must specify at least one trust anchor");
                }
                X500Principal certIssuerName = currCertImpl.getIssuerX500Principal();
                while (anchors.hasNext() && (seekIssuerCert || seekResponderCert)) {
                    TrustAnchor anchor = (TrustAnchor) anchors.next();
                    X509Certificate anchorCert = anchor.getTrustedCert();
                    X500Principal anchorSubjectName = anchorCert.getSubjectX500Principal();
                    if (dump) {
                        System.out.println("Issuer DN is " + certIssuerName);
                        System.out.println("Subject DN is " + anchorSubjectName);
                    }
                    if (seekIssuerCert && certIssuerName.equals(anchorSubjectName)) {
                        issuerCertImpl = X509CertImpl.toImpl(anchorCert);
                        seekIssuerCert = false;
                        if (!seekResponderCert && responderCert == null) {
                            responderCert = anchorCert;
                            if (DEBUG != null) {
                                DEBUG.println("Responder's certificate is the" + " same as the issuer of the certificate " + "being validated");
                            }
                        }
                    }
                    if (seekResponderCert) {
                        if ((responderSubjectName != null && responderSubjectName.equals(anchorSubjectName)) || (responderIssuerName != null && responderSerialNumber != null && responderIssuerName.equals(anchorCert.getIssuerX500Principal()) && responderSerialNumber.equals(anchorCert.getSerialNumber()))) {
                            responderCert = anchorCert;
                            seekResponderCert = false;
                        }
                    }
                }
                if (issuerCertImpl == null) {
                    throw new CertPathValidatorException("No trusted certificate for " + currCertImpl.getIssuerDN());
                }
                if (seekResponderCert) {
                    if (DEBUG != null) {
                        DEBUG.println("Searching cert stores for responder's " + "certificate");
                    }
                    X509CertSelector filter = null;
                    if (responderSubjectName != null) {
                        filter = new X509CertSelector();
                        filter.setSubject(responderSubjectName.getName());
                    } else if (responderIssuerName != null && responderSerialNumber != null) {
                        filter = new X509CertSelector();
                        filter.setIssuer(responderIssuerName.getName());
                        filter.setSerialNumber(responderSerialNumber);
                    }
                    if (filter != null) {
                        List<CertStore> certStores = pkixParams.getCertStores();
                        AlgorithmChecker algChecker = AlgorithmChecker.getInstance();
                        for (CertStore certStore : certStores) {
                            for (Certificate selected : certStore.getCertificates(filter)) {
                                try {
                                    algChecker.check(selected);
                                    responderCert = (X509Certificate) selected;
                                    seekResponderCert = false;
                                    break;
                                } catch (CertPathValidatorException cpve) {
                                    if (DEBUG != null) {
                                        DEBUG.println("OCSP responder certificate " + "algorithm check failed: " + cpve);
                                    }
                                }
                            }
                            if (!seekResponderCert) {
                                break;
                            }
                        }
                    }
                }
            }
            if (seekResponderCert) {
                throw new CertPathValidatorException("Cannot find the responder's certificate " + "(set using the OCSP security properties).");
            }
            OCSPRequest ocspRequest = new OCSPRequest(currCertImpl, issuerCertImpl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            if (DEBUG != null) {
                DEBUG.println("connecting to OCSP service at: " + url);
            }
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-type", "application/ocsp-request");
            byte[] bytes = ocspRequest.encodeBytes();
            CertId certId = ocspRequest.getCertId();
            con.setRequestProperty("Content-length", String.valueOf(bytes.length));
            out = con.getOutputStream();
            out.write(bytes);
            out.flush();
            if (DEBUG != null && con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                DEBUG.println("Received HTTP error: " + con.getResponseCode() + " - " + con.getResponseMessage());
            }
            in = con.getInputStream();
            int contentLength = con.getContentLength();
            byte[] response = IOUtils.readFully(in, contentLength, false);
            OCSPResponse ocspResponse = new OCSPResponse(response, pkixParams, responderCert);
            if (!certId.equals(ocspResponse.getCertId())) {
                throw new CertPathValidatorException("Certificate in the OCSP response does not match the " + "certificate supplied in the OCSP request.");
            }
            SerialNumber serialNumber = currCertImpl.getSerialNumberObject();
            int certOCSPStatus = ocspResponse.getCertStatus(serialNumber);
            if (DEBUG != null) {
                DEBUG.println("Status of certificate (with serial number " + serialNumber.getNumber() + ") is: " + OCSPResponse.certStatusToText(certOCSPStatus));
            }
            if (certOCSPStatus == OCSPResponse.CERT_STATUS_REVOKED) {
                Throwable t = new CertificateRevokedException(ocspResponse.getRevocationTime(), ocspResponse.getRevocationReason(), responderCert.getSubjectX500Principal(), ocspResponse.getSingleExtensions());
                throw new CertPathValidatorException(t.getMessage(), t, null, -1, BasicReason.REVOKED);
            } else if (certOCSPStatus == OCSPResponse.CERT_STATUS_UNKNOWN) {
                throw new CertPathValidatorException("Certificate's revocation status is unknown", null, cp, remainingCerts, BasicReason.UNDETERMINED_REVOCATION_STATUS);
            }
        } catch (Exception e) {
            throw new CertPathValidatorException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    throw new CertPathValidatorException(ioe);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ioe) {
                    throw new CertPathValidatorException(ioe);
                }
            }
        }
    }

    private static URL getOCSPServerURL(X509CertImpl currCertImpl, String[] properties) throws CertificateParsingException, CertPathValidatorException {
        if (properties[0] != null) {
            try {
                return new URL(properties[0]);
            } catch (java.net.MalformedURLException e) {
                throw new CertPathValidatorException(e);
            }
        }
        AuthorityInfoAccessExtension aia = currCertImpl.getAuthorityInfoAccessExtension();
        if (aia == null) {
            throw new CertPathValidatorException("Must specify the location of an OCSP Responder");
        }
        List<AccessDescription> descriptions = aia.getAccessDescriptions();
        for (AccessDescription description : descriptions) {
            if (description.getAccessMethod().equals(AccessDescription.Ad_OCSP_Id)) {
                GeneralName generalName = description.getAccessLocation();
                if (generalName.getType() == GeneralNameInterface.NAME_URI) {
                    try {
                        URIName uri = (URIName) generalName.getName();
                        return (new URL(uri.getName()));
                    } catch (java.net.MalformedURLException e) {
                        throw new CertPathValidatorException(e);
                    }
                }
            }
        }
        throw new CertPathValidatorException("Cannot find the location of the OCSP Responder");
    }

    private static String[] getOCSPProperties() {
        final String[] properties = new String[4];
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

            public Void run() {
                properties[0] = Security.getProperty(OCSP_URL_PROP);
                properties[1] = Security.getProperty(OCSP_CERT_SUBJECT_PROP);
                properties[2] = Security.getProperty(OCSP_CERT_ISSUER_PROP);
                properties[3] = Security.getProperty(OCSP_CERT_NUMBER_PROP);
                return null;
            }
        });
        return properties;
    }

    private static String stripOutSeparators(String value) {
        char[] chars = value.toCharArray();
        StringBuilder hexNumber = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            if (HEX_DIGITS.indexOf(chars[i]) != -1) {
                hexNumber.append(chars[i]);
            }
        }
        return hexNumber.toString();
    }
}
