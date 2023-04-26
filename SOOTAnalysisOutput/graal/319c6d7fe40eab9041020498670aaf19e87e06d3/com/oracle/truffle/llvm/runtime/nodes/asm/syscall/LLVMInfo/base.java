package com.oracle.truffle.llvm.runtime.nodes.asm.syscall;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.llvm.runtime.nodes.asm.support.LLVMString;
import com.oracle.truffle.llvm.runtime.memory.LLVMMemory;
import com.oracle.truffle.llvm.runtime.pointer.LLVMNativePointer;

public class LLVMInfo {

    private static final int UTS_FIELD_LENGTH = 65;

    public static final String SYSNAME;

    public static final String RELEASE;

    public static final String MACHINE;

    static {
        SYSNAME = System.getProperty("os.name");
        RELEASE = System.getProperty("os.version");
        String arch = System.getProperty("os.arch");
        if ("amd64".equals(arch)) {
            arch = "x86_64";
        }
        MACHINE = arch;
    }

    private static String readFile(String name, String fallback) {
        CompilerAsserts.neverPartOfCompilation();
        try {
            Path path = Paths.get(name);
            if (Files.exists(path)) {
                return new String(Files.readAllBytes(path)).trim();
            }
        } catch (Exception e) {
        }
        return fallback;
    }

    @TruffleBoundary
    public static String getHostname() {
        String hostname = readFile("/proc/sys/kernel/hostname", null);
        if (hostname != null) {
            return hostname;
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    @TruffleBoundary
    public static String getVersion() {
        return readFile("/proc/sys/kernel/version", null);
    }

    @TruffleBoundary
    public static String getDomainName() {
        return readFile("/proc/sys/kernel/domainname", null);
    }

    public static long uname(LLVMMemory memory, LLVMNativePointer name) {
        LLVMNativePointer ptr = name;
        LLVMString.strcpy(memory, ptr, SYSNAME);
        ptr = ptr.increment(UTS_FIELD_LENGTH);
        LLVMString.strcpy(memory, ptr, getHostname());
        ptr = ptr.increment(UTS_FIELD_LENGTH);
        LLVMString.strcpy(memory, ptr, RELEASE);
        ptr = ptr.increment(UTS_FIELD_LENGTH);
        LLVMString.strcpy(memory, ptr, getVersion());
        ptr = ptr.increment(UTS_FIELD_LENGTH);
        LLVMString.strcpy(memory, ptr, MACHINE);
        ptr = ptr.increment(UTS_FIELD_LENGTH);
        LLVMString.strcpy(memory, ptr, getDomainName());
        return 0;
    }

    @TruffleBoundary
    private static LLVMProcessStat getstat() {
        String stat = readFile("/proc/self/stat", null);
        if (stat == null) {
            return null;
        } else {
            return new LLVMProcessStat(stat);
        }
    }

    @TruffleBoundary
    public static long getpid() {
        LLVMProcessStat stat = getstat();
        if (stat != null) {
            return stat.getPid();
        }
        String info = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        return Long.parseLong(info.split("@")[0]);
    }

    @TruffleBoundary
    public static long getppid() {
        LLVMProcessStat stat = getstat();
        if (stat != null) {
            return stat.getPpid();
        } else {
            return 1;
        }
    }
}
