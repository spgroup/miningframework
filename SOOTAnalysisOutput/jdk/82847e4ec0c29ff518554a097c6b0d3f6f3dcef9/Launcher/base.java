import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Launcher {

    static {
        System.loadLibrary("Launcher");
    }

    private static native void launch0(String[] cmdarray, int fd) throws IOException;

    private static void launch(String className, String[] options, String[] args, int fd) throws IOException {
        int optsLen = (options == null) ? 0 : options.length;
        int argsLen = (args == null) ? 0 : args.length;
        int len = 1 + optsLen + 1 + argsLen;
        String[] cmdarray = new String[len];
        int pos = 0;
        cmdarray[pos++] = Util.javaCommand();
        if (options != null) {
            for (String opt : options) {
                cmdarray[pos++] = opt;
            }
        }
        cmdarray[pos++] = className;
        if (args != null) {
            for (String arg : args) {
                cmdarray[pos++] = arg;
            }
        }
        launch0(cmdarray, fd);
    }

    public static SocketChannel launchWithSocketChannel(String className, String[] options, String[] args) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(0));
        InetSocketAddress isa = new InetSocketAddress(InetAddress.getLocalHost(), ssc.socket().getLocalPort());
        SocketChannel sc1 = SocketChannel.open(isa);
        SocketChannel sc2 = ssc.accept();
        launch(className, options, args, Util.getFD(sc2));
        sc2.close();
        ssc.close();
        return sc1;
    }

    public static SocketChannel launchWithSocketChannel(String className, String[] args) throws IOException {
        return launchWithSocketChannel(className, null, args);
    }

    public static SocketChannel launchWithSocketChannel(String className) throws IOException {
        return launchWithSocketChannel(className, null);
    }

    public static SocketChannel launchWithServerSocketChannel(String className, String[] options, String[] args) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(0));
        int port = ssc.socket().getLocalPort();
        launch(className, options, args, Util.getFD(ssc));
        ssc.close();
        InetSocketAddress isa = new InetSocketAddress(InetAddress.getLocalHost(), port);
        return SocketChannel.open(isa);
    }

    public static SocketChannel launchWithServerSocketChannel(String className, String[] args) throws IOException {
        return launchWithServerSocketChannel(className, null, args);
    }

    public static SocketChannel launchWithServerSocketChannel(String className) throws IOException {
        return launchWithServerSocketChannel(className, null);
    }

    public static DatagramChannel launchWithDatagramChannel(String className, String[] options, String[] args) throws IOException {
        DatagramChannel dc = DatagramChannel.open();
        dc.socket().bind(new InetSocketAddress(0));
        int port = dc.socket().getLocalPort();
        launch(className, options, args, Util.getFD(dc));
        dc.close();
        dc = DatagramChannel.open();
        InetAddress address = InetAddress.getLocalHost();
        if (address.isLoopbackAddress()) {
            address = InetAddress.getLoopbackAddress();
        }
        InetSocketAddress isa = new InetSocketAddress(address, port);
        dc.connect(isa);
        return dc;
    }

    public static DatagramChannel launchWithDatagramChannel(String className, String[] args) throws IOException {
        return launchWithDatagramChannel(className, null, args);
    }

    public static DatagramChannel launchWithDatagramChannel(String className) throws IOException {
        return launchWithDatagramChannel(className, null);
    }
}
