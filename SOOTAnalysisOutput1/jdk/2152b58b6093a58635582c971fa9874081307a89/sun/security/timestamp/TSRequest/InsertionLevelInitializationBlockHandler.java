package sun.security.timestamp;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Extension;
import sun.security.util.DerValue;
import sun.security.util.DerOutputStream;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;

public class TSRequest {

    private int version = 1;

    private AlgorithmId hashAlgorithmId = null;

    private byte[] hashValue;

    private String policyId = null;

    private BigInteger nonce = null;

    private boolean returnCertificate = false;

    private X509Extension[] extensions = null;

    public TSRequest(byte[] toBeTimeStamped, MessageDigest messageDigest) throws NoSuchAlgorithmException {
        this.hashAlgorithmId = AlgorithmId.get(messageDigest.getAlgorithm());
        this.hashValue = messageDigest.digest(toBeTimeStamped);
    }

    public byte[] getHashedMessage() {
        return hashValue.clone();
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
        hashAlgorithmId.encode(messageImprint);
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