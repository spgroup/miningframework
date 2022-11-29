package jdk.tools.jaotc.binformat.macho;

import java.nio.ByteOrder;
import jdk.tools.jaotc.binformat.macho.MachO;
import jdk.tools.jaotc.binformat.macho.MachO.mach_header_64;

public class MachOTargetInfo {

    private static final int arch;

    private static final int subarch;

    private static final int endian = mach_header_64.CPU_SUBTYPE_LITTLE_ENDIAN;

    private static final String osName;

    static {
        String archStr = System.getProperty("os.arch").toLowerCase();
        if (ByteOrder.nativeOrder() != ByteOrder.LITTLE_ENDIAN) {
            System.out.println("Only Little Endian byte order supported!");
        }
        if (archStr.equals("amd64") || archStr.equals("x86_64")) {
            arch = mach_header_64.CPU_TYPE_X86_64;
            subarch = mach_header_64.CPU_SUBTYPE_I386_ALL;
        } else {
            System.out.println("Unsupported architecture " + archStr);
            arch = mach_header_64.CPU_TYPE_ANY;
            subarch = 0;
        }
        osName = System.getProperty("os.name").toLowerCase();
    }

    public static int getMachOArch() {
        return arch;
    }

    public static int getMachOSubArch() {
        return subarch;
    }

    public static int getMachOEndian() {
        return endian;
    }

    public static String getOsName() {
        return osName;
    }
}
