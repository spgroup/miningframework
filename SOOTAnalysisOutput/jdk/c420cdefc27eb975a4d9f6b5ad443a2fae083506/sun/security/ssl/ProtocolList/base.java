package sun.security.ssl;

import java.util.*;

final class ProtocolList {

    private static final ProtocolList SUPPORTED;

    private final Collection<ProtocolVersion> protocols;

    private String[] protocolNames;

    final ProtocolVersion min, max;

    final ProtocolVersion helloVersion;

    ProtocolList(String[] names) {
        if (names == null) {
            throw new IllegalArgumentException("Protocols may not be null");
        }
        protocols = new ArrayList<ProtocolVersion>(3);
        for (int i = 0; i < names.length; i++) {
            ProtocolVersion version = ProtocolVersion.valueOf(names[i]);
            if (protocols.contains(version) == false) {
                protocols.add(version);
            }
        }
        if ((protocols.size() == 1) && protocols.contains(ProtocolVersion.SSL20Hello)) {
            throw new IllegalArgumentException("SSLv2Hello" + "cannot be enabled unless TLSv1 or SSLv3 is also enabled");
        }
        min = contains(ProtocolVersion.SSL30) ? ProtocolVersion.SSL30 : ProtocolVersion.TLS10;
        max = contains(ProtocolVersion.TLS10) ? ProtocolVersion.TLS10 : ProtocolVersion.SSL30;
        if (protocols.contains(ProtocolVersion.SSL20Hello)) {
            helloVersion = ProtocolVersion.SSL20Hello;
        } else {
            helloVersion = min;
        }
    }

    boolean contains(ProtocolVersion protocolVersion) {
        if (protocolVersion == ProtocolVersion.SSL20Hello) {
            return false;
        }
        return protocols.contains(protocolVersion);
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

    static ProtocolList getDefault() {
        return SUPPORTED;
    }

    static ProtocolList getSupported() {
        return SUPPORTED;
    }

    static {
        if (SunJSSE.isFIPS()) {
            SUPPORTED = new ProtocolList(new String[] { ProtocolVersion.TLS10.name });
        } else {
            SUPPORTED = new ProtocolList(new String[] { ProtocolVersion.SSL20Hello.name, ProtocolVersion.SSL30.name, ProtocolVersion.TLS10.name });
        }
    }
}
