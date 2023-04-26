import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.io.*;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;
import sun.net.spi.nameservice.NameService;
import sun.net.spi.nameservice.NameServiceDescriptor;
import sun.security.krb5.*;
import sun.security.krb5.internal.*;
import sun.security.krb5.internal.ccache.CredentialsCache;
import sun.security.krb5.internal.crypto.KeyUsage;
import sun.security.krb5.internal.ktab.KeyTab;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class KDC {

    private static SecureRandom secureRandom = new SecureRandom();

    private TreeMap<String, char[]> passwords = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private String realm;

    private String kdc;

    private int port;

    private BlockingQueue<Job> q = new ArrayBlockingQueue<>(100);

    private Map<Option, Object> options = new HashMap<>();

    private Thread thread1, thread2, thread3;

    DatagramSocket u1 = null;

    ServerSocket t1 = null;

    public static enum Option {

        PREAUTH_REQUIRED, ONLY_RC4_TGT, RC4_FIRST_PREAUTH, ONLY_ONE_PREAUTH
    }

    static {
        System.setProperty("sun.net.spi.nameservice.provider.1", "ns,mock");
    }

    public static void main(String[] args) throws Exception {
        KDC kdc = create("RABBIT.HOLE", "kdc.rabbit.hole", 0, false);
        kdc.addPrincipal("dummy", "bogus".toCharArray());
        kdc.addPrincipal("foo", "bar".toCharArray());
        kdc.addPrincipalRandKey("krbtgt/RABBIT.HOLE");
        kdc.addPrincipalRandKey("server/host.rabbit.hole");
        kdc.addPrincipalRandKey("backend/host.rabbit.hole");
        KDC.saveConfig("krb5.conf", kdc, "forwardable = true");
    }

    public static KDC create(String realm) throws IOException {
        return create(realm, "kdc." + realm.toLowerCase(), 0, true);
    }

    public static KDC existing(String realm, String kdc, int port) {
        KDC k = new KDC(realm, kdc);
        k.port = port;
        return k;
    }

    public static KDC create(String realm, String kdc, int port, boolean asDaemon) throws IOException {
        return new KDC(realm, kdc, port, asDaemon);
    }

    public void setOption(Option key, Object value) {
        options.put(key, value);
    }

    public static void writeMultiKtab(String tab, KDC... kdcs) throws IOException, KrbException {
        KeyTab ktab = KeyTab.create(tab);
        for (KDC kdc : kdcs) {
            for (String name : kdc.passwords.keySet()) {
                ktab.addEntry(new PrincipalName(name, name.indexOf('/') < 0 ? PrincipalName.KRB_NT_UNKNOWN : PrincipalName.KRB_NT_SRV_HST), kdc.passwords.get(name), -1, true);
            }
        }
        ktab.save();
    }

    public void writeKtab(String tab) throws IOException, KrbException {
        KDC.writeMultiKtab(tab, this);
    }

    public void addPrincipal(String user, char[] pass) {
        if (user.indexOf('@') < 0) {
            user = user + "@" + realm;
        }
        passwords.put(user, pass);
    }

    public void addPrincipalRandKey(String user) {
        addPrincipal(user, randomPassword());
    }

    public String getRealm() {
        return realm;
    }

    public String getKDC() {
        return kdc;
    }

    public static void saveConfig(String file, KDC kdc, Object... more) throws IOException {
        File f = new File(file);
        StringBuffer sb = new StringBuffer();
        sb.append("[libdefaults]\ndefault_realm = ");
        sb.append(kdc.realm);
        sb.append("\n");
        for (Object o : more) {
            if (o instanceof String) {
                sb.append(o);
                sb.append("\n");
            }
        }
        sb.append("\n[realms]\n");
        sb.append(realmLineForKDC(kdc));
        for (Object o : more) {
            if (o instanceof KDC) {
                sb.append(realmLineForKDC((KDC) o));
            }
        }
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(sb.toString().getBytes());
        fos.close();
    }

    public int getPort() {
        return port;
    }

    private KDC(String realm, String kdc) {
        this.realm = realm;
        this.kdc = kdc;
    }

    protected KDC(String realm, String kdc, int port, boolean asDaemon) throws IOException {
        this(realm, kdc);
        startServer(port, asDaemon);
    }

    private static char[] randomPassword() {
        char[] pass = new char[32];
        for (int i = 0; i < 31; i++) pass[i] = (char) secureRandom.nextInt();
        pass[31] = 'Z';
        return pass;
    }

    private static EncryptionKey generateRandomKey(int eType) throws KrbException {
        char[] pass = randomPassword();
        String algo;
        switch(eType) {
            case EncryptedData.ETYPE_DES_CBC_MD5:
                algo = "DES";
                break;
            case EncryptedData.ETYPE_DES3_CBC_HMAC_SHA1_KD:
                algo = "DESede";
                break;
            case EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96:
                algo = "AES128";
                break;
            case EncryptedData.ETYPE_ARCFOUR_HMAC:
                algo = "ArcFourHMAC";
                break;
            case EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96:
                algo = "AES256";
                break;
            default:
                algo = "DES";
                break;
        }
        return new EncryptionKey(pass, "NOTHING", algo);
    }

    private char[] getPassword(PrincipalName p, boolean server) throws KrbException {
        String pn = p.toString();
        if (p.getRealmString() == null) {
            pn = pn + "@" + getRealm();
        }
        char[] pass = passwords.get(pn);
        if (pass == null) {
            throw new KrbException(server ? Krb5.KDC_ERR_S_PRINCIPAL_UNKNOWN : Krb5.KDC_ERR_C_PRINCIPAL_UNKNOWN);
        }
        return pass;
    }

    private String getSalt(PrincipalName p) {
        String pn = p.toString();
        if (p.getRealmString() == null) {
            pn = pn + "@" + getRealm();
        }
        if (passwords.containsKey(pn)) {
            try {
                p = new PrincipalName(passwords.ceilingEntry(pn).getKey());
            } catch (RealmException re) {
            }
        }
        String s = p.getRealmString();
        if (s == null)
            s = getRealm();
        for (String n : p.getNameStrings()) {
            s += n;
        }
        return s;
    }

    private EncryptionKey keyForUser(PrincipalName p, int etype, boolean server) throws KrbException {
        try {
            Integer kvno = null;
            if (p.toString().indexOf('/') > 0) {
                char[] pass = getPassword(p, server);
                if (Character.isDigit(pass[pass.length - 1])) {
                    kvno = pass[pass.length - 1] - '0';
                }
            }
            return new EncryptionKey(EncryptionKeyDotStringToKey(getPassword(p, server), getSalt(p), null, etype), etype, kvno);
        } catch (KrbException ke) {
            throw ke;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> policies = new HashMap<>();

    public void setPolicy(String rule, String value) {
        if (value == null) {
            policies.remove(rule);
        } else {
            policies.put(rule, value);
        }
    }

    private boolean configMatch(String c, String s, String rule) {
        String policy = policies.get(rule);
        boolean result = false;
        if (policy == null) {
            result = false;
        } else if (policy.length() == 0) {
            result = true;
        } else {
            String[] names = policy.split("\\s+");
            for (String name : names) {
                if (name.equals(s)) {
                    result = true;
                    break;
                }
            }
        }
        if (result) {
            System.out.printf(">>>> Policy match result (%s vs %s on %s) %b\n", c, s, rule, result);
        }
        return result;
    }

    private byte[] processMessage(byte[] in) throws Exception {
        if ((in[0] & 0x1f) == Krb5.KRB_AS_REQ)
            return processAsReq(in);
        else
            return processTgsReq(in);
    }

    private byte[] processTgsReq(byte[] in) throws Exception {
        TGSReq tgsReq = new TGSReq(in);
        try {
            System.out.println(realm + "> " + tgsReq.reqBody.cname + " sends TGS-REQ for " + tgsReq.reqBody.sname);
            KDCReqBody body = tgsReq.reqBody;
            int[] eTypes = KDCReqBodyDotEType(body);
            int e2 = eTypes[0];
            int e3 = eTypes[0];
            PAData[] pas = kDCReqDotPAData(tgsReq);
            Ticket tkt = null;
            EncTicketPart etp = null;
            if (pas == null || pas.length == 0) {
                throw new KrbException(Krb5.KDC_ERR_PADATA_TYPE_NOSUPP);
            } else {
                for (PAData pa : pas) {
                    if (pa.getType() == Krb5.PA_TGS_REQ) {
                        APReq apReq = new APReq(pa.getValue());
                        EncryptedData ed = apReq.authenticator;
                        tkt = apReq.ticket;
                        int te = tkt.encPart.getEType();
                        tkt.sname.setRealm(tkt.realm);
                        EncryptionKey kkey = keyForUser(tkt.sname, te, true);
                        byte[] bb = tkt.encPart.decrypt(kkey, KeyUsage.KU_TICKET);
                        DerInputStream derIn = new DerInputStream(bb);
                        DerValue der = derIn.getDerValue();
                        etp = new EncTicketPart(der.toByteArray());
                    }
                }
                if (tkt == null) {
                    throw new KrbException(Krb5.KDC_ERR_PADATA_TYPE_NOSUPP);
                }
            }
            EncryptionKey ckey = etp.key;
            EncryptionKey key = generateRandomKey(e2);
            KerberosTime till = body.till;
            if (till == null) {
                throw new KrbException(Krb5.KDC_ERR_NEVER_VALID);
            } else if (till.isZero()) {
                till = new KerberosTime(new Date().getTime() + 1000 * 3600 * 11);
            }
            boolean[] bFlags = new boolean[Krb5.TKT_OPTS_MAX + 1];
            if (body.kdcOptions.get(KDCOptions.FORWARDABLE)) {
                bFlags[Krb5.TKT_OPTS_FORWARDABLE] = true;
            }
            if (body.kdcOptions.get(KDCOptions.FORWARDED) || etp.flags.get(Krb5.TKT_OPTS_FORWARDED)) {
                bFlags[Krb5.TKT_OPTS_FORWARDED] = true;
            }
            if (body.kdcOptions.get(KDCOptions.RENEWABLE)) {
                bFlags[Krb5.TKT_OPTS_RENEWABLE] = true;
            }
            if (body.kdcOptions.get(KDCOptions.PROXIABLE)) {
                bFlags[Krb5.TKT_OPTS_PROXIABLE] = true;
            }
            if (body.kdcOptions.get(KDCOptions.POSTDATED)) {
                bFlags[Krb5.TKT_OPTS_POSTDATED] = true;
            }
            if (body.kdcOptions.get(KDCOptions.ALLOW_POSTDATE)) {
                bFlags[Krb5.TKT_OPTS_MAY_POSTDATE] = true;
            }
            if (configMatch("", body.sname.getNameString(), "ok-as-delegate")) {
                bFlags[Krb5.TKT_OPTS_DELEGATE] = true;
            }
            bFlags[Krb5.TKT_OPTS_INITIAL] = true;
            TicketFlags tFlags = new TicketFlags(bFlags);
            EncTicketPart enc = new EncTicketPart(tFlags, key, etp.crealm, etp.cname, new TransitedEncoding(1, new byte[0]), new KerberosTime(new Date()), body.from, till, body.rtime, body.addresses, null);
            EncryptionKey skey = keyForUser(body.sname, e3, true);
            if (skey == null) {
                throw new KrbException(Krb5.KDC_ERR_SUMTYPE_NOSUPP);
            }
            Ticket t = new Ticket(body.crealm, body.sname, new EncryptedData(skey, enc.asn1Encode(), KeyUsage.KU_TICKET));
            EncTGSRepPart enc_part = new EncTGSRepPart(key, new LastReq(new LastReqEntry[] { new LastReqEntry(0, new KerberosTime(new Date().getTime() - 10000)) }), body.getNonce(), new KerberosTime(new Date().getTime() + 1000 * 3600 * 24), tFlags, new KerberosTime(new Date()), body.from, till, body.rtime, body.crealm, body.sname, body.addresses);
            EncryptedData edata = new EncryptedData(ckey, enc_part.asn1Encode(), KeyUsage.KU_ENC_TGS_REP_PART_SESSKEY);
            TGSRep tgsRep = new TGSRep(null, etp.crealm, etp.cname, t, edata);
            System.out.println("     Return " + tgsRep.cname + " ticket for " + tgsRep.ticket.sname);
            DerOutputStream out = new DerOutputStream();
            out.write(DerValue.createTag(DerValue.TAG_APPLICATION, true, (byte) Krb5.KRB_TGS_REP), tgsRep.asn1Encode());
            return out.toByteArray();
        } catch (KrbException ke) {
            ke.printStackTrace(System.out);
            KRBError kerr = ke.getError();
            KDCReqBody body = tgsReq.reqBody;
            System.out.println("     Error " + ke.returnCode() + " " + ke.returnCodeMessage());
            if (kerr == null) {
                kerr = new KRBError(null, null, null, new KerberosTime(new Date()), 0, ke.returnCode(), body.crealm, body.cname, new Realm(getRealm()), body.sname, KrbException.errorMessage(ke.returnCode()), null);
            }
            return kerr.asn1Encode();
        }
    }

    private byte[] processAsReq(byte[] in) throws Exception {
        ASReq asReq = new ASReq(in);
        int[] eTypes = null;
        List<PAData> outPAs = new ArrayList<>();
        try {
            System.out.println(realm + "> " + asReq.reqBody.cname + " sends AS-REQ for " + asReq.reqBody.sname);
            KDCReqBody body = asReq.reqBody;
            body.cname.setRealm(getRealm());
            eTypes = KDCReqBodyDotEType(body);
            int eType = eTypes[0];
            EncryptionKey ckey = keyForUser(body.cname, eType, false);
            EncryptionKey skey = keyForUser(body.sname, eType, true);
            if (options.containsKey(KDC.Option.ONLY_RC4_TGT)) {
                int tgtEType = EncryptedData.ETYPE_ARCFOUR_HMAC;
                boolean found = false;
                for (int i = 0; i < eTypes.length; i++) {
                    if (eTypes[i] == tgtEType) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new KrbException(Krb5.KDC_ERR_ETYPE_NOSUPP);
                }
                skey = keyForUser(body.sname, tgtEType, true);
            }
            if (ckey == null) {
                throw new KrbException(Krb5.KDC_ERR_ETYPE_NOSUPP);
            }
            if (skey == null) {
                throw new KrbException(Krb5.KDC_ERR_SUMTYPE_NOSUPP);
            }
            EncryptionKey key = generateRandomKey(eType);
            KerberosTime till = body.till;
            if (till == null) {
                throw new KrbException(Krb5.KDC_ERR_NEVER_VALID);
            } else if (till.isZero()) {
                till = new KerberosTime(new Date().getTime() + 1000 * 3600 * 11);
            }
            boolean[] bFlags = new boolean[Krb5.TKT_OPTS_MAX + 1];
            if (body.kdcOptions.get(KDCOptions.FORWARDABLE)) {
                bFlags[Krb5.TKT_OPTS_FORWARDABLE] = true;
            }
            if (body.kdcOptions.get(KDCOptions.RENEWABLE)) {
                bFlags[Krb5.TKT_OPTS_RENEWABLE] = true;
            }
            if (body.kdcOptions.get(KDCOptions.PROXIABLE)) {
                bFlags[Krb5.TKT_OPTS_PROXIABLE] = true;
            }
            if (body.kdcOptions.get(KDCOptions.POSTDATED)) {
                bFlags[Krb5.TKT_OPTS_POSTDATED] = true;
            }
            if (body.kdcOptions.get(KDCOptions.ALLOW_POSTDATE)) {
                bFlags[Krb5.TKT_OPTS_MAY_POSTDATE] = true;
            }
            bFlags[Krb5.TKT_OPTS_INITIAL] = true;
            int[] epas = eTypes;
            if (options.containsKey(KDC.Option.RC4_FIRST_PREAUTH)) {
                for (int i = 1; i < epas.length; i++) {
                    if (epas[i] == EncryptedData.ETYPE_ARCFOUR_HMAC) {
                        epas[i] = epas[0];
                        epas[0] = EncryptedData.ETYPE_ARCFOUR_HMAC;
                        break;
                    }
                }
                ;
            } else if (options.containsKey(KDC.Option.ONLY_ONE_PREAUTH)) {
                epas = new int[] { eTypes[0] };
            }
            DerValue[] pas = new DerValue[epas.length];
            for (int i = 0; i < epas.length; i++) {
                pas[i] = new DerValue(new ETypeInfo2(epas[i], epas[i] == EncryptedData.ETYPE_ARCFOUR_HMAC ? null : getSalt(body.cname), null).asn1Encode());
            }
            DerOutputStream eid = new DerOutputStream();
            eid.putSequence(pas);
            outPAs.add(new PAData(Krb5.PA_ETYPE_INFO2, eid.toByteArray()));
            boolean allOld = true;
            for (int i : eTypes) {
                if (i == EncryptedData.ETYPE_AES128_CTS_HMAC_SHA1_96 || i == EncryptedData.ETYPE_AES256_CTS_HMAC_SHA1_96) {
                    allOld = false;
                    break;
                }
            }
            if (allOld) {
                for (int i = 0; i < epas.length; i++) {
                    pas[i] = new DerValue(new ETypeInfo(epas[i], epas[i] == EncryptedData.ETYPE_ARCFOUR_HMAC ? null : getSalt(body.cname)).asn1Encode());
                }
                eid = new DerOutputStream();
                eid.putSequence(pas);
                outPAs.add(new PAData(Krb5.PA_ETYPE_INFO, eid.toByteArray()));
            }
            PAData[] inPAs = kDCReqDotPAData(asReq);
            if (inPAs == null || inPAs.length == 0) {
                Object preauth = options.get(Option.PREAUTH_REQUIRED);
                if (preauth == null || preauth.equals(Boolean.TRUE)) {
                    throw new KrbException(Krb5.KDC_ERR_PREAUTH_REQUIRED);
                }
            } else {
                try {
                    EncryptedData data = newEncryptedData(new DerValue(inPAs[0].getValue()));
                    EncryptionKey pakey = keyForUser(body.cname, data.getEType(), false);
                    data.decrypt(pakey, KeyUsage.KU_PA_ENC_TS);
                } catch (Exception e) {
                    throw new KrbException(Krb5.KDC_ERR_PREAUTH_FAILED);
                }
                bFlags[Krb5.TKT_OPTS_PRE_AUTHENT] = true;
            }
            TicketFlags tFlags = new TicketFlags(bFlags);
            EncTicketPart enc = new EncTicketPart(tFlags, key, body.crealm, body.cname, new TransitedEncoding(1, new byte[0]), new KerberosTime(new Date()), body.from, till, body.rtime, body.addresses, null);
            Ticket t = new Ticket(body.crealm, body.sname, new EncryptedData(skey, enc.asn1Encode(), KeyUsage.KU_TICKET));
            EncASRepPart enc_part = new EncASRepPart(key, new LastReq(new LastReqEntry[] { new LastReqEntry(0, new KerberosTime(new Date().getTime() - 10000)) }), body.getNonce(), new KerberosTime(new Date().getTime() + 1000 * 3600 * 24), tFlags, new KerberosTime(new Date()), body.from, till, body.rtime, body.crealm, body.sname, body.addresses);
            EncryptedData edata = new EncryptedData(ckey, enc_part.asn1Encode(), KeyUsage.KU_ENC_AS_REP_PART);
            ASRep asRep = new ASRep(outPAs.toArray(new PAData[outPAs.size()]), body.crealm, body.cname, t, edata);
            System.out.println("     Return " + asRep.cname + " ticket for " + asRep.ticket.sname);
            DerOutputStream out = new DerOutputStream();
            out.write(DerValue.createTag(DerValue.TAG_APPLICATION, true, (byte) Krb5.KRB_AS_REP), asRep.asn1Encode());
            byte[] result = out.toByteArray();
            String ccache = System.getProperty("test.kdc.save.ccache");
            if (ccache != null) {
                asRep.encKDCRepPart = enc_part;
                sun.security.krb5.internal.ccache.Credentials credentials = new sun.security.krb5.internal.ccache.Credentials(asRep);
                asReq.reqBody.cname.setRealm(getRealm());
                CredentialsCache cache = CredentialsCache.create(asReq.reqBody.cname, ccache);
                if (cache == null) {
                    throw new IOException("Unable to create the cache file " + ccache);
                }
                cache.update(credentials);
                cache.save();
                new File(ccache).deleteOnExit();
            }
            return result;
        } catch (KrbException ke) {
            ke.printStackTrace(System.out);
            KRBError kerr = ke.getError();
            KDCReqBody body = asReq.reqBody;
            System.out.println("     Error " + ke.returnCode() + " " + ke.returnCodeMessage());
            byte[] eData = null;
            if (kerr == null) {
                if (ke.returnCode() == Krb5.KDC_ERR_PREAUTH_REQUIRED || ke.returnCode() == Krb5.KDC_ERR_PREAUTH_FAILED) {
                    DerOutputStream bytes = new DerOutputStream();
                    bytes.write(new PAData(Krb5.PA_ENC_TIMESTAMP, new byte[0]).asn1Encode());
                    for (PAData p : outPAs) {
                        bytes.write(p.asn1Encode());
                    }
                    DerOutputStream temp = new DerOutputStream();
                    temp.write(DerValue.tag_Sequence, bytes);
                    eData = temp.toByteArray();
                }
                kerr = new KRBError(null, null, null, new KerberosTime(new Date()), 0, ke.returnCode(), body.crealm, body.cname, new Realm(getRealm()), body.sname, KrbException.errorMessage(ke.returnCode()), eData);
            }
            return kerr.asn1Encode();
        }
    }

    private static String realmLineForKDC(KDC kdc) {
        return String.format("  %s = {\n    kdc = %s:%d\n  }\n", kdc.realm, kdc.kdc, kdc.port);
    }

    protected void startServer(int port, boolean asDaemon) throws IOException {
        if (port > 0) {
            u1 = new DatagramSocket(port, InetAddress.getByName("127.0.0.1"));
            t1 = new ServerSocket(port);
        } else {
            while (true) {
                try {
                    port = 8000 + new java.util.Random().nextInt(10000);
                    u1 = null;
                    u1 = new DatagramSocket(port, InetAddress.getByName("127.0.0.1"));
                    t1 = new ServerSocket(port);
                    break;
                } catch (Exception e) {
                    if (u1 != null)
                        u1.close();
                }
            }
        }
        final DatagramSocket udp = u1;
        final ServerSocket tcp = t1;
        System.out.println("Start KDC on " + port);
        this.port = port;
        thread1 = new Thread() {

            public void run() {
                while (true) {
                    try {
                        byte[] inbuf = new byte[8192];
                        DatagramPacket p = new DatagramPacket(inbuf, inbuf.length);
                        udp.receive(p);
                        System.out.println("-----------------------------------------------");
                        System.out.println(">>>>> UDP packet received");
                        q.put(new Job(processMessage(Arrays.copyOf(inbuf, p.getLength())), udp, p));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread1.setDaemon(asDaemon);
        thread1.start();
        thread2 = new Thread() {

            public void run() {
                while (true) {
                    try {
                        Socket socket = tcp.accept();
                        System.out.println("-----------------------------------------------");
                        System.out.println(">>>>> TCP connection established");
                        DataInputStream in = new DataInputStream(socket.getInputStream());
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                        byte[] token = new byte[in.readInt()];
                        in.readFully(token);
                        q.put(new Job(processMessage(token), socket, out));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread2.setDaemon(asDaemon);
        thread2.start();
        thread3 = new Thread() {

            public void run() {
                while (true) {
                    try {
                        q.take().send();
                    } catch (Exception e) {
                    }
                }
            }
        };
        thread3.setDaemon(true);
        thread3.start();
    }

    public void terminate() {
        try {
            thread1.stop();
            thread2.stop();
            thread3.stop();
            u1.close();
            t1.close();
        } catch (Exception e) {
        }
    }

    private static class Job {

        byte[] token;

        Socket s;

        DataOutputStream out;

        DatagramSocket s2;

        DatagramPacket dp;

        boolean useTCP;

        Job(byte[] token, Socket s, DataOutputStream out) {
            useTCP = true;
            this.token = token;
            this.s = s;
            this.out = out;
        }

        Job(byte[] token, DatagramSocket s2, DatagramPacket dp) {
            useTCP = false;
            this.token = token;
            this.s2 = s2;
            this.dp = dp;
        }

        void send() {
            try {
                if (useTCP) {
                    System.out.println(">>>>> TCP request honored");
                    out.writeInt(token.length);
                    out.write(token);
                    s.close();
                } else {
                    System.out.println(">>>>> UDP request honored");
                    s2.send(new DatagramPacket(token, token.length, dp.getAddress(), dp.getPort()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class KDCNameService implements NameServiceDescriptor {

        @Override
        public NameService createNameService() throws Exception {
            NameService ns = new NameService() {

                @Override
                public InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException {
                    return new InetAddress[] { InetAddress.getByAddress(host, new byte[] { 127, 0, 0, 1 }) };
                }

                @Override
                public String getHostByAddr(byte[] addr) throws UnknownHostException {
                    throw new UnknownHostException();
                }
            };
            return ns;
        }

        @Override
        public String getProviderName() {
            return "mock";
        }

        @Override
        public String getType() {
            return "ns";
        }
    }

    private static final Field getPADataField;

    private static final Field getEType;

    private static final Constructor<EncryptedData> ctorEncryptedData;

    private static final Method stringToKey;

    static {
        try {
            ctorEncryptedData = EncryptedData.class.getDeclaredConstructor(DerValue.class);
            ctorEncryptedData.setAccessible(true);
            getPADataField = KDCReq.class.getDeclaredField("pAData");
            getPADataField.setAccessible(true);
            getEType = KDCReqBody.class.getDeclaredField("eType");
            getEType.setAccessible(true);
            stringToKey = EncryptionKey.class.getDeclaredMethod("stringToKey", char[].class, String.class, byte[].class, Integer.TYPE);
            stringToKey.setAccessible(true);
        } catch (NoSuchFieldException nsfe) {
            throw new AssertionError(nsfe);
        } catch (NoSuchMethodException nsme) {
            throw new AssertionError(nsme);
        }
    }

    private EncryptedData newEncryptedData(DerValue der) {
        try {
            return ctorEncryptedData.newInstance(der);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static PAData[] kDCReqDotPAData(KDCReq req) {
        try {
            return (PAData[]) getPADataField.get(req);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static int[] KDCReqBodyDotEType(KDCReqBody body) {
        try {
            return (int[]) getEType.get(body);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static byte[] EncryptionKeyDotStringToKey(char[] password, String salt, byte[] s2kparams, int keyType) throws KrbCryptoException {
        try {
            return (byte[]) stringToKey.invoke(null, password, salt, s2kparams, keyType);
        } catch (InvocationTargetException ex) {
            throw (KrbCryptoException) ex.getCause();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
