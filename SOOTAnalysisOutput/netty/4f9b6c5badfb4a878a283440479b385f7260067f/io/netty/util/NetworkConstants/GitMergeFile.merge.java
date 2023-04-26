package io.netty.util;

import io.netty.logging.InternalLogger;
import io.netty.logging.InternalLoggerFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public final class NetworkConstants {

    public static final InetAddress LOCALHOST;

    public static final NetworkInterface LOOPBACK_IF;

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NetworkConstants.class);

    static {
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            try {
                localhost = InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 });
            } catch (UnknownHostException e1) {
                try {
                    localhost = InetAddress.getByAddress(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 });
                } catch (UnknownHostException e2) {
                    logger.error("Failed to resolve localhost - Incorrect network configuration?", e2);
                }
            }
        }
        LOCALHOST = localhost;
        NetworkInterface loopbackInterface;
        try {
            loopbackInterface = NetworkInterface.getByInetAddress(LOCALHOST);
        } catch (SocketException e) {
            loopbackInterface = null;
        }
        if (loopbackInterface == null) {
            try {
                for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements(); ) {
                    NetworkInterface networkInterface = interfaces.nextElement();
                    if (networkInterface.isLoopback()) {
                        loopbackInterface = networkInterface;
                        break;
                    }
                }
            } catch (SocketException e) {
                logger.error("Failed to enumerate network interfaces", e);
            }
        }
        LOOPBACK_IF = loopbackInterface;
    }

    private NetworkConstants() {
    }
}
