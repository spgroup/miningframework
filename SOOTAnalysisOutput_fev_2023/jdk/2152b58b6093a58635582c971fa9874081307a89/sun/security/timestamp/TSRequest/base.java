package sun.security.timestamp;

import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.X509Extension;
import sun.security.util.DerValue;
import sun.security.util.DerOutputStream;
import sun.security.util.ObjectIdentifier;

public class TSRequest {

    private static final ObjectIdentifier SHA1_OID;

    private static final ObjectIdentifier MD5_OID;

    static {
        ObjectIdentifier sha1 = null;
        ObjectIdentifier md5 = null;
        try {
            sha1 = new ObjectIdentifier("1.3.14.3.2.26");
            md5 = new ObjectIdentifier("1.2.840.113549.2.5");
        } catch (IOException ioe) {
        }
        SHA1_OID = sha1;
        MD5_OID = md5;
    }

    private int version = 1;

    private ObjectIdentifier hashAlgorithmId = null;

    private byte[] hashValue;

    private String policyId = null;

    private BigInteger nonce = null;

    private boolean returnCertificate = false;

    private X509Extension[] extensions = null;

    public TSRequest(byte[] hashValue, String hashAlgorithm) {
        if ("MD5".equalsIgnoreCase(hashAlgorithm)) {
            hashAlgorithmId = MD5_OID;
            assert hashValue.length == 16;
        } else if ("SHA-1".equalsIgnoreCase(hashAlgorithm) || "SHA".equalsIgnoreCase(hashAlgorithm) || "SHA1".equalsIgnoreCase(hashAlgorithm)) {
            hashAlgorithmId = SHA1_OID;
            assert hashValue.length == 20;
        }
        this.hashValue = new byte[hashValue.length];
        System.arraycopy(hashValue, 0, this.hashValue, 0, hashValue.length);
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public void setNonce(BigInteger nonce) {
        this.nonce = nonce;
    }

    public void requestCertificate(boolean returnCertificate) {
        this.returnCertificate = returnCertificate;
    }

    public void setExtensions(X509Extension[] extensions) {
        this.extensions = extensions;
    }

    public byte[] encode() throws IOException {
        DerOutputStream request = new DerOutputStream();
        request.putInteger(version);
        DerOutputStream messageImprint = new DerOutputStream();
        DerOutputStream hashAlgorithm = new DerOutputStream();
        hashAlgorithm.putOID(hashAlgorithmId);
        messageImprint.write(DerValue.tag_Sequence, hashAlgorithm);
        messageImprint.putOctetString(hashValue);
        request.write(DerValue.tag_Sequence, messageImprint);
        if (policyId != null) {
            request.putOID(new ObjectIdentifier(policyId));
        }
        if (nonce != null) {
            request.putInteger(nonce);
        }
        if (returnCertificate) {
            request.putBoolean(true);
        }
        DerOutputStream out = new DerOutputStream();
        out.write(DerValue.tag_Sequence, request);
        return out.toByteArray();
    }
}
