package sun.security.ssl;

import java.util.*;

final class ProtocolList {

    private static final ProtocolList SUPPORTED;

    private static final ProtocolList CLIENT_DEFAULT;

    private static final ProtocolList SERVER_DEFAULT;

    private final ArrayList<ProtocolVersion> protocols;

    private String[] protocolNames;

    final ProtocolVersion min, max;

    final ProtocolVersion helloVersion;

    ProtocolList(String[] names) {
        this(convert(names));
    }

    ProtocolList(ArrayList<ProtocolVersion> versions) {
        this.protocols = versions;
        if ((protocols.size() == 1) && protocols.contains(ProtocolVersion.SSL20Hello)) {
            throw new IllegalArgumentException("SSLv2Hello cannot be " + "enabled unless at least one other supported version " + "is also enabled.");
        }
        if (protocols.size() != 0) {
            Collections.sort(protocols);
            min = protocols.get(0);
            max = protocols.get(protocols.size() - 1);
            helloVersion = protocols.get(0);
        } else {
            min = ProtocolVersion.NONE;
            max = ProtocolVersion.NONE;
            helloVersion = ProtocolVersion.NONE;
        }
    }

    private static ArrayList<ProtocolVersion> convert(String[] names) {
        if (names == null) {
            throw new IllegalArgumentException("Protocols may not be null");
        }
        ArrayList<ProtocolVersion> versions = new ArrayList<ProtocolVersion>(3);
        for (int i = 0; i < names.length; i++) {
            ProtocolVersion version = ProtocolVersion.valueOf(names[i]);
            if (versions.contains(version) == false) {
                versions.add(version);
            }
        }
        return versions;
    }

    boolean contains(ProtocolVersion protocolVersion) {
        if (protocolVersion == ProtocolVersion.SSL20Hello) {
            return false;
        }
        return protocols.contains(protocolVersion);
    }

    Collection<ProtocolVersion> collection() {
        return protocols;
    }

    ProtocolVersion selectProtocolVersion(ProtocolVersion protocolVersion) {
        ProtocolVersion selectedVersion = null;
        for (ProtocolVersion pv : protocols) {
            if (pv.v > protocolVersion.v) {
                break;
            }
            selectedVersion = pv;
        }
        return selectedVersion;
    }

    synchronized String[] toStringArray() {
        if (protocolNames == null) {
            protocolNames = new String[protocols.size()];
            int i = 0;
            for (ProtocolVersion version : protocols) {
                protocolNames[i++] = version.name;
            }
        }
        return protocolNames.clone();
    }

    public String toString() {
        return protocols.toString();
    }

    static ProtocolList getDefault(boolean isServer) {
        return isServer ? SERVER_DEFAULT : CLIENT_DEFAULT;
    }

    static boolean isDefaultProtocolList(ProtocolList protocols) {
        return protocols == CLIENT_DEFAULT || protocols == SERVER_DEFAULT;
    }

    static ProtocolList getSupported() {
        return SUPPORTED;
    }

    static {
        if (SunJSSE.isFIPS()) {
            SUPPORTED = new ProtocolList(new String[] { ProtocolVersion.TLS10.name, ProtocolVersion.TLS11.name, ProtocolVersion.TLS12.name });
            SERVER_DEFAULT = SUPPORTED;
            CLIENT_DEFAULT = new ProtocolList(new String[] { ProtocolVersion.TLS10.name });
        } else {
            SUPPORTED = new ProtocolList(new String[] { ProtocolVersion.SSL20Hello.name, ProtocolVersion.SSL30.name, ProtocolVersion.TLS10.name, ProtocolVersion.TLS11.name, ProtocolVersion.TLS12.name });
            SERVER_DEFAULT = SUPPORTED;
            CLIENT_DEFAULT = new ProtocolList(new String[] { ProtocolVersion.SSL30.name, ProtocolVersion.TLS10.name });
        }
    }
}