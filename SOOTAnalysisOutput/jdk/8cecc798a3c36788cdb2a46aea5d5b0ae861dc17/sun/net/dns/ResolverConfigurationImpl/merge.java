package sun.net.dns;

import java.util.List;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class ResolverConfigurationImpl extends ResolverConfiguration {

    private static Object lock = new Object();

    private final Options opts;

    private static boolean changed = false;

    private static long lastRefresh = -1;

    private static final int TIMEOUT = 120000;

    private static String os_searchlist;

    private static String os_nameservers;

    private static LinkedList<String> searchlist;

    private static LinkedList<String> nameservers;

    private LinkedList<String> stringToList(String str) {
        LinkedList<String> ll = new LinkedList<>();
        StringTokenizer st = new StringTokenizer(str, ", ");
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (!ll.contains(s)) {
                ll.add(s);
            }
        }
        return ll;
    }

    private void loadConfig() {
        assert Thread.holdsLock(lock);
        if (changed) {
            changed = false;
        } else {
            if (lastRefresh >= 0) {
                long currTime = System.currentTimeMillis();
                if ((currTime - lastRefresh) < TIMEOUT) {
                    return;
                }
            }
        }
        loadDNSconfig0();
        lastRefresh = System.currentTimeMillis();
        searchlist = stringToList(os_searchlist);
        nameservers = stringToList(os_nameservers);
        os_searchlist = null;
        os_nameservers = null;
    }

    ResolverConfigurationImpl() {
        opts = new OptionsImpl();
    }

    @SuppressWarnings("unchecked")
    public List<String> searchlist() {
        synchronized (lock) {
            loadConfig();
            return (List<String>) searchlist.clone();
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> nameservers() {
        synchronized (lock) {
            loadConfig();
            return (List<String>) nameservers.clone();
        }
    }

    public Options options() {
        return opts;
    }

    static class AddressChangeListener extends Thread {

        public void run() {
            for (; ; ) {
                if (notifyAddrChange0() != 0)
                    return;
                synchronized (lock) {
                    changed = true;
                }
            }
        }
    }

    static native void init0();

    static native void loadDNSconfig0();

    static native int notifyAddrChange0();

    static {
        jdk.internal.loader.BootLoader.loadLibrary("net");
        init0();
        AddressChangeListener thr = new AddressChangeListener();
        thr.setDaemon(true);
        thr.start();
    }
}

class OptionsImpl extends ResolverConfiguration.Options {
}
