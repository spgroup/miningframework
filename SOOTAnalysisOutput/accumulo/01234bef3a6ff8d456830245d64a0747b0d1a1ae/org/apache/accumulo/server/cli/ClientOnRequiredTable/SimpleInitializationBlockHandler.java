package org.apache.accumulo.server.cli;

import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.mock.MockInstance;
import org.apache.accumulo.server.client.HdfsZooInstance;

public class ClientOnRequiredTable extends org.apache.accumulo.core.cli.ClientOnRequiredTable {

    {
        setPrincipal("root");
    }

    @Override
    synchronized public Instance getInstance() {
        if (cachedInstance != null)
            return cachedInstance;
        if (mock)
            return cachedInstance = new MockInstance(instance);
        if (instance == null) {
            return cachedInstance = HdfsZooInstance.getInstance();
        }
        return cachedInstance = new ZooKeeperInstance(getClientConfiguration());
    }
}