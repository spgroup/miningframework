package org.graalvm.compiler.serviceprovider;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import jdk.vm.ci.services.JVMCIPermission;

public final class GraalServices {

    private static int getJavaSpecificationVersion() {
        throw shouldNotReachHere();
    }

    public static final int JAVA_SPECIFICATION_VERSION = getJavaSpecificationVersion();

    public static final boolean Java8OrEarlier = JAVA_SPECIFICATION_VERSION <= 8;

    private GraalServices() {
    }

    private static InternalError shouldNotReachHere() {
        throw new InternalError("JDK specific overlay missing");
    }

    public static <S> Iterable<S> load(Class<S> service) {
        throw shouldNotReachHere();
    }

    public static <S> S loadSingle(Class<S> service, boolean required) {
        throw shouldNotReachHere();
    }

    public static InputStream getClassfileAsStream(Class<?> c) throws IOException {
        throw shouldNotReachHere();
    }

    public static boolean isToStringTrusted(Class<?> c) {
        throw shouldNotReachHere();
    }

    public static String getExecutionID() {
        throw shouldNotReachHere();
    }

    public static long getGlobalTimeStamp() {
        throw shouldNotReachHere();
    }

    public static long getThreadAllocatedBytes(long id) {
        throw shouldNotReachHere();
    }

    public static long getCurrentThreadAllocatedBytes() {
        throw shouldNotReachHere();
    }

    public static long getCurrentThreadCpuTime() {
        throw shouldNotReachHere();
    }

    public static boolean isThreadAllocatedMemorySupported() {
        throw shouldNotReachHere();
    }

    public static boolean isCurrentThreadCpuTimeSupported() {
        throw shouldNotReachHere();
    }

    public static List<String> getInputArguments() {
        throw shouldNotReachHere();
    }
}
