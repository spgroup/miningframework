package sun.net.ftp.impl;

import java.net.*;
import java.io.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import sun.net.ftp.*;
import sun.util.logging.PlatformLogger;

public class FtpClient extends sun.net.ftp.FtpClient {

    private static int defaultSoTimeout;

    private static int defaultConnectTimeout;

    private static final PlatformLogger logger = PlatformLogger.getLogger("sun.net.ftp.FtpClient");

    private Proxy proxy;

    private Socket server;

    private PrintStream out;

    private InputStream in;

    private int readTimeout = -1;

    private int connectTimeout = -1;

    private static String encoding = "ISO8859_1";

    private InetSocketAddress serverAddr;

    private boolean replyPending = false;

    private boolean loggedIn = false;

    private boolean useCrypto = false;

    private SSLSocketFactory sslFact;

    private Socket oldSocket;

    private Vector<String> serverResponse = new Vector<String>(1);

    private FtpReplyCode lastReplyCode = null;

    private String welcomeMsg;

    private final boolean passiveMode = true;

    private TransferType type = TransferType.BINARY;

    private long restartOffset = 0;

    private long lastTransSize = -1;

    private String lastFileName;

    private static String[] patStrings = { "([\\-ld](?:[r\\-][w\\-][x\\-]){3})\\s*\\d+ (\\w+)\\s*(\\w+)\\s*(\\d+)\\s*([A-Z][a-z][a-z]\\s*\\d+)\\s*(\\d\\d:\\d\\d)\\s*(\\p{Print}*)", "([\\-ld](?:[r\\-][w\\-][x\\-]){3})\\s*\\d+ (\\w+)\\s*(\\w+)\\s*(\\d+)\\s*([A-Z][a-z][a-z]\\s*\\d+)\\s*(\\d{4})\\s*(\\p{Print}*)", "(\\d{2}/\\d{2}/\\d{4})\\s*(\\d{2}:\\d{2}[ap])\\s*((?:[0-9,]+)|(?:<DIR>))\\s*(\\p{Graph}*)", "(\\d{2}-\\d{2}-\\d{2})\\s*(\\d{2}:\\d{2}[AP]M)\\s*((?:[0-9,]+)|(?:<DIR>))\\s*(\\p{Graph}*)" };

    private static int[][] patternGroups = { { 7, 4, 5, 6, 0, 1, 2, 3 }, { 7, 4, 5, 0, 6, 1, 2, 3 }, { 4, 3, 1, 2, 0, 0, 0, 0 }, { 4, 3, 1, 2, 0, 0, 0, 0 } };

    private static Pattern[] patterns;

    private static Pattern linkp = Pattern.compile("(\\p{Print}+) \\-\\> (\\p{Print}+)$");

    private DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, java.util.Locale.US);

    static {
        final int[] vals = { 0, 0 };
        final String[] encs = { null };
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            public Object run() {
                vals[0] = Integer.getInteger("sun.net.client.defaultReadTimeout", 0).intValue();
                vals[1] = Integer.getInteger("sun.net.client.defaultConnectTimeout", 0).intValue();
                encs[0] = System.getProperty("file.encoding", "ISO8859_1");
                return null;
            }
        });
        if (vals[0] == 0) {
            defaultSoTimeout = -1;
        } else {
            defaultSoTimeout = vals[0];
        }
        if (vals[1] == 0) {
            defaultConnectTimeout = -1;
        } else {
            defaultConnectTimeout = vals[1];
        }
        encoding = encs[0];
        try {
            if (!isASCIISuperset(encoding)) {
                encoding = "ISO8859_1";
            }
        } catch (Exception e) {
            encoding = "ISO8859_1";
        }
        patterns = new Pattern[patStrings.length];
        for (int i = 0; i < patStrings.length; i++) {
            patterns[i] = Pattern.compile(patStrings[i]);
        }
    }

    private static boolean isASCIISuperset(String encoding) throws Exception {
        String chkS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz-_.!~*'();/?:@&=+$,";
        byte[] chkB = { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 45, 95, 46, 33, 126, 42, 39, 40, 41, 59, 47, 63, 58, 64, 38, 61, 43, 36, 44 };
        byte[] b = chkS.getBytes(encoding);
        return java.util.Arrays.equals(b, chkB);
    }

    private class DefaultParser implements FtpDirParser {

        private DefaultParser() {
        }

        public FtpDirEntry parseLine(String line) {
            String fdate = null;
            String fsize = null;
            String time = null;
            String filename = null;
            String permstring = null;
            String username = null;
            String groupname = null;
            boolean dir = false;
            Calendar now = Calendar.getInstance();
            int year = now.get(Calendar.YEAR);
            Matcher m = null;
            for (int j = 0; j < patterns.length; j++) {
                m = patterns[j].matcher(line);
                if (m.find()) {
                    filename = m.group(patternGroups[j][0]);
                    fsize = m.group(patternGroups[j][1]);
                    fdate = m.group(patternGroups[j][2]);
                    if (patternGroups[j][4] > 0) {
                        fdate += (", " + m.group(patternGroups[j][4]));
                    } else if (patternGroups[j][3] > 0) {
                        fdate += (", " + String.valueOf(year));
                    }
                    if (patternGroups[j][3] > 0) {
                        time = m.group(patternGroups[j][3]);
                    }
                    if (patternGroups[j][5] > 0) {
                        permstring = m.group(patternGroups[j][5]);
                        dir = permstring.startsWith("d");
                    }
                    if (patternGroups[j][6] > 0) {
                        username = m.group(patternGroups[j][6]);
                    }
                    if (patternGroups[j][7] > 0) {
                        groupname = m.group(patternGroups[j][7]);
                    }
                    if ("<DIR>".equals(fsize)) {
                        dir = true;
                        fsize = null;
                    }
                }
            }
            if (filename != null) {
                Date d;
                try {
                    d = df.parse(fdate);
                } catch (Exception e) {
                    d = null;
                }
                if (d != null && time != null) {
                    int c = time.indexOf(":");
                    now.setTime(d);
                    now.set(Calendar.HOUR, Integer.parseInt(time.substring(0, c)));
                    now.set(Calendar.MINUTE, Integer.parseInt(time.substring(c + 1)));
                    d = now.getTime();
                }
                Matcher m2 = linkp.matcher(filename);
                if (m2.find()) {
                    filename = m2.group(1);
                }
                boolean[][] perms = new boolean[3][3];
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        perms[i][j] = (permstring.charAt((i * 3) + j) != '-');
                    }
                }
                FtpDirEntry file = new FtpDirEntry(filename);
                file.setUser(username).setGroup(groupname);
                file.setSize(Long.parseLong(fsize)).setLastModified(d);
                file.setPermissions(perms);
                file.setType(dir ? FtpDirEntry.Type.DIR : (line.charAt(0) == 'l' ? FtpDirEntry.Type.LINK : FtpDirEntry.Type.FILE));
                return file;
            }
            return null;
        }
    }

    private class MLSxParser implements FtpDirParser {

        private SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");

        public FtpDirEntry parseLine(String line) {
            String name = null;
            int i = line.lastIndexOf(";");
            if (i > 0) {
                name = line.substring(i + 1).trim();
                line = line.substring(0, i);
            } else {
                name = line.trim();
                line = "";
            }
            FtpDirEntry file = new FtpDirEntry(name);
            while (!line.isEmpty()) {
                String s;
                i = line.indexOf(";");
                if (i > 0) {
                    s = line.substring(0, i);
                    line = line.substring(i + 1);
                } else {
                    s = line;
                    line = "";
                }
                i = s.indexOf("=");
                if (i > 0) {
                    String fact = s.substring(0, i);
                    String value = s.substring(i + 1);
                    file.addFact(fact, value);
                }
            }
            String s = file.getFact("Size");
            if (s != null) {
                file.setSize(Long.parseLong(s));
            }
            s = file.getFact("Modify");
            if (s != null) {
                Date d = null;
                try {
                    d = df.parse(s);
                } catch (ParseException ex) {
                }
                if (d != null) {
                    file.setLastModified(d);
                }
            }
            s = file.getFact("Create");
            if (s != null) {
                Date d = null;
                try {
                    d = df.parse(s);
                } catch (ParseException ex) {
                }
                if (d != null) {
                    file.setCreated(d);
                }
            }
            s = file.getFact("Type");
            if (s != null) {
                if (s.equalsIgnoreCase("file")) {
                    file.setType(FtpDirEntry.Type.FILE);
                }
                if (s.equalsIgnoreCase("dir")) {
                    file.setType(FtpDirEntry.Type.DIR);
                }
                if (s.equalsIgnoreCase("cdir")) {
                    file.setType(FtpDirEntry.Type.CDIR);
                }
                if (s.equalsIgnoreCase("pdir")) {
                    file.setType(FtpDirEntry.Type.PDIR);
                }
            }
            return file;
        }
    }

    private FtpDirParser parser = new DefaultParser();

    private FtpDirParser mlsxParser = new MLSxParser();

    private static Pattern transPat = null;

    private void getTransferSize() {
        lastTransSize = -1;
        String response = getLastResponseString();
        if (transPat == null) {
            transPat = Pattern.compile("150 Opening .*\\((\\d+) bytes\\).");
        }
        Matcher m = transPat.matcher(response);
        if (m.find()) {
            String s = m.group(1);
            lastTransSize = Long.parseLong(s);
        }
    }

    private void getTransferName() {
        lastFileName = null;
        String response = getLastResponseString();
        int i = response.indexOf("unique file name:");
        int e = response.lastIndexOf(')');
        if (i >= 0) {
            i += 17;
            lastFileName = response.substring(i, e);
        }
    }

    private int readServerResponse() throws IOException {
        StringBuffer replyBuf = new StringBuffer(32);
        int c;
        int continuingCode = -1;
        int code;
        String response;
        serverResponse.setSize(0);
        while (true) {
            while ((c = in.read()) != -1) {
                if (c == '\r') {
                    if ((c = in.read()) != '\n') {
                        replyBuf.append('\r');
                    }
                }
                replyBuf.append((char) c);
                if (c == '\n') {
                    break;
                }
            }
            response = replyBuf.toString();
            replyBuf.setLength(0);
            if (logger.isLoggable(PlatformLogger.Level.FINEST)) {
                logger.finest("Server [" + serverAddr + "] --> " + response);
            }
            if (response.length() == 0) {
                code = -1;
            } else {
                try {
                    code = Integer.parseInt(response.substring(0, 3));
                } catch (NumberFormatException e) {
                    code = -1;
                } catch (StringIndexOutOfBoundsException e) {
                    continue;
                }
            }
            serverResponse.addElement(response);
            if (continuingCode != -1) {
                if (code != continuingCode || (response.length() >= 4 && response.charAt(3) == '-')) {
                    continue;
                } else {
                    continuingCode = -1;
                    break;
                }
            } else if (response.length() >= 4 && response.charAt(3) == '-') {
                continuingCode = code;
                continue;
            } else {
                break;
            }
        }
        return code;
    }

    private void sendServer(String cmd) {
        out.print(cmd);
        if (logger.isLoggable(PlatformLogger.Level.FINEST)) {
            logger.finest("Server [" + serverAddr + "] <-- " + cmd);
        }
    }

    private String getResponseString() {
        return serverResponse.elementAt(0);
    }

    private Vector<String> getResponseStrings() {
        return serverResponse;
    }

    private boolean readReply() throws IOException {
        lastReplyCode = FtpReplyCode.find(readServerResponse());
        if (lastReplyCode.isPositivePreliminary()) {
            replyPending = true;
            return true;
        }
        if (lastReplyCode.isPositiveCompletion() || lastReplyCode.isPositiveIntermediate()) {
            if (lastReplyCode == FtpReplyCode.CLOSING_DATA_CONNECTION) {
                getTransferName();
            }
            return true;
        }
        return false;
    }

    private boolean issueCommand(String cmd) throws IOException {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected");
        }
        if (replyPending) {
            try {
                completePending();
            } catch (sun.net.ftp.FtpProtocolException e) {
            }
        }
        sendServer(cmd + "\r\n");
        return readReply();
    }

    private void issueCommandCheck(String cmd) throws sun.net.ftp.FtpProtocolException, IOException {
        if (!issueCommand(cmd)) {
            throw new sun.net.ftp.FtpProtocolException(cmd + ":" + getResponseString(), getLastReplyCode());
        }
    }

    private static Pattern epsvPat = null;

    private static Pattern pasvPat = null;

    private Socket openPassiveDataConnection(String cmd) throws sun.net.ftp.FtpProtocolException, IOException {
        String serverAnswer;
        int port;
        InetSocketAddress dest = null;
        if (issueCommand("EPSV ALL")) {
            issueCommandCheck("EPSV");
            serverAnswer = getResponseString();
            if (epsvPat == null) {
                epsvPat = Pattern.compile("^229 .* \\(\\|\\|\\|(\\d+)\\|\\)");
            }
            Matcher m = epsvPat.matcher(serverAnswer);
            if (!m.find()) {
                throw new sun.net.ftp.FtpProtocolException("EPSV failed : " + serverAnswer);
            }
            String s = m.group(1);
            port = Integer.parseInt(s);
            InetAddress add = server.getInetAddress();
            if (add != null) {
                dest = new InetSocketAddress(add, port);
            } else {
                dest = InetSocketAddress.createUnresolved(serverAddr.getHostName(), port);
            }
        } else {
            issueCommandCheck("PASV");
            serverAnswer = getResponseString();
            if (pasvPat == null) {
                pasvPat = Pattern.compile("227 .* \\(?(\\d{1,3},\\d{1,3},\\d{1,3},\\d{1,3}),(\\d{1,3}),(\\d{1,3})\\)?");
            }
            Matcher m = pasvPat.matcher(serverAnswer);
            if (!m.find()) {
                throw new sun.net.ftp.FtpProtocolException("PASV failed : " + serverAnswer);
            }
            port = Integer.parseInt(m.group(3)) + (Integer.parseInt(m.group(2)) << 8);
            String s = m.group(1).replace(',', '.');
            dest = new InetSocketAddress(s, port);
        }
        Socket s;
        if (proxy != null) {
            if (proxy.type() == Proxy.Type.SOCKS) {
                s = AccessController.doPrivileged(new PrivilegedAction<Socket>() {

                    public Socket run() {
                        return new Socket(proxy);
                    }
                });
            } else {
                s = new Socket(Proxy.NO_PROXY);
            }
        } else {
            s = new Socket();
        }
        InetAddress serverAddress = AccessController.doPrivileged(new PrivilegedAction<InetAddress>() {

            @Override
            public InetAddress run() {
                return server.getLocalAddress();
            }
        });
        s.bind(new InetSocketAddress(serverAddress, 0));
        if (connectTimeout >= 0) {
            s.connect(dest, connectTimeout);
        } else {
            if (defaultConnectTimeout > 0) {
                s.connect(dest, defaultConnectTimeout);
            } else {
                s.connect(dest);
            }
        }
        if (readTimeout >= 0) {
            s.setSoTimeout(readTimeout);
        } else if (defaultSoTimeout > 0) {
            s.setSoTimeout(defaultSoTimeout);
        }
        if (useCrypto) {
            try {
                s = sslFact.createSocket(s, dest.getHostName(), dest.getPort(), true);
            } catch (Exception e) {
                throw new sun.net.ftp.FtpProtocolException("Can't open secure data channel: " + e);
            }
        }
        if (!issueCommand(cmd)) {
            s.close();
            if (getLastReplyCode() == FtpReplyCode.FILE_UNAVAILABLE) {
                throw new FileNotFoundException(cmd);
            }
            throw new sun.net.ftp.FtpProtocolException(cmd + ":" + getResponseString(), getLastReplyCode());
        }
        return s;
    }

    private Socket openDataConnection(String cmd) throws sun.net.ftp.FtpProtocolException, IOException {
        Socket clientSocket;
        if (passiveMode) {
            try {
                return openPassiveDataConnection(cmd);
            } catch (sun.net.ftp.FtpProtocolException e) {
                String errmsg = e.getMessage();
                if (!errmsg.startsWith("PASV") && !errmsg.startsWith("EPSV")) {
                    throw e;
                }
            }
        }
        ServerSocket portSocket;
        InetAddress myAddress;
        String portCmd;
        if (proxy != null && proxy.type() == Proxy.Type.SOCKS) {
            throw new sun.net.ftp.FtpProtocolException("Passive mode failed");
        }
        portSocket = new ServerSocket(0, 1, server.getLocalAddress());
        try {
            myAddress = portSocket.getInetAddress();
            if (myAddress.isAnyLocalAddress()) {
                myAddress = server.getLocalAddress();
            }
            portCmd = "EPRT |" + ((myAddress instanceof Inet6Address) ? "2" : "1") + "|" + myAddress.getHostAddress() + "|" + portSocket.getLocalPort() + "|";
            if (!issueCommand(portCmd) || !issueCommand(cmd)) {
                portCmd = "PORT ";
                byte[] addr = myAddress.getAddress();
                for (int i = 0; i < addr.length; i++) {
                    portCmd = portCmd + (addr[i] & 0xFF) + ",";
                }
                portCmd = portCmd + ((portSocket.getLocalPort() >>> 8) & 0xff) + "," + (portSocket.getLocalPort() & 0xff);
                issueCommandCheck(portCmd);
                issueCommandCheck(cmd);
            }
            if (connectTimeout >= 0) {
                portSocket.setSoTimeout(connectTimeout);
            } else {
                if (defaultConnectTimeout > 0) {
                    portSocket.setSoTimeout(defaultConnectTimeout);
                }
            }
            clientSocket = portSocket.accept();
            if (readTimeout >= 0) {
                clientSocket.setSoTimeout(readTimeout);
            } else {
                if (defaultSoTimeout > 0) {
                    clientSocket.setSoTimeout(defaultSoTimeout);
                }
            }
        } finally {
            portSocket.close();
        }
        if (useCrypto) {
            try {
                clientSocket = sslFact.createSocket(clientSocket, serverAddr.getHostName(), serverAddr.getPort(), true);
            } catch (Exception ex) {
                throw new IOException(ex.getLocalizedMessage());
            }
        }
        return clientSocket;
    }

    private InputStream createInputStream(InputStream in) {
        if (type == TransferType.ASCII) {
            return new sun.net.TelnetInputStream(in, false);
        }
        return in;
    }

    private OutputStream createOutputStream(OutputStream out) {
        if (type == TransferType.ASCII) {
            return new sun.net.TelnetOutputStream(out, false);
        }
        return out;
    }

    protected FtpClient() {
    }

    public static sun.net.ftp.FtpClient create() {
        return new FtpClient();
    }

    public sun.net.ftp.FtpClient enablePassiveMode(boolean passive) {
        return this;
    }

    public boolean isPassiveModeEnabled() {
        return passiveMode;
    }

    public sun.net.ftp.FtpClient setConnectTimeout(int timeout) {
        connectTimeout = timeout;
        return this;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public sun.net.ftp.FtpClient setReadTimeout(int timeout) {
        readTimeout = timeout;
        return this;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public sun.net.ftp.FtpClient setProxy(Proxy p) {
        proxy = p;
        return this;
    }

    public Proxy getProxy() {
        return proxy;
    }

    private void tryConnect(InetSocketAddress dest, int timeout) throws IOException {
        if (isConnected()) {
            disconnect();
        }
        server = doConnect(dest, timeout);
        try {
            out = new PrintStream(new BufferedOutputStream(server.getOutputStream()), true, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(encoding + "encoding not found", e);
        }
        in = new BufferedInputStream(server.getInputStream());
    }

    private Socket doConnect(InetSocketAddress dest, int timeout) throws IOException {
        Socket s;
        if (proxy != null) {
            if (proxy.type() == Proxy.Type.SOCKS) {
                s = AccessController.doPrivileged(new PrivilegedAction<Socket>() {

                    public Socket run() {
                        return new Socket(proxy);
                    }
                });
            } else {
                s = new Socket(Proxy.NO_PROXY);
            }
        } else {
            s = new Socket();
        }
        if (timeout >= 0) {
            s.connect(dest, timeout);
        } else {
            if (connectTimeout >= 0) {
                s.connect(dest, connectTimeout);
            } else {
                if (defaultConnectTimeout > 0) {
                    s.connect(dest, defaultConnectTimeout);
                } else {
                    s.connect(dest);
                }
            }
        }
        if (readTimeout >= 0) {
            s.setSoTimeout(readTimeout);
        } else if (defaultSoTimeout > 0) {
            s.setSoTimeout(defaultSoTimeout);
        }
        return s;
    }

    private void disconnect() throws IOException {
        if (isConnected()) {
            server.close();
        }
        server = null;
        in = null;
        out = null;
        lastTransSize = -1;
        lastFileName = null;
        restartOffset = 0;
        welcomeMsg = null;
        lastReplyCode = null;
        serverResponse.setSize(0);
    }

    public boolean isConnected() {
        return server != null;
    }

    public SocketAddress getServerAddress() {
        return server == null ? null : server.getRemoteSocketAddress();
    }

    public sun.net.ftp.FtpClient connect(SocketAddress dest) throws sun.net.ftp.FtpProtocolException, IOException {
        return connect(dest, -1);
    }

    public sun.net.ftp.FtpClient connect(SocketAddress dest, int timeout) throws sun.net.ftp.FtpProtocolException, IOException {
        if (!(dest instanceof InetSocketAddress)) {
            throw new IllegalArgumentException("Wrong address type");
        }
        serverAddr = (InetSocketAddress) dest;
        tryConnect(serverAddr, timeout);
        if (!readReply()) {
            throw new sun.net.ftp.FtpProtocolException("Welcome message: " + getResponseString(), lastReplyCode);
        }
        welcomeMsg = getResponseString().substring(4);
        return this;
    }

    private void tryLogin(String user, char[] password) throws sun.net.ftp.FtpProtocolException, IOException {
        issueCommandCheck("USER " + user);
        if (lastReplyCode == FtpReplyCode.NEED_PASSWORD) {
            if ((password != null) && (password.length > 0)) {
                issueCommandCheck("PASS " + String.valueOf(password));
            }
        }
    }

    public sun.net.ftp.FtpClient login(String user, char[] password) throws sun.net.ftp.FtpProtocolException, IOException {
        if (!isConnected()) {
            throw new sun.net.ftp.FtpProtocolException("Not connected yet", FtpReplyCode.BAD_SEQUENCE);
        }
        if (user == null || user.length() == 0) {
            throw new IllegalArgumentException("User name can't be null or empty");
        }
        tryLogin(user, password);
        String l;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < serverResponse.size(); i++) {
            l = serverResponse.elementAt(i);
            if (l != null) {
                if (l.length() >= 4 && l.startsWith("230")) {
                    l = l.substring(4);
                }
                sb.append(l);
            }
        }
        welcomeMsg = sb.toString();
        loggedIn = true;
        return this;
    }

    public sun.net.ftp.FtpClient login(String user, char[] password, String account) throws sun.net.ftp.FtpProtocolException, IOException {
        if (!isConnected()) {
            throw new sun.net.ftp.FtpProtocolException("Not connected yet", FtpReplyCode.BAD_SEQUENCE);
        }
        if (user == null || user.length() == 0) {
            throw new IllegalArgumentException("User name can't be null or empty");
        }
        tryLogin(user, password);
        if (lastReplyCode == FtpReplyCode.NEED_ACCOUNT) {
            issueCommandCheck("ACCT " + account);
        }
        StringBuffer sb = new StringBuffer();
        if (serverResponse != null) {
            for (String l : serverResponse) {
                if (l != null) {
                    if (l.length() >= 4 && l.startsWith("230")) {
                        l = l.substring(4);
                    }
                    sb.append(l);
                }
            }
        }
        welcomeMsg = sb.toString();
        loggedIn = true;
        return this;
    }

    public void close() throws IOException {
        if (isConnected()) {
            issueCommand("QUIT");
            loggedIn = false;
        }
        disconnect();
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public sun.net.ftp.FtpClient changeDirectory(String remoteDirectory) throws sun.net.ftp.FtpProtocolException, IOException {
        if (remoteDirectory == null || "".equals(remoteDirectory)) {
            throw new IllegalArgumentException("directory can't be null or empty");
        }
        issueCommandCheck("CWD " + remoteDirectory);
        return this;
    }

    public sun.net.ftp.FtpClient changeToParentDirectory() throws sun.net.ftp.FtpProtocolException, IOException {
        issueCommandCheck("CDUP");
        return this;
    }

    public String getWorkingDirectory() throws sun.net.ftp.FtpProtocolException, IOException {
        issueCommandCheck("PWD");
        String answ = getResponseString();
        if (!answ.startsWith("257")) {
            return null;
        }
        return answ.substring(5, answ.lastIndexOf('"'));
    }

    public sun.net.ftp.FtpClient setRestartOffset(long offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset can't be negative");
        }
        restartOffset = offset;
        return this;
    }

    public sun.net.ftp.FtpClient getFile(String name, OutputStream local) throws sun.net.ftp.FtpProtocolException, IOException {
        int mtu = 1500;
        if (restartOffset > 0) {
            Socket s;
            try {
                s = openDataConnection("REST " + restartOffset);
            } finally {
                restartOffset = 0;
            }
            issueCommandCheck("RETR " + name);
            getTransferSize();
            InputStream remote = createInputStream(s.getInputStream());
            byte[] buf = new byte[mtu * 10];
            int l;
            while ((l = remote.read(buf)) >= 0) {
                if (l > 0) {
                    local.write(buf, 0, l);
                }
            }
            remote.close();
        } else {
            Socket s = openDataConnection("RETR " + name);
            getTransferSize();
            InputStream remote = createInputStream(s.getInputStream());
            byte[] buf = new byte[mtu * 10];
            int l;
            while ((l = remote.read(buf)) >= 0) {
                if (l > 0) {
                    local.write(buf, 0, l);
                }
            }
            remote.close();
        }
        return completePending();
    }

    public InputStream getFileStream(String name) throws sun.net.ftp.FtpProtocolException, IOException {
        Socket s;
        if (restartOffset > 0) {
            try {
                s = openDataConnection("REST " + restartOffset);
            } finally {
                restartOffset = 0;
            }
            if (s == null) {
                return null;
            }
            issueCommandCheck("RETR " + name);
            getTransferSize();
            return createInputStream(s.getInputStream());
        }
        s = openDataConnection("RETR " + name);
        if (s == null) {
            return null;
        }
        getTransferSize();
        return createInputStream(s.getInputStream());
    }

    public OutputStream putFileStream(String name, boolean unique) throws sun.net.ftp.FtpProtocolException, IOException {
        String cmd = unique ? "STOU " : "STOR ";
        Socket s = openDataConnection(cmd + name);
        if (s == null) {
            return null;
        }
        boolean bm = (type == TransferType.BINARY);
        return new sun.net.TelnetOutputStream(s.getOutputStream(), bm);
    }

    public sun.net.ftp.FtpClient putFile(String name, InputStream local, boolean unique) throws sun.net.ftp.FtpProtocolException, IOException {
        String cmd = unique ? "STOU " : "STOR ";
        int mtu = 1500;
        if (type == TransferType.BINARY) {
            Socket s = openDataConnection(cmd + name);
            OutputStream remote = createOutputStream(s.getOutputStream());
            byte[] buf = new byte[mtu * 10];
            int l;
            while ((l = local.read(buf)) >= 0) {
                if (l > 0) {
                    remote.write(buf, 0, l);
                }
            }
            remote.close();
        }
        return completePending();
    }

    public sun.net.ftp.FtpClient appendFile(String name, InputStream local) throws sun.net.ftp.FtpProtocolException, IOException {
        int mtu = 1500;
        Socket s = openDataConnection("APPE " + name);
        OutputStream remote = createOutputStream(s.getOutputStream());
        byte[] buf = new byte[mtu * 10];
        int l;
        while ((l = local.read(buf)) >= 0) {
            if (l > 0) {
                remote.write(buf, 0, l);
            }
        }
        remote.close();
        return completePending();
    }

    public sun.net.ftp.FtpClient rename(String from, String to) throws sun.net.ftp.FtpProtocolException, IOException {
        issueCommandCheck("RNFR " + from);
        issueCommandCheck("RNTO " + to);
        return this;
    }

    public sun.net.ftp.FtpClient deleteFile(String name) throws sun.net.ftp.FtpProtocolException, IOException {
        issueCommandCheck("DELE " + name);
        return this;
    }

    public sun.net.ftp.FtpClient makeDirectory(String name) throws sun.net.ftp.FtpProtocolException, IOException {
        issueCommandCheck("MKD " + name);
        return this;
    }

    public sun.net.ftp.FtpClient removeDirectory(String name) throws sun.net.ftp.FtpProtocolException, IOException {
        issueCommandCheck("RMD " + name);
        return this;
    }

    public sun.net.ftp.FtpClient noop() throws sun.net.ftp.FtpProtocolException, IOException {
        issueCommandCheck("NOOP");
        return this;
    }

    public String getStatus(String name) throws sun.net.ftp.FtpProtocolException, IOException {
        issueCommandCheck((name == null ? "STAT" : "STAT " + name));
        Vector<String> resp = getResponseStrings();
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i < resp.size() - 1; i++) {
            sb.append(resp.get(i));
        }
        return sb.toString();
    }

    public List<String> getFeatures() throws sun.net.ftp.FtpProtocolException, IOException {
        ArrayList<String> features = new ArrayList<String>();
        issueCommandCheck("FEAT");
        Vector<String> resp = getResponseStrings();
        for (int i = 1; i < resp.size() - 1; i++) {
            String s = resp.get(i);
            features.add(s.substring(1, s.length() - 1));
        }
        return features;
    }

    public sun.net.ftp.FtpClient abort() throws sun.net.ftp.FtpProtocolException, IOException {
        issueCommandCheck("ABOR");
        return this;
    }

    public sun.net.ftp.FtpClient completePending() throws sun.net.ftp.FtpProtocolException, IOException {
        while (replyPending) {
            replyPending = false;
            if (!readReply()) {
                throw new sun.net.ftp.FtpProtocolException(getLastResponseString(), lastReplyCode);
            }
        }
        return this;
    }

    public sun.net.ftp.FtpClient reInit() throws sun.net.ftp.FtpProtocolException, IOException {
        issueCommandCheck("REIN");
        loggedIn = false;
        if (useCrypto) {
            if (server instanceof SSLSocket) {
                javax.net.ssl.SSLSession session = ((SSLSocket) server).getSession();
                session.invalidate();
                server = oldSocket;
                oldSocket = null;
                try {
                    out = new PrintStream(new BufferedOutputStream(server.getOutputStream()), true, encoding);
                } catch (UnsupportedEncodingException e) {
                    throw new InternalError(encoding + "encoding not found", e);
                }
                in = new BufferedInputStream(server.getInputStream());
            }
        }
        useCrypto = false;
        return this;
    }

    public sun.net.ftp.FtpClient setType(TransferType type) throws sun.net.ftp.FtpProtocolException, IOException {
        String cmd = "NOOP";
        this.type = type;
        if (type == TransferType.ASCII) {
            cmd = "TYPE A";
        }
        if (type == TransferType.BINARY) {
            cmd = "TYPE I";
        }
        if (type == TransferType.EBCDIC) {
            cmd = "TYPE E";
        }
        issueCommandCheck(cmd);
        return this;
    }

    public InputStream list(String path) throws sun.net.ftp.FtpProtocolException, IOException {
        Socket s;
        s = openDataConnection(path == null ? "LIST" : "LIST " + path);
        if (s != null) {
            return createInputStream(s.getInputStream());
        }
        return null;
    }

    public InputStream nameList(String path) throws sun.net.ftp.FtpProtocolException, IOException {
        Socket s;
        s = openDataConnection("NLST " + path);
        if (s != null) {
            return createInputStream(s.getInputStream());
        }
        return null;
    }

    public long getSize(String path) throws sun.net.ftp.FtpProtocolException, IOException {
        if (path == null || path.length() == 0) {
            throw new IllegalArgumentException("path can't be null or empty");
        }
        issueCommandCheck("SIZE " + path);
        if (lastReplyCode == FtpReplyCode.FILE_STATUS) {
            String s = getResponseString();
            s = s.substring(4, s.length() - 1);
            return Long.parseLong(s);
        }
        return -1;
    }

    private static String[] MDTMformats = { "yyyyMMddHHmmss.SSS", "yyyyMMddHHmmss" };

    private static SimpleDateFormat[] dateFormats = new SimpleDateFormat[MDTMformats.length];

    static {
        for (int i = 0; i < MDTMformats.length; i++) {
            dateFormats[i] = new SimpleDateFormat(MDTMformats[i]);
            dateFormats[i].setTimeZone(TimeZone.getTimeZone("GMT"));
        }
    }

    public Date getLastModified(String path) throws sun.net.ftp.FtpProtocolException, IOException {
        issueCommandCheck("MDTM " + path);
        if (lastReplyCode == FtpReplyCode.FILE_STATUS) {
            String s = getResponseString().substring(4);
            Date d = null;
            for (SimpleDateFormat dateFormat : dateFormats) {
                try {
                    d = dateFormat.parse(s);
                } catch (ParseException ex) {
                }
                if (d != null) {
                    return d;
                }
            }
        }
        return null;
    }

    public sun.net.ftp.FtpClient setDirParser(FtpDirParser p) {
        parser = p;
        return this;
    }

    private class FtpFileIterator implements Iterator<FtpDirEntry>, Closeable {

        private BufferedReader in = null;

        private FtpDirEntry nextFile = null;

        private FtpDirParser fparser = null;

        private boolean eof = false;

        public FtpFileIterator(FtpDirParser p, BufferedReader in) {
            this.in = in;
            this.fparser = p;
            readNext();
        }

        private void readNext() {
            nextFile = null;
            if (eof) {
                return;
            }
            String line = null;
            try {
                do {
                    line = in.readLine();
                    if (line != null) {
                        nextFile = fparser.parseLine(line);
                        if (nextFile != null) {
                            return;
                        }
                    }
                } while (line != null);
                in.close();
            } catch (IOException iOException) {
            }
            eof = true;
        }

        public boolean hasNext() {
            return nextFile != null;
        }

        public FtpDirEntry next() {
            FtpDirEntry ret = nextFile;
            readNext();
            return ret;
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void close() throws IOException {
            if (in != null && !eof) {
                in.close();
            }
            eof = true;
            nextFile = null;
        }
    }

    public Iterator<FtpDirEntry> listFiles(String path) throws sun.net.ftp.FtpProtocolException, IOException {
        Socket s = null;
        BufferedReader sin = null;
        try {
            s = openDataConnection(path == null ? "MLSD" : "MLSD " + path);
        } catch (sun.net.ftp.FtpProtocolException FtpException) {
        }
        if (s != null) {
            sin = new BufferedReader(new InputStreamReader(s.getInputStream()));
            return new FtpFileIterator(mlsxParser, sin);
        } else {
            s = openDataConnection(path == null ? "LIST" : "LIST " + path);
            if (s != null) {
                sin = new BufferedReader(new InputStreamReader(s.getInputStream()));
                return new FtpFileIterator(parser, sin);
            }
        }
        return null;
    }

    private boolean sendSecurityData(byte[] buf) throws IOException {
        BASE64Encoder encoder = new BASE64Encoder();
        String s = encoder.encode(buf);
        return issueCommand("ADAT " + s);
    }

    private byte[] getSecurityData() {
        String s = getLastResponseString();
        if (s.substring(4, 9).equalsIgnoreCase("ADAT=")) {
            BASE64Decoder decoder = new BASE64Decoder();
            try {
                return decoder.decodeBuffer(s.substring(9, s.length() - 1));
            } catch (IOException e) {
            }
        }
        return null;
    }

    public sun.net.ftp.FtpClient useKerberos() throws sun.net.ftp.FtpProtocolException, IOException {
        return this;
    }

    public String getWelcomeMsg() {
        return welcomeMsg;
    }

    public FtpReplyCode getLastReplyCode() {
        return lastReplyCode;
    }

    public String getLastResponseString() {
        StringBuffer sb = new StringBuffer();
        if (serverResponse != null) {
            for (String l : serverResponse) {
                if (l != null) {
                    sb.append(l);
                }
            }
        }
        return sb.toString();
    }

    public long getLastTransferSize() {
        return lastTransSize;
    }

    public String getLastFileName() {
        return lastFileName;
    }

    public sun.net.ftp.FtpClient startSecureSession() throws sun.net.ftp.FtpProtocolException, IOException {
        if (!isConnected()) {
            throw new sun.net.ftp.FtpProtocolException("Not connected yet", FtpReplyCode.BAD_SEQUENCE);
        }
        if (sslFact == null) {
            try {
                sslFact = (SSLSocketFactory) SSLSocketFactory.getDefault();
            } catch (Exception e) {
                throw new IOException(e.getLocalizedMessage());
            }
        }
        issueCommandCheck("AUTH TLS");
        Socket s = null;
        try {
            s = sslFact.createSocket(server, serverAddr.getHostName(), serverAddr.getPort(), true);
        } catch (javax.net.ssl.SSLException ssle) {
            try {
                disconnect();
            } catch (Exception e) {
            }
            throw ssle;
        }
        oldSocket = server;
        server = s;
        try {
            out = new PrintStream(new BufferedOutputStream(server.getOutputStream()), true, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(encoding + "encoding not found", e);
        }
        in = new BufferedInputStream(server.getInputStream());
        issueCommandCheck("PBSZ 0");
        issueCommandCheck("PROT P");
        useCrypto = true;
        return this;
    }

    public sun.net.ftp.FtpClient endSecureSession() throws sun.net.ftp.FtpProtocolException, IOException {
        if (!useCrypto) {
            return this;
        }
        issueCommandCheck("CCC");
        issueCommandCheck("PROT C");
        useCrypto = false;
        server = oldSocket;
        oldSocket = null;
        try {
            out = new PrintStream(new BufferedOutputStream(server.getOutputStream()), true, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(encoding + "encoding not found", e);
        }
        in = new BufferedInputStream(server.getInputStream());
        return this;
    }

    public sun.net.ftp.FtpClient allocate(long size) throws sun.net.ftp.FtpProtocolException, IOException {
        issueCommandCheck("ALLO " + size);
        return this;
    }

    public sun.net.ftp.FtpClient structureMount(String struct) throws sun.net.ftp.FtpProtocolException, IOException {
        issueCommandCheck("SMNT " + struct);
        return this;
    }

    public String getSystem() throws sun.net.ftp.FtpProtocolException, IOException {
        issueCommandCheck("SYST");
        String resp = getResponseString();
        return resp.substring(4);
    }

    public String getHelp(String cmd) throws sun.net.ftp.FtpProtocolException, IOException {
        issueCommandCheck("HELP " + cmd);
        Vector<String> resp = getResponseStrings();
        if (resp.size() == 1) {
            return resp.get(0).substring(4);
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i < resp.size() - 1; i++) {
            sb.append(resp.get(i).substring(3));
        }
        return sb.toString();
    }

    public sun.net.ftp.FtpClient siteCmd(String cmd) throws sun.net.ftp.FtpProtocolException, IOException {
        issueCommandCheck("SITE " + cmd);
        return this;
    }
}