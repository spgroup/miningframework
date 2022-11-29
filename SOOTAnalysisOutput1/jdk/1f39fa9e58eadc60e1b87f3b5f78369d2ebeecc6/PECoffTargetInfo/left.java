package jdk.tools.jaotc.binformat.pecoff;

import java.nio.ByteOrder;
import jdk.tools.jaotc.binformat.pecoff.PECoff;
import jdk.tools.jaotc.binformat.pecoff.PECoff.IMAGE_FILE_HEADER;

public class PECoffTargetInfo {

    private static final char arch;

    private static String osName;

    static {
        String archStr = System.getProperty("os.arch").toLowerCase();
        if (ByteOrder.nativeOrder() != ByteOrder.LITTLE_ENDIAN) {
            System.out.println("Only Little Endian byte order supported!");
        }
        if (archStr.equals("amd64") || archStr.equals("x86_64")) {
            arch = IMAGE_FILE_HEADER.IMAGE_FILE_MACHINE_AMD64;
        } else {
            System.out.println("Unsupported architecture " + archStr);
            arch = IMAGE_FILE_HEADER.IMAGE_FILE_MACHINE_UNKNOWN;
        }
        osName = System.getProperty("os.name").toLowerCase();
        if (!osName.contains("windows")) {
            System.out.println("Unsupported Operating System " + osName);
            osName = "Unknown";
        }
    }

    public static char getPECoffArch() {
        return arch;
    }

    public static String getOsName() {
        return osName;
    }
}
