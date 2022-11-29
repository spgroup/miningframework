package compiler.ciReplay;

import sun.hotspot.WhiteBox;

public class TestDumpReplay {

    private static final WhiteBox WHITE_BOX = WhiteBox.getWhiteBox();

    private static final String emptyString;

    static {
        emptyString = "";
    }

    public static void m1() {
        m2();
    }

    public static void m2() {
        m3();
    }

    public static void m3() {
    }

    public static void main(String[] args) {
        String directive = "[{ match: \"*.*\", DumpReplay: true }]";
        if (WHITE_BOX.addCompilerDirective(directive) != 1) {
            throw new RuntimeException("Failed to add compiler directive");
        }
        for (int i = 0; i < 10_000; ++i) {
            m1();
        }
    }
}
