package jenkins.security.seed;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;
import hudson.model.User;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;
import javax.annotation.Nonnull;
import java.net.URL;
import java.util.Random;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class UserSeedChangeListenerTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    {
        j.timeout = 0;
    }

    @Test
    public void onProgrammaticUserSeedChange_listenerTriggered() throws Exception {
        TestUserSeedChangeListener testListener = j.jenkins.getExtensionList(UserSeedChangeListener.class).get(TestUserSeedChangeListener.class);
        String userId = "alice";
        User alice = User.getById(userId, true);
        assertNull(testListener.lastUserIdReceived);
        UserSeedProperty userSeed = alice.getProperty(UserSeedProperty.class);
        assertNull(testListener.lastUserIdReceived);
        userSeed.renewSeed();
        assertThat(testListener.lastUserIdReceived, is(userId));
        assertThat(testListener.userWasNull, is(false));
    }

    @Test
    public void onWebCallUserSeedChange_listenerTriggered() throws Exception {
        j.jenkins.setCrumbIssuer(null);
        TestUserSeedChangeListener testListener = j.jenkins.getExtensionList(UserSeedChangeListener.class).get(TestUserSeedChangeListener.class);
        String userId = "alice";
        User alice = User.getById(userId, true);
        UserSeedProperty userSeed = alice.getProperty(UserSeedProperty.class);
        JenkinsRule.WebClient wc = j.createWebClient();
        WebRequest webRequest = new WebRequest(new URL(j.getURL() + alice.getUrl() + "/" + userSeed.getDescriptor().getDescriptorUrl() + "/renewSessionSeed"), HttpMethod.POST);
        assertNull(testListener.lastUserIdReceived);
        wc.getPage(webRequest);
        assertThat(testListener.lastUserIdReceived, is(userId));
        assertThat(testListener.userWasNull, is(false));
    }

    @TestExtension
    public static class TestUserSeedChangeListener extends UserSeedChangeListener {

        String lastUserIdReceived;

        boolean userWasNull;

        @Override
        public void onUserSeedRenewed(@Nonnull User user) {
            if (user == null) {
                userWasNull = true;
            }
            lastUserIdReceived = user.getId();
        }
    }
}
