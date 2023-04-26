package com.oracle.truffle.api;

import java.security.AccessController;
import java.security.PrivilegedAction;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.nodes.NodeInfo;

public final class TruffleOptions {

    private TruffleOptions() {
    }

    public static final boolean TraceRewrites;

    public static final boolean DetailedRewriteReasons;

    public static final String TraceRewritesFilterClass;

    public static final NodeCost TraceRewritesFilterFromCost;

    public static final NodeCost TraceRewritesFilterToCost;

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
        final Object[] objs = new Object[3];
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

            public Void run() {
                values[0] = Boolean.getBoolean("truffle.TraceRewrites");
                objs[0] = System.getProperty("truffle.TraceRewritesFilterClass");
                objs[1] = parseNodeInfoKind(System.getProperty("truffle.TraceRewritesFilterFromCost"));
                objs[2] = parseNodeInfoKind(System.getProperty("truffle.TraceRewritesFilterToCost"));
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
        TraceRewritesFilterClass = (String) objs[0];
        TraceRewritesFilterFromCost = (NodeCost) objs[1];
        TraceRewritesFilterToCost = (NodeCost) objs[2];
    }
}