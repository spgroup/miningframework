package jdk.net;

import java.net.SocketException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import jdk.net.ExtendedSocketOptions.PlatformSocketOptions;

class SolarisSocketOptions extends PlatformSocketOptions {

    public SolarisSocketOptions() {
    }

    @Override
    native int setFlowOption(int fd, int priority, long bandwidth) throws SocketException;

    @Override
    native int getFlowOption(int fd, SocketFlow f) throws SocketException;

    @Override
    native boolean flowSupported();

    private static native void init();

    static {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

            public Void run() {
                System.loadLibrary("extnet");
                return null;
            }
        });
        init();
    }
}
