package sun.jvmstat.perfdata.monitor.protocol.local;

import java.io.File;
import java.io.FilenameFilter;
import jdk.internal.vm.VMSupport;

public class PerfDataFile {

    private PerfDataFile() {
    }

    public static final String tmpDirName;

    public static final String dirNamePrefix = "hsperfdata_";

    public static final String userDirNamePattern = "hsperfdata_\\S*";

    public static final String fileNamePattern = "^[0-9]+$";

    public static final String tmpFileNamePattern = "^hsperfdata_[0-9]+(_[1-2]+)?$";

    public static File getFile(int lvmid) {
        if (lvmid == 0) {
            return null;
        }
        File tmpDir = new File(tmpDirName);
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
        File newest = null;
        for (int i = 0; i < files.length; i++) {
            File f = new File(tmpDirName + files[i]);
            File candidate = null;
            if (f.exists() && f.isDirectory()) {
                String name = Integer.toString(lvmid);
                candidate = new File(f.getName(), name);
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
        return newest;
    }

    public static File getFile(String user, int lvmid) {
        if (lvmid == 0) {
            return null;
        }
        String basename = getTempDirectory(user) + Integer.toString(lvmid);
        File f = new File(basename);
        if (f.exists() && f.isFile() && f.canRead()) {
            return f;
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
            return Integer.parseInt(file.getName());
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
        return tmpDirName;
    }

    public static String getTempDirectory(String user) {
        return tmpDirName + dirNamePrefix + user + File.separator;
    }

    static {
        String tmpdir = VMSupport.getVMTemporaryDirectory();
        if (tmpdir.lastIndexOf(File.separator) != (tmpdir.length() - 1)) {
            tmpdir = tmpdir + File.separator;
        }
        tmpDirName = tmpdir;
    }
}
