package sun.jvmstat.perfdata.monitor.protocol.local;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.io.FilenameFilter;
import sun.jvmstat.PlatformSupport;

public class PerfDataFile {

    private PerfDataFile() {
    }

    public static final String dirNamePrefix = "hsperfdata_";

    public static final String userDirNamePattern = "hsperfdata_\\S*";

    public static final String fileNamePattern = "^[0-9]+$";

    public static final String tmpFileNamePattern = "^hsperfdata_[0-9]+(_[1-2]+)?$";

    private static final PlatformSupport platSupport = PlatformSupport.getInstance();

    public static File getFile(int lvmid) {
        if (lvmid == 0) {
            return null;
        }
        List<String> tmpDirs = getTempDirectories(null, lvmid);
        File newest = null;
        for (String dir : tmpDirs) {
            File tmpDir = new File(dir);
            String[] files = tmpDir.list(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    if (!name.startsWith(dirNamePrefix)) {
                        return false;
                    }
                    File candidate = new File(dir, name);
                    return ((candidate.isDirectory() || candidate.isFile()) && candidate.canRead());
                }
            });
            long newestTime = 0;
            for (String file : files) {
                File f = new File(dir + file);
                File candidate = null;
                if (f.exists() && f.isDirectory()) {
                    String name = f.getAbsolutePath() + File.separator + lvmid;
                    candidate = new File(name);
                    if (!candidate.exists()) {
                        name = f.getAbsolutePath() + File.separator + platSupport.getNamespaceVmId(lvmid);
                        candidate = new File(name);
                    }
                } else if (f.exists() && f.isFile()) {
                    candidate = f;
                } else {
                    candidate = f;
                }
                if (candidate.exists() && candidate.isFile() && candidate.canRead()) {
                    long modTime = candidate.lastModified();
                    if (modTime >= newestTime) {
                        newestTime = modTime;
                        newest = candidate;
                    }
                }
            }
        }
        return newest;
    }

    public static File getFile(String user, int lvmid) {
        if (lvmid == 0) {
            return null;
        }
        List<String> tmpDirs = getTempDirectories(user, lvmid);
        String basename;
        File f;
        for (String dir : tmpDirs) {
            basename = dir + lvmid;
            f = new File(basename);
            if (f.exists() && f.isFile() && f.canRead()) {
                return f;
            }
            basename = dir + platSupport.getNamespaceVmId(lvmid);
            f = new File(basename);
            if (f.exists() && f.isFile() && f.canRead()) {
                return f;
            }
        }
        long newestTime = 0;
        File newest = null;
        for (int i = 0; i < 2; i++) {
            if (i == 0) {
                basename = getTempDirectory() + Integer.toString(lvmid);
            } else {
                basename = getTempDirectory() + Integer.toString(lvmid) + Integer.toString(i);
            }
            f = new File(basename);
            if (f.exists() && f.isFile() && f.canRead()) {
                long modTime = f.lastModified();
                if (modTime >= newestTime) {
                    newestTime = modTime;
                    newest = f;
                }
            }
        }
        return newest;
    }

    public static int getLocalVmId(File file) {
        try {
            return (platSupport.getLocalVmId(file));
        } catch (NumberFormatException e) {
        }
        String name = file.getName();
        if (name.startsWith(dirNamePrefix)) {
            int first = name.indexOf('_');
            int last = name.lastIndexOf('_');
            try {
                if (first == last) {
                    return Integer.parseInt(name.substring(first + 1));
                } else {
                    return Integer.parseInt(name.substring(first + 1, last));
                }
            } catch (NumberFormatException e) {
            }
        }
        throw new IllegalArgumentException("file name does not match pattern");
    }

    public static String getTempDirectory() {
        return PlatformSupport.getTemporaryDirectory();
    }

    public static String getTempDirectory(String user) {
        return getTempDirectory() + dirNamePrefix + user + File.separator;
    }

    public static List<String> getTempDirectories(String userName, int vmid) {
        List<String> list = platSupport.getTemporaryDirectories(vmid);
        if (userName == null) {
            return list;
        }
        List<String> nameList = list.stream().map(name -> name + dirNamePrefix + userName + File.separator).collect(Collectors.toList());
        return nameList;
    }
}
