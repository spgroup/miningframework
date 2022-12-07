package org.apache.solr.hadoop.hack;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileContext;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.metrics2.lib.DefaultMetricsSystem;
import org.apache.hadoop.service.AbstractService;
import org.apache.hadoop.service.CompositeService;
import org.apache.hadoop.util.Shell;
import org.apache.hadoop.util.Shell.ShellCommandExecutor;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.event.Dispatcher;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.exceptions.YarnRuntimeException;
import org.apache.hadoop.yarn.factories.RecordFactory;
import org.apache.hadoop.yarn.factory.providers.RecordFactoryProvider;
import org.apache.hadoop.yarn.server.api.ResourceTracker;
import org.apache.hadoop.yarn.server.api.protocolrecords.NodeHeartbeatRequest;
import org.apache.hadoop.yarn.server.api.protocolrecords.NodeHeartbeatResponse;
import org.apache.hadoop.yarn.server.api.protocolrecords.RegisterNodeManagerRequest;
import org.apache.hadoop.yarn.server.api.protocolrecords.RegisterNodeManagerResponse;
import org.apache.hadoop.yarn.server.nodemanager.Context;
import org.apache.hadoop.yarn.server.nodemanager.NodeHealthCheckerService;
import org.apache.hadoop.yarn.server.nodemanager.NodeManager;
import org.apache.hadoop.yarn.server.nodemanager.NodeStatusUpdater;
import org.apache.hadoop.yarn.server.nodemanager.NodeStatusUpdaterImpl;
import org.apache.hadoop.yarn.server.resourcemanager.ResourceManager;
import org.apache.hadoop.yarn.server.resourcemanager.ResourceTrackerService;

public class MiniYARNCluster extends CompositeService {

    private static final Log LOG = LogFactory.getLog(MiniYARNCluster.class);

    static {
        DefaultMetricsSystem.setMiniClusterMode(true);
    }

    private NodeManager[] nodeManagers;

    private ResourceManager resourceManager;

    private ResourceManagerWrapper resourceManagerWrapper;

    private File testWorkDir;

    private int numLocalDirs;

    private int numLogDirs;

    public MiniYARNCluster(String testName, int noOfNodeManagers, int numLocalDirs, int numLogDirs, File testWorkDir) {
        super(testName.replace("$", ""));
        this.numLocalDirs = numLocalDirs;
        this.numLogDirs = numLogDirs;
        String testSubDir = testName.replace("$", "");
        File targetWorkDir = new File(testWorkDir, testSubDir);
        try {
            FileContext.getLocalFSFileContext().delete(new Path(targetWorkDir.getAbsolutePath()), true);
        } catch (Exception e) {
            LOG.warn("COULD NOT CLEANUP", e);
            throw new YarnRuntimeException("could not cleanup test dir: " + e, e);
        }
        if (Shell.WINDOWS) {
            String targetPath = targetWorkDir.getAbsolutePath();
            File link = new File(System.getProperty("java.io.tmpdir"), String.valueOf(System.nanoTime()));
            String linkPath = link.getAbsolutePath();
            try {
                FileContext.getLocalFSFileContext().delete(new Path(linkPath), true);
            } catch (IOException e) {
                throw new YarnRuntimeException("could not cleanup symlink: " + linkPath, e);
            }
            targetWorkDir.mkdirs();
            ShellCommandExecutor shexec = new ShellCommandExecutor(Shell.getSymlinkCommand(targetPath, linkPath));
            try {
                shexec.execute();
            } catch (IOException e) {
                throw new YarnRuntimeException(String.format(Locale.ENGLISH, "failed to create symlink from %s to %s, shell output: %s", linkPath, targetPath, shexec.getOutput()), e);
            }
            this.testWorkDir = link;
        } else {
            this.testWorkDir = targetWorkDir;
        }
        resourceManagerWrapper = new ResourceManagerWrapper();
        addService(resourceManagerWrapper);
        nodeManagers = new CustomNodeManager[noOfNodeManagers];
        for (int index = 0; index < noOfNodeManagers; index++) {
            addService(new NodeManagerWrapper(index));
            nodeManagers[index] = new CustomNodeManager();
        }
    }

    @Override
    public void serviceInit(Configuration conf) throws Exception {
        super.serviceInit(conf instanceof YarnConfiguration ? conf : new YarnConfiguration(conf));
    }

    public File getTestWorkDir() {
        return testWorkDir;
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    public NodeManager getNodeManager(int i) {
        return this.nodeManagers[i];
    }

    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
    }

    private class ResourceManagerWrapper extends AbstractService {

        public ResourceManagerWrapper() {
            super(ResourceManagerWrapper.class.getName());
        }

        @Override
        public synchronized void serviceStart() throws Exception {
            try {
                getConfig().setBoolean(YarnConfiguration.IS_MINI_YARN_CLUSTER, true);
                if (!getConfig().getBoolean(YarnConfiguration.YARN_MINICLUSTER_FIXED_PORTS, YarnConfiguration.DEFAULT_YARN_MINICLUSTER_FIXED_PORTS)) {
                    String hostname = MiniYARNCluster.getHostname();
                    getConfig().set(YarnConfiguration.RM_ADDRESS, hostname + ":0");
                    getConfig().set(YarnConfiguration.RM_ADMIN_ADDRESS, hostname + ":0");
                    getConfig().set(YarnConfiguration.RM_SCHEDULER_ADDRESS, hostname + ":0");
                    getConfig().set(YarnConfiguration.RM_RESOURCE_TRACKER_ADDRESS, hostname + ":0");
                    getConfig().set(YarnConfiguration.RM_WEBAPP_ADDRESS, hostname + ":0");
                }
                resourceManager = new ResourceManager() {

                    @Override
                    protected void doSecureLogin() throws IOException {
                    }
                };
                resourceManager.init(getConfig());
                new Thread() {

                    public void run() {
                        resourceManager.start();
                    }
                }.start();
                int waitCount = 0;
                while (resourceManager.getServiceState() == STATE.INITED && waitCount++ < 60) {
                    LOG.info("Waiting for RM to start...");
                    Thread.sleep(1500);
                }
                if (resourceManager.getServiceState() != STATE.STARTED) {
                    throw new IOException("ResourceManager failed to start. Final state is " + resourceManager.getServiceState());
                }
                super.serviceStart();
            } catch (Throwable t) {
                throw new YarnRuntimeException(t);
            }
            LOG.info("MiniYARN ResourceManager address: " + getConfig().get(YarnConfiguration.RM_ADDRESS));
            LOG.info("MiniYARN ResourceManager web address: " + getConfig().get(YarnConfiguration.RM_WEBAPP_ADDRESS));
        }

        @Override
        public synchronized void serviceStop() throws Exception {
            if (resourceManager != null) {
                resourceManager.stop();
            }
            super.serviceStop();
            if (Shell.WINDOWS) {
                String testWorkDirPath = testWorkDir.getAbsolutePath();
                try {
                    FileContext.getLocalFSFileContext().delete(new Path(testWorkDirPath), true);
                } catch (IOException e) {
                    LOG.warn("could not cleanup symlink: " + testWorkDir.getAbsolutePath());
                }
            }
        }
    }

    private class NodeManagerWrapper extends AbstractService {

        int index = 0;

        public NodeManagerWrapper(int i) {
            super(NodeManagerWrapper.class.getName() + "_" + i);
            index = i;
        }

        public synchronized void serviceInit(Configuration conf) throws Exception {
            Configuration config = new YarnConfiguration(conf);
            super.serviceInit(config);
        }

        private String prepareDirs(String dirType, int numDirs) {
            File[] dirs = new File[numDirs];
            String dirsString = "";
            for (int i = 0; i < numDirs; i++) {
                dirs[i] = new File(testWorkDir, MiniYARNCluster.this.getName() + "-" + dirType + "Dir-nm-" + index + "_" + i);
                dirs[i].mkdirs();
                LOG.info("Created " + dirType + "Dir in " + dirs[i].getAbsolutePath());
                String delimiter = (i > 0) ? "," : "";
                dirsString = dirsString.concat(delimiter + dirs[i].getAbsolutePath());
            }
            return dirsString;
        }

        public synchronized void serviceStart() throws Exception {
            try {
                String localDirsString = prepareDirs("local", numLocalDirs);
                getConfig().set(YarnConfiguration.NM_LOCAL_DIRS, localDirsString);
                String logDirsString = prepareDirs("log", numLogDirs);
                getConfig().set(YarnConfiguration.NM_LOG_DIRS, logDirsString);
                File remoteLogDir = new File(testWorkDir, MiniYARNCluster.this.getName() + "-remoteLogDir-nm-" + index);
                remoteLogDir.mkdir();
                getConfig().set(YarnConfiguration.NM_REMOTE_APP_LOG_DIR, remoteLogDir.getAbsolutePath());
                getConfig().setInt(YarnConfiguration.NM_PMEM_MB, 4 * 1024);
                getConfig().set(YarnConfiguration.NM_ADDRESS, MiniYARNCluster.getHostname() + ":0");
                getConfig().set(YarnConfiguration.NM_LOCALIZER_ADDRESS, MiniYARNCluster.getHostname() + ":0");
                getConfig().set(YarnConfiguration.NM_WEBAPP_ADDRESS, MiniYARNCluster.getHostname() + ":0");
                if (!getConfig().getBoolean(YarnConfiguration.YARN_MINICLUSTER_CONTROL_RESOURCE_MONITORING, YarnConfiguration.DEFAULT_YARN_MINICLUSTER_CONTROL_RESOURCE_MONITORING)) {
                    getConfig().setBoolean(YarnConfiguration.NM_PMEM_CHECK_ENABLED, false);
                    getConfig().setBoolean(YarnConfiguration.NM_VMEM_CHECK_ENABLED, false);
                }
                LOG.info("Starting NM: " + index);
                nodeManagers[index].init(getConfig());
                new Thread() {

                    public void run() {
                        nodeManagers[index].start();
                    }
                }.start();
                int waitCount = 0;
                while (nodeManagers[index].getServiceState() == STATE.INITED && waitCount++ < 60) {
                    LOG.info("Waiting for NM " + index + " to start...");
                    Thread.sleep(1000);
                }
                if (nodeManagers[index].getServiceState() != STATE.STARTED) {
                    throw new IOException("NodeManager " + index + " failed to start");
                }
                super.serviceStart();
            } catch (Throwable t) {
                throw new YarnRuntimeException(t);
            }
        }

        @Override
        public synchronized void serviceStop() throws Exception {
            if (nodeManagers[index] != null) {
                nodeManagers[index].stop();
            }
            super.serviceStop();
        }
    }

    private class CustomNodeManager extends NodeManager {

        @Override
        protected void doSecureLogin() throws IOException {
        }

        @Override
        protected NodeStatusUpdater createNodeStatusUpdater(Context context, Dispatcher dispatcher, NodeHealthCheckerService healthChecker) {
            return new NodeStatusUpdaterImpl(context, dispatcher, healthChecker, metrics) {

                @Override
                protected ResourceTracker getRMClient() {
                    final ResourceTrackerService rt = resourceManager.getResourceTrackerService();
                    final RecordFactory recordFactory = RecordFactoryProvider.getRecordFactory(null);
                    return new ResourceTracker() {

                        @Override
                        public NodeHeartbeatResponse nodeHeartbeat(NodeHeartbeatRequest request) throws YarnException, IOException {
                            NodeHeartbeatResponse response = recordFactory.newRecordInstance(NodeHeartbeatResponse.class);
                            try {
                                response = rt.nodeHeartbeat(request);
                            } catch (YarnException e) {
                                LOG.info("Exception in heartbeat from node " + request.getNodeStatus().getNodeId(), e);
                                throw e;
                            }
                            return response;
                        }

                        @Override
                        public RegisterNodeManagerResponse registerNodeManager(RegisterNodeManagerRequest request) throws YarnException, IOException {
                            RegisterNodeManagerResponse response = recordFactory.newRecordInstance(RegisterNodeManagerResponse.class);
                            try {
                                response = rt.registerNodeManager(request);
                            } catch (YarnException e) {
                                LOG.info("Exception in node registration from " + request.getNodeId().toString(), e);
                                throw e;
                            }
                            return response;
                        }
                    };
                }

                @Override
                protected void stopRMProxy() {
                    return;
                }
            };
        }
    }
}
