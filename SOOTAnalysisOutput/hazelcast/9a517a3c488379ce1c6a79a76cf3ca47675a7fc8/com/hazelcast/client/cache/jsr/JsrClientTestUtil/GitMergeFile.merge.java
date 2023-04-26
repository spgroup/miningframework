package com.hazelcast.client.cache.jsr;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.instance.HazelcastInstanceFactory;
import static com.hazelcast.cache.jsr.JsrTestUtil.clearCachingProviderRegistry;
import static com.hazelcast.cache.jsr.JsrTestUtil.clearSystemProperties;
import static com.hazelcast.cache.jsr.JsrTestUtil.setSystemProperties;
import static com.hazelcast.test.HazelcastTestSupport.assertThatIsNoParallelTest;

public final class JsrClientTestUtil {

<<<<<<< MINE
    private JsrClientTestUtil() {
    }

    public static void setup() {
        assertThatIsNoParallelTest();
        setSystemProperties("client");
    }

=======
    public static void setup() {
        assertThatIsNoParallelTest();
        setSystemProperties("client");
    }

>>>>>>> YOURS
    public static void setupWithHazelcastInstance() {
        assertThatIsNoParallelTest();
        setSystemProperties("client");
        Config config = new Config();
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        Hazelcast.newHazelcastInstance(config);
    }

    public static void cleanup() {
        clearSystemProperties();
        clearCachingProviderRegistry();
        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
        HazelcastInstanceFactory.terminateAll();
    }
}
