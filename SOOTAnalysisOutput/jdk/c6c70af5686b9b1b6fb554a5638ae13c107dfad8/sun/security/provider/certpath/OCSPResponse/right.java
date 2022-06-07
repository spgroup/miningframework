package sun.security.provider.certpath;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CRLReason;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sun.misc.HexDumpEncoder;
import sun.security.x509.*;
import sun.security.util.*;

public final class OCSPResponse {

    public enum ResponseStatus {

        SUCCESSFUL,
        MALFORMED_REQUEST,
        INTERNAL_ERROR,
        TRY_LATER,
        UNUSED,
        SIG_REQUIRED,
        UNAUTHORIZED
    }

    private static ResponseStatus[] rsvalues = ResponseStatus.values();

    private static final Debug DEBUG = Debug.getInstance("certpath");

    private static final boolean dump = false;

    private static final ObjectIdentifier OCSP_BASIC_RESPONSE_OID = ObjectIdentifier.newInternal(new int[] { 1, 3, 6, 1, 5, 5, 7, 48, 1, 1 });

    private static final ObjectIdentifier OCSP_NONCE_EXTENSION_OID = ObjectIdentifier.newInternal(new int[] { 1, 3, 6, 1, 5, 5, 7, 48, 1, 2 });

    private static final int CERT_STATUS_GOOD = 0;

    private static final int CERT_STATUS_REVOKED = 1;

    private static final int CERT_STATUS_UNKNOWN = 2;

    private static final int NAME_TAG = 1;

    private static final int KEY_TAG = 2;

    private static final String KP_OCSP_SIGNING_OID = "1.3.6.1.5.5.7.3.9";

    private final ResponseStatus responseStatus;

    private final Map<CertId, SingleResponse> singleResponseMap;

    private static final long MAX_CLOCK_SKEW = 900000;

    private static CRLReason[] values = CRLReason.values();

    OCSPResponse(byte[] bytes, Date dateCheckedAgainst, X509Certificate responderCert) throws IOException, CertPathValidatorException {
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
        int status = derIn.getEnumerated();
        if (status >= 0 && status < rsvalues.length) {
            responseStatus = rsvalues[status];
        } else {
            throw new IOException("Unknown OCSPResponse status: " + status);
        }
        if (DEBUG != null) {
            DEBUG.println("OCSP response status: " + responseStatus);
        }
        if (responseStatus != ResponseStatus.SUCCESSFUL) {
            singleResponseMap = Collections.emptyMap();
            return;
        }
        der = derIn.getDerValue();
        if (!der.isContextSpecific((byte) 0)) {
            throw new IOException("Bad encoding in responseBytes element " + "of OCSP response: expected ASN.1 context specific tag 0.");
        }
        DerValue tmp = der.data.getDerValue();
        if (tmp.tag != DerValue.tag_Sequence) {
            throw new IOException("Bad encoding in responseBytes element " + "of OCSP response: expected ASN.1 SEQUENCE tag.");
        }
        derIn = tmp.data;
        ObjectIdentifier responseType = derIn.getOID();
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
        if (seqTmp.length < 3) {
            throw new IOException("Unexpected BasicOCSPResponse value");
        }
        DerValue responseData = seqTmp[0];
        byte[] responseDataDer = seqTmp[0].toByteArray();
        if (responseData.tag != DerValue.tag_Sequence) {
            throw new IOException("Bad encoding in tbsResponseData " + "element of OCSP response: expected ASN.1 SEQUENCE tag.");
        }
        DerInputStream seqDerIn = responseData.data;
        DerValue seq = seqDerIn.getDerValue();
        if (seq.isContextSpecific((byte) 0)) {
            if (seq.isConstructed() && seq.isContextSpecific()) {
                seq = seq.data.getDerValue();
                int version = seq.getInteger();
                if (seq.data.available() != 0) {
                    throw new IOException("Bad encoding in version " + " element of OCSP response: bad format");
                }
                seq = seqDerIn.getDerValue();
            }
        }
        short tag = (byte) (seq.tag & 0x1f);
        if (tag == NAME_TAG) {
            if (DEBUG != null) {
                X500Name responderName = new X500Name(seq.getData());
                DEBUG.println("OCSP Responder name: " + responderName);
            }
        } else if (tag == KEY_TAG) {
        } else {
            throw new IOException("Bad encoding in responderID element of " + "OCSP response: expected ASN.1 context specific tag 0 or 1");
        }
        seq = seqDerIn.getDerValue();
        if (DEBUG != null) {
            Date producedAtDate = seq.getGeneralizedTime();
            DEBUG.println("OCSP response produced at: " + producedAtDate);
        }
        DerValue[] singleResponseDer = seqDerIn.getSequence(1);
        singleResponseMap = new HashMap<CertId, SingleResponse>(singleResponseDer.length);
        if (DEBUG != null) {
            DEBUG.println("OCSP number of SingleResponses: " + singleResponseDer.length);
        }
        for (int i = 0; i < singleResponseDer.length; i++) {
            SingleResponse singleResponse = new SingleResponse(singleResponseDer[i]);
            singleResponseMap.put(singleResponse.getCertId(), singleResponse);
        }
        if (seqDerIn.available() > 0) {
            seq = seqDerIn.getDerValue();
            if (seq.isContextSpecific((byte) 1)) {
                DerValue[] responseExtDer = seq.data.getSequence(3);
                for (int i = 0; i < responseExtDer.length; i++) {
                    Extension responseExtension = new Extension(responseExtDer[i]);
                    if (DEBUG != null) {
                        DEBUG.println("OCSP extension: " + responseExtension);
                    }
                    if (responseExtension.getExtensionId().equals(OCSP_NONCE_EXTENSION_OID)) {
                    } else if (responseExtension.isCritical()) {
                        throw new IOException("Unsupported OCSP critical extension: " + responseExtension.getExtensionId());
                    }
                }
            }
        }
        AlgorithmId sigAlgId = AlgorithmId.parse(seqTmp[1]);
        byte[] signature = seqTmp[2].getBitString();
        X509CertImpl[] x509Certs = null;
        if (seqTmp.length > 3) {
            DerValue seqCert = seqTmp[3];
            if (!seqCert.isContextSpecific((byte) 0)) {
                throw new IOException("Bad encoding in certs element of " + "OCSP response: expected ASN.1 context specific tag 0.");
            }
            DerValue[] certs = seqCert.getData().getSequence(3);
            x509Certs = new X509CertImpl[certs.length];
            try {
                for (int i = 0; i < certs.length; i++) {
                    x509Certs[i] = new X509CertImpl(certs[i].toByteArray());
                }
            } catch (CertificateException ce) {
                throw new IOException("Bad encoding in X509 Certificate", ce);
            }
        }
        if (x509Certs != null && x509Certs[0] != null) {
            X509CertImpl cert = x509Certs[0];
            if (cert.equals(responderCert)) {
            } else if (cert.getIssuerX500Principal().equals(responderCert.getSubjectX500Principal())) {
                try {
                    List<String> keyPurposes = cert.getExtendedKeyUsage();
                    if (keyPurposes == null || !keyPurposes.contains(KP_OCSP_SIGNING_OID)) {
                        throw new CertPathValidatorException("Responder's certificate not valid for signing " + "OCSP responses");
                    }
                } catch (CertificateParsingException cpe) {
                    throw new CertPathValidatorException("Responder's certificate not valid for signing " + "OCSP responses", cpe);
                }
                try {
                    if (dateCheckedAgainst == null) {
                        cert.checkValidity();
                    } else {
                        cert.checkValidity(dateCheckedAgainst);
                    }
                } catch (GeneralSecurityException e) {
                    throw new CertPathValidatorException("Responder's certificate not within the " + "validity period", e);
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
                throw new CertPathValidatorException("Responder's certificate is not authorized to sign " + "OCSP responses");
            }
        }
        if (responderCert != null) {
            if (!verifyResponse(responseDataDer, responderCert, sigAlgId, signature)) {
                throw new CertPathValidatorException("Error verifying OCSP Responder's signature");
            }
        } else {
            throw new CertPathValidatorException("Unable to verify OCSP Responder's signature");
        }
    }

    ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    private boolean verifyResponse(byte[] responseData, X509Certificate cert, AlgorithmId sigAlgId, byte[] signBytes) throws CertPathValidatorException {
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
            throw new CertPathValidatorException(ike);
        } catch (NoSuchAlgorithmException nsae) {
            throw new CertPathValidatorException(nsae);
        } catch (SignatureException se) {
            throw new CertPathValidatorException(se);
        }
    }

    SingleResponse getSingleResponse(CertId certId) {
        return singleResponseMap.get(certId);
    }

    final static class SingleResponse implements OCSP.RevocationStatus {

        private final CertId certId;

        private final CertStatus certStatus;

        private final Date thisUpdate;

        private final Date nextUpdate;

        private final Date revocationTime;

        private final CRLReason revocationReason;

        private final Map<String, java.security.cert.Extension> singleExtensions;

        private SingleResponse(DerValue der) throws IOException {
            if (der.tag != DerValue.tag_Sequence) {
                throw new IOException("Bad ASN.1 encoding in SingleResponse");
            }
            DerInputStream tmp = der.data;
            certId = new CertId(tmp.getDerValue().data);
            DerValue derVal = tmp.getDerValue();
            short tag = (byte) (derVal.tag & 0x1f);
            if (tag == CERT_STATUS_REVOKED) {
                certStatus = CertStatus.REVOKED;
                revocationTime = derVal.data.getGeneralizedTime();
                if (derVal.data.available() != 0) {
                    DerValue dv = derVal.data.getDerValue();
                    tag = (byte) (dv.tag & 0x1f);
                    if (tag == 0) {
                        int reason = dv.data.getEnumerated();
                        if (reason >= 0 && reason < values.length) {
                            revocationReason = values[reason];
                        } else {
                            revocationReason = CRLReason.UNSPECIFIED;
                        }
                    } else {
                        revocationReason = CRLReason.UNSPECIFIED;
                    }
                } else {
                    revocationReason = CRLReason.UNSPECIFIED;
                }
                if (DEBUG != null) {
                    DEBUG.println("Revocation time: " + revocationTime);
                    DEBUG.println("Revocation reason: " + revocationReason);
                }
            } else {
                revocationTime = null;
                revocationReason = CRLReason.UNSPECIFIED;
                if (tag == CERT_STATUS_GOOD) {
                    certStatus = CertStatus.GOOD;
                } else if (tag == CERT_STATUS_UNKNOWN) {
                    certStatus = CertStatus.UNKNOWN;
                } else {
                    throw new IOException("Invalid certificate status");
                }
            }
            thisUpdate = tmp.getGeneralizedTime();
            if (tmp.available() == 0) {
                nextUpdate = null;
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
                } else {
                    nextUpdate = null;
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
                } else {
                    singleExtensions = Collections.emptyMap();
                }
            } else {
                singleExtensions = Collections.emptyMap();
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

        @Override
        public CertStatus getCertStatus() {
            return certStatus;
        }

        private CertId getCertId() {
            return certId;
        }

        @Override
        public Date getRevocationTime() {
            return (Date) revocationTime.clone();
        }

        @Override
        public CRLReason getRevocationReason() {
            return revocationReason;
        }

        @Override
        public Map<String, java.security.cert.Extension> getSingleExtensions() {
            return Collections.unmodifiableMap(singleExtensions);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("SingleResponse:  \n");
            sb.append(certId);
            sb.append("\nCertStatus: " + certStatus + "\n");
            if (certStatus == CertStatus.REVOKED) {
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
