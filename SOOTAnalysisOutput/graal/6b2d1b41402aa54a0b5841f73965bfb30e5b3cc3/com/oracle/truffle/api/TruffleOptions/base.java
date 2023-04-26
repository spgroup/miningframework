package com.oracle.truffle.api;

import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.nodes.NodeInfo;
import java.security.AccessController;
import java.security.PrivilegedAction;

public final class TruffleOptions {

    private TruffleOptions() {
    }

    public static final boolean TraceRewrites;

    public static final boolean DetailedRewriteReasons;

    public static String TraceRewritesFilterClass;

    public static NodeCost TraceRewritesFilterFromCost;

    public static NodeCost TraceRewritesFilterToCost;

    public static final boolean TraceASTJSON;

    public static final boolean AOT;

    private static NodeCost parseNodeInfoKind(String kind) {
        if (kind == null) {
            return null;
        }
        return NodeCost.valueOf(kind);
    }

    static {
        final boolean[] values = new boolean[4];
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

            public Void run() {
                values[0] = Boolean.getBoolean("truffle.TraceRewrites");
                TraceRewritesFilterClass = System.getProperty("truffle.TraceRewritesFilterClass");
                TraceRewritesFilterFromCost = parseNodeInfoKind(System.getProperty("truffle.TraceRewritesFilterFromCost"));
                TraceRewritesFilterToCost = parseNodeInfoKind(System.getProperty("truffle.TraceRewritesFilterToCost"));
                values[1] = Boolean.getBoolean("truffle.DetailedRewriteReasons");
                values[2] = Boolean.getBoolean("truffle.TraceASTJSON");
                values[3] = Boolean.getBoolean("com.oracle.truffle.aot");
                return null;
            }
        });
        TraceRewrites = values[0];
        DetailedRewriteReasons = values[1];
        TraceASTJSON = values[2];
        AOT = values[3];
    }
}
