package jenkins.slaves;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.AbortException;
import hudson.Extension;
import hudson.Util;
import hudson.model.Computer;
import hudson.remoting.Channel;
import hudson.remoting.ChannelBuilder;
import hudson.slaves.SlaveComputer;
import jenkins.AgentProtocol;
import jenkins.model.Jenkins;
import jenkins.security.ChannelConfigurator;
import org.jenkinsci.remoting.engine.JnlpServer3Handshake;
import org.jenkinsci.remoting.nio.NioChannelHub;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.util.SystemProperties;

@Extension
public class JnlpSlaveAgentProtocol3 extends AgentProtocol {

    @Inject
    NioChannelSelector hub;

    @Override
    public boolean isOptIn() {
        return !ENABLED;
    }

    @Override
    public String getName() {
        return forceEnabled != Boolean.FALSE ? "JNLP3-connect" : null;
    }

    @Override
    public String getDisplayName() {
        return Messages.JnlpSlaveAgentProtocol3_displayName();
    }

    @Override
    public void handle(Socket socket) throws IOException, InterruptedException {
        new Handler(hub.getHub(), socket).run();
    }

    static class Handler extends JnlpServer3Handshake {

        private SlaveComputer computer;

        private PrintWriter logw;

        private OutputStream log;

        public Handler(NioChannelHub hub, Socket socket) throws IOException {
            super(hub, Computer.threadPoolForRemoting, socket);
        }

        protected void run() throws IOException, InterruptedException {
            try {
                Channel channel = connect();
                computer.setChannel(channel, log, new Channel.Listener() {

                    @Override
                    public void onClosed(Channel channel, IOException cause) {
                        if (cause != null)
                            LOGGER.log(Level.WARNING, Thread.currentThread().getName() + " for + " + getNodeName() + " terminated", cause);
                        try {
                            socket.close();
                        } catch (IOException e) {
                        }
                    }
                });
            } catch (AbortException e) {
                logw.println(e.getMessage());
                logw.println("Failed to establish the connection with the agent");
                throw e;
            } catch (IOException e) {
                logw.println("Failed to establish the connection with the agent " + getNodeName());
                e.printStackTrace(logw);
                throw e;
            }
        }

        @Override
        public ChannelBuilder createChannelBuilder(String nodeName) {
            log = computer.openLogFile();
            logw = new PrintWriter(log, true);
            logw.println("JNLP agent connected from " + socket.getInetAddress());
            ChannelBuilder cb = super.createChannelBuilder(nodeName).withHeaderStream(log);
            for (ChannelConfigurator cc : ChannelConfigurator.all()) {
                cc.onChannelBuilding(cb, computer);
            }
            return cb;
        }

        @Override
        protected String getNodeSecret(String nodeName) throws Failure {
            computer = (SlaveComputer) Jenkins.getInstance().getComputer(nodeName);
            if (computer == null) {
                throw new Failure("Agent trying to register for invalid node: " + nodeName);
            }
            return computer.getJnlpMac();
        }
    }

    private static final Logger LOGGER = Logger.getLogger(JnlpSlaveAgentProtocol3.class.getName());

    @Restricted(NoExternalUse.class)
    @SuppressFBWarnings(value = "MS_SHOULD_BE_REFACTORED_TO_BE_FINAL", justification = "Part of the administrative API for System Groovy scripts.")
    public static boolean ENABLED;

    private static final Boolean forceEnabled;

    static {
        forceEnabled = SystemProperties.optBoolean(JnlpSlaveAgentProtocol3.class.getName() + ".enabled");
        if (forceEnabled != null) {
            ENABLED = forceEnabled;
        }
    }
}
