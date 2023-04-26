package jenkins.slaves;

import hudson.AbortException;
import hudson.Extension;
import hudson.model.Computer;
import hudson.remoting.Channel;
import hudson.remoting.Channel.Listener;
import hudson.remoting.ChannelBuilder;
import hudson.remoting.Engine;
import hudson.slaves.SlaveComputer;
import jenkins.AgentProtocol;
import jenkins.model.Jenkins;
import jenkins.security.ChannelConfigurator;
import jenkins.security.HMACConfidentialKey;
import org.jenkinsci.Symbol;
import org.jenkinsci.remoting.engine.JnlpServerHandshake;
import org.jenkinsci.remoting.nio.NioChannelHub;
import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
@Symbol("jnlp")
public class JnlpSlaveAgentProtocol extends AgentProtocol {

    @Inject
    NioChannelSelector hub;

    @Override
    public String getName() {
        return "JNLP-connect";
    }

    @Override
    public void handle(Socket socket) throws IOException, InterruptedException {
        new Handler(hub.getHub(), socket).run();
    }

    protected static class Handler extends JnlpServerHandshake {

        @Deprecated
        public Handler(Socket socket) throws IOException {
            this(null, socket);
        }

        public Handler(NioChannelHub hub, Socket socket) throws IOException {
            super(hub, Computer.threadPoolForRemoting, socket);
        }

        protected void run() throws IOException, InterruptedException {
            final String secret = in.readUTF();
            final String nodeName = in.readUTF();
            if (!SLAVE_SECRET.mac(nodeName).equals(secret)) {
                error("Unauthorized access");
                return;
            }
            SlaveComputer computer = (SlaveComputer) Jenkins.getInstance().getComputer(nodeName);
            if (computer == null) {
                error("No such agent: " + nodeName);
                return;
            }
            if (computer.getChannel() != null) {
                error(nodeName + " is already connected to this master. Rejecting this connection.");
                return;
            }
            out.println(Engine.GREETING_SUCCESS);
            jnlpConnect(computer);
        }

        protected Channel jnlpConnect(SlaveComputer computer) throws InterruptedException, IOException {
            final String nodeName = computer.getName();
            final OutputStream log = computer.openLogFile();
            PrintWriter logw = new PrintWriter(log, true);
            logw.println("JNLP agent connected from " + socket.getInetAddress());
            try {
                ChannelBuilder cb = createChannelBuilder(nodeName);
                for (ChannelConfigurator cc : ChannelConfigurator.all()) {
                    cc.onChannelBuilding(cb, computer);
                }
                computer.setChannel(cb.withHeaderStream(log).build(socket), log, new Listener() {

                    @Override
                    public void onClosed(Channel channel, IOException cause) {
                        if (cause != null)
                            LOGGER.log(Level.WARNING, Thread.currentThread().getName() + " for " + nodeName + " terminated", cause);
                        try {
                            socket.close();
                        } catch (IOException e) {
                        }
                    }
                });
                return computer.getChannel();
            } catch (AbortException e) {
                logw.println(e.getMessage());
                logw.println("Failed to establish the connection with the agent");
                throw e;
            } catch (IOException e) {
                logw.println("Failed to establish the connection with the agent " + nodeName);
                e.printStackTrace(logw);
                throw e;
            }
        }
    }

    private static final Logger LOGGER = Logger.getLogger(JnlpSlaveAgentProtocol.class.getName());

    public static final HMACConfidentialKey SLAVE_SECRET = new HMACConfidentialKey(JnlpSlaveAgentProtocol.class, "secret");
}
