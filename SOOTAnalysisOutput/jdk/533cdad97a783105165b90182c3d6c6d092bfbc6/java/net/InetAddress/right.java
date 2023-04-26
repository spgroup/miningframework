package java.net;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.ServiceLoader;
import java.security.AccessController;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import sun.security.action.*;
import sun.net.InetAddressCachePolicy;
import sun.net.util.IPAddressUtil;
import sun.net.spi.nameservice.*;

public class InetAddress implements java.io.Serializable {

    static final int IPv4 = 1;

    static final int IPv6 = 2;

    static transient boolean preferIPv6Address = false;

    static class InetAddressHolder {

        InetAddressHolder() {
        }

        InetAddressHolder(String hostName, int address, int family) {
            this.hostName = hostName;
            this.address = address;
            this.family = family;
        }

        String hostName;

        String getHostName() {
            return hostName;
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

    private final transient InetAddressHolder holder;

    InetAddressHolder holder() {
        return holder;
    }

    private static List<NameService> nameServices = null;

    private transient String canonicalHostName = null;

    private static final long serialVersionUID = 3286316764910316507L;

    static {
        preferIPv6Address = java.security.AccessController.doPrivileged(new GetBooleanAction("java.net.preferIPv6Addresses")).booleanValue();
        AccessController.doPrivileged(new java.security.PrivilegedAction<Void>() {

            public Void run() {
                System.loadLibrary("net");
                return null;
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
        for (NameService nameService : nameServices) {
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
                break;
            } catch (SecurityException e) {
                host = addr.getHostAddress();
                break;
            } catch (UnknownHostException e) {
                host = addr.getHostAddress();
            }
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
        return ((hostName != null) ? hostName : "") + "/" + getHostAddress();
    }

    private static Cache addressCache = new Cache(Cache.Type.Positive);

    private static Cache negativeCache = new Cache(Cache.Type.Negative);

    private static boolean addressCacheInit = false;

    static InetAddress[] unknown_array;

    static InetAddressImpl impl;

    private static final HashMap<String, Void> lookupTable = new HashMap<>();

    static final class CacheEntry {

        CacheEntry(InetAddress[] addresses, long expiration) {
            this.addresses = addresses;
            this.expiration = expiration;
        }

        InetAddress[] addresses;

        long expiration;
    }

    static final class Cache {

        private LinkedHashMap<String, CacheEntry> cache;

        private Type type;

        enum Type {

            Positive, Negative
        }

        public Cache(Type type) {
            this.type = type;
            cache = new LinkedHashMap<String, CacheEntry>();
        }

        private int getPolicy() {
            if (type == Type.Positive) {
                return InetAddressCachePolicy.get();
            } else {
                return InetAddressCachePolicy.getNegative();
            }
        }

        public Cache put(String host, InetAddress[] addresses) {
            int policy = getPolicy();
            if (policy == InetAddressCachePolicy.NEVER) {
                return this;
            }
            if (policy != InetAddressCachePolicy.FOREVER) {
                LinkedList<String> expired = new LinkedList<>();
                long now = System.currentTimeMillis();
                for (String key : cache.keySet()) {
                    CacheEntry entry = cache.get(key);
                    if (entry.expiration >= 0 && entry.expiration < now) {
                        expired.add(key);
                    } else {
                        break;
                    }
                }
                for (String key : expired) {
                    cache.remove(key);
                }
            }
            long expiration;
            if (policy == InetAddressCachePolicy.FOREVER) {
                expiration = -1;
            } else {
                expiration = System.currentTimeMillis() + (policy * 1000);
            }
            CacheEntry entry = new CacheEntry(addresses, expiration);
            cache.put(host, entry);
            return this;
        }

        public CacheEntry get(String host) {
            int policy = getPolicy();
            if (policy == InetAddressCachePolicy.NEVER) {
                return null;
            }
            CacheEntry entry = cache.get(host);
            if (entry != null && policy != InetAddressCachePolicy.FOREVER) {
                if (entry.expiration >= 0 && entry.expiration < System.currentTimeMillis()) {
                    cache.remove(host);
                    entry = null;
                }
            }
            return entry;
        }
    }

    private static void cacheInitIfNeeded() {
        assert Thread.holdsLock(addressCache);
        if (addressCacheInit) {
            return;
        }
        unknown_array = new InetAddress[1];
        unknown_array[0] = impl.anyLocalAddress();
        addressCache.put(impl.anyLocalAddress().getHostName(), unknown_array);
        addressCacheInit = true;
    }

    private static void cacheAddresses(String hostname, InetAddress[] addresses, boolean success) {
        hostname = hostname.toLowerCase();
        synchronized (addressCache) {
            cacheInitIfNeeded();
            if (success) {
                addressCache.put(hostname, addresses);
            } else {
                negativeCache.put(hostname, addresses);
            }
        }
    }

    private static InetAddress[] getCachedAddresses(String hostname) {
        hostname = hostname.toLowerCase();
        synchronized (addressCache) {
            cacheInitIfNeeded();
            CacheEntry entry = addressCache.get(hostname);
            if (entry == null) {
                entry = negativeCache.get(hostname);
            }
            if (entry != null) {
                return entry.addresses;
            }
        }
        return null;
    }

    private static NameService createNSProvider(String provider) {
        if (provider == null)
            return null;
        NameService nameService = null;
        if (provider.equals("default")) {
            nameService = new NameService() {

                public InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException {
                    return impl.lookupAllHostAddr(host);
                }

                public String getHostByAddr(byte[] addr) throws UnknownHostException {
                    return impl.getHostByAddr(addr);
                }
            };
        } else {
            final String providerName = provider;
            try {
                nameService = java.security.AccessController.doPrivileged(new java.security.PrivilegedExceptionAction<NameService>() {

                    public NameService run() {
                        Iterator<NameServiceDescriptor> itr = ServiceLoader.load(NameServiceDescriptor.class).iterator();
                        while (itr.hasNext()) {
                            NameServiceDescriptor nsd = itr.next();
                            if (providerName.equalsIgnoreCase(nsd.getType() + "," + nsd.getProviderName())) {
                                try {
                                    return nsd.createNameService();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.err.println("Cannot create name service:" + providerName + ": " + e);
                                }
                            }
                        }
                        return null;
                    }
                });
            } catch (java.security.PrivilegedActionException e) {
            }
        }
        return nameService;
    }

    static {
        impl = InetAddressImplFactory.create();
        String provider = null;
        ;
        String propPrefix = "sun.net.spi.nameservice.provider.";
        int n = 1;
        nameServices = new ArrayList<NameService>();
        provider = AccessController.doPrivileged(new GetPropertyAction(propPrefix + n));
        while (provider != null) {
            NameService ns = createNSProvider(provider);
            if (ns != null)
                nameServices.add(ns);
            n++;
            provider = AccessController.doPrivileged(new GetPropertyAction(propPrefix + n));
        }
        if (nameServices.size() == 0) {
            NameService ns = createNSProvider("default");
            nameServices.add(ns);
        }
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
                if ((pos = host.indexOf("%")) != -1) {
                    numericZone = checkNumericZone(host);
                    if (numericZone == -1) {
                        ifname = host.substring(pos + 1);
                    }
                }
                addr = IPAddressUtil.textToNumericFormatV6(host);
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
        return getAllByName0(host, reqAddr, true);
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
        return getAllByName0(host, null, check);
    }

    private static InetAddress[] getAllByName0(String host, InetAddress reqAddr, boolean check) throws UnknownHostException {
        if (check) {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkConnect(host, -1);
            }
        }
        InetAddress[] addresses = getCachedAddresses(host);
        if (addresses == null) {
            addresses = getAddressesFromNameService(host, reqAddr);
        }
        if (addresses == unknown_array)
            throw new UnknownHostException(host);
        return addresses.clone();
    }

    private static InetAddress[] getAddressesFromNameService(String host, InetAddress reqAddr) throws UnknownHostException {
        InetAddress[] addresses = null;
        boolean success = false;
        UnknownHostException ex = null;
        if ((addresses = checkLookupTable(host)) == null) {
            try {
                for (NameService nameService : nameServices) {
                    try {
                        addresses = nameService.lookupAllHostAddr(host);
                        success = true;
                        break;
                    } catch (UnknownHostException uhe) {
                        if (host.equalsIgnoreCase("localhost")) {
                            InetAddress[] local = new InetAddress[] { impl.loopbackAddress() };
                            addresses = local;
                            success = true;
                            break;
                        } else {
                            addresses = unknown_array;
                            success = false;
                            ex = uhe;
                        }
                    }
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
                cacheAddresses(host, addresses, success);
                if (!success && ex != null)
                    throw ex;
            } finally {
                updateLookupTable(host);
            }
        }
        return addresses;
    }

    private static InetAddress[] checkLookupTable(String host) {
        synchronized (lookupTable) {
            if (lookupTable.containsKey(host) == false) {
                lookupTable.put(host, null);
                return null;
            }
            while (lookupTable.containsKey(host)) {
                try {
                    lookupTable.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        InetAddress[] addresses = getCachedAddresses(host);
        if (addresses == null) {
            synchronized (lookupTable) {
                lookupTable.put(host, null);
                return null;
            }
        }
        return addresses;
    }

    private static void updateLookupTable(String host) {
        synchronized (lookupTable) {
            lookupTable.remove(host);
            lookupTable.notifyAll();
        }
    }

    public static InetAddress getByAddress(byte[] addr) throws UnknownHostException {
        return getByAddress(null, addr);
    }

    private static InetAddress cachedLocalHost = null;

    private static long cacheTime = 0;

    private static final long maxCacheTime = 5000L;

    private static final Object cacheLock = new Object();

    public static InetAddress getLocalHost() throws UnknownHostException {
        SecurityManager security = System.getSecurityManager();
        try {
            String local = impl.getLocalHostName();
            if (security != null) {
                security.checkConnect(local, -1);
            }
            if (local.equals("localhost")) {
                return impl.loopbackAddress();
            }
            InetAddress ret = null;
            synchronized (cacheLock) {
                long now = System.currentTimeMillis();
                if (cachedLocalHost != null) {
                    if ((now - cacheTime) < maxCacheTime)
                        ret = cachedLocalHost;
                    else
                        cachedLocalHost = null;
                }
                if (ret == null) {
                    InetAddress[] localAddrs;
                    try {
                        localAddrs = InetAddress.getAddressesFromNameService(local, null);
                    } catch (UnknownHostException uhe) {
                        UnknownHostException uhe2 = new UnknownHostException(local + ": " + uhe.getMessage());
                        uhe2.initCause(uhe);
                        throw uhe2;
                    }
                    cachedLocalHost = localAddrs[0];
                    cacheTime = now;
                    ret = localAddrs[0];
                }
            }
            return ret;
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
        String prefix = AccessController.doPrivileged(new GetPropertyAction("impl.prefix", ""));
        try {
            impl = Class.forName("java.net." + prefix + implName).newInstance();
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: java.net." + prefix + implName + ":\ncheck impl.prefix property " + "in your properties file.");
        } catch (InstantiationException e) {
            System.err.println("Could not instantiate: java.net." + prefix + implName + ":\ncheck impl.prefix property " + "in your properties file.");
        } catch (IllegalAccessException e) {
            System.err.println("Cannot access class: java.net." + prefix + implName + ":\ncheck impl.prefix property " + "in your properties file.");
        }
        if (impl == null) {
            try {
                impl = Class.forName(implName).newInstance();
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

    private static final sun.misc.Unsafe UNSAFE;

    static {
        try {
            sun.misc.Unsafe unsafe = sun.misc.Unsafe.getUnsafe();
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
        s.flush();
    }
}

class InetAddressImplFactory {

    static InetAddressImpl create() {
        return InetAddress.loadImpl(isIPv6Supported() ? "Inet6AddressImpl" : "Inet4AddressImpl");
    }

    static native boolean isIPv6Supported();
}
