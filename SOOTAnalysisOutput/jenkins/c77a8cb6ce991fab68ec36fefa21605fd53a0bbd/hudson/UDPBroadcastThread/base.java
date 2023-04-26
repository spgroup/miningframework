package hudson;

import jenkins.util.SystemProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Hudson;
import jenkins.model.Jenkins;
import hudson.util.OneShotEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDPBroadcastThread extends Thread {

    private final Jenkins jenkins;

    public final OneShotEvent ready = new OneShotEvent();

    private MulticastSocket mcs;

    private boolean shutdown;

    static boolean udpHandlingProblem;

    @Deprecated
    public UDPBroadcastThread(Hudson jenkins) throws IOException {
        this((Jenkins) jenkins);
    }

    public UDPBroadcastThread(Jenkins jenkins) throws IOException {
        super("Jenkins UDP " + PORT + " monitoring thread");
        this.jenkins = jenkins;
        mcs = new MulticastSocket(PORT);
    }

    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    @Override
    public void run() {
        try {
            mcs.joinGroup(MULTICAST);
            ready.signal();
            while (true) {
                byte[] buf = new byte[2048];
                DatagramPacket p = new DatagramPacket(buf, buf.length);
                mcs.receive(p);
                SocketAddress sender = p.getSocketAddress();
                TcpSlaveAgentListener tal = jenkins.getTcpSlaveAgentListener();
                StringBuilder rsp = new StringBuilder("<hudson>");
                tag(rsp, "version", Jenkins.VERSION);
                tag(rsp, "url", jenkins.getRootUrl());
                tag(rsp, "server-id", jenkins.getLegacyInstanceId());
                tag(rsp, "slave-port", tal == null ? null : tal.getPort());
                for (UDPBroadcastFragment f : UDPBroadcastFragment.all()) f.buildFragment(rsp, sender);
                rsp.append("</hudson>");
                byte[] response = rsp.toString().getBytes(StandardCharsets.UTF_8);
                mcs.send(new DatagramPacket(response, response.length, sender));
            }
        } catch (ClosedByInterruptException e) {
        } catch (SocketException e) {
            if (shutdown) {
                return;
            }
            LOGGER.log(Level.INFO, "Cannot listen to UDP port {0}, skipping: {1}", new Object[] { PORT, e });
            LOGGER.log(Level.FINE, null, e);
        } catch (IOException e) {
            if (shutdown)
                return;
            LOGGER.log(Level.WARNING, "UDP handling problem", e);
            udpHandlingProblem = true;
        }
    }

    private void tag(StringBuilder buf, String tag, Object value) {
        if (value == null)
            return;
        buf.append('<').append(tag).append('>').append(value).append("</").append(tag).append('>');
    }

    public void shutdown() {
        shutdown = true;
        mcs.close();
        interrupt();
    }

    public static final int PORT = SystemProperties.getInteger("hudson.udp", -1);

    private static final Logger LOGGER = Logger.getLogger(UDPBroadcastThread.class.getName());

    public static InetAddress MULTICAST;

    static {
        try {
            MULTICAST = InetAddress.getByAddress(new byte[] { (byte) 239, (byte) 77, (byte) 124, (byte) 213 });
        } catch (UnknownHostException e) {
            throw new Error(e);
        }
    }
}
