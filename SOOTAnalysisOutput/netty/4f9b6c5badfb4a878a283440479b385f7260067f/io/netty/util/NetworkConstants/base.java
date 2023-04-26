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
                    logger.error("Failed to resolve localhost", e2);
                }
            }
        }
        LOCALHOST = localhost;
        NetworkInterface loopbackIf;
        try {
            loopbackIf = NetworkInterface.getByInetAddress(LOCALHOST);
        } catch (SocketException e) {
            loopbackIf = null;
        }
        if (loopbackIf == null) {
            try {
                for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements(); ) {
                    NetworkInterface nif = e.nextElement();
                    if (nif.isLoopback()) {
                        loopbackIf = nif;
                        break;
                    }
                }
            } catch (SocketException e) {
                logger.error("Failed to enumerate network interfaces", e);
            }
        }
        LOOPBACK_IF = loopbackIf;
    }

    private NetworkConstants() {
    }
}
