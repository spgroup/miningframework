package jenkins.security;

import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

public class ConfidentialStoreRule extends ExternalResource {

    private final TemporaryFolder tmp = new TemporaryFolder();

    @Override
    protected void before() throws Throwable {
        tmp.create();
        ConfidentialStore.TEST.set(new DefaultConfidentialStore(tmp.getRoot()));
    }

    @Override
    protected void after() {
        ConfidentialStore.TEST.set(null);
        tmp.delete();
    }

    static {
        ConfidentialStore.TEST = new ThreadLocal<>();
    }
}