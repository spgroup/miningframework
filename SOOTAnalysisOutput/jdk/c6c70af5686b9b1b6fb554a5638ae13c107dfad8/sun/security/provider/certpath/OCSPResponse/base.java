package sun.security.provider.certpath;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CRLReason;
import java.security.cert.X509Certificate;
import java.security.cert.PKIXParameters;
import javax.security.auth.x500.X500Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import sun.misc.HexDumpEncoder;
import sun.security.x509.*;
import sun.security.util.*;

class OCSPResponse {

    public static final int CERT_STATUS_GOOD = 0;

    public static final int CERT_STATUS_REVOKED = 1;

    public static final int CERT_STATUS_UNKNOWN = 2;

    private static final Debug DEBUG = Debug.getInstance("certpath");

    private static final boolean dump = false;

    private static final ObjectIdentifier OCSP_BASIC_RESPONSE_OID;

    private static final ObjectIdentifier OCSP_NONCE_EXTENSION_OID;

    static {
        ObjectIdentifier tmp1 = null;
        ObjectIdentifier tmp2 = null;
        try {
            tmp1 = new ObjectIdentifier("1.3.6.1.5.5.7.48.1.1");
            tmp2 = new ObjectIdentifier("1.3.6.1.5.5.7.48.1.2");
        } catch (Exception e) {
        }
        OCSP_BASIC_RESPONSE_OID = tmp1;
        OCSP_NONCE_EXTENSION_OID = tmp2;
    }

    private static final int OCSP_RESPONSE_OK = 0;

    private static final int NAME_TAG = 1;

    private static final int KEY_TAG = 2;

    private static final String KP_OCSP_SIGNING_OID = "1.3.6.1.5.5.7.3.9";

    private SingleResponse singleResponse;

    private static final long MAX_CLOCK_SKEW = 900000;

    private static CRLReason[] values = CRLReason.values();

    OCSPResponse(byte[] bytes, PKIXParameters params, X509Certificate responderCert) throws IOException, CertPathValidatorException {
        try {
            int responseStatus;
            ObjectIdentifier responseType;
            int version;
            CertificateIssuerName responderName = null;
            Date producedAtDate;
            AlgorithmId sigAlgId;
            byte[] ocspNonce;
            if (dump) {
                HexDumpEncoder hexEnc = new HexDumpEncoder();
                System.out.println("OCSPResponse bytes are...");
                System.out.println(hexEnc.encode(bytes));
            }
            DerValue der = new DerValue(bytes);
            if (der.tag != DerValue.tag_Sequence) {
                throw new IOException("Bad encoding in OCSP response: " + "expected ASN.1 SEQUENCE tag.");
            }
            DerInputStream derIn = der.getData();
            responseStatus = derIn.getEnumerated();
            if (DEBUG != null) {
                DEBUG.println("OCSP response: " + responseToText(responseStatus));
            }
            if (responseStatus != OCSP_RESPONSE_OK) {
                throw new CertPathValidatorException("OCSP Response Failure: " + responseToText(responseStatus));
            }
            der = derIn.getDerValue();
            if (!der.isContextSpecific((byte) 0)) {
                throw new IOException("Bad encoding in responseBytes element " + "of OCSP response: expected ASN.1 context specific tag 0.");
            }
            ;
            DerValue tmp = der.data.getDerValue();
            if (tmp.tag != DerValue.tag_Sequence) {
                throw new IOException("Bad encoding in responseBytes element " + "of OCSP response: expected ASN.1 SEQUENCE tag.");
            }
            derIn = tmp.data;
            responseType = derIn.getOID();
            if (responseType.equals(OCSP_BASIC_RESPONSE_OID)) {
                if (DEBUG != null) {
                    DEBUG.println("OCSP response type: basic");
                }
            } else {
                if (DEBUG != null) {
                    DEBUG.println("OCSP response type: " + responseType);
                }
                throw new IOException("Unsupported OCSP response type: " + responseType);
            }
            DerInputStream basicOCSPResponse = new DerInputStream(derIn.getOctetString());
            DerValue[] seqTmp = basicOCSPResponse.getSequence(2);
            DerValue responseData = seqTmp[0];
            byte[] responseDataDer = seqTmp[0].toByteArray();
            if (responseData.tag != DerValue.tag_Sequence) {
                throw new IOException("Bad encoding in tbsResponseData " + " element of OCSP response: expected ASN.1 SEQUENCE tag.");
            }
            DerInputStream seqDerIn = responseData.data;
            DerValue seq = seqDerIn.getDerValue();
            if (seq.isContextSpecific((byte) 0)) {
                if (seq.isConstructed() && seq.isContextSpecific()) {
                    seq = seq.data.getDerValue();
                    version = seq.getInteger();
                    if (seq.data.available() != 0) {
                        throw new IOException("Bad encoding in version " + " element of OCSP response: bad format");
                    }
                    seq = seqDerIn.getDerValue();
                }
            }
            short tag = (byte) (seq.tag & 0x1f);
            if (tag == NAME_TAG) {
                responderName = new CertificateIssuerName(seq.getData());
                if (DEBUG != null) {
                    DEBUG.println("OCSP Responder name: " + responderName);
                }
            } else if (tag == KEY_TAG) {
            } else {
                throw new IOException("Bad encoding in responderID element " + "of OCSP response: expected ASN.1 context specific tag 0 " + "or 1");
            }
            seq = seqDerIn.getDerValue();
            producedAtDate = seq.getGeneralizedTime();
            DerValue[] singleResponseDer = seqDerIn.getSequence(1);
            singleResponse = new SingleResponse(singleResponseDer[0]);
            if (seqDerIn.available() > 0) {
                seq = seqDerIn.getDerValue();
                if (seq.isContextSpecific((byte) 1)) {
                    DerValue[] responseExtDer = seq.data.getSequence(3);
                    Extension[] responseExtension = new Extension[responseExtDer.length];
                    for (int i = 0; i < responseExtDer.length; i++) {
                        responseExtension[i] = new Extension(responseExtDer[i]);
                        if (DEBUG != null) {
                            DEBUG.println("OCSP extension: " + responseExtension[i]);
                        }
                        if ((responseExtension[i].getExtensionId()).equals(OCSP_NONCE_EXTENSION_OID)) {
                            ocspNonce = responseExtension[i].getExtensionValue();
                        } else if (responseExtension[i].isCritical()) {
                            throw new IOException("Unsupported OCSP critical extension: " + responseExtension[i].getExtensionId());
                        }
                    }
                }
            }
            sigAlgId = AlgorithmId.parse(seqTmp[1]);
            byte[] signature = seqTmp[2].getBitString();
            X509CertImpl[] x509Certs = null;
            if (seqTmp.length > 3) {
                DerValue seqCert = seqTmp[3];
                if (!seqCert.isContextSpecific((byte) 0)) {
                    throw new IOException("Bad encoding in certs element " + "of OCSP response: expected ASN.1 context specific tag 0.");
                }
                DerValue[] certs = (seqCert.getData()).getSequence(3);
                x509Certs = new X509CertImpl[certs.length];
                for (int i = 0; i < certs.length; i++) {
                    x509Certs[i] = new X509CertImpl(certs[i].toByteArray());
                }
            }
            if (x509Certs != null && x509Certs[0] != null) {
                X509CertImpl cert = x509Certs[0];
                if (cert.equals(responderCert)) {
                } else if (cert.getIssuerX500Principal().equals(responderCert.getSubjectX500Principal())) {
                    List<String> keyPurposes = cert.getExtendedKeyUsage();
                    if (keyPurposes == null || !keyPurposes.contains(KP_OCSP_SIGNING_OID)) {
                        if (DEBUG != null) {
                            DEBUG.println("Responder's certificate is not " + "valid for signing OCSP responses.");
                        }
                        throw new CertPathValidatorException("Responder's certificate not valid for signing " + "OCSP responses");
                    }
                    try {
                        Date dateCheckedAgainst = params.getDate();
                        if (dateCheckedAgainst == null) {
                            cert.checkValidity();
                        } else {
                            cert.checkValidity(dateCheckedAgainst);
                        }
                    } catch (GeneralSecurityException e) {
                        if (DEBUG != null) {
                            DEBUG.println("Responder's certificate is not " + "within the validity period.");
                        }
                        throw new CertPathValidatorException("Responder's certificate not within the " + "validity period");
                    }
                    Extension noCheck = cert.getExtension(PKIXExtensions.OCSPNoCheck_Id);
                    if (noCheck != null) {
                        if (DEBUG != null) {
                            DEBUG.println("Responder's certificate includes " + "the extension id-pkix-ocsp-nocheck.");
                        }
                    } else {
                    }
                    try {
                        cert.verify(responderCert.getPublicKey());
                        responderCert = cert;
                    } catch (GeneralSecurityException e) {
                        responderCert = null;
                    }
                } else {
                    if (DEBUG != null) {
                        DEBUG.println("Responder's certificate is not " + "authorized to sign OCSP responses.");
                    }
                    throw new CertPathValidatorException("Responder's certificate not authorized to sign " + "OCSP responses");
                }
            }
            if (responderCert != null) {
                if (!verifyResponse(responseDataDer, responderCert, sigAlgId, signature, params)) {
                    if (DEBUG != null) {
                        DEBUG.println("Error verifying OCSP Responder's " + "signature");
                    }
                    throw new CertPathValidatorException("Error verifying OCSP Responder's signature");
                }
            } else {
                if (DEBUG != null) {
                    DEBUG.println("Unable to verify OCSP Responder's " + "signature");
                }
                throw new CertPathValidatorException("Unable to verify OCSP Responder's signature");
            }
        } catch (CertPathValidatorException cpve) {
            throw cpve;
        } catch (Exception e) {
            throw new CertPathValidatorException(e);
        }
    }

    private boolean verifyResponse(byte[] responseData, X509Certificate cert, AlgorithmId sigAlgId, byte[] signBytes, PKIXParameters params) throws SignatureException {
        try {
            Signature respSignature = Signature.getInstance(sigAlgId.getName());
            respSignature.initVerify(cert);
            respSignature.update(responseData);
            if (respSignature.verify(signBytes)) {
                if (DEBUG != null) {
                    DEBUG.println("Verified signature of OCSP Responder");
                }
                return true;
            } else {
                if (DEBUG != null) {
                    DEBUG.println("Error verifying signature of OCSP Responder");
                }
                return false;
            }
        } catch (InvalidKeyException ike) {
            throw new SignatureException(ike);
        } catch (NoSuchAlgorithmException nsae) {
            throw new SignatureException(nsae);
        }
    }

    int getCertStatus(SerialNumber sn) {
        return singleResponse.getStatus();
    }

    CertId getCertId() {
        return singleResponse.getCertId();
    }

    Date getRevocationTime() {
        return singleResponse.getRevocationTime();
    }

    CRLReason getRevocationReason() {
        return singleResponse.getRevocationReason();
    }

    Map<String, java.security.cert.Extension> getSingleExtensions() {
        return singleResponse.getSingleExtensions();
    }

    static private String responseToText(int status) {
        switch(status) {
            case 0:
                return "Successful";
            case 1:
                return "Malformed request";
            case 2:
                return "Internal error";
            case 3:
                return "Try again later";
            case 4:
                return "Unused status code";
            case 5:
                return "Request must be signed";
            case 6:
                return "Request is unauthorized";
            default:
                return ("Unknown status code: " + status);
        }
    }

    static String certStatusToText(int certStatus) {
        switch(certStatus) {
            case 0:
                return "Good";
            case 1:
                return "Revoked";
            case 2:
                return "Unknown";
            default:
                return ("Unknown certificate status code: " + certStatus);
        }
    }

    private class SingleResponse {

        private CertId certId;

        private int certStatus;

        private Date thisUpdate;

        private Date nextUpdate;

        private Date revocationTime;

        private CRLReason revocationReason = CRLReason.UNSPECIFIED;

        private HashMap<String, java.security.cert.Extension> singleExtensions;

        private SingleResponse(DerValue der) throws IOException {
            if (der.tag != DerValue.tag_Sequence) {
                throw new IOException("Bad ASN.1 encoding in SingleResponse");
            }
            DerInputStream tmp = der.data;
            certId = new CertId(tmp.getDerValue().data);
            DerValue derVal = tmp.getDerValue();
            short tag = (byte) (derVal.tag & 0x1f);
            if (tag == CERT_STATUS_GOOD) {
                certStatus = CERT_STATUS_GOOD;
            } else if (tag == CERT_STATUS_REVOKED) {
                certStatus = CERT_STATUS_REVOKED;
                revocationTime = derVal.data.getGeneralizedTime();
                if (derVal.data.available() != 0) {
                    int reason = derVal.getEnumerated();
                    if (reason >= 0 && reason < values.length) {
                        revocationReason = values[reason];
                    }
                }
                if (DEBUG != null) {
                    DEBUG.println("Revocation time: " + revocationTime);
                    DEBUG.println("Revocation reason: " + revocationReason);
                }
            } else if (tag == CERT_STATUS_UNKNOWN) {
                certStatus = CERT_STATUS_UNKNOWN;
            } else {
                throw new IOException("Invalid certificate status");
            }
            thisUpdate = tmp.getGeneralizedTime();
            if (tmp.available() == 0) {
            } else {
                derVal = tmp.getDerValue();
                tag = (byte) (derVal.tag & 0x1f);
                if (tag == 0) {
                    nextUpdate = derVal.data.getGeneralizedTime();
                    if (tmp.available() == 0) {
                    } else {
                        derVal = tmp.getDerValue();
                        tag = (byte) (derVal.tag & 0x1f);
                    }
                }
            }
            if (tmp.available() > 0) {
                derVal = tmp.getDerValue();
                if (derVal.isContextSpecific((byte) 1)) {
                    DerValue[] singleExtDer = derVal.data.getSequence(3);
                    singleExtensions = new HashMap<String, java.security.cert.Extension>(singleExtDer.length);
                    for (int i = 0; i < singleExtDer.length; i++) {
                        Extension ext = new Extension(singleExtDer[i]);
                        singleExtensions.put(ext.getId(), ext);
                        if (DEBUG != null) {
                            DEBUG.println("OCSP single extension: " + ext);
                        }
                    }
                }
            }
            long now = System.currentTimeMillis();
            Date nowPlusSkew = new Date(now + MAX_CLOCK_SKEW);
            Date nowMinusSkew = new Date(now - MAX_CLOCK_SKEW);
            if (DEBUG != null) {
                String until = "";
                if (nextUpdate != null) {
                    until = " until " + nextUpdate;
                }
                DEBUG.println("Response's validity interval is from " + thisUpdate + until);
            }
            if ((thisUpdate != null && nowPlusSkew.before(thisUpdate)) || (nextUpdate != null && nowMinusSkew.after(nextUpdate))) {
                if (DEBUG != null) {
                    DEBUG.println("Response is unreliable: its validity " + "interval is out-of-date");
                }
                throw new IOException("Response is unreliable: its validity " + "interval is out-of-date");
            }
        }

        private int getStatus() {
            return certStatus;
        }

        private CertId getCertId() {
            return certId;
        }

        private Date getRevocationTime() {
            return revocationTime;
        }

        private CRLReason getRevocationReason() {
            return revocationReason;
        }

        private Map<String, java.security.cert.Extension> getSingleExtensions() {
            return singleExtensions;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("SingleResponse:  \n");
            sb.append(certId);
            sb.append("\nCertStatus: " + certStatusToText(getCertStatus(null)) + "\n");
            if (certStatus == CERT_STATUS_REVOKED) {
                sb.append("revocationTime is " + revocationTime + "\n");
                sb.append("revocationReason is " + revocationReason + "\n");
            }
            sb.append("thisUpdate is " + thisUpdate + "\n");
            if (nextUpdate != null) {
                sb.append("nextUpdate is " + nextUpdate + "\n");
            }
            return sb.toString();
        }
    }
}
