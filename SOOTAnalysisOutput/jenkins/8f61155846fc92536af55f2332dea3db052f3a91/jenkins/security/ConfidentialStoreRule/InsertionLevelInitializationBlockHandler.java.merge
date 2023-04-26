package jenkins.security;

import org.junit.rules.ExternalResource;

public class ConfidentialStoreRule extends ExternalResource {

    @Override
    protected void before() throws Throwable {
        ConfidentialStore.Mock.INSTANCE.clear();
    }
}