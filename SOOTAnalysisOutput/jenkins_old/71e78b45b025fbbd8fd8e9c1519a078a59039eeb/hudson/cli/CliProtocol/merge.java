package hudson.cli;

import hudson.Extension;
import hudson.Util;
import hudson.model.Computer;
import hudson.remoting.Channel;
import hudson.remoting.Channel.Mode;
import hudson.remoting.ChannelBuilder;
import jenkins.AgentProtocol;
import jenkins.model.Jenkins;
import jenkins.slaves.NioChannelSelector;
import org.jenkinsci.Symbol;
import org.jenkinsci.remoting.nio.NioChannelHub;
import javax.inject.Inject;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

@Deprecated
@Extension
@Symbol("cli")
public class CliProtocol extends AgentProtocol {

    @Inject
    NioChannelSelector nio;

    @Override
    public boolean isOptIn() {
        return true;
    }

    @Override
    public String getName() {
        return jenkins.CLI.get().isEnabled() ? "CLI-connect" : null;
    }

    @Override
    public boolean isDeprecated() {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Jenkins CLI Protocol/1 (deprecated, unencrypted)";
    }

    @Override
    public void handle(Socket socket) throws IOException, InterruptedException {
        new Handler(nio.getHub(), socket).run();
    }

    protected static class Handler {

        protected final NioChannelHub hub;

        protected final Socket socket;

        @Deprecated
        public Handler(Socket socket) {
            this(null, socket);
        }

        public Handler(NioChannelHub hub, Socket socket) {
            this.hub = hub;
            this.socket = socket;
        }

        public void run() throws IOException, InterruptedException {
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), true);
            out.println("Welcome");
            runCli(new Connection(socket));
        }

        protected void runCli(Connection c) throws IOException, InterruptedException {
            ChannelBuilder cb;
            String name = "CLI channel from " + socket.getInetAddress();
            cb = new ChannelBuilder(name, Computer.threadPoolForRemoting);
            Channel channel = cb.withMode(Mode.BINARY).withRestricted(true).withBaseLoader(Jenkins.getActiveInstance().pluginManager.uberClassLoader).build(new BufferedInputStream(c.in), new BufferedOutputStream(c.out));
            channel.setProperty(CliEntryPoint.class.getName(), new CliManagerImpl(channel));
            channel.join();
        }
    }
}
