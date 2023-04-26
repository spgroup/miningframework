package sun.security.krb5;

import sun.security.krb5.internal.*;
import sun.security.krb5.internal.crypto.*;
import sun.security.jgss.krb5.Krb5AcceptCredential;
import java.net.InetAddress;
import sun.security.util.*;
import java.io.IOException;
import java.util.Arrays;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import sun.security.krb5.internal.rcache.AuthTimeWithHash;

public class KrbApReq {

    private byte[] obuf;

    private KerberosTime ctime;

    private int cusec;

    private Authenticator authenticator;

    private Credentials creds;

    private APReq apReqMessg;

    private static ReplayCache rcache = ReplayCache.getInstance();

    private static boolean DEBUG = Krb5.DEBUG;

    private static final char[] hexConst = "0123456789ABCDEF".toCharArray();

    private static final MessageDigest md;

    static {
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Impossible");
        }
    }

    public KrbApReq(Credentials tgsCred, boolean mutualRequired, boolean useSubKey, boolean useSeqNumber, Checksum cksum) throws Asn1Exception, KrbCryptoException, KrbException, IOException {
        APOptions apOptions = (mutualRequired ? new APOptions(Krb5.AP_OPTS_MUTUAL_REQUIRED) : new APOptions());
        if (DEBUG)
            System.out.println(">>> KrbApReq: APOptions are " + apOptions);
        EncryptionKey subKey = (useSubKey ? new EncryptionKey(tgsCred.getSessionKey()) : null);
        SeqNumber seqNum = new LocalSeqNumber();
        init(apOptions, tgsCred, cksum, subKey, seqNum, null, KeyUsage.KU_AP_REQ_AUTHENTICATOR);
    }

    public KrbApReq(byte[] message, Krb5AcceptCredential cred, InetAddress initiator) throws KrbException, IOException {
        obuf = message;
        if (apReqMessg == null)
            decode();
        authenticate(cred, initiator);
    }

    KrbApReq(APOptions apOptions, Ticket ticket, EncryptionKey key, PrincipalName cname, Checksum cksum, KerberosTime ctime, EncryptionKey subKey, SeqNumber seqNumber, AuthorizationData authorizationData) throws Asn1Exception, IOException, KdcErrException, KrbCryptoException {
        init(apOptions, ticket, key, cname, cksum, ctime, subKey, seqNumber, authorizationData, KeyUsage.KU_PA_TGS_REQ_AUTHENTICATOR);
    }

    private void init(APOptions options, Credentials tgs_creds, Checksum cksum, EncryptionKey subKey, SeqNumber seqNumber, AuthorizationData authorizationData, int usage) throws KrbException, IOException {
        ctime = KerberosTime.now();
        init(options, tgs_creds.ticket, tgs_creds.key, tgs_creds.client, cksum, ctime, subKey, seqNumber, authorizationData, usage);
    }

    private void init(APOptions apOptions, Ticket ticket, EncryptionKey key, PrincipalName cname, Checksum cksum, KerberosTime ctime, EncryptionKey subKey, SeqNumber seqNumber, AuthorizationData authorizationData, int usage) throws Asn1Exception, IOException, KdcErrException, KrbCryptoException {
        createMessage(apOptions, ticket, key, cname, cksum, ctime, subKey, seqNumber, authorizationData, usage);
        obuf = apReqMessg.asn1Encode();
    }

    void decode() throws KrbException, IOException {
        DerValue encoding = new DerValue(obuf);
        decode(encoding);
    }

    void decode(DerValue encoding) throws KrbException, IOException {
        apReqMessg = null;
        try {
            apReqMessg = new APReq(encoding);
        } catch (Asn1Exception e) {
            apReqMessg = null;
            KRBError err = new KRBError(encoding);
            String errStr = err.getErrorString();
            String eText;
            if (errStr.charAt(errStr.length() - 1) == 0)
                eText = errStr.substring(0, errStr.length() - 1);
            else
                eText = errStr;
            KrbException ke = new KrbException(err.getErrorCode(), eText);
            ke.initCause(e);
            throw ke;
        }
    }

    private void authenticate(Krb5AcceptCredential cred, InetAddress initiator) throws KrbException, IOException {
        int encPartKeyType = apReqMessg.ticket.encPart.getEType();
        Integer kvno = apReqMessg.ticket.encPart.getKeyVersionNumber();
        EncryptionKey[] keys = cred.getKrb5EncryptionKeys(apReqMessg.ticket.sname);
        EncryptionKey dkey = EncryptionKey.findKey(encPartKeyType, kvno, keys);
        if (dkey == null) {
            throw new KrbException(Krb5.API_INVALID_ARG, "Cannot find key of appropriate type to decrypt AP REP - " + EType.toString(encPartKeyType));
        }
        byte[] bytes = apReqMessg.ticket.encPart.decrypt(dkey, KeyUsage.KU_TICKET);
        byte[] temp = apReqMessg.ticket.encPart.reset(bytes);
        EncTicketPart enc_ticketPart = new EncTicketPart(temp);
        checkPermittedEType(enc_ticketPart.key.getEType());
        byte[] bytes2 = apReqMessg.authenticator.decrypt(enc_ticketPart.key, KeyUsage.KU_AP_REQ_AUTHENTICATOR);
        byte[] temp2 = apReqMessg.authenticator.reset(bytes2);
        authenticator = new Authenticator(temp2);
        ctime = authenticator.ctime;
        cusec = authenticator.cusec;
        authenticator.ctime = authenticator.ctime.withMicroSeconds(authenticator.cusec);
        if (!authenticator.cname.equals(enc_ticketPart.cname)) {
            throw new KrbApErrException(Krb5.KRB_AP_ERR_BADMATCH);
        }
        if (!authenticator.ctime.inClockSkew())
            throw new KrbApErrException(Krb5.KRB_AP_ERR_SKEW);
        byte[] hash = md.digest(apReqMessg.authenticator.cipher);
        char[] h = new char[hash.length * 2];
        for (int i = 0; i < hash.length; i++) {
            h[2 * i] = hexConst[(hash[i] & 0xff) >> 4];
            h[2 * i + 1] = hexConst[hash[i] & 0xf];
        }
        AuthTimeWithHash time = new AuthTimeWithHash(authenticator.cname.toString(), apReqMessg.ticket.sname.toString(), authenticator.ctime.getSeconds(), authenticator.cusec, new String(h));
        rcache.checkAndStore(KerberosTime.now(), time);
        if (initiator != null) {
            HostAddress sender = new HostAddress(initiator);
            if (enc_ticketPart.caddr != null && !enc_ticketPart.caddr.inList(sender)) {
                if (DEBUG) {
                    System.out.println(">>> KrbApReq: initiator is " + sender.getInetAddress() + ", but caddr is " + Arrays.toString(enc_ticketPart.caddr.getInetAddresses()));
                }
                throw new KrbApErrException(Krb5.KRB_AP_ERR_BADADDR);
            }
        }
        KerberosTime now = KerberosTime.now();
        if ((enc_ticketPart.starttime != null && enc_ticketPart.starttime.greaterThanWRTClockSkew(now)) || enc_ticketPart.flags.get(Krb5.TKT_OPTS_INVALID))
            throw new KrbApErrException(Krb5.KRB_AP_ERR_TKT_NYV);
        if (enc_ticketPart.endtime != null && now.greaterThanWRTClockSkew(enc_ticketPart.endtime)) {
            throw new KrbApErrException(Krb5.KRB_AP_ERR_TKT_EXPIRED);
        }
        creds = new Credentials(apReqMessg.ticket, authenticator.cname, apReqMessg.ticket.sname, enc_ticketPart.key, enc_ticketPart.flags, enc_ticketPart.authtime, enc_ticketPart.starttime, enc_ticketPart.endtime, enc_ticketPart.renewTill, enc_ticketPart.caddr, enc_ticketPart.authorizationData);
        if (DEBUG) {
            System.out.println(">>> KrbApReq: authenticate succeed.");
        }
    }

    public Credentials getCreds() {
        return creds;
    }

    KerberosTime getCtime() {
        if (ctime != null)
            return ctime;
        return authenticator.ctime;
    }

    int cusec() {
        return cusec;
    }

    APOptions getAPOptions() throws KrbException, IOException {
        if (apReqMessg == null)
            decode();
        if (apReqMessg != null)
            return apReqMessg.apOptions;
        return null;
    }

    public boolean getMutualAuthRequired() throws KrbException, IOException {
        if (apReqMessg == null)
            decode();
        if (apReqMessg != null)
            return apReqMessg.apOptions.get(Krb5.AP_OPTS_MUTUAL_REQUIRED);
        return false;
    }

    boolean useSessionKey() throws KrbException, IOException {
        if (apReqMessg == null)
            decode();
        if (apReqMessg != null)
            return apReqMessg.apOptions.get(Krb5.AP_OPTS_USE_SESSION_KEY);
        return false;
    }

    public EncryptionKey getSubKey() {
        return authenticator.getSubKey();
    }

    public Integer getSeqNumber() {
        return authenticator.getSeqNumber();
    }

    public Checksum getChecksum() {
        return authenticator.getChecksum();
    }

    public byte[] getMessage() {
        return obuf;
    }

    public PrincipalName getClient() {
        return creds.getClient();
    }

    private void createMessage(APOptions apOptions, Ticket ticket, EncryptionKey key, PrincipalName cname, Checksum cksum, KerberosTime ctime, EncryptionKey subKey, SeqNumber seqNumber, AuthorizationData authorizationData, int usage) throws Asn1Exception, IOException, KdcErrException, KrbCryptoException {
        Integer seqno = null;
        if (seqNumber != null)
            seqno = new Integer(seqNumber.current());
        authenticator = new Authenticator(cname, cksum, ctime.getMicroSeconds(), ctime, subKey, seqno, authorizationData);
        byte[] temp = authenticator.asn1Encode();
        EncryptedData encAuthenticator = new EncryptedData(key, temp, usage);
        apReqMessg = new APReq(apOptions, ticket, encAuthenticator);
    }

    private static void checkPermittedEType(int target) throws KrbException {
        int[] etypes = EType.getDefaults("permitted_enctypes");
        if (!EType.isSupported(target, etypes)) {
            throw new KrbException(EType.toString(target) + " encryption type not in permitted_enctypes list");
        }
    }
}