package sun.hotspot.gc;

import sun.hotspot.WhiteBox;

public enum GC {

    Serial(1), Parallel(2), ConcMarkSweep(3), G1(4);

    private static final WhiteBox WB = WhiteBox.getWhiteBox();

    private final int name;

    private GC(int name) {
        this.name = name;
    }

    public boolean isSupported() {
        return WB.isGCSupported(name);
    }

    public boolean isSelected() {
        return WB.isGCSelected(name);
    }

    public static boolean isSelectedErgonomically() {
        return WB.isGCSelectedErgonomically();
    }
}