package com.oracle.truffle.llvm.runtime.nodes.asm.syscall;

public class LLVMInfo {

    public static final String SYSNAME;

    public static final String MACHINE;

    static {
        SYSNAME = System.getProperty("os.name");
        String arch = System.getProperty("os.arch");
        if ("amd64".equals(arch)) {
            arch = "x86_64";
        }
        MACHINE = arch;
    }
}
