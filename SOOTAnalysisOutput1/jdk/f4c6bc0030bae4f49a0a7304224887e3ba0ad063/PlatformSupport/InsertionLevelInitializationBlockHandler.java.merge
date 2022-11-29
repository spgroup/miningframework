package sun.jvmstat;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.List;
import jdk.internal.vm.VMSupport;

public class PlatformSupport {

    private static final String tmpDirName;

    static {
        String tmpdir = VMSupport.getVMTemporaryDirectory();
        if (tmpdir.lastIndexOf(File.separator) != (tmpdir.length() - 1)) {
            tmpdir = tmpdir + File.separator;
        }
        tmpDirName = tmpdir;
    }

    public static PlatformSupport getInstance() {
        try {
            Class<?> c = Class.forName("sun.jvmstat.PlatformSupportImpl");
            @SuppressWarnings("unchecked")
            Constructor<PlatformSupport> cntr = (Constructor<PlatformSupport>) c.getConstructor();
            return cntr.newInstance();
        } catch (ClassNotFoundException e) {
            return new PlatformSupport();
        } catch (ReflectiveOperationException e) {
            throw new InternalError(e);
        }
    }

    PlatformSupport() {
    }

    public static String getTemporaryDirectory() {
        return tmpDirName;
    }

    public List<String> getTemporaryDirectories(int vmid) {
        return List.of(tmpDirName);
    }

    public int getLocalVmId(File file) throws NumberFormatException {
        return Integer.parseInt(file.getName());
    }

    public int getNamespaceVmId(int pid) {
        return pid;
    }
}