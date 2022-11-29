package jdk.tools.jaotc.jnilibelf;

import java.nio.ByteOrder;
import jdk.tools.jaotc.jnilibelf.JNILibELFAPI.ELF;

public class JNIELFTargetInfo {

    private static final int elfClass;

    private static final int arch;

    private static final int endian;

    private static final String osName;

    static {
        String archStr = System.getProperty("os.arch").toLowerCase();
        String datamodelStr = System.getProperty("sun.arch.data.model");
        if (datamodelStr.equals("32")) {
            elfClass = ELF.ELFCLASS32;
        } else if (datamodelStr.equals("64")) {
            elfClass = ELF.ELFCLASS64;
        } else {
            System.out.println("Failed to discover ELF class!");
            elfClass = ELF.ELFCLASSNONE;
        }
        ByteOrder bo = ByteOrder.nativeOrder();
        if (bo == ByteOrder.LITTLE_ENDIAN) {
            endian = ELF.ELFDATA2LSB;
        } else if (bo == ByteOrder.BIG_ENDIAN) {
            endian = ELF.ELFDATA2MSB;
        } else {
            System.out.println("Failed to discover endian-ness!");
            endian = ELF.ELFDATANONE;
        }
        if (archStr.equals("x86")) {
            arch = ELF.EM_386;
        } else if (archStr.equals("amd64") || archStr.equals("x86_64")) {
            arch = ELF.EM_X64_64;
        } else if (archStr.equals("sparcv9")) {
            arch = ELF.EM_SPARCV9;
        } else {
            System.out.println("Unsupported architecture " + archStr);
            arch = ELF.EM_NONE;
        }
        osName = System.getProperty("os.name").toLowerCase();
    }

    public static int getELFArch() {
        return arch;
    }

    public static int getELFClass() {
        return elfClass;
    }

    public static int getELFEndian() {
        return endian;
    }

    public static String getOsName() {
        return osName;
    }

    public static int createReloca() {
        switch(arch) {
            case ELF.EM_X64_64:
                return 1;
            default:
                return 0;
        }
    }

    public static int sizeOfSymtabEntry() {
        return JNILibELFAPI.size_of_Sym(elfClass);
    }

    public static int sizeOfRelocEntry() {
        if (createReloca() == 1) {
            return JNILibELFAPI.size_of_Rela(elfClass);
        } else {
            return JNILibELFAPI.size_of_Rel(elfClass);
        }
    }
}
