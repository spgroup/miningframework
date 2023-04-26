import java.io.*;
import java.nio.BufferUnderflowException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jdk.test.lib.Platform;
import sun.security.jgss.GSSUtil;
import sun.security.krb5.internal.rcache.AuthTime;

public class ReplayCacheTestProc {

    private static Proc[] pa;

    private static Proc pi;

    private static List<Req> reqs = new ArrayList<>();

    private static String HOST = "localhost";

    private static final String SERVICE;

    static {
        String tmp = System.getProperty("test.service");
        SERVICE = (tmp == null) ? "service" : tmp;
    }

    private static String cwd = System.getProperty("os.name").startsWith("SunOS") ? "/var/krb5/rcache/" : System.getProperty("user.dir");

    private static MessageDigest md5, sha256;

    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException nsae) {
            throw new AssertionError("Impossible", nsae);
        }
    }

    private static long uid;

    public static void main0(String[] args) throws Exception {
        System.setProperty("java.security.krb5.conf", OneKDC.KRB5_CONF);
        if (args.length == 0) {
            int nc = 5;
            int ns = 5;
            String[] libs;
            Ex[] result;
            int numPerType = 2;
            uid = jdk.internal.misc.VM.geteuid();
            KDC kdc = KDC.create(OneKDC.REALM, HOST, 0, true);
            for (int i = 0; i < nc; i++) {
                kdc.addPrincipal(client(i), OneKDC.PASS);
            }
            kdc.addPrincipalRandKey("krbtgt/" + OneKDC.REALM);
            for (int i = 0; i < ns; i++) {
                kdc.addPrincipalRandKey(service(i));
            }
            kdc.writeKtab(OneKDC.KTAB);
            KDC.saveConfig(OneKDC.KRB5_CONF, kdc);
            String userLibs = System.getProperty("test.libs");
            if (userLibs != null) {
                libs = userLibs.split(",");
            } else {
                if (Platform.isOSX() || Platform.isWindows()) {
                    libs = new String[] { "J" };
                } else {
                    if (acceptor("N", "sanity").waitFor() != 0) {
                        Proc.d("Native mode sanity check failed, only java");
                        libs = new String[] { "J" };
                    } else {
                        libs = new String[] { "J", "N" };
                    }
                }
            }
            pi = Proc.create("ReplayCacheTestProc").debug("C").args("initiator").start();
            int na = libs.length * numPerType;
            pa = new Proc[na];
            for (int i = 0; i < na; i++) {
                pa[i] = acceptor(libs[i / numPerType], "" + (char) ('a' + i % numPerType));
            }
            String userRuns = System.getProperty("test.runs");
            if (userRuns == null) {
                result = new Ex[Integer.parseInt(System.getProperty("test.autoruns", "100"))];
                Random r = new Random();
                for (int i = 0; i < result.length; i++) {
                    boolean expected = reqs.isEmpty() || r.nextBoolean();
                    result[i] = new Ex(i, expected ? req(r.nextInt(nc), r.nextInt(ns)) : r.nextInt(reqs.size()), pa[r.nextInt(na)], expected);
                }
            } else if (userRuns.isEmpty()) {
                int count = 0;
                result = new Ex[libs.length * libs.length];
                for (int i = 0; i < libs.length; i++) {
                    result[count] = new Ex(count, req(0, 0), pa[i * numPerType], true);
                    count++;
                    for (int j = 0; j < libs.length; j++) {
                        if (i == j) {
                            continue;
                        }
                        result[count] = new Ex(count, i, pa[j * numPerType], false);
                        count++;
                    }
                }
            } else {
                String[] runs = userRuns.split(",");
                result = new Ex[runs.length];
                for (int i = 0; i < runs.length; i++) {
                    UserRun run = new UserRun(runs[i]);
                    result[i] = new Ex(i, run.req() == -1 ? req(run.client(), run.service()) : result[run.req()].req, Arrays.stream(pa).filter(p -> p.debug().equals(run.acceptor())).findFirst().orElseThrow(() -> new Exception("no acceptor named " + run.acceptor())), run.success());
                }
            }
            for (Ex x : result) {
                x.run();
            }
            pi.println("END");
            for (int i = 0; i < na; i++) {
                pa[i].println("END");
            }
            System.out.println("\nAll Test Results\n================");
            boolean finalOut = true;
            System.out.println("        req**  client    service  acceptor   Result");
            System.out.println("----  -------  ------  ---------  --------  -------");
            for (int i = 0; i < result.length; i++) {
                boolean out = result[i].expected == result[i].actual;
                finalOut &= out;
                System.out.printf("%3d:    %3d%s      c%d    s%d %4s  %8s   %s  %s\n", i, result[i].req, result[i].expected ? "**" : "  ", reqs.get(result[i].req).client, reqs.get(result[i].req).service, "(" + result[i].csize + ")", result[i].acceptor.debug(), result[i].actual ? "++" : "--", out ? "   " : "xxx");
            }
            System.out.println("\nPath of Reqs\n============");
            for (int j = 0; ; j++) {
                boolean found = false;
                for (int i = 0; i < result.length; i++) {
                    if (result[i].req == j) {
                        if (!found) {
                            System.out.printf("%3d (c%s -> s%s): ", j, reqs.get(j).client, reqs.get(j).service);
                        }
                        System.out.printf("%s%s(%d)%s", found ? " -> " : "", result[i].acceptor.debug(), i, result[i].actual != result[i].expected ? "xxx" : "");
                        found = true;
                    }
                }
                System.out.println();
                if (!found) {
                    break;
                }
            }
            if (!finalOut)
                throw new Exception();
        } else if (args[0].equals("Nsanity")) {
            Proc.d("Detect start");
            Context s = Context.fromUserKtab("*", OneKDC.KTAB, true);
            s.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
        } else if (args[0].equals("initiator")) {
            while (true) {
                String title = Proc.textIn();
                Proc.d("Client see " + title);
                if (title.equals("END"))
                    break;
                String[] cas = title.split(" ");
                Context c = Context.fromUserPass(cas[0], OneKDC.PASS, false);
                c.startAsClient(cas[1], GSSUtil.GSS_KRB5_MECH_OID);
                c.x().requestCredDeleg(true);
                byte[] token = c.take(new byte[0]);
                Proc.d("Client AP-REQ generated");
                Proc.binOut(token);
            }
        } else {
            Proc.d(System.getProperty("java.vm.version"));
            Proc.d(System.getProperty("sun.security.jgss.native"));
            Proc.d(System.getProperty("sun.security.jgss.lib"));
            Proc.d("---------------------------------\n");
            Proc.d("Server start");
            Context s = Context.fromUserKtab("*", OneKDC.KTAB, true);
            Proc.d("Server login");
            while (true) {
                String title = Proc.textIn();
                Proc.d("Server sees " + title);
                if (title.equals("END"))
                    break;
                s.startAsServer(GSSUtil.GSS_KRB5_MECH_OID);
                byte[] token = Proc.binIn();
                try {
                    s.take(token);
                    Proc.textOut("true");
                    Proc.d("Good");
                } catch (Exception e) {
                    Proc.textOut("false");
                    Proc.d("Bad");
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            main0(args);
        } catch (Exception e) {
            Proc.d(e);
            throw e;
        }
    }

    private static String client(int p) {
        return "client" + p;
    }

    private static String service(int p) {
        return SERVICE + p + "/" + HOST;
    }

    private static String dfl(int p) {
        return SERVICE + p + (uid == -1 ? "" : ("_" + uid));
    }

    private static int req(int client, int service) throws Exception {
        pi.println(client(client) + " " + service(service));
        Req req = new Req(client, service, pi.readData());
        reqs.add(req);
        return reqs.size() - 1;
    }

    private static Proc acceptor(String type, String suffix) throws Exception {
        Proc p;
        String label;
        String lib;
        int pos = type.indexOf('=');
        if (pos < 0) {
            label = type;
            lib = null;
        } else {
            label = type.substring(0, pos);
            lib = type.substring(pos + 1);
        }
        if (type.startsWith("J")) {
            if (lib == null) {
                p = Proc.create("ReplayCacheTestProc");
            } else {
                p = Proc.create("ReplayCacheTestProc", lib);
            }
            p.prop("sun.security.krb5.rcache", "dfl").prop("java.io.tmpdir", cwd);
            String useMD5 = System.getProperty("jdk.krb5.rcache.useMD5");
            if (useMD5 != null) {
                p.prop("jdk.krb5.rcache.useMD5", useMD5);
            }
        } else {
            p = Proc.create("ReplayCacheTestProc").env("KRB5_CONFIG", OneKDC.KRB5_CONF).env("KRB5_KTNAME", OneKDC.KTAB).env("KRB5RCACHEDIR", cwd).prop("sun.security.jgss.native", "true").prop("javax.security.auth.useSubjectCredsOnly", "false").prop("sun.security.nativegss.debug", "true");
            if (lib != null) {
                String libDir = lib.substring(0, lib.lastIndexOf('/'));
                p.prop("sun.security.jgss.lib", lib).env("DYLD_LIBRARY_PATH", libDir).env("LD_LIBRARY_PATH", libDir);
            }
        }
        Proc.d(label + suffix + " started");
        return p.args(label + suffix).debug(label + suffix).start();
    }

    private static void record(String label, Req req) throws Exception {
        byte[] data = Base64.getDecoder().decode(req.msg);
        data = Arrays.copyOfRange(data, 17, data.length);
        try (PrintStream ps = new PrintStream(new FileOutputStream("log.txt", true))) {
            ps.printf("%s:\nmsg: %s\nMD5: %s\nSHA-256: %s\n\n", label, req.msg, hex(md5.digest(data)), hex(sha256.digest(data)));
        }
    }

    private static String hex(byte[] hash) {
        char[] h = new char[hash.length * 2];
        char[] hexConst = "0123456789ABCDEF".toCharArray();
        for (int i = 0; i < hash.length; i++) {
            h[2 * i] = hexConst[(hash[i] & 0xff) >> 4];
            h[2 * i + 1] = hexConst[hash[i] & 0xf];
        }
        return new String(h);
    }

    private static int csize(int p) throws Exception {
        try (SeekableByteChannel chan = Files.newByteChannel(Paths.get(cwd, dfl(p)), StandardOpenOption.READ)) {
            chan.position(6);
            int cc = 0;
            while (true) {
                try {
                    if (AuthTime.readFrom(chan) != null)
                        cc++;
                } catch (BufferUnderflowException e) {
                    break;
                }
            }
            return cc;
        } catch (IOException ioe) {
            return 0;
        }
    }

    private static class Ex {

        int i;

        int req;

        Proc acceptor;

        boolean expected;

        boolean actual;

        int csize;

        String hash;

        Ex(int i, int req, Proc acceptor, boolean expected) {
            this.i = i;
            this.req = req;
            this.acceptor = acceptor;
            this.expected = expected;
        }

        void run() throws Exception {
            Req r = reqs.get(req);
            acceptor.println("TEST");
            acceptor.println(r.msg);
            String reply = acceptor.readData();
            actual = Boolean.valueOf(reply);
            csize = csize(r.service);
            String label = String.format("%03d-client%d-%s%d-%s-%s", i, r.client, SERVICE, r.service, acceptor.debug(), actual);
            record(label, r);
            if (new File(cwd, dfl(r.service)).exists()) {
                Files.copy(Paths.get(cwd, dfl(r.service)), Paths.get(label), StandardCopyOption.COPY_ATTRIBUTES);
            }
        }
    }

    private static class Req {

        String msg;

        int client;

        int service;

        Req(int client, int service, String msg) {
            this.msg = msg;
            this.client = client;
            this.service = service;
        }
    }

    private static class UserRun {

        static final Pattern p = Pattern.compile("(c(\\d)+s(\\d+)|r(\\d+))(.*)(.)");

        final Matcher m;

        UserRun(String run) {
            m = p.matcher(run);
            m.find();
        }

        int req() {
            return group(4);
        }

        int client() {
            return group(2);
        }

        int service() {
            return group(3);
        }

        String acceptor() {
            return m.group(5);
        }

        boolean success() {
            return m.group(6).equals("v");
        }

        int group(int i) {
            String g = m.group(i);
            return g == null ? -1 : Integer.parseInt(g);
        }
    }
}