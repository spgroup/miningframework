package sun.security.ssl;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.security.spec.ECParameterSpec;
import javax.net.ssl.SSLProtocolException;

final class HelloExtensions {

    private List<HelloExtension> extensions;

    private int encodedLength;

    HelloExtensions() {
        extensions = Collections.emptyList();
    }

    HelloExtensions(HandshakeInStream s) throws IOException {
        int len = s.getInt16();
        extensions = new ArrayList<HelloExtension>();
        encodedLength = len + 2;
        while (len > 0) {
            int type = s.getInt16();
            int extlen = s.getInt16();
            ExtensionType extType = ExtensionType.get(type);
            HelloExtension extension;
            if (extType == ExtensionType.EXT_SERVER_NAME) {
                extension = new ServerNameExtension(s, extlen);
            } else if (extType == ExtensionType.EXT_SIGNATURE_ALGORITHMS) {
                extension = new SignatureAlgorithmsExtension(s, extlen);
            } else if (extType == ExtensionType.EXT_ELLIPTIC_CURVES) {
                extension = new SupportedEllipticCurvesExtension(s, extlen);
            } else if (extType == ExtensionType.EXT_EC_POINT_FORMATS) {
                extension = new SupportedEllipticPointFormatsExtension(s, extlen);
            } else if (extType == ExtensionType.EXT_RENEGOTIATION_INFO) {
                extension = new RenegotiationInfoExtension(s, extlen);
            } else {
                extension = new UnknownExtension(s, extlen, extType);
            }
            extensions.add(extension);
            len -= extlen + 4;
        }
        if (len != 0) {
            throw new SSLProtocolException("Error parsing extensions: extra data");
        }
    }

    List<HelloExtension> list() {
        return extensions;
    }

    void add(HelloExtension ext) {
        if (extensions.isEmpty()) {
            extensions = new ArrayList<HelloExtension>();
        }
        extensions.add(ext);
        encodedLength = -1;
    }

    HelloExtension get(ExtensionType type) {
        for (HelloExtension ext : extensions) {
            if (ext.type == type) {
                return ext;
            }
        }
        return null;
    }

    int length() {
        if (encodedLength >= 0) {
            return encodedLength;
        }
        if (extensions.isEmpty()) {
            encodedLength = 0;
        } else {
            encodedLength = 2;
            for (HelloExtension ext : extensions) {
                encodedLength += ext.length();
            }
        }
        return encodedLength;
    }

    void send(HandshakeOutStream s) throws IOException {
        int length = length();
        if (length == 0) {
            return;
        }
        s.putInt16(length - 2);
        for (HelloExtension ext : extensions) {
            ext.send(s);
        }
    }

    void print(PrintStream s) throws IOException {
        for (HelloExtension ext : extensions) {
            s.println(ext.toString());
        }
    }
}

final class ExtensionType {

    final int id;

    final String name;

    private ExtensionType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String toString() {
        return name;
    }

    static List<ExtensionType> knownExtensions = new ArrayList<ExtensionType>(9);

    static ExtensionType get(int id) {
        for (ExtensionType ext : knownExtensions) {
            if (ext.id == id) {
                return ext;
            }
        }
        return new ExtensionType(id, "type_" + id);
    }

    private static ExtensionType e(int id, String name) {
        ExtensionType ext = new ExtensionType(id, name);
        knownExtensions.add(ext);
        return ext;
    }

    final static ExtensionType EXT_SERVER_NAME = e(0x0000, "server_name");

    final static ExtensionType EXT_MAX_FRAGMENT_LENGTH = e(0x0001, "max_fragment_length");

    final static ExtensionType EXT_CLIENT_CERTIFICATE_URL = e(0x0002, "client_certificate_url");

    final static ExtensionType EXT_TRUSTED_CA_KEYS = e(0x0003, "trusted_ca_keys");

    final static ExtensionType EXT_TRUNCATED_HMAC = e(0x0004, "truncated_hmac");

    final static ExtensionType EXT_STATUS_REQUEST = e(0x0005, "status_request");

    final static ExtensionType EXT_USER_MAPPING = e(0x0006, "user_mapping");

    final static ExtensionType EXT_CERT_TYPE = e(0x0009, "cert_type");

    final static ExtensionType EXT_ELLIPTIC_CURVES = e(0x000A, "elliptic_curves");

    final static ExtensionType EXT_EC_POINT_FORMATS = e(0x000B, "ec_point_formats");

    final static ExtensionType EXT_SRP = e(0x000C, "srp");

    final static ExtensionType EXT_SIGNATURE_ALGORITHMS = e(0x000D, "signature_algorithms");

    final static ExtensionType EXT_RENEGOTIATION_INFO = e(0xff01, "renegotiation_info");
}

abstract class HelloExtension {

    final ExtensionType type;

    HelloExtension(ExtensionType type) {
        this.type = type;
    }

    abstract int length();

    abstract void send(HandshakeOutStream s) throws IOException;

    public abstract String toString();
}

final class UnknownExtension extends HelloExtension {

    private final byte[] data;

    UnknownExtension(HandshakeInStream s, int len, ExtensionType type) throws IOException {
        super(type);
        data = new byte[len];
        if (len != 0) {
            s.read(data);
        }
    }

    int length() {
        return 4 + data.length;
    }

    void send(HandshakeOutStream s) throws IOException {
        s.putInt16(type.id);
        s.putBytes16(data);
    }

    public String toString() {
        return "Unsupported extension " + type + ", data: " + Debug.toString(data);
    }
}

final class ServerNameExtension extends HelloExtension {

    final static int NAME_HOST_NAME = 0;

    private List<ServerName> names;

    private int listLength;

    ServerNameExtension(List<String> hostnames) throws IOException {
        super(ExtensionType.EXT_SERVER_NAME);
        listLength = 0;
        names = new ArrayList<ServerName>(hostnames.size());
        for (String hostname : hostnames) {
            if (hostname != null && hostname.length() != 0) {
                ServerName serverName = new ServerName(NAME_HOST_NAME, hostname);
                names.add(serverName);
                listLength += serverName.length;
            }
        }
        if (names.size() > 1) {
            throw new SSLProtocolException("The ServerNameList MUST NOT contain more than " + "one name of the same name_type");
        }
        if (listLength == 0) {
            throw new SSLProtocolException("The ServerNameList cannot be empty");
        }
    }

    ServerNameExtension(HandshakeInStream s, int len) throws IOException {
        super(ExtensionType.EXT_SERVER_NAME);
        int remains = len;
        if (len >= 2) {
            listLength = s.getInt16();
            if (listLength == 0 || listLength + 2 != len) {
                throw new SSLProtocolException("Invalid " + type + " extension");
            }
            remains -= 2;
            names = new ArrayList<ServerName>();
            while (remains > 0) {
                ServerName name = new ServerName(s);
                names.add(name);
                remains -= name.length;
            }
        } else if (len == 0) {
            listLength = 0;
            names = Collections.<ServerName>emptyList();
        }
        if (remains != 0) {
            throw new SSLProtocolException("Invalid server_name extension");
        }
    }

    static class ServerName {

        final int length;

        final int type;

        final byte[] data;

        final String hostname;

        ServerName(int type, String hostname) throws IOException {
            this.type = type;
            this.hostname = hostname;
            this.data = hostname.getBytes("UTF8");
            this.length = data.length + 3;
        }

        ServerName(HandshakeInStream s) throws IOException {
            type = s.getInt8();
            data = s.getBytes16();
            length = data.length + 3;
            if (type == NAME_HOST_NAME) {
                hostname = new String(data, "UTF8");
            } else {
                hostname = null;
            }
        }

        public String toString() {
            if (type == NAME_HOST_NAME) {
                return "host_name: " + hostname;
            } else {
                return "unknown-" + type + ": " + Debug.toString(data);
            }
        }
    }

    int length() {
        return listLength == 0 ? 4 : 6 + listLength;
    }

    void send(HandshakeOutStream s) throws IOException {
        s.putInt16(type.id);
        s.putInt16(listLength + 2);
        if (listLength != 0) {
            s.putInt16(listLength);
            for (ServerName name : names) {
                s.putInt8(name.type);
                s.putBytes16(name.data);
            }
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (ServerName name : names) {
            buffer.append("[" + name + "]");
        }
        return "Extension " + type + ", server_name: " + buffer;
    }
}

final class SupportedEllipticCurvesExtension extends HelloExtension {

    static final SupportedEllipticCurvesExtension DEFAULT;

    private static final boolean fips;

    static {
        int[] ids;
        fips = SunJSSE.isFIPS();
        if (fips == false) {
            ids = new int[] { 23, 1, 3, 19, 21, 6, 7, 9, 10, 24, 11, 12, 25, 13, 14, 15, 16, 17, 2, 18, 4, 5, 20, 8, 22 };
        } else {
            ids = new int[] { 23, 1, 3, 19, 21, 6, 7, 9, 10, 24, 11, 12, 25, 13, 14 };
        }
        DEFAULT = new SupportedEllipticCurvesExtension(ids);
    }

    private final int[] curveIds;

    private SupportedEllipticCurvesExtension(int[] curveIds) {
        super(ExtensionType.EXT_ELLIPTIC_CURVES);
        this.curveIds = curveIds;
    }

    SupportedEllipticCurvesExtension(HandshakeInStream s, int len) throws IOException {
        super(ExtensionType.EXT_ELLIPTIC_CURVES);
        int k = s.getInt16();
        if (((len & 1) != 0) || (k + 2 != len)) {
            throw new SSLProtocolException("Invalid " + type + " extension");
        }
        curveIds = new int[k >> 1];
        for (int i = 0; i < curveIds.length; i++) {
            curveIds[i] = s.getInt16();
        }
    }

    boolean contains(int index) {
        for (int curveId : curveIds) {
            if (index == curveId) {
                return true;
            }
        }
        return false;
    }

    int[] curveIds() {
        return curveIds;
    }

    int length() {
        return 6 + (curveIds.length << 1);
    }

    void send(HandshakeOutStream s) throws IOException {
        s.putInt16(type.id);
        int k = curveIds.length << 1;
        s.putInt16(k + 2);
        s.putInt16(k);
        for (int curveId : curveIds) {
            s.putInt16(curveId);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Extension " + type + ", curve names: {");
        boolean first = true;
        for (int curveId : curveIds) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            String oid = getCurveOid(curveId);
            if (oid != null) {
                ECParameterSpec spec = JsseJce.getECParameterSpec(oid);
                if (spec != null) {
                    sb.append(spec.toString().split(" ")[0]);
                } else {
                    sb.append(oid);
                }
            } else if (curveId == ARBITRARY_PRIME) {
                sb.append("arbitrary_explicit_prime_curves");
            } else if (curveId == ARBITRARY_CHAR2) {
                sb.append("arbitrary_explicit_char2_curves");
            } else {
                sb.append("unknown curve " + curveId);
            }
        }
        sb.append("}");
        return sb.toString();
    }

    static boolean isSupported(int index) {
        if ((index <= 0) || (index >= NAMED_CURVE_OID_TABLE.length)) {
            return false;
        }
        if (fips == false) {
            return true;
        }
        return DEFAULT.contains(index);
    }

    static int getCurveIndex(ECParameterSpec params) {
        String oid = JsseJce.getNamedCurveOid(params);
        if (oid == null) {
            return -1;
        }
        Integer n = curveIndices.get(oid);
        return (n == null) ? -1 : n;
    }

    static String getCurveOid(int index) {
        if ((index > 0) && (index < NAMED_CURVE_OID_TABLE.length)) {
            return NAMED_CURVE_OID_TABLE[index];
        }
        return null;
    }

    private final static int ARBITRARY_PRIME = 0xff01;

    private final static int ARBITRARY_CHAR2 = 0xff02;

    private final static String[] NAMED_CURVE_OID_TABLE = new String[] { null, "1.3.132.0.1", "1.3.132.0.2", "1.3.132.0.15", "1.3.132.0.24", "1.3.132.0.25", "1.3.132.0.26", "1.3.132.0.27", "1.3.132.0.3", "1.3.132.0.16", "1.3.132.0.17", "1.3.132.0.36", "1.3.132.0.37", "1.3.132.0.38", "1.3.132.0.39", "1.3.132.0.9", "1.3.132.0.8", "1.3.132.0.30", "1.3.132.0.31", "1.2.840.10045.3.1.1", "1.3.132.0.32", "1.3.132.0.33", "1.3.132.0.10", "1.2.840.10045.3.1.7", "1.3.132.0.34", "1.3.132.0.35" };

    private final static Map<String, Integer> curveIndices;

    static {
        curveIndices = new HashMap<String, Integer>();
        for (int i = 1; i < NAMED_CURVE_OID_TABLE.length; i++) {
            curveIndices.put(NAMED_CURVE_OID_TABLE[i], i);
        }
    }
}

final class SupportedEllipticPointFormatsExtension extends HelloExtension {

    final static int FMT_UNCOMPRESSED = 0;

    final static int FMT_ANSIX962_COMPRESSED_PRIME = 1;

    final static int FMT_ANSIX962_COMPRESSED_CHAR2 = 2;

    static final HelloExtension DEFAULT = new SupportedEllipticPointFormatsExtension(new byte[] { FMT_UNCOMPRESSED });

    private final byte[] formats;

    private SupportedEllipticPointFormatsExtension(byte[] formats) {
        super(ExtensionType.EXT_EC_POINT_FORMATS);
        this.formats = formats;
    }

    SupportedEllipticPointFormatsExtension(HandshakeInStream s, int len) throws IOException {
        super(ExtensionType.EXT_EC_POINT_FORMATS);
        formats = s.getBytes8();
        boolean uncompressed = false;
        for (int format : formats) {
            if (format == FMT_UNCOMPRESSED) {
                uncompressed = true;
                break;
            }
        }
        if (uncompressed == false) {
            throw new SSLProtocolException("Peer does not support uncompressed points");
        }
    }

    int length() {
        return 5 + formats.length;
    }

    void send(HandshakeOutStream s) throws IOException {
        s.putInt16(type.id);
        s.putInt16(formats.length + 1);
        s.putBytes8(formats);
    }

    private static String toString(byte format) {
        int f = format & 0xff;
        switch(f) {
            case FMT_UNCOMPRESSED:
                return "uncompressed";
            case FMT_ANSIX962_COMPRESSED_PRIME:
                return "ansiX962_compressed_prime";
            case FMT_ANSIX962_COMPRESSED_CHAR2:
                return "ansiX962_compressed_char2";
            default:
                return "unknown-" + f;
        }
    }

    public String toString() {
        List<String> list = new ArrayList<String>();
        for (byte format : formats) {
            list.add(toString(format));
        }
        return "Extension " + type + ", formats: " + list;
    }
}

final class RenegotiationInfoExtension extends HelloExtension {

    private final byte[] renegotiated_connection;

    RenegotiationInfoExtension(byte[] clientVerifyData, byte[] serverVerifyData) {
        super(ExtensionType.EXT_RENEGOTIATION_INFO);
        if (clientVerifyData.length != 0) {
            renegotiated_connection = new byte[clientVerifyData.length + serverVerifyData.length];
            System.arraycopy(clientVerifyData, 0, renegotiated_connection, 0, clientVerifyData.length);
            if (serverVerifyData.length != 0) {
                System.arraycopy(serverVerifyData, 0, renegotiated_connection, clientVerifyData.length, serverVerifyData.length);
            }
        } else {
            renegotiated_connection = new byte[0];
        }
    }

    RenegotiationInfoExtension(HandshakeInStream s, int len) throws IOException {
        super(ExtensionType.EXT_RENEGOTIATION_INFO);
        if (len < 1) {
            throw new SSLProtocolException("Invalid " + type + " extension");
        }
        int renegoInfoDataLen = s.getInt8();
        if (renegoInfoDataLen + 1 != len) {
            throw new SSLProtocolException("Invalid " + type + " extension");
        }
        renegotiated_connection = new byte[renegoInfoDataLen];
        if (renegoInfoDataLen != 0) {
            s.read(renegotiated_connection, 0, renegoInfoDataLen);
        }
    }

    int length() {
        return 5 + renegotiated_connection.length;
    }

    void send(HandshakeOutStream s) throws IOException {
        s.putInt16(type.id);
        s.putInt16(renegotiated_connection.length + 1);
        s.putBytes8(renegotiated_connection);
    }

    boolean isEmpty() {
        return renegotiated_connection.length == 0;
    }

    byte[] getRenegotiatedConnection() {
        return renegotiated_connection;
    }

    public String toString() {
        return "Extension " + type + ", renegotiated_connection: " + (renegotiated_connection.length == 0 ? "<empty>" : Debug.toString(renegotiated_connection));
    }
}

final class SignatureAlgorithmsExtension extends HelloExtension {

    private Collection<SignatureAndHashAlgorithm> algorithms;

    private int algorithmsLen;

    SignatureAlgorithmsExtension(Collection<SignatureAndHashAlgorithm> signAlgs) {
        super(ExtensionType.EXT_SIGNATURE_ALGORITHMS);
        algorithms = new ArrayList<SignatureAndHashAlgorithm>(signAlgs);
        algorithmsLen = SignatureAndHashAlgorithm.sizeInRecord() * algorithms.size();
    }

    SignatureAlgorithmsExtension(HandshakeInStream s, int len) throws IOException {
        super(ExtensionType.EXT_SIGNATURE_ALGORITHMS);
        algorithmsLen = s.getInt16();
        if (algorithmsLen == 0 || algorithmsLen + 2 != len) {
            throw new SSLProtocolException("Invalid " + type + " extension");
        }
        algorithms = new ArrayList<SignatureAndHashAlgorithm>();
        int remains = algorithmsLen;
        int sequence = 0;
        while (remains > 1) {
            int hash = s.getInt8();
            int signature = s.getInt8();
            SignatureAndHashAlgorithm algorithm = SignatureAndHashAlgorithm.valueOf(hash, signature, ++sequence);
            algorithms.add(algorithm);
            remains -= 2;
        }
        if (remains != 0) {
            throw new SSLProtocolException("Invalid server_name extension");
        }
    }

    Collection<SignatureAndHashAlgorithm> getSignAlgorithms() {
        return algorithms;
    }

    @Override
    int length() {
        return 6 + algorithmsLen;
    }

    @Override
    void send(HandshakeOutStream s) throws IOException {
        s.putInt16(type.id);
        s.putInt16(algorithmsLen + 2);
        s.putInt16(algorithmsLen);
        for (SignatureAndHashAlgorithm algorithm : algorithms) {
            s.putInt8(algorithm.getHashValue());
            s.putInt8(algorithm.getSignatureValue());
        }
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        boolean opened = false;
        for (SignatureAndHashAlgorithm signAlg : algorithms) {
            if (opened) {
                buffer.append(", " + signAlg.getAlgorithmName());
            } else {
                buffer.append(signAlg.getAlgorithmName());
                opened = true;
            }
        }
        return "Extension " + type + ", signature_algorithms: " + buffer;
    }
}