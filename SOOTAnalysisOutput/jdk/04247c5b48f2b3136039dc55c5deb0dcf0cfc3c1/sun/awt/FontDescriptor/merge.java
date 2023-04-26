package sun.awt;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import sun.nio.cs.HistoricallyNamedCharset;

public class FontDescriptor implements Cloneable {

    static {
        NativeLibLoader.loadLibraries();
        initIDs();
    }

    String nativeName;

    public CharsetEncoder encoder;

    String charsetName;

    private int[] exclusionRanges;

    public FontDescriptor(String nativeName, CharsetEncoder encoder, int[] exclusionRanges) {
        this.nativeName = nativeName;
        this.encoder = encoder;
        this.exclusionRanges = exclusionRanges;
        this.useUnicode = false;
        Charset cs = encoder.charset();
        if (cs instanceof HistoricallyNamedCharset)
            this.charsetName = ((HistoricallyNamedCharset) cs).historicalName();
        else
            this.charsetName = cs.name();
    }

    public String getNativeName() {
        return nativeName;
    }

    public CharsetEncoder getFontCharsetEncoder() {
        return encoder;
    }

    public String getFontCharsetName() {
        return charsetName;
    }

    public int[] getExclusionRanges() {
        return exclusionRanges;
    }

    public boolean isExcluded(char ch) {
        for (int i = 0; i < exclusionRanges.length; ) {
            int lo = (exclusionRanges[i++]);
            int up = (exclusionRanges[i++]);
            if (ch >= lo && ch <= up) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return super.toString() + " [" + nativeName + "|" + encoder + "]";
    }

    private static native void initIDs();

    public CharsetEncoder unicodeEncoder;

    boolean useUnicode;

    public boolean useUnicode() {
        if (useUnicode && unicodeEncoder == null) {
            try {
                this.unicodeEncoder = isLE ? StandardCharsets.UTF_16LE.newEncoder() : StandardCharsets.UTF_16BE.newEncoder();
            } catch (IllegalArgumentException x) {
            }
        }
        return useUnicode;
    }

    static boolean isLE;

    static {
        String enc = (String) java.security.AccessController.doPrivileged(new sun.security.action.GetPropertyAction("sun.io.unicode.encoding", "UnicodeBig"));
        isLE = !"UnicodeBig".equals(enc);
    }
}
