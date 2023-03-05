package org.graalvm.compiler.hotspot.test;

import org.junit.Ignore;
import org.junit.Test;
import org.graalvm.compiler.api.directives.GraalDirectives;

public class GraalOSRLockTest extends GraalOSRTestBase {

    @Ignore("OSR with locks not supported")
    @Test
    public void testOSR() {
        testOSR("test");
    }

    static int limit = 10000;

    private static Object lock = new Object();

    public static ReturnValue test() {
        synchronized (lock) {
            for (int i = 0; i < limit * limit; i++) {
                GraalDirectives.blackhole(i);
                if (GraalDirectives.inCompiledCode()) {
                    return ReturnValue.SUCCESS;
                }
            }
            return ReturnValue.FAILURE;
        }
    }
}
