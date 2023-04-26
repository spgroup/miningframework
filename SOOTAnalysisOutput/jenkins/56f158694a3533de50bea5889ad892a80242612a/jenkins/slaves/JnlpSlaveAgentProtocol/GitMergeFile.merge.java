package jenkins.slaves;

import hudson.Extension;
<<<<<<< MINE
import hudson.ExtensionList;
=======
>>>>>>> YOURS
import hudson.Util;
import hudson.model.Computer;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import jenkins.AgentProtocol;
import jenkins.model.Jenkins;
import jenkins.security.HMACConfidentialKey;
import org.jenkinsci.Symbol;
import org.jenkinsci.remoting.engine.JnlpClientDatabase;
import org.jenkinsci.remoting.engine.JnlpConnectionState;
import org.jenkinsci.remoting.engine.JnlpProtocol1Handler;

@Extension
@Symbol("jnlp")
public class JnlpSlaveAgentProtocol extends AgentProtocol {

    private static final Logger LOGGER = Logger.getLogger(JnlpSlaveAgentProtocol.class.getName());

    public static final HMACConfidentialKey SLAVE_SECRET = new HMACConfidentialKey(JnlpSlaveAgentProtocol.class, "secret");

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
<<<<<<< MINE
    }

    @Override
    public String getName() {
        return handler.isEnabled() ? handler.getName() : null;
=======
    }

    @Override
    public String getName() {
        return "JNLP-connect";
    }

    @Override
    public String getDisplayName() {
        return Messages.JnlpSlaveAgentProtocol_displayName();
    }

    @Override
    public void handle(Socket socket) throws IOException, InterruptedException {
        new Handler(hub.getHub(), socket).run();
>>>>>>> YOURS
    }

    @Override
    public String getDisplayName() {
        return Messages.JnlpSlaveAgentProtocol_displayName();
    }

    @Override
    public void handle(Socket socket) throws IOException, InterruptedException {
        handler.handle(socket, Collections.singletonMap(JnlpConnectionState.COOKIE_KEY, JnlpAgentReceiver.generateCookie()), ExtensionList.lookup(JnlpAgentReceiver.class));
    }

    private static final boolean OPT_IN;

<<<<<<< MINE
=======
    public static final HMACConfidentialKey SLAVE_SECRET = new HMACConfidentialKey(JnlpSlaveAgentProtocol.class, "secret");

    private static final boolean OPT_IN;

>>>>>>> YOURS
    static {
        byte hash = Util.fromHexString(Jenkins.getInstance().getLegacyInstanceId())[0];
        OPT_IN = (hash % 10) == 0;
    }
}
