package jdk.tools.jaotc.jnilibelf;

public class JNILibELFAPI {

    static {
        System.loadLibrary("jelfshim");
    }

    public static enum OpenFlags {

        O_RDONLY(0x0), O_WRONLY(0x1), O_RDWR(0x2), O_CREAT(0x40);

        private final int intVal;

        private OpenFlags(int v) {
            intVal = v;
        }

        public int intValue() {
            return intVal;
        }
    }

    public interface ELF {

        int EI_NIDENT = 16;

        int EI_CLASS = 4;

        int ELFCLASSNONE = 0;

        int ELFCLASS32 = 1;

        int ELFCLASS64 = 2;

        int ELFCLASSNUM = 3;

        int EI_DATA = 5;

        int ELFDATANONE = 0;

        int ELFDATA2LSB = 1;

        int ELFDATA2MSB = 2;

        int ELFDATANUM = 3;

        int EM_NONE = 0;

        int EM_SPARC = 2;

        int EM_386 = 3;

        int EM_SPARCV9 = 43;

        int EM_X64_64 = 62;

        int ET_NONE = 0;

        int ET_REL = 1;

        int ET_EXEC = 2;

        int ET_DYN = 3;

        int ET_CORE = 4;

        int ET_NUM = 5;

        int ET_LOOS = 0xfe00;

        int ET_HIOS = 0xfeff;

        int ET_LOPROC = 0xff00;

        int ET_HIPROC = 0xffff;

        int EV_NONE = 0;

        int EV_CURRENT = 1;

        int EV_NUM = 2;

        int PT_NULL = 0;

        int PT_LOAD = 1;

        int PT_DYNAMIC = 2;

        int PT_INTERP = 3;

        int PT_NOTE = 4;

        int PT_SHLIB = 5;

        int PT_PHDR = 6;

        int PT_TLS = 7;

        int PT_NUM = 8;

        int PT_LOOS = 0x60000000;

        int PT_GNU_EH_FRAME = 0x6474e550;

        int PT_GNU_STACK = 0x6474e551;

        int PT_GNU_RELRO = 0x6474e552;

        int PT_LOSUNW = 0x6ffffffa;

        int PT_SUNWBSS = 0x6ffffffa;

        int PT_SUNWSTACK = 0x6ffffffb;

        int PT_HISUNW = 0x6fffffff;

        int PT_HIOS = 0x6fffffff;

        int PT_LOPROC = 0x70000000;

        int PT_HIPROC = 0x7fffffff;

        int SHN_UNDEF = 0;

        int SHN_LORESERVE = 0xff00;

        int SHN_LOPROC = 0xff00;

        int SHN_BEFORE = 0xff00;

        int SHN_AFTER = 0xff01;

        int SHN_HIPROC = 0xff1f;

        int SHN_LOOS = 0xff20;

        int SHN_HIOS = 0xff3f;

        int SHN_ABS = 0xfff1;

        int SHN_COMMON = 0xfff2;

        int SHN_XINDEX = 0xffff;

        int SHN_HIRESERVE = 0xffff;

        int SHT_NULL = 0;

        int SHT_PROGBITS = 1;

        int SHT_SYMTAB = 2;

        int SHT_STRTAB = 3;

        int SHT_RELA = 4;

        int SHT_HASH = 5;

        int SHT_DYNAMIC = 6;

        int SHT_NOTE = 7;

        int SHT_NOBITS = 8;

        int SHT_REL = 9;

        int SHT_SHLIB = 10;

        int SHT_DYNSYM = 11;

        int SHT_INIT_ARRAY = 14;

        int SHT_FINI_ARRAY = 15;

        int SHT_PREINIT_ARRAY = 16;

        int SHT_GROUP = 17;

        int SHT_SYMTAB_SHNDX = 18;

        int SHT_NUM = 19;

        int SHT_LOOS = 0x60000000;

        int SHT_GNU_ATTRIBUTES = 0x6ffffff5;

        int SHT_GNU_HASH = 0x6ffffff6;

        int SHT_GNU_LIBLIST = 0x6ffffff7;

        int SHT_CHECKSUM = 0x6ffffff8;

        int SHT_LOSUNW = 0x6ffffffa;

        int SHT_SUNW_move = 0x6ffffffa;

        int SHT_SUNW_COMDAT = 0x6ffffffb;

        int SHT_SUNW_syminfo = 0x6ffffffc;

        int SHT_GNU_verdef = 0x6ffffffd;

        int SHT_GNU_verneed = 0x6ffffffe;

        int SHT_GNU_versym = 0x6fffffff;

        int SHT_HISUNW = 0x6fffffff;

        int SHT_HIOS = 0x6fffffff;

        int SHT_LOPROC = 0x70000000;

        int SHT_HIPROC = 0x7fffffff;

        int SHT_LOUSER = 0x80000000;

        int SHT_HIUSER = 0x8fffffff;

        int SHF_WRITE = (1 << 0);

        int SHF_ALLOC = (1 << 1);

        int SHF_EXECINSTR = (1 << 2);

        int SHF_MERGE = (1 << 4);

        int SHF_STRINGS = (1 << 5);

        int SHF_INFO_LINK = (1 << 6);

        int SHF_LINK_ORDER = (1 << 7);

        int SHF_OS_NONCONFORMING = (1 << 8);

        int SHF_GROUP = (1 << 9);

        int SHF_TLS = (1 << 10);

        int SHF_MASKOS = 0x0ff00000;

        int SHF_MASKPROC = 0xf0000000;

        int SHF_ORDERED = (1 << 30);

        int SHF_EXCLUDE = (1 << 31);

        int STB_LOCAL = 0;

        int STB_GLOBAL = 1;

        int STB_WEAK = 2;

        int STB_NUM = 3;

        int STB_LOOS = 10;

        int STB_GNU_UNIQUE = 10;

        int STB_HIOS = 12;

        int STB_LOPROC = 13;

        int STB_HIPROC = 15;

        int STT_NOTYPE = 0;

        int STT_OBJECT = 1;

        int STT_FUNC = 2;

        int STT_SECTION = 3;

        int STT_FILE = 4;

        int STT_COMMON = 5;

        int STT_TLS = 6;

        int STT_NUM = 7;

        int STT_LOOS = 10;

        int STT_GNU_IFUNC = 10;

        int STT_HIOS = 12;

        int STT_LOPROC = 13;

        int STT_HIPROC = 15;
    }

    public interface LibELF {

        public static enum Elf_Cmd {

            ELF_C_NULL("NULL"),
            ELF_C_READ("READ"),
            ELF_C_RDWR("RDWR"),
            ELF_C_WRITE("WRITE"),
            ELF_C_CLR("CLR"),
            ELF_C_SET("SET"),
            ELF_C_FDDONE("FDDONE"),
            ELF_C_FDREAD("FDREAD"),
            ELF_C_READ_MMAP("READ_MMAP"),
            ELF_C_RDWR_MMAP("RDWR_MMAP"),
            ELF_C_WRITE_MMAP("WRITE_MMAP"),
            ELF_C_READ_MMAP_PRIVATE("READ_MMAP_PRIVATE"),
            ELF_C_EMPTY("EMPTY"),
            ELF_C_WRIMAGE("WRIMAGE"),
            ELF_C_IMAGE("IMAGE"),
            ELF_C_NUM("NUM");

            private final int intVal;

            private final String name;

            private Elf_Cmd(String cmd) {
                name = "ELF_C_" + cmd;
                switch(cmd) {
                    case "NULL":
                        intVal = jdk.tools.jaotc.jnilibelf.linux.Elf_Cmd.ELF_C_NULL.ordinal();
                        break;
                    case "READ":
                        intVal = jdk.tools.jaotc.jnilibelf.linux.Elf_Cmd.ELF_C_READ.ordinal();
                        break;
                    case "RDWR":
                        if (JNIELFTargetInfo.getOsName().equals("linux")) {
                            intVal = jdk.tools.jaotc.jnilibelf.linux.Elf_Cmd.ELF_C_RDWR.ordinal();
                        } else if (JNIELFTargetInfo.getOsName().equals("sunos")) {
                            intVal = jdk.tools.jaotc.jnilibelf.sunos.Elf_Cmd.ELF_C_RDWR.ordinal();
                        } else {
                            intVal = -1;
                        }
                        break;
                    case "WRITE":
                        if (JNIELFTargetInfo.getOsName().equals("linux")) {
                            intVal = jdk.tools.jaotc.jnilibelf.linux.Elf_Cmd.ELF_C_WRITE.ordinal();
                        } else if (JNIELFTargetInfo.getOsName().equals("sunos")) {
                            intVal = jdk.tools.jaotc.jnilibelf.sunos.Elf_Cmd.ELF_C_WRITE.ordinal();
                        } else {
                            intVal = -1;
                        }
                        break;
                    case "CLR":
                        if (JNIELFTargetInfo.getOsName().equals("linux")) {
                            intVal = jdk.tools.jaotc.jnilibelf.linux.Elf_Cmd.ELF_C_CLR.ordinal();
                        } else if (JNIELFTargetInfo.getOsName().equals("sunos")) {
                            intVal = jdk.tools.jaotc.jnilibelf.sunos.Elf_Cmd.ELF_C_CLR.ordinal();
                        } else {
                            intVal = -1;
                        }
                        break;
                    case "SET":
                        if (JNIELFTargetInfo.getOsName().equals("linux")) {
                            intVal = jdk.tools.jaotc.jnilibelf.linux.Elf_Cmd.ELF_C_SET.ordinal();
                        } else if (JNIELFTargetInfo.getOsName().equals("sunos")) {
                            intVal = jdk.tools.jaotc.jnilibelf.sunos.Elf_Cmd.ELF_C_SET.ordinal();
                        } else {
                            intVal = -1;
                        }
                        break;
                    case "FDDONE":
                        if (JNIELFTargetInfo.getOsName().equals("linux")) {
                            intVal = jdk.tools.jaotc.jnilibelf.linux.Elf_Cmd.ELF_C_FDDONE.ordinal();
                        } else if (JNIELFTargetInfo.getOsName().equals("sunos")) {
                            intVal = jdk.tools.jaotc.jnilibelf.sunos.Elf_Cmd.ELF_C_FDDONE.ordinal();
                        } else {
                            intVal = -1;
                        }
                        break;
                    case "FDREAD":
                        if (JNIELFTargetInfo.getOsName().equals("linux")) {
                            intVal = jdk.tools.jaotc.jnilibelf.linux.Elf_Cmd.ELF_C_FDREAD.ordinal();
                        } else if (JNIELFTargetInfo.getOsName().equals("sunos")) {
                            intVal = jdk.tools.jaotc.jnilibelf.sunos.Elf_Cmd.ELF_C_FDREAD.ordinal();
                        } else {
                            intVal = -1;
                        }
                        break;
                    case "NUM":
                        if (JNIELFTargetInfo.getOsName().equals("linux")) {
                            intVal = jdk.tools.jaotc.jnilibelf.linux.Elf_Cmd.ELF_C_NUM.ordinal();
                        } else if (JNIELFTargetInfo.getOsName().equals("sunos")) {
                            intVal = jdk.tools.jaotc.jnilibelf.sunos.Elf_Cmd.ELF_C_NUM.ordinal();
                        } else {
                            intVal = -1;
                        }
                        break;
                    case "READ_MMAP":
                        if (JNIELFTargetInfo.getOsName().equals("linux")) {
                            intVal = jdk.tools.jaotc.jnilibelf.linux.Elf_Cmd.ELF_C_READ_MMAP.ordinal();
                        } else {
                            intVal = -1;
                        }
                        break;
                    case "RDWR_MMAP":
                        if (JNIELFTargetInfo.getOsName().equals("linux")) {
                            intVal = jdk.tools.jaotc.jnilibelf.linux.Elf_Cmd.ELF_C_RDWR_MMAP.ordinal();
                        } else {
                            intVal = -1;
                        }
                        break;
                    case "WRITE_MMAP":
                        if (JNIELFTargetInfo.getOsName().equals("linux")) {
                            intVal = jdk.tools.jaotc.jnilibelf.linux.Elf_Cmd.ELF_C_WRITE_MMAP.ordinal();
                        } else {
                            intVal = -1;
                        }
                        break;
                    case "READ_MMAP_PRIVATE":
                        if (JNIELFTargetInfo.getOsName().equals("linux")) {
                            intVal = jdk.tools.jaotc.jnilibelf.linux.Elf_Cmd.ELF_C_READ_MMAP_PRIVATE.ordinal();
                        } else {
                            intVal = -1;
                        }
                        break;
                    case "EMPTY":
                        if (JNIELFTargetInfo.getOsName().equals("linux")) {
                            intVal = jdk.tools.jaotc.jnilibelf.linux.Elf_Cmd.ELF_C_EMPTY.ordinal();
                        } else {
                            intVal = -1;
                        }
                        break;
                    case "WRIMAGE":
                        if (JNIELFTargetInfo.getOsName().equals("linux")) {
                            intVal = jdk.tools.jaotc.jnilibelf.sunos.Elf_Cmd.ELF_C_WRIMAGE.ordinal();
                        } else {
                            intVal = -1;
                        }
                        break;
                    case "IMAGE":
                        if (JNIELFTargetInfo.getOsName().equals("linux")) {
                            intVal = jdk.tools.jaotc.jnilibelf.sunos.Elf_Cmd.ELF_C_IMAGE.ordinal();
                        } else {
                            intVal = -1;
                        }
                        break;
                    default:
                        intVal = -1;
                }
            }

            public int intValue() {
                assert intVal != -1 : "enum " + name + "not supported on " + JNIELFTargetInfo.getOsName();
                return intVal;
            }

            public String getName() {
                return name;
            }
        }

        public static enum Elf_Type {

            ELF_T_BYTE(0),
            ELF_T_ADDR(1),
            ELF_T_DYN(2),
            ELF_T_EHDR(3),
            ELF_T_HALF(4),
            ELF_T_OFF(5),
            ELF_T_PHDR(6),
            ELF_T_RELA(7),
            ELF_T_REL(8),
            ELF_T_SHDR(9),
            ELF_T_SWORD(10),
            ELF_T_SYM(11),
            ELF_T_WORD(12),
            ELF_T_XWORD(13),
            ELF_T_SXWORD(14),
            ELF_T_VDEF(15),
            ELF_T_VDAUX(16),
            ELF_T_VNEED(17),
            ELF_T_VNAUX(18),
            ELF_T_NHDR(19),
            ELF_T_SYMINFO(20),
            ELF_T_MOVE(21),
            ELF_T_LIB(22),
            ELF_T_GNUHASH(23),
            ELF_T_AUXV(24),
            ELF_T_NUM(25);

            private final int intVal;

            private Elf_Type(int v) {
                intVal = v;
            }

            public int intValue() {
                return intVal;
            }
        }

        int ELF_F_DIRTY = 0x1;

        int ELF_F_LAYOUT = 0x4;

        int ELF_F_PERMISSIVE = 0x8;

        public static enum Elf_Kind {

            ELF_K_NONE(0), ELF_K_AR(1), ELF_K_COFF(2), ELF_K_ELF(3), ELF_K_NUM(4);

            private final int intVal;

            private Elf_Kind(int v) {
                intVal = v;
            }

            public int intValue() {
                return intVal;
            }
        }
    }

    static native int elf_version(int v);

    static native String elfshim_version();

    static native Pointer elf_begin(int fildes, int elfCRead, Pointer elfHdrPtr);

    static native int elf_end(Pointer elfPtr);

    static native int elf_kind(Pointer elfPtr);

    static native int elf_flagphdr(Pointer elfPtr, int cmd, int flags);

    static native Pointer elf_newscn(Pointer elfPtr);

    static native Pointer elf_newdata(Pointer scnPtr);

    static native Pointer elf64_getshdr(Pointer scnPtr);

    static native long elf_update(Pointer elfPtr, int cmd);

    static native String elf_errmsg(int error);

    static native int elf_ndxscn(Pointer scn);

    static native Pointer gelf_newehdr(Pointer elf, int elfclass);

    static native Pointer gelf_newphdr(Pointer elf, int phnum);

    static native int size_of_Sym(int elfClass);

    static native int size_of_Rela(int elfClass);

    static native int size_of_Rel(int elfClass);

    static native void ehdr_set_data_encoding(Pointer ehdr, int val);

    static native void set_Ehdr_e_machine(int elfclass, Pointer structPtr, int val);

    static native void set_Ehdr_e_type(int elfclass, Pointer structPtr, int val);

    static native void set_Ehdr_e_version(int elfclass, Pointer structPtr, int val);

    static native void set_Ehdr_e_shstrndx(int elfclass, Pointer structPtr, int val);

    static native void phdr_set_type_self(int elfclass, Pointer ehdr, Pointer phdr);

    static native void set_Shdr_sh_name(int elfclass, Pointer structPtr, int val);

    static native void set_Shdr_sh_type(int elfclass, Pointer structPtr, int val);

    static native void set_Shdr_sh_flags(int elfclass, Pointer structPtr, int val);

    static native void set_Shdr_sh_entsize(int elfclass, Pointer structPtr, int val);

    static native void set_Shdr_sh_link(int elfclass, Pointer structPtr, int val);

    static native void set_Shdr_sh_info(int elfclass, Pointer structPtr, int val);

    static native void set_Data_d_align(Pointer structPtr, int val);

    static native void set_Data_d_off(Pointer structPtr, int val);

    static native void set_Data_d_buf(Pointer structPtr, Pointer val);

    static native void set_Data_d_type(Pointer structPtr, int val);

    static native void set_Data_d_size(Pointer structPtr, int val);

    static native void set_Data_d_version(Pointer structPtr, int val);

    static native long create_sym_entry(int elfclass, int index, int type, int bind, int shndx, int size, int value);

    static native long create_reloc_entry(int elfclass, int roffset, int symtabIdx, int relocType, int raddend, int reloca);

    static native int open_rw(String fileName);

    static native int open(String fileName, int flags);

    static native int open(String fileName, int flags, int mode);

    static native int close(int fd);
}
