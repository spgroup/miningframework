package org.graalvm.compiler.hotspot;

import java.lang.reflect.Method;
import jdk.vm.ci.hotspot.HotSpotResolvedObjectType;

public class FingerprintUtil {

    private static Method getFingerprint;

    static {
        try {
            getFingerprint = HotSpotResolvedObjectType.class.getMethod("getFingerprint");
        } catch (Exception e) {
        }
    }

    public static long getFingerprint(HotSpotResolvedObjectType type) {
        if (getFingerprint != null) {
            try {
                return ((Long) getFingerprint.invoke(type)).longValue();
            } catch (Exception e) {
            }
        }
        return 0;
    }
}
