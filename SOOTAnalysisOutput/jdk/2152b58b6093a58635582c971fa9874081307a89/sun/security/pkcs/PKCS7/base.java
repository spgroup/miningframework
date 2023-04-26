package sun.security.pkcs;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.CRLException;
import java.security.cert.CertificateFactory;
import java.security.*;
import sun.security.util.*;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;
import sun.security.x509.X509CRLImpl;
import sun.security.x509.X500Name;

public class PKCS7 {

    private ObjectIdentifier contentType;

    private BigInteger version = null;

    private AlgorithmId[] digestAlgorithmIds = null;

    private ContentInfo contentInfo = null;

    private X509Certificate[] certificates = null;

    private X509CRL[] crls = null;

    private SignerInfo[] signerInfos = null;

    private boolean oldStyle = false;

    private Principal[] certIssuerNames;

    public PKCS7(InputStream in) throws ParsingException, IOException {
        DataInputStream dis = new DataInputStream(in);
        byte[] data = new byte[dis.available()];
        dis.readFully(data);
        parse(new DerInputStream(data));
    }

    public PKCS7(DerInputStream derin) throws ParsingException {
        parse(derin);
    }

    public PKCS7(byte[] bytes) throws ParsingException {
        try {
            DerInputStream derin = new DerInputStream(bytes);
            parse(derin);
        } catch (IOException ioe1) {
            ParsingException pe = new ParsingException("Unable to parse the encoded bytes");
            pe.initCause(ioe1);
            throw pe;
        }
    }

    private void parse(DerInputStream derin) throws ParsingException {
        try {
            derin.mark(derin.available());
            parse(derin, false);
        } catch (IOException ioe) {
            try {
                derin.reset();
                parse(derin, true);
                oldStyle = true;
            } catch (IOException ioe1) {
                ParsingException pe = new ParsingException(ioe1.getMessage());
                pe.initCause(ioe1);
                throw pe;
            }
        }
    }

    private void parse(DerInputStream derin, boolean oldStyle) throws IOException {
        contentInfo = new ContentInfo(derin, oldStyle);
        contentType = contentInfo.contentType;
        DerValue content = contentInfo.getContent();
        if (contentType.equals((Object) ContentInfo.SIGNED_DATA_OID)) {
            parseSignedData(content);
        } else if (contentType.equals((Object) ContentInfo.OLD_SIGNED_DATA_OID)) {
            parseOldSignedData(content);
        } else if (contentType.equals((Object) ContentInfo.NETSCAPE_CERT_SEQUENCE_OID)) {
            parseNetscapeCertChain(content);
        } else {
            throw new ParsingException("content type " + contentType + " not supported.");
        }
    }

    public PKCS7(AlgorithmId[] digestAlgorithmIds, ContentInfo contentInfo, X509Certificate[] certificates, X509CRL[] crls, SignerInfo[] signerInfos) {
        version = BigInteger.ONE;
        this.digestAlgorithmIds = digestAlgorithmIds;
        this.contentInfo = contentInfo;
        this.certificates = certificates;
        this.crls = crls;
        this.signerInfos = signerInfos;
    }

    public PKCS7(AlgorithmId[] digestAlgorithmIds, ContentInfo contentInfo, X509Certificate[] certificates, SignerInfo[] signerInfos) {
        this(digestAlgorithmIds, contentInfo, certificates, null, signerInfos);
    }

    private void parseNetscapeCertChain(DerValue val) throws ParsingException, IOException {
        DerInputStream dis = new DerInputStream(val.toByteArray());
        DerValue[] contents = dis.getSequence(2);
        certificates = new X509Certificate[contents.length];
        CertificateFactory certfac = null;
        try {
            certfac = CertificateFactory.getInstance("X.509");
        } catch (CertificateException ce) {
        }
        for (int i = 0; i < contents.length; i++) {
            ByteArrayInputStream bais = null;
            try {
                if (certfac == null)
                    certificates[i] = new X509CertImpl(contents[i]);
                else {
                    byte[] encoded = contents[i].toByteArray();
                    bais = new ByteArrayInputStream(encoded);
                    certificates[i] = (X509Certificate) certfac.generateCertificate(bais);
                    bais.close();
                    bais = null;
                }
            } catch (CertificateException ce) {
                ParsingException pe = new ParsingException(ce.getMessage());
                pe.initCause(ce);
                throw pe;
            } catch (IOException ioe) {
                ParsingException pe = new ParsingException(ioe.getMessage());
                pe.initCause(ioe);
                throw pe;
            } finally {
                if (bais != null)
                    bais.close();
            }
        }
    }

    private void parseSignedData(DerValue val) throws ParsingException, IOException {
        DerInputStream dis = val.toDerInputStream();
        version = dis.getBigInteger();
        DerValue[] digestAlgorithmIdVals = dis.getSet(1);
        int len = digestAlgorithmIdVals.length;
        digestAlgorithmIds = new AlgorithmId[len];
        try {
            for (int i = 0; i < len; i++) {
                DerValue oid = digestAlgorithmIdVals[i];
                digestAlgorithmIds[i] = AlgorithmId.parse(oid);
            }
        } catch (IOException e) {
            ParsingException pe = new ParsingException("Error parsing digest AlgorithmId IDs: " + e.getMessage());
            pe.initCause(e);
            throw pe;
        }
        contentInfo = new ContentInfo(dis);
        CertificateFactory certfac = null;
        try {
            certfac = CertificateFactory.getInstance("X.509");
        } catch (CertificateException ce) {
        }
        if ((byte) (dis.peekByte()) == (byte) 0xA0) {
            DerValue[] certVals = dis.getSet(2, true);
            len = certVals.length;
            certificates = new X509Certificate[len];
            for (int i = 0; i < len; i++) {
                ByteArrayInputStream bais = null;
                try {
                    if (certfac == null)
                        certificates[i] = new X509CertImpl(certVals[i]);
                    else {
                        byte[] encoded = certVals[i].toByteArray();
                        bais = new ByteArrayInputStream(encoded);
                        certificates[i] = (X509Certificate) certfac.generateCertificate(bais);
                        bais.close();
                        bais = null;
                    }
                } catch (CertificateException ce) {
                    ParsingException pe = new ParsingException(ce.getMessage());
                    pe.initCause(ce);
                    throw pe;
                } catch (IOException ioe) {
                    ParsingException pe = new ParsingException(ioe.getMessage());
                    pe.initCause(ioe);
                    throw pe;
                } finally {
                    if (bais != null)
                        bais.close();
                }
            }
        }
        if ((byte) (dis.peekByte()) == (byte) 0xA1) {
            DerValue[] crlVals = dis.getSet(1, true);
            len = crlVals.length;
            crls = new X509CRL[len];
            for (int i = 0; i < len; i++) {
                ByteArrayInputStream bais = null;
                try {
                    if (certfac == null)
                        crls[i] = new X509CRLImpl(crlVals[i]);
                    else {
                        byte[] encoded = crlVals[i].toByteArray();
                        bais = new ByteArrayInputStream(encoded);
                        crls[i] = (X509CRL) certfac.generateCRL(bais);
                        bais.close();
                        bais = null;
                    }
                } catch (CRLException e) {
                    ParsingException pe = new ParsingException(e.getMessage());
                    pe.initCause(e);
                    throw pe;
                } finally {
                    if (bais != null)
                        bais.close();
                }
            }
        }
        DerValue[] signerInfoVals = dis.getSet(1);
        len = signerInfoVals.length;
        signerInfos = new SignerInfo[len];
        for (int i = 0; i < len; i++) {
            DerInputStream in = signerInfoVals[i].toDerInputStream();
            signerInfos[i] = new SignerInfo(in);
        }
    }

    private void parseOldSignedData(DerValue val) throws ParsingException, IOException {
        DerInputStream dis = val.toDerInputStream();
        version = dis.getBigInteger();
        DerValue[] digestAlgorithmIdVals = dis.getSet(1);
        int len = digestAlgorithmIdVals.length;
        digestAlgorithmIds = new AlgorithmId[len];
        try {
            for (int i = 0; i < len; i++) {
                DerValue oid = digestAlgorithmIdVals[i];
                digestAlgorithmIds[i] = AlgorithmId.parse(oid);
            }
        } catch (IOException e) {
            throw new ParsingException("Error parsing digest AlgorithmId IDs");
        }
        contentInfo = new ContentInfo(dis, true);
        CertificateFactory certfac = null;
        try {
            certfac = CertificateFactory.getInstance("X.509");
        } catch (CertificateException ce) {
        }
        DerValue[] certVals = dis.getSet(2);
        len = certVals.length;
        certificates = new X509Certificate[len];
        for (int i = 0; i < len; i++) {
            ByteArrayInputStream bais = null;
            try {
                if (certfac == null)
                    certificates[i] = new X509CertImpl(certVals[i]);
                else {
                    byte[] encoded = certVals[i].toByteArray();
                    bais = new ByteArrayInputStream(encoded);
                    certificates[i] = (X509Certificate) certfac.generateCertificate(bais);
                    bais.close();
                    bais = null;
                }
            } catch (CertificateException ce) {
                ParsingException pe = new ParsingException(ce.getMessage());
                pe.initCause(ce);
                throw pe;
            } catch (IOException ioe) {
                ParsingException pe = new ParsingException(ioe.getMessage());
                pe.initCause(ioe);
                throw pe;
            } finally {
                if (bais != null)
                    bais.close();
            }
        }
        dis.getSet(0);
        DerValue[] signerInfoVals = dis.getSet(1);
        len = signerInfoVals.length;
        signerInfos = new SignerInfo[len];
        for (int i = 0; i < len; i++) {
            DerInputStream in = signerInfoVals[i].toDerInputStream();
            signerInfos[i] = new SignerInfo(in, true);
        }
    }

    public void encodeSignedData(OutputStream out) throws IOException {
        DerOutputStream derout = new DerOutputStream();
        encodeSignedData(derout);
        out.write(derout.toByteArray());
    }

    public void encodeSignedData(DerOutputStream out) throws IOException {
        DerOutputStream signedData = new DerOutputStream();
        signedData.putInteger(version);
        signedData.putOrderedSetOf(DerValue.tag_Set, digestAlgorithmIds);
        contentInfo.encode(signedData);
        if (certificates != null && certificates.length != 0) {
            X509CertImpl[] implCerts = new X509CertImpl[certificates.length];
            for (int i = 0; i < certificates.length; i++) {
                if (certificates[i] instanceof X509CertImpl)
                    implCerts[i] = (X509CertImpl) certificates[i];
                else {
                    try {
                        byte[] encoded = certificates[i].getEncoded();
                        implCerts[i] = new X509CertImpl(encoded);
                    } catch (CertificateException ce) {
                        throw new IOException(ce);
                    }
                }
            }
            signedData.putOrderedSetOf((byte) 0xA0, implCerts);
        }
        if (crls != null && crls.length != 0) {
            Set<X509CRLImpl> implCRLs = new HashSet<X509CRLImpl>(crls.length);
            for (X509CRL crl : crls) {
                if (crl instanceof X509CRLImpl)
                    implCRLs.add((X509CRLImpl) crl);
                else {
                    try {
                        byte[] encoded = crl.getEncoded();
                        implCRLs.add(new X509CRLImpl(encoded));
                    } catch (CRLException ce) {
                        throw new IOException(ce);
                    }
                }
            }
            signedData.putOrderedSetOf((byte) 0xA1, implCRLs.toArray(new X509CRLImpl[implCRLs.size()]));
        }
        signedData.putOrderedSetOf(DerValue.tag_Set, signerInfos);
        DerValue signedDataSeq = new DerValue(DerValue.tag_Sequence, signedData.toByteArray());
        ContentInfo block = new ContentInfo(ContentInfo.SIGNED_DATA_OID, signedDataSeq);
        block.encode(out);
    }

    public SignerInfo verify(SignerInfo info, byte[] bytes) throws NoSuchAlgorithmException, SignatureException {
        return info.verify(this, bytes);
    }

    public SignerInfo[] verify(byte[] bytes) throws NoSuchAlgorithmException, SignatureException {
        Vector<SignerInfo> intResult = new Vector<SignerInfo>();
        for (int i = 0; i < signerInfos.length; i++) {
            SignerInfo signerInfo = verify(signerInfos[i], bytes);
            if (signerInfo != null) {
                intResult.addElement(signerInfo);
            }
        }
        if (!intResult.isEmpty()) {
            SignerInfo[] result = new SignerInfo[intResult.size()];
            intResult.copyInto(result);
            return result;
        }
        return null;
    }

    public SignerInfo[] verify() throws NoSuchAlgorithmException, SignatureException {
        return verify(null);
    }

    public BigInteger getVersion() {
        return version;
    }

    public AlgorithmId[] getDigestAlgorithmIds() {
        return digestAlgorithmIds;
    }

    public ContentInfo getContentInfo() {
        return contentInfo;
    }

    public X509Certificate[] getCertificates() {
        if (certificates != null)
            return certificates.clone();
        else
            return null;
    }

    public X509CRL[] getCRLs() {
        if (crls != null)
            return crls.clone();
        else
            return null;
    }

    public SignerInfo[] getSignerInfos() {
        return signerInfos;
    }

    public X509Certificate getCertificate(BigInteger serial, X500Name issuerName) {
        if (certificates != null) {
            if (certIssuerNames == null)
                populateCertIssuerNames();
            for (int i = 0; i < certificates.length; i++) {
                X509Certificate cert = certificates[i];
                BigInteger thisSerial = cert.getSerialNumber();
                if (serial.equals(thisSerial) && issuerName.equals(certIssuerNames[i])) {
                    return cert;
                }
            }
        }
        return null;
    }

    private void populateCertIssuerNames() {
        if (certificates == null)
            return;
        certIssuerNames = new Principal[certificates.length];
        for (int i = 0; i < certificates.length; i++) {
            X509Certificate cert = certificates[i];
            Principal certIssuerName = cert.getIssuerDN();
            if (!(certIssuerName instanceof X500Name)) {
                try {
                    X509CertInfo tbsCert = new X509CertInfo(cert.getTBSCertificate());
                    certIssuerName = (Principal) tbsCert.get(CertificateIssuerName.NAME + "." + CertificateIssuerName.DN_NAME);
                } catch (Exception e) {
                }
            }
            certIssuerNames[i] = certIssuerName;
        }
    }

    public String toString() {
        String out = "";
        out += contentInfo + "\n";
        if (version != null)
            out += "PKCS7 :: version: " + Debug.toHexString(version) + "\n";
        if (digestAlgorithmIds != null) {
            out += "PKCS7 :: digest AlgorithmIds: \n";
            for (int i = 0; i < digestAlgorithmIds.length; i++) out += "\t" + digestAlgorithmIds[i] + "\n";
        }
        if (certificates != null) {
            out += "PKCS7 :: certificates: \n";
            for (int i = 0; i < certificates.length; i++) out += "\t" + i + ".   " + certificates[i] + "\n";
        }
        if (crls != null) {
            out += "PKCS7 :: crls: \n";
            for (int i = 0; i < crls.length; i++) out += "\t" + i + ".   " + crls[i] + "\n";
        }
        if (signerInfos != null) {
            out += "PKCS7 :: signer infos: \n";
            for (int i = 0; i < signerInfos.length; i++) out += ("\t" + i + ".  " + signerInfos[i] + "\n");
        }
        return out;
    }

    public boolean isOldStyle() {
        return this.oldStyle;
    }
}
