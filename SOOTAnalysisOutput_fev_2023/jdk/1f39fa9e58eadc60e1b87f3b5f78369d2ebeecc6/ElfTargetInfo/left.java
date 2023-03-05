package jdk.tools.jaotc.binformat.elf;

import java.nio.ByteOrder;
import jdk.tools.jaotc.binformat.elf.Elf;
import jdk.tools.jaotc.binformat.elf.Elf.Elf64_Ehdr;

public class ElfTargetInfo {

    private static final char arch;

    private static final int endian = Elf64_Ehdr.ELFDATA2LSB;

    private static String osName;

    static {
        String archStr = System.getProperty("os.arch").toLowerCase();
        if (ByteOrder.nativeOrder() != ByteOrder.LITTLE_ENDIAN) {
            System.out.println("Only Little Endian byte order supported!");
        }
        if (archStr.equals("amd64") || archStr.equals("x86_64")) {
            arch = Elf64_Ehdr.EM_X86_64;
        } else {
            System.out.println("Unsupported architecture " + archStr);
            arch = Elf64_Ehdr.EM_NONE;
        }
        osName = System.getProperty("os.name").toLowerCase();
        if (!osName.equals("linux") && !osName.equals("sunos")) {
            System.out.println("Unsupported Operating System " + osName);
            osName = "Unknown";
        }
    }

    public static char getElfArch() {
        return arch;
    }

    public static int getElfEndian() {
        return endian;
    }

    public static String getOsName() {
        return osName;
    }
}
