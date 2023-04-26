package java.net;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.util.Enumeration;
import java.util.Arrays;

public final class Inet6Address extends InetAddress {

    static final int INADDRSZ = 16;

    private transient int cached_scope_id;

    private class Inet6AddressHolder {

        private Inet6AddressHolder() {
            ipaddress = new byte[INADDRSZ];
        }

        private Inet6AddressHolder(byte[] ipaddress, int scope_id, boolean scope_id_set, NetworkInterface ifname, boolean scope_ifname_set) {
            this.ipaddress = ipaddress;
            this.scope_id = scope_id;
            this.scope_id_set = scope_id_set;
            this.scope_ifname_set = scope_ifname_set;
            this.scope_ifname = ifname;
        }

        byte[] ipaddress;

        int scope_id;

        boolean scope_id_set;

        NetworkInterface scope_ifname;

        boolean scope_ifname_set;

        void setAddr(byte[] addr) {
            if (addr.length == INADDRSZ) {
                System.arraycopy(addr, 0, ipaddress, 0, INADDRSZ);
            }
        }

        void init(byte[] addr, int scope_id) {
            setAddr(addr);
            if (scope_id >= 0) {
                this.scope_id = scope_id;
                this.scope_id_set = true;
            }
        }

        void init(byte[] addr, NetworkInterface nif) throws UnknownHostException {
            setAddr(addr);
            if (nif != null) {
                this.scope_id = deriveNumericScope(ipaddress, nif);
                this.scope_id_set = true;
                this.scope_ifname = nif;
                this.scope_ifname_set = true;
            }
        }

        String getHostAddress() {
            String s = numericToTextFormat(ipaddress);
            if (scope_ifname != null) {
                s = s + "%" + scope_ifname.getName();
            } else if (scope_id_set) {
                s = s + "%" + scope_id;
            }
            return s;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Inet6AddressHolder)) {
                return false;
            }
            Inet6AddressHolder that = (Inet6AddressHolder) o;
            return Arrays.equals(this.ipaddress, that.ipaddress);
        }

        public int hashCode() {
            if (ipaddress != null) {
                int hash = 0;
                int i = 0;
                while (i < INADDRSZ) {
                    int j = 0;
                    int component = 0;
                    while (j < 4 && i < INADDRSZ) {
                        component = (component << 8) + ipaddress[i];
                        j++;
                        i++;
                    }
                    hash += component;
                }
                return hash;
            } else {
                return 0;
            }
        }

        boolean isIPv4CompatibleAddress() {
            if ((ipaddress[0] == 0x00) && (ipaddress[1] == 0x00) && (ipaddress[2] == 0x00) && (ipaddress[3] == 0x00) && (ipaddress[4] == 0x00) && (ipaddress[5] == 0x00) && (ipaddress[6] == 0x00) && (ipaddress[7] == 0x00) && (ipaddress[8] == 0x00) && (ipaddress[9] == 0x00) && (ipaddress[10] == 0x00) && (ipaddress[11] == 0x00)) {
                return true;
            }
            return false;
        }

        boolean isMulticastAddress() {
            return ((ipaddress[0] & 0xff) == 0xff);
        }

        boolean isAnyLocalAddress() {
            byte test = 0x00;
            for (int i = 0; i < INADDRSZ; i++) {
                test |= ipaddress[i];
            }
            return (test == 0x00);
        }

        boolean isLoopbackAddress() {
            byte test = 0x00;
            for (int i = 0; i < 15; i++) {
                test |= ipaddress[i];
            }
            return (test == 0x00) && (ipaddress[15] == 0x01);
        }

        boolean isLinkLocalAddress() {
            return ((ipaddress[0] & 0xff) == 0xfe && (ipaddress[1] & 0xc0) == 0x80);
        }

        boolean isSiteLocalAddress() {
            return ((ipaddress[0] & 0xff) == 0xfe && (ipaddress[1] & 0xc0) == 0xc0);
        }

        boolean isMCGlobal() {
            return ((ipaddress[0] & 0xff) == 0xff && (ipaddress[1] & 0x0f) == 0x0e);
        }

        boolean isMCNodeLocal() {
            return ((ipaddress[0] & 0xff) == 0xff && (ipaddress[1] & 0x0f) == 0x01);
        }

        boolean isMCLinkLocal() {
            return ((ipaddress[0] & 0xff) == 0xff && (ipaddress[1] & 0x0f) == 0x02);
        }

        boolean isMCSiteLocal() {
            return ((ipaddress[0] & 0xff) == 0xff && (ipaddress[1] & 0x0f) == 0x05);
        }

        boolean isMCOrgLocal() {
            return ((ipaddress[0] & 0xff) == 0xff && (ipaddress[1] & 0x0f) == 0x08);
        }
    }

    private final transient Inet6AddressHolder holder6;

    private static final long serialVersionUID = 6880410070516793377L;

    static {
        init();
    }

    Inet6Address() {
        super();
        holder.init(null, IPv6);
        holder6 = new Inet6AddressHolder();
    }

    Inet6Address(String hostName, byte[] addr, int scope_id) {
        holder.init(hostName, IPv6);
        holder6 = new Inet6AddressHolder();
        holder6.init(addr, scope_id);
    }

    Inet6Address(String hostName, byte[] addr) {
        holder6 = new Inet6AddressHolder();
        try {
            initif(hostName, addr, null);
        } catch (UnknownHostException e) {
        }
    }

    Inet6Address(String hostName, byte[] addr, NetworkInterface nif) throws UnknownHostException {
        holder6 = new Inet6AddressHolder();
        initif(hostName, addr, nif);
    }

    Inet6Address(String hostName, byte[] addr, String ifname) throws UnknownHostException {
        holder6 = new Inet6AddressHolder();
        initstr(hostName, addr, ifname);
    }

    public static Inet6Address getByAddress(String host, byte[] addr, NetworkInterface nif) throws UnknownHostException {
        if (host != null && host.length() > 0 && host.charAt(0) == '[') {
            if (host.charAt(host.length() - 1) == ']') {
                host = host.substring(1, host.length() - 1);
            }
        }
        if (addr != null) {
            if (addr.length == Inet6Address.INADDRSZ) {
                return new Inet6Address(host, addr, nif);
            }
        }
        throw new UnknownHostException("addr is of illegal length");
    }

    public static Inet6Address getByAddress(String host, byte[] addr, int scope_id) throws UnknownHostException {
        if (host != null && host.length() > 0 && host.charAt(0) == '[') {
            if (host.charAt(host.length() - 1) == ']') {
                host = host.substring(1, host.length() - 1);
            }
        }
        if (addr != null) {
            if (addr.length == Inet6Address.INADDRSZ) {
                return new Inet6Address(host, addr, scope_id);
            }
        }
        throw new UnknownHostException("addr is of illegal length");
    }

    private void initstr(String hostName, byte[] addr, String ifname) throws UnknownHostException {
        try {
            NetworkInterface nif = NetworkInterface.getByName(ifname);
            if (nif == null) {
                throw new UnknownHostException("no such interface " + ifname);
            }
            initif(hostName, addr, nif);
        } catch (SocketException e) {
            throw new UnknownHostException("SocketException thrown" + ifname);
        }
    }

    private void initif(String hostName, byte[] addr, NetworkInterface nif) throws UnknownHostException {
        int family = -1;
        holder6.init(addr, nif);
        if (addr.length == INADDRSZ) {
            family = IPv6;
        }
        holder.init(hostName, family);
    }

    private static boolean isDifferentLocalAddressType(byte[] thisAddr, byte[] otherAddr) {
        if (Inet6Address.isLinkLocalAddress(thisAddr) && !Inet6Address.isLinkLocalAddress(otherAddr)) {
            return false;
        }
        if (Inet6Address.isSiteLocalAddress(thisAddr) && !Inet6Address.isSiteLocalAddress(otherAddr)) {
            return false;
        }
        return true;
    }

    private static int deriveNumericScope(byte[] thisAddr, NetworkInterface ifc) throws UnknownHostException {
        Enumeration<InetAddress> addresses = ifc.getInetAddresses();
        while (addresses.hasMoreElements()) {
            InetAddress addr = addresses.nextElement();
            if (!(addr instanceof Inet6Address)) {
                continue;
            }
            Inet6Address ia6_addr = (Inet6Address) addr;
            if (!isDifferentLocalAddressType(thisAddr, ia6_addr.getAddress())) {
                continue;
            }
            return ia6_addr.getScopeId();
        }
        throw new UnknownHostException("no scope_id found");
    }

    private int deriveNumericScope(String ifname) throws UnknownHostException {
        Enumeration<NetworkInterface> en;
        try {
            en = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new UnknownHostException("could not enumerate local network interfaces");
        }
        while (en.hasMoreElements()) {
            NetworkInterface ifc = en.nextElement();
            if (ifc.getName().equals(ifname)) {
                return deriveNumericScope(holder6.ipaddress, ifc);
            }
        }
        throw new UnknownHostException("No matching address found for interface : " + ifname);
    }

    private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField("ipaddress", byte[].class), new ObjectStreamField("scope_id", int.class), new ObjectStreamField("scope_id_set", boolean.class), new ObjectStreamField("scope_ifname_set", boolean.class), new ObjectStreamField("ifname", String.class) };

    private static final long FIELDS_OFFSET;

    private static final jdk.internal.misc.Unsafe UNSAFE;

    static {
        try {
            jdk.internal.misc.Unsafe unsafe = jdk.internal.misc.Unsafe.getUnsafe();
            FIELDS_OFFSET = unsafe.objectFieldOffset(Inet6Address.class.getDeclaredField("holder6"));
            UNSAFE = unsafe;
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        NetworkInterface scope_ifname = null;
        if (getClass().getClassLoader() != null) {
            throw new SecurityException("invalid address type");
        }
        ObjectInputStream.GetField gf = s.readFields();
        byte[] ipaddress = (byte[]) gf.get("ipaddress", null);
        int scope_id = gf.get("scope_id", -1);
        boolean scope_id_set = gf.get("scope_id_set", false);
        boolean scope_ifname_set = gf.get("scope_ifname_set", false);
        String ifname = (String) gf.get("ifname", null);
        if (ifname != null && !"".equals(ifname)) {
            try {
                scope_ifname = NetworkInterface.getByName(ifname);
                if (scope_ifname == null) {
                    scope_id_set = false;
                    scope_ifname_set = false;
                    scope_id = 0;
                } else {
                    scope_ifname_set = true;
                    try {
                        scope_id = deriveNumericScope(ipaddress, scope_ifname);
                    } catch (UnknownHostException e) {
                    }
                }
            } catch (SocketException e) {
            }
        }
        ipaddress = ipaddress.clone();
        if (ipaddress.length != INADDRSZ) {
            throw new InvalidObjectException("invalid address length: " + ipaddress.length);
        }
        if (holder.getFamily() != IPv6) {
            throw new InvalidObjectException("invalid address family type");
        }
        Inet6AddressHolder h = new Inet6AddressHolder(ipaddress, scope_id, scope_id_set, scope_ifname, scope_ifname_set);
        UNSAFE.putObject(this, FIELDS_OFFSET, h);
    }

    private synchronized void writeObject(ObjectOutputStream s) throws IOException {
        String ifname = null;
        if (holder6.scope_ifname != null) {
            ifname = holder6.scope_ifname.getName();
            holder6.scope_ifname_set = true;
        }
        ObjectOutputStream.PutField pfields = s.putFields();
        pfields.put("ipaddress", holder6.ipaddress);
        pfields.put("scope_id", holder6.scope_id);
        pfields.put("scope_id_set", holder6.scope_id_set);
        pfields.put("scope_ifname_set", holder6.scope_ifname_set);
        pfields.put("ifname", ifname);
        s.writeFields();
    }

    @Override
    public boolean isMulticastAddress() {
        return holder6.isMulticastAddress();
    }

    @Override
    public boolean isAnyLocalAddress() {
        return holder6.isAnyLocalAddress();
    }

    @Override
    public boolean isLoopbackAddress() {
        return holder6.isLoopbackAddress();
    }

    @Override
    public boolean isLinkLocalAddress() {
        return holder6.isLinkLocalAddress();
    }

    static boolean isLinkLocalAddress(byte[] ipaddress) {
        return ((ipaddress[0] & 0xff) == 0xfe && (ipaddress[1] & 0xc0) == 0x80);
    }

    @Override
    public boolean isSiteLocalAddress() {
        return holder6.isSiteLocalAddress();
    }

    static boolean isSiteLocalAddress(byte[] ipaddress) {
        return ((ipaddress[0] & 0xff) == 0xfe && (ipaddress[1] & 0xc0) == 0xc0);
    }

    @Override
    public boolean isMCGlobal() {
        return holder6.isMCGlobal();
    }

    @Override
    public boolean isMCNodeLocal() {
        return holder6.isMCNodeLocal();
    }

    @Override
    public boolean isMCLinkLocal() {
        return holder6.isMCLinkLocal();
    }

    @Override
    public boolean isMCSiteLocal() {
        return holder6.isMCSiteLocal();
    }

    @Override
    public boolean isMCOrgLocal() {
        return holder6.isMCOrgLocal();
    }

    @Override
    public byte[] getAddress() {
        return holder6.ipaddress.clone();
    }

    public int getScopeId() {
        return holder6.scope_id;
    }

    public NetworkInterface getScopedInterface() {
        return holder6.scope_ifname;
    }

    @Override
    public String getHostAddress() {
        return holder6.getHostAddress();
    }

    @Override
    public int hashCode() {
        return holder6.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Inet6Address))
            return false;
        Inet6Address inetAddr = (Inet6Address) obj;
        return holder6.equals(inetAddr.holder6);
    }

    public boolean isIPv4CompatibleAddress() {
        return holder6.isIPv4CompatibleAddress();
    }

    private static final int INT16SZ = 2;

    static String numericToTextFormat(byte[] src) {
        StringBuilder sb = new StringBuilder(39);
        for (int i = 0; i < (INADDRSZ / INT16SZ); i++) {
            sb.append(Integer.toHexString(((src[i << 1] << 8) & 0xff00) | (src[(i << 1) + 1] & 0xff)));
            if (i < (INADDRSZ / INT16SZ) - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }

    private static native void init();
}