package org.apache.accumulo.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.apache.accumulo.core.conf.AccumuloConfiguration;
import org.apache.accumulo.core.conf.Property;
import org.apache.accumulo.core.util.Pair;
import org.apache.accumulo.core.volume.Volume;
import org.apache.accumulo.core.volume.VolumeConfiguration;
import org.apache.accumulo.fate.zookeeper.ZooUtil;
import org.apache.accumulo.server.fs.VolumeUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

public class ServerConstants {

    public static final String VERSION_DIR = "version";

    public static final String INSTANCE_ID_DIR = "instance_id";

    public static final Integer WIRE_VERSION = 3;

    public static final int SHORTEN_RFILE_KEYS = 8;

    public static final int MOVE_TO_REPLICATION_TABLE = 7;

    public static final int DATA_VERSION = SHORTEN_RFILE_KEYS;

    public static final int MOVE_TO_ROOT_TABLE = 6;

    public static final int MOVE_DELETE_MARKERS = 5;

    public static final int LOGGING_TO_HDFS = 4;

    public static final BitSet CAN_UPGRADE = new BitSet();

    static {
        for (int i : new int[] { DATA_VERSION, MOVE_TO_REPLICATION_TABLE, MOVE_TO_ROOT_TABLE }) {
            CAN_UPGRADE.set(i);
        }
    }

    public static final BitSet NEEDS_UPGRADE = new BitSet();

    static {
        NEEDS_UPGRADE.xor(CAN_UPGRADE);
        NEEDS_UPGRADE.clear(DATA_VERSION);
    }

    private static String[] baseUris = null;

    private static List<Pair<Path, Path>> replacementsList = null;

    public static String[] getBaseUris(ServerContext context) {
        return getBaseUris(context.getConfiguration(), context.getHadoopConf());
    }

    public static synchronized String[] getBaseUris(AccumuloConfiguration conf, Configuration hadoopConf) {
        if (baseUris == null) {
            baseUris = checkBaseUris(conf, hadoopConf, VolumeConfiguration.getVolumeUris(conf, hadoopConf), false);
        }
        return baseUris;
    }

    public static String[] checkBaseUris(AccumuloConfiguration conf, Configuration hadoopConf, String[] configuredBaseDirs, boolean ignore) {
        String firstDir = null;
        String firstIid = null;
        Integer firstVersion = null;
        ArrayList<String> baseDirsList = new ArrayList<>();
        for (String baseDir : configuredBaseDirs) {
            Path path = new Path(baseDir, INSTANCE_ID_DIR);
            String currentIid;
            int currentVersion;
            try {
                currentIid = ZooUtil.getInstanceIDFromHdfs(path, conf, hadoopConf);
                Path vpath = new Path(baseDir, VERSION_DIR);
                currentVersion = ServerUtil.getAccumuloPersistentVersion(vpath.getFileSystem(hadoopConf), vpath);
            } catch (Exception e) {
                if (ignore)
                    continue;
                else
                    throw new IllegalArgumentException("Accumulo volume " + path + " not initialized", e);
            }
            if (firstIid == null) {
                firstIid = currentIid;
                firstDir = baseDir;
                firstVersion = currentVersion;
            } else if (!currentIid.equals(firstIid)) {
                throw new IllegalArgumentException("Configuration " + Property.INSTANCE_VOLUMES.getKey() + " contains paths that have different instance ids " + baseDir + " has " + currentIid + " and " + firstDir + " has " + firstIid);
            } else if (currentVersion != firstVersion) {
                throw new IllegalArgumentException("Configuration " + Property.INSTANCE_VOLUMES.getKey() + " contains paths that have different versions " + baseDir + " has " + currentVersion + " and " + firstDir + " has " + firstVersion);
            }
            baseDirsList.add(baseDir);
        }
        if (baseDirsList.size() == 0) {
            throw new RuntimeException("None of the configured paths are initialized.");
        }
        return baseDirsList.toArray(new String[baseDirsList.size()]);
    }

    public static final String TABLE_DIR = "tables";

    public static final String RECOVERY_DIR = "recovery";

    public static final String WAL_DIR = "wal";

    public static String[] getTablesDirs(ServerContext context) {
        return VolumeConfiguration.prefix(getBaseUris(context), TABLE_DIR);
    }

    public static String[] getRecoveryDirs(ServerContext context) {
        return VolumeConfiguration.prefix(getBaseUris(context), RECOVERY_DIR);
    }

    public static Path getInstanceIdLocation(Volume v) {
        return v.prefixChild(INSTANCE_ID_DIR);
    }

    public static Path getDataVersionLocation(Volume v) {
        return v.prefixChild(VERSION_DIR);
    }

    public static synchronized List<Pair<Path, Path>> getVolumeReplacements(AccumuloConfiguration conf, Configuration hadoopConf) {
        if (replacementsList == null) {
            String replacements = conf.get(Property.INSTANCE_VOLUMES_REPLACEMENTS);
            replacements = replacements.trim();
            if (replacements.isEmpty())
                return Collections.emptyList();
            String[] pairs = replacements.split(",");
            List<Pair<Path, Path>> ret = new ArrayList<>();
            for (String pair : pairs) {
                String[] uris = pair.split("\\s+");
                if (uris.length != 2)
                    throw new IllegalArgumentException(Property.INSTANCE_VOLUMES_REPLACEMENTS.getKey() + " contains malformed pair " + pair);
                Path p1, p2;
                try {
                    p1 = new Path(new URI(VolumeUtil.removeTrailingSlash(uris[0].trim())));
                    if (p1.toUri().getScheme() == null)
                        throw new IllegalArgumentException(Property.INSTANCE_VOLUMES_REPLACEMENTS.getKey() + " contains " + uris[0] + " which is not fully qualified");
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException(Property.INSTANCE_VOLUMES_REPLACEMENTS.getKey() + " contains " + uris[0] + " which has a syntax error", e);
                }
                try {
                    p2 = new Path(new URI(VolumeUtil.removeTrailingSlash(uris[1].trim())));
                    if (p2.toUri().getScheme() == null)
                        throw new IllegalArgumentException(Property.INSTANCE_VOLUMES_REPLACEMENTS.getKey() + " contains " + uris[1] + " which is not fully qualified");
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException(Property.INSTANCE_VOLUMES_REPLACEMENTS.getKey() + " contains " + uris[1] + " which has a syntax error", e);
                }
                ret.add(new Pair<>(p1, p2));
            }
            HashSet<Path> baseDirs = new HashSet<>();
            for (String baseDir : getBaseUris(conf, hadoopConf)) {
                baseDirs.add(new Path(baseDir));
            }
            for (Pair<Path, Path> pair : ret) if (!baseDirs.contains(pair.getSecond()))
                throw new IllegalArgumentException(Property.INSTANCE_VOLUMES_REPLACEMENTS.getKey() + " contains " + pair.getSecond() + " which is not a configured volume");
            replacementsList = ret;
        }
        return replacementsList;
    }
}
