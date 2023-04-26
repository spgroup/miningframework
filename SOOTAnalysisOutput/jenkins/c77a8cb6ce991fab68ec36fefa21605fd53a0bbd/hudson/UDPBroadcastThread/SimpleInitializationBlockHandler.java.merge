package hudson;

import hudson.init.Initializer;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import java.io.IOException;
import java.util.logging.Logger;
import static hudson.init.InitMilestone.COMPLETED;

@Deprecated
@Restricted(NoExternalUse.class)
public class UDPBroadcastThread {

    public static final int PORT = SystemProperties.getInteger("hudson.udp", -1);

    private static final Logger LOGGER = Logger.getLogger(UDPBroadcastThread.class.getName());

    @Initializer(before = COMPLETED)
    public static void warn() {
        if (PORT > 0) {
            LOGGER.warning("UDP broadcast capability has been removed from Jenkins. More information: https://jenkins.io/redirect/udp-broadcast");
        }
    }
}