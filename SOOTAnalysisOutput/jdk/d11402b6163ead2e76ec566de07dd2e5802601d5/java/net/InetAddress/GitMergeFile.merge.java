package java.net;

import java.util.NavigableSet;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.security.AccessController;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.lang.annotation.Native;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import jdk.internal.misc.JavaNetInetAddressAccess;
import jdk.internal.misc.SharedSecrets;
import sun.security.action.*;
import sun.net.InetAddressCachePolicy;
import sun.net.util.IPAddressUtil;

public class InetAddress implements java.io.Serializable {

    @Native
    static final int PREFER_IPV4_VALUE = 0;

    @Native
    static final int PREFER_IPV6_VALUE = 1;

    @Native
    static final int PREFER_SYSTEM_VALUE = 2;

    @Native
    static final int IPv4 = 1;

    @Native
    static final int IPv6 = 2;

    static transient final int preferIPv6Address;

    static class InetAddressHolder {

        String originalHostName;

        InetAddressHolder() {
        }

        InetAddressHolder(String hostName, int address, int family) {
            this.originalHostName = hostName;
            this.hostName = hostName;
            this.address = address;
            this.family = family;
        }

        void init(String hostName, int family) {
            this.originalHostName = hostName;
            this.hostName = hostName;
            if (family != -1) {
                this.family = family;
            }
        }

        String hostName;

        String getHostName() {
            return hostName;
        }

        String getOriginalHostName() {
            return originalHostName;
        }

        int address;

        int getAddress() {
            return address;
        }

        int family;

        int getFamily() {
            return family;
        }
    }

    final transient InetAddressHolder holder;

    InetAddressHolder holder() {
        return holder;
    }

    private static transient NameService nameService = null;

    private transient String canonicalHostName = null;

    private static final long serialVersionUID = 3286316764910316507L;

    static {
        String str = java.security.AccessController.doPrivileged(new GetPropertyAction("java.net.preferIPv6Addresses"));
        if (str == null) {
            preferIPv6Address = PREFER_IPV4_VALUE;
        } else if (str.equalsIgnoreCase("true")) {
            preferIPv6Address = PREFER_IPV6_VALUE;
        } else if (str.equalsIgnoreCase("false")) {
            preferIPv6Address = PREFER_IPV4_VALUE;
        } else if (str.equalsIgnoreCase("system")) {
            preferIPv6Address = PREFER_SYSTEM_VALUE;
        } else {
            preferIPv6Address = PREFER_IPV4_VALUE;
        }
        AccessController.doPrivileged(new java.security.PrivilegedAction<>() {

            public Void run() {
                System.loadLibrary("net");
                return null;
            }
        });
        SharedSecrets.setJavaNetInetAddressAccess(new JavaNetInetAddressAccess() {

            public String getOriginalHostName(InetAddress ia) {
                return ia.holder.getOriginalHostName();
            }

            public InetAddress getByName(String hostName, InetAddress hostAddress) throws UnknownHostException {
                return InetAddress.getByName(hostName, hostAddress);
            }
        });
        init();
    }

    InetAddress() {
        holder = new InetAddressHolder();
    }

    private Object readResolve() throws ObjectStreamException {
        return new Inet4Address(holder().getHostName(), holder().getAddress());
    }

    public boolean isMulticastAddress() {
        return false;
    }

    public boolean isAnyLocalAddress() {
        return false;
    }

    public boolean isLoopbackAddress() {
        return false;
    }

    public boolean isLinkLocalAddress() {
        return false;
    }

    public boolean isSiteLocalAddress() {
        return false;
    }

    public boolean isMCGlobal() {
        return false;
    }

    public boolean isMCNodeLocal() {
        return false;
    }

    public boolean isMCLinkLocal() {
        return false;
    }

    public boolean isMCSiteLocal() {
        return false;
    }

    public boolean isMCOrgLocal() {
        return false;
    }

    public boolean isReachable(int timeout) throws IOException {
        return isReachable(null, 0, timeout);
    }

    public boolean isReachable(NetworkInterface netif, int ttl, int timeout) throws IOException {
        if (ttl < 0)
            throw new IllegalArgumentException("ttl can't be negative");
        if (timeout < 0)
            throw new IllegalArgumentException("timeout can't be negative");
        return impl.isReachable(this, timeout, netif, ttl);
    }

    public String getHostName() {
        return getHostName(true);
    }

    String getHostName(boolean check) {
        if (holder().getHostName() == null) {
            holder().hostName = InetAddress.getHostFromNameService(this, check);
        }
        return holder().getHostName();
    }

    public String getCanonicalHostName() {
        if (canonicalHostName == null) {
            canonicalHostName = InetAddress.getHostFromNameService(this, true);
        }
        return canonicalHostName;
    }

    private static String getHostFromNameService(InetAddress addr, boolean check) {
        String host = null;
        try {
            host = nameService.getHostByAddr(addr.getAddress());
            if (check) {
                SecurityManager sec = System.getSecurityManager();
                if (sec != null) {
                    sec.checkConnect(host, -1);
                }
            }
            InetAddress[] arr = InetAddress.getAllByName0(host, check);
            boolean ok = false;
            if (arr != null) {
                for (int i = 0; !ok && i < arr.length; i++) {
                    ok = addr.equals(arr[i]);
                }
            }
            if (!ok) {
                host = addr.getHostAddress();
                return host;
            }
        } catch (SecurityException e) {
            host = addr.getHostAddress();
        } catch (UnknownHostException e) {
            host = addr.getHostAddress();
        }
        return host;
    }

    public byte[] getAddress() {
        return null;
    }

    public String getHostAddress() {
        return null;
    }

    public int hashCode() {
        return -1;
    }

    public boolean equals(Object obj) {
        return false;
    }

    public String toString() {
        String hostName = holder().getHostName();
        return Objects.toString(hostName, "") + "/" + getHostAddress();
    }

    private static final ConcurrentMap<String, Addresses> cache = new ConcurrentHashMap<>();

    private static final NavigableSet<CachedAddresses> expirySet = new ConcurrentSkipListSet<>();

    private interface Addresses {

        InetAddress[] get() throws UnknownHostException;
    }

    private static final class CachedAddresses implements Addresses, Comparable<CachedAddresses> {

        private static final AtomicLong seq = new AtomicLong();

        final String host;

        final InetAddress[] inetAddresses;

        final long expiryTime;

        final long id = seq.incrementAndGet();

        CachedAddresses(String host, InetAddress[] inetAddresses, long expiryTime) {
            this.host = host;
            this.inetAddresses = inetAddresses;
            this.expiryTime = expiryTime;
        }

        @Override
        public InetAddress[] get() throws UnknownHostException {
            if (inetAddresses == null) {
                throw new UnknownHostException(host);
            }
            return inetAddresses;
        }

        @Override
        public int compareTo(CachedAddresses other) {
            long diff = this.expiryTime - other.expiryTime;
            if (diff < 0L)
                return -1;
            if (diff > 0L)
                return 1;
            return Long.compare(this.id, other.id);
        }
    }

    private static final class NameServiceAddresses implements Addresses {

        private final String host;

        private final InetAddress reqAddr;

        NameServiceAddresses(String host, InetAddress reqAddr) {
            this.host = host;
            this.reqAddr = reqAddr;
        }

        @Override
        public InetAddress[] get() throws UnknownHostException {
            Addresses addresses;
            synchronized (this) {
                addresses = cache.putIfAbsent(host, this);
                if (addresses == null) {
                    addresses = this;
                }
                if (addresses == this) {
                    InetAddress[] inetAddresses;
                    UnknownHostException ex;
                    int cachePolicy;
                    try {
                        inetAddresses = getAddressesFromNameService(host, reqAddr);
                        ex = null;
                        cachePolicy = InetAddressCachePolicy.get();
                    } catch (UnknownHostException uhe) {
                        inetAddresses = null;
                        ex = uhe;
                        cachePolicy = InetAddressCachePolicy.getNegative();
                    }
                    if (cachePolicy == InetAddressCachePolicy.NEVER) {
                        cache.remove(host, this);
                    } else {
                        CachedAddresses cachedAddresses = new CachedAddresses(host, inetAddresses, cachePolicy == InetAddressCachePolicy.FOREVER ? 0L : System.nanoTime() + 1000_000_000L * cachePolicy);
                        if (cache.replace(host, this, cachedAddresses) && cachePolicy != InetAddressCachePolicy.FOREVER) {
                            expirySet.add(cachedAddresses);
                        }
                    }
                    if (inetAddresses == null) {
                        throw ex == null ? new UnknownHostException(host) : ex;
                    }
                    return inetAddresses;
                }
            }
            return addresses.get();
        }
    }

    private interface NameService {

        InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException;

        String getHostByAddr(byte[] addr) throws UnknownHostException;
    }

    private static final class PlatformNameService implements NameService {

        public InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException {
            return impl.lookupAllHostAddr(host);
        }

        public String getHostByAddr(byte[] addr) throws UnknownHostException {
            return impl.getHostByAddr(addr);
        }
    }

    private static final class HostsFileNameService implements NameService {

        private final String hostsFile;

        public HostsFileNameService(String hostsFileName) {
            this.hostsFile = hostsFileName;
        }

        private String addrToString(byte[] addr) {
            String stringifiedAddress = null;
            if (addr.length == Inet4Address.INADDRSZ) {
                stringifiedAddress = Inet4Address.numericToTextFormat(addr);
            } else {
                byte[] newAddr = IPAddressUtil.convertFromIPv4MappedAddress(addr);
                if (newAddr != null) {
                    stringifiedAddress = Inet4Address.numericToTextFormat(addr);
                } else {
                    stringifiedAddress = Inet6Address.numericToTextFormat(addr);
                }
            }
            return stringifiedAddress;
        }

        @Override
        public String getHostByAddr(byte[] addr) throws UnknownHostException {
            String hostEntry;
            String host = null;
            String addrString = addrToString(addr);
            try (Scanner hostsFileScanner = new Scanner(new File(hostsFile), "UTF-8")) {
                while (hostsFileScanner.hasNextLine()) {
                    hostEntry = hostsFileScanner.nextLine();
                    if (!hostEntry.startsWith("#")) {
                        hostEntry = removeComments(hostEntry);
                        if (hostEntry.contains(addrString)) {
                            host = extractHost(hostEntry, addrString);
                            if (host != null) {
                                break;
                            }
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                throw new UnknownHostException("Unable to resolve address " + addrString + " as hosts file " + hostsFile + " not found ");
            }
            if ((host == null) || (host.equals("")) || (host.equals(" "))) {
                throw new UnknownHostException("Requested address " + addrString + " resolves to an invalid entry in hosts file " + hostsFile);
            }
            return host;
        }

        public InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException {
            String hostEntry;
            String addrStr = null;
            InetAddress[] res = null;
            byte[] addr = new byte[4];
            ArrayList<InetAddress> inetAddresses = null;
            try (Scanner hostsFileScanner = new Scanner(new File(hostsFile), "UTF-8")) {
                while (hostsFileScanner.hasNextLine()) {
                    hostEntry = hostsFileScanner.nextLine();
                    if (!hostEntry.startsWith("#")) {
                        hostEntry = removeComments(hostEntry);
                        if (hostEntry.contains(host)) {
                            addrStr = extractHostAddr(hostEntry, host);
                            if ((addrStr != null) && (!addrStr.equals(""))) {
                                addr = createAddressByteArray(addrStr);
                                if (inetAddresses == null) {
                                    inetAddresses = new ArrayList<>(1);
                                }
                                if (addr != null) {
                                    inetAddresses.add(InetAddress.getByAddress(host, addr));
                                }
                            }
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                throw new UnknownHostException("Unable to resolve host " + host + " as hosts file " + hostsFile + " not found ");
            }
            if (inetAddresses != null) {
                res = inetAddresses.toArray(new InetAddress[inetAddresses.size()]);
            } else {
                throw new UnknownHostException("Unable to resolve host " + host + " in hosts file " + hostsFile);
            }
            return res;
        }

        private String removeComments(String hostsEntry) {
            String filteredEntry = hostsEntry;
            int hashIndex;
            if ((hashIndex = hostsEntry.indexOf("#")) != -1) {
                filteredEntry = hostsEntry.substring(0, hashIndex);
            }
            return filteredEntry;
        }

        private byte[] createAddressByteArray(String addrStr) {
            byte[] addrArray;
            addrArray = IPAddressUtil.textToNumericFormatV4(addrStr);
            if (addrArray == null) {
                addrArray = IPAddressUtil.textToNumericFormatV6(addrStr);
            }
            return addrArray;
        }

        private String extractHostAddr(String hostEntry, String host) {
            String[] mapping = hostEntry.split("\\s+");
            String hostAddr = null;
            if (mapping.length >= 2) {
                for (int i = 1; i < mapping.length; i++) {
                    if (mapping[i].equalsIgnoreCase(host)) {
                        hostAddr = mapping[0];
                    }
                }
            }
            return hostAddr;
        }

        private String extractHost(String hostEntry, String addrString) {
            String[] mapping = hostEntry.split("\\s+");
            String host = null;
            if (mapping.length >= 2) {
                if (mapping[0].equalsIgnoreCase(addrString)) {
                    host = mapping[1];
                }
            }
            return host;
        }
    }

    static final InetAddressImpl impl;

    static {
        impl = InetAddressImplFactory.create();
        nameService = createNameService();
    }

    private static NameService createNameService() {
        String hostsFileName = GetPropertyAction.privilegedGetProperty("jdk.net.hosts.file");
        NameService theNameService;
        if (hostsFileName != null) {
            theNameService = new HostsFileNameService(hostsFileName);
        } else {
            theNameService = new PlatformNameService();
        }
        return theNameService;
    }

    public static InetAddress getByAddress(String host, byte[] addr) throws UnknownHostException {
        if (host != null && host.length() > 0 && host.charAt(0) == '[') {
            if (host.charAt(host.length() - 1) == ']') {
                host = host.substring(1, host.length() - 1);
            }
        }
        if (addr != null) {
            if (addr.length == Inet4Address.INADDRSZ) {
                return new Inet4Address(host, addr);
            } else if (addr.length == Inet6Address.INADDRSZ) {
                byte[] newAddr = IPAddressUtil.convertFromIPv4MappedAddress(addr);
                if (newAddr != null) {
                    return new Inet4Address(host, newAddr);
                } else {
                    return new Inet6Address(host, addr);
                }
            }
        }
        throw new UnknownHostException("addr is of illegal length");
    }

    public static InetAddress getByName(String host) throws UnknownHostException {
        return InetAddress.getAllByName(host)[0];
    }

    private static InetAddress getByName(String host, InetAddress reqAddr) throws UnknownHostException {
        return InetAddress.getAllByName(host, reqAddr)[0];
    }

    public static InetAddress[] getAllByName(String host) throws UnknownHostException {
        return getAllByName(host, null);
    }

    private static InetAddress[] getAllByName(String host, InetAddress reqAddr) throws UnknownHostException {
        if (host == null || host.length() == 0) {
            InetAddress[] ret = new InetAddress[1];
            ret[0] = impl.loopbackAddress();
            return ret;
        }
        boolean ipv6Expected = false;
        if (host.charAt(0) == '[') {
            if (host.length() > 2 && host.charAt(host.length() - 1) == ']') {
                host = host.substring(1, host.length() - 1);
                ipv6Expected = true;
            } else {
                throw new UnknownHostException(host + ": invalid IPv6 address");
            }
        }
        if (Character.digit(host.charAt(0), 16) != -1 || (host.charAt(0) == ':')) {
            byte[] addr = null;
            int numericZone = -1;
            String ifname = null;
            addr = IPAddressUtil.textToNumericFormatV4(host);
            if (addr == null) {
                int pos;
                if ((pos = host.indexOf('%')) != -1) {
                    numericZone = checkNumericZone(host);
                    if (numericZone == -1) {
                        ifname = host.substring(pos + 1);
                    }
                }
                if ((addr = IPAddressUtil.textToNumericFormatV6(host)) == null && host.contains(":")) {
                    throw new UnknownHostException(host + ": invalid IPv6 address");
                }
            } else if (ipv6Expected) {
                throw new UnknownHostException("[" + host + "]");
            }
            InetAddress[] ret = new InetAddress[1];
            if (addr != null) {
                if (addr.length == Inet4Address.INADDRSZ) {
                    ret[0] = new Inet4Address(null, addr);
                } else {
                    if (ifname != null) {
                        ret[0] = new Inet6Address(null, addr, ifname);
                    } else {
                        ret[0] = new Inet6Address(null, addr, numericZone);
                    }
                }
                return ret;
            }
        } else if (ipv6Expected) {
            throw new UnknownHostException("[" + host + "]");
        }
        return getAllByName0(host, reqAddr, true, true);
    }

    public static InetAddress getLoopbackAddress() {
        return impl.loopbackAddress();
    }

    private static int checkNumericZone(String s) throws UnknownHostException {
        int percent = s.indexOf('%');
        int slen = s.length();
        int digit, zone = 0;
        if (percent == -1) {
            return -1;
        }
        for (int i = percent + 1; i < slen; i++) {
            char c = s.charAt(i);
            if (c == ']') {
                if (i == percent + 1) {
                    return -1;
                }
                break;
            }
            if ((digit = Character.digit(c, 10)) < 0) {
                return -1;
            }
            zone = (zone * 10) + digit;
        }
        return zone;
    }

    private static InetAddress[] getAllByName0(String host) throws UnknownHostException {
        return getAllByName0(host, true);
    }

    static InetAddress[] getAllByName0(String host, boolean check) throws UnknownHostException {
        return getAllByName0(host, null, check, true);
    }

    private static InetAddress[] getAllByName0(String host, InetAddress reqAddr, boolean check, boolean useCache) throws UnknownHostException {
        if (check) {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkConnect(host, -1);
            }
        }
        long now = System.nanoTime();
        for (CachedAddresses caddrs : expirySet) {
            if ((caddrs.expiryTime - now) < 0L) {
                if (expirySet.remove(caddrs)) {
                    cache.remove(caddrs.host, caddrs);
                }
            } else {
                break;
            }
        }
        Addresses addrs;
        if (useCache) {
            addrs = cache.get(host);
        } else {
            addrs = cache.remove(host);
            if (addrs != null) {
                if (addrs instanceof CachedAddresses) {
                    expirySet.remove(addrs);
                }
                addrs = null;
            }
        }
        if (addrs == null) {
            Addresses oldAddrs = cache.putIfAbsent(host, addrs = new NameServiceAddresses(host, reqAddr));
            if (oldAddrs != null) {
                addrs = oldAddrs;
            }
        }
        return addrs.get().clone();
    }

    static InetAddress[] getAddressesFromNameService(String host, InetAddress reqAddr) throws UnknownHostException {
        InetAddress[] addresses = null;
        UnknownHostException ex = null;
        try {
            addresses = nameService.lookupAllHostAddr(host);
        } catch (UnknownHostException uhe) {
            if (host.equalsIgnoreCase("localhost")) {
                addresses = new InetAddress[] { impl.loopbackAddress() };
            } else {
                ex = uhe;
            }
        }
        if (addresses == null) {
            throw ex == null ? new UnknownHostException(host) : ex;
        }
        if (reqAddr != null && addresses.length > 1 && !addresses[0].equals(reqAddr)) {
            int i = 1;
            for (; i < addresses.length; i++) {
                if (addresses[i].equals(reqAddr)) {
                    break;
                }
            }
            if (i < addresses.length) {
                InetAddress tmp, tmp2 = reqAddr;
                for (int j = 0; j < i; j++) {
                    tmp = addresses[j];
                    addresses[j] = tmp2;
                    tmp2 = tmp;
                }
                addresses[i] = tmp2;
            }
        }
        return addresses;
    }

    public static InetAddress getByAddress(byte[] addr) throws UnknownHostException {
        return getByAddress(null, addr);
    }

    private static final class CachedLocalHost {

        final String host;

        final InetAddress addr;

        final long expiryTime = System.nanoTime() + 5000_000_000L;

        CachedLocalHost(String host, InetAddress addr) {
            this.host = host;
            this.addr = addr;
        }
    }

    private static volatile CachedLocalHost cachedLocalHost;

    public static InetAddress getLocalHost() throws UnknownHostException {
        SecurityManager security = System.getSecurityManager();
        try {
            CachedLocalHost clh = cachedLocalHost;
            if (clh != null && (clh.expiryTime - System.nanoTime()) >= 0L) {
                if (security != null) {
                    security.checkConnect(clh.host, -1);
                }
                return clh.addr;
            }
            String local = impl.getLocalHostName();
            if (security != null) {
                security.checkConnect(local, -1);
            }
            InetAddress localAddr;
            if (local.equals("localhost")) {
                localAddr = impl.loopbackAddress();
            } else {
                try {
                    localAddr = getAllByName0(local, null, false, false)[0];
                } catch (UnknownHostException uhe) {
                    UnknownHostException uhe2 = new UnknownHostException(local + ": " + uhe.getMessage());
                    uhe2.initCause(uhe);
                    throw uhe2;
                }
            }
            cachedLocalHost = new CachedLocalHost(local, localAddr);
            return localAddr;
        } catch (java.lang.SecurityException e) {
            return impl.loopbackAddress();
        }
    }

    private static native void init();

    static InetAddress anyLocalAddress() {
        return impl.anyLocalAddress();
    }

    static InetAddressImpl loadImpl(String implName) {
        Object impl = null;
        String prefix = GetPropertyAction.privilegedGetProperty("impl.prefix", "");
        try {
            @SuppressWarnings("deprecation")
            Object tmp = Class.forName("java.net." + prefix + implName).newInstance();
            impl = tmp;
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: java.net." + prefix + implName + ":\ncheck impl.prefix property " + "in your properties file.");
        } catch (InstantiationException e) {
            System.err.println("Could not instantiate: java.net." + prefix + implName + ":\ncheck impl.prefix property " + "in your properties file.");
        } catch (IllegalAccessException e) {
            System.err.println("Cannot access class: java.net." + prefix + implName + ":\ncheck impl.prefix property " + "in your properties file.");
        }
        if (impl == null) {
            try {
                @SuppressWarnings("deprecation")
                Object tmp = Class.forName(implName).newInstance();
                impl = tmp;
            } catch (Exception e) {
                throw new Error("System property impl.prefix incorrect");
            }
        }
        return (InetAddressImpl) impl;
    }

    private void readObjectNoData(ObjectInputStream s) throws IOException, ClassNotFoundException {
        if (getClass().getClassLoader() != null) {
            throw new SecurityException("invalid address type");
        }
    }

    private static final long FIELDS_OFFSET;

    private static final jdk.internal.misc.Unsafe UNSAFE;

    static {
        try {
            jdk.internal.misc.Unsafe unsafe = jdk.internal.misc.Unsafe.getUnsafe();
            FIELDS_OFFSET = unsafe.objectFieldOffset(InetAddress.class.getDeclaredField("holder"));
            UNSAFE = unsafe;
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        if (getClass().getClassLoader() != null) {
            throw new SecurityException("invalid address type");
        }
        GetField gf = s.readFields();
        String host = (String) gf.get("hostName", null);
        int address = gf.get("address", 0);
        int family = gf.get("family", 0);
        InetAddressHolder h = new InetAddressHolder(host, address, family);
        UNSAFE.putObject(this, FIELDS_OFFSET, h);
    }

    private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField("hostName", String.class), new ObjectStreamField("address", int.class), new ObjectStreamField("family", int.class) };

    private void writeObject(ObjectOutputStream s) throws IOException {
        if (getClass().getClassLoader() != null) {
            throw new SecurityException("invalid address type");
        }
        PutField pf = s.putFields();
        pf.put("hostName", holder().getHostName());
        pf.put("address", holder().getAddress());
        pf.put("family", holder().getFamily());
        s.writeFields();
    }
}

class InetAddressImplFactory {

    static InetAddressImpl create() {
        return InetAddress.loadImpl(isIPv6Supported() ? "Inet6AddressImpl" : "Inet4AddressImpl");
    }

    static native boolean isIPv6Supported();
}
