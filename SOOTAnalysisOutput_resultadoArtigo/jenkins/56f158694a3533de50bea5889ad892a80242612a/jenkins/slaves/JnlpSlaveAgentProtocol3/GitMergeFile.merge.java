package jenkins.slaves;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
<<<<<<< MINE
=======
import hudson.AbortException;
>>>>>>> YOURS
import hudson.Extension;
import hudson.ExtensionList;
import hudson.Util;
import hudson.model.Computer;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import jenkins.AgentProtocol;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.jenkinsci.remoting.engine.JnlpClientDatabase;
import org.jenkinsci.remoting.engine.JnlpConnectionState;
import org.jenkinsci.remoting.engine.JnlpProtocol3Handler;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Deprecated
@Extension
public class JnlpSlaveAgentProtocol3 extends AgentProtocol {

    private NioChannelSelector hub;

    private JnlpProtocol3Handler handler;

    @Inject
    public void setHub(NioChannelSelector hub) {
        this.hub = hub;
        this.handler = new JnlpProtocol3Handler(JnlpAgentReceiver.DATABASE, Computer.threadPoolForRemoting, hub.getHub(), true);
    }

    @Override
    public boolean isOptIn() {
        return !ENABLED;
<<<<<<< MINE
=======
    }

    @Override
    public String getName() {
        return forceEnabled != Boolean.FALSE ? "JNLP3-connect" : null;
    }

    @Override
    public String getDisplayName() {
        return Messages.JnlpSlaveAgentProtocol3_displayName();
>>>>>>> YOURS
    }

    @Override
    public String getName() {
        return forceEnabled != Boolean.FALSE ? handler.getName() : null;
    }

    @Override
    public String getDisplayName() {
        return Messages.JnlpSlaveAgentProtocol3_displayName();
    }

    @Override
    public void handle(Socket socket) throws IOException, InterruptedException {
        handler.handle(socket, Collections.singletonMap(JnlpConnectionState.COOKIE_KEY, JnlpAgentReceiver.generateCookie()), ExtensionList.lookup(JnlpAgentReceiver.class));
    }

    @Restricted(NoExternalUse.class)
    @SuppressFBWarnings(value = "MS_SHOULD_BE_REFACTORED_TO_BE_FINAL", justification = "Part of the administrative API for System Groovy scripts.")
    public static boolean ENABLED;

    private static final Boolean forceEnabled;

    static {
        forceEnabled = SystemProperties.optBoolean(JnlpSlaveAgentProtocol3.class.getName() + ".enabled");
        if (forceEnabled != null) {
            ENABLED = forceEnabled;
<<<<<<< MINE
        } else {
            byte hash = Util.fromHexString(Jenkins.getActiveInstance().getLegacyInstanceId())[0];
            ENABLED = (hash % 10) == 0;
=======
>>>>>>> YOURS
        }
    }
}
