package jenkins.slaves;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.Util;
import hudson.model.Computer;
import jenkins.AgentProtocol;
import jenkins.model.Jenkins;
import jenkins.security.HMACConfidentialKey;
import org.jenkinsci.Symbol;
import org.jenkinsci.remoting.engine.JnlpClientDatabase;
import org.jenkinsci.remoting.engine.JnlpConnectionState;
import org.jenkinsci.remoting.engine.JnlpProtocol1Handler;
import javax.inject.Inject;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

@Extension
@Symbol("jnlp")
public class JnlpSlaveAgentProtocol extends AgentProtocol {

    private NioChannelSelector hub;

    private JnlpProtocol1Handler handler;

    @Inject
    public void setHub(NioChannelSelector hub) {
        this.hub = hub;
        this.handler = new JnlpProtocol1Handler(JnlpAgentReceiver.DATABASE, Computer.threadPoolForRemoting, hub.getHub(), true);
    }

    @Override
    public boolean isOptIn() {
        return OPT_IN;
    }

    @Override
    public String getName() {
        return handler.isEnabled() ? handler.getName() : null;
    }

    @Override
    public String getDisplayName() {
        return Messages.JnlpSlaveAgentProtocol_displayName();
    }

    @Override
    public void handle(Socket socket) throws IOException, InterruptedException {
        handler.handle(socket, Collections.singletonMap(JnlpConnectionState.COOKIE_KEY, JnlpAgentReceiver.generateCookie()), ExtensionList.lookup(JnlpAgentReceiver.class));
    }

    private static final Logger LOGGER = Logger.getLogger(JnlpSlaveAgentProtocol.class.getName());

    public static final HMACConfidentialKey SLAVE_SECRET = new HMACConfidentialKey(JnlpSlaveAgentProtocol.class, "secret");

    private static final boolean OPT_IN;

    static {
        byte hash = Util.fromHexString(Jenkins.getInstance().getLegacyInstanceId())[0];
        OPT_IN = (hash % 10) == 0;
    }
}