package sun.hotspot.gc;

import java.util.ArrayList;
import java.util.List;
import sun.hotspot.WhiteBox;

public enum GC {

    Serial(1), Parallel(2), ConcMarkSweep(4), G1(8);

    private static final GC CURRENT_GC;

    private static final int ALL_GC_CODES;

    private static final boolean IS_BY_ERGO;

    static {
        WhiteBox WB = WhiteBox.getWhiteBox();
        ALL_GC_CODES = WB.allSupportedGC();
        IS_BY_ERGO = WB.gcSelectedByErgo();
        int currentCode = WB.currentGC();
        GC tmp = null;
        for (GC gc : GC.values()) {
            if (gc.code == currentCode) {
                tmp = gc;
                break;
            }
        }
        if (tmp == null) {
            throw new Error("Unknown current GC code " + currentCode);
        }
        CURRENT_GC = tmp;
    }

    private final int code;

    private GC(int code) {
        this.code = code;
    }

    public boolean isSupported() {
        return (ALL_GC_CODES & code) != 0;
    }

    public static GC current() {
        return CURRENT_GC;
    }

    public static boolean currentSetByErgo() {
        return IS_BY_ERGO;
    }

    public static List<GC> allSupported() {
        List<GC> list = new ArrayList<>();
        for (GC gc : GC.values()) {
            if (gc.isSupported()) {
                list.add(gc);
            }
        }
        return list;
    }
}
