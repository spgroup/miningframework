package sun.font;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.swing.plaf.FontUIResource;
import sun.util.logging.PlatformLogger;

public final class FontUtilities {

    public static boolean isSolaris;

    public static boolean isLinux;

    public static boolean isMacOSX;

    public static boolean useT2K;

    public static boolean isWindows;

    public static boolean isOpenJDK;

    static final String LUCIDA_FILE_NAME = "LucidaSansRegular.ttf";

    private static boolean debugFonts = false;

    private static PlatformLogger logger = null;

    private static boolean logging;

    static {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @SuppressWarnings("deprecation")
            @Override
            public Object run() {
                String osName = System.getProperty("os.name", "unknownOS");
                isSolaris = osName.startsWith("SunOS");
                isLinux = osName.startsWith("Linux");
                isMacOSX = osName.contains("OS X");
                String t2kStr = System.getProperty("sun.java2d.font.scaler");
                if (t2kStr != null) {
                    useT2K = "t2k".equals(t2kStr);
                } else {
                    useT2K = false;
                }
                isWindows = osName.startsWith("Windows");
                String jreLibDirName = System.getProperty("java.home", "") + File.separator + "lib";
                String jreFontDirName = jreLibDirName + File.separator + "fonts";
                File lucidaFile = new File(jreFontDirName + File.separator + LUCIDA_FILE_NAME);
                isOpenJDK = !lucidaFile.exists();
                String debugLevel = System.getProperty("sun.java2d.debugfonts");
                if (debugLevel != null && !debugLevel.equals("false")) {
                    debugFonts = true;
                    logger = PlatformLogger.getLogger("sun.java2d");
                    if (debugLevel.equals("warning")) {
                        logger.setLevel(PlatformLogger.Level.WARNING);
                    } else if (debugLevel.equals("severe")) {
                        logger.setLevel(PlatformLogger.Level.SEVERE);
                    }
                }
                if (debugFonts) {
                    logger = PlatformLogger.getLogger("sun.java2d");
                    logging = logger.isEnabled();
                }
                return null;
            }
        });
    }

    public static final int MIN_LAYOUT_CHARCODE = 0x0300;

    public static final int MAX_LAYOUT_CHARCODE = 0x206F;

    public static Font2D getFont2D(Font font) {
        return FontAccess.getFontAccess().getFont2D(font);
    }

    public static boolean isComplexScript(char[] chs, int start, int limit) {
        for (int i = start; i < limit; i++) {
            if (chs[i] < MIN_LAYOUT_CHARCODE) {
                continue;
            } else if (isComplexCharCode(chs[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isComplexText(char[] chs, int start, int limit) {
        for (int i = start; i < limit; i++) {
            if (chs[i] < MIN_LAYOUT_CHARCODE) {
                continue;
            } else if (isNonSimpleChar(chs[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNonSimpleChar(char ch) {
        return isComplexCharCode(ch) || (ch >= CharToGlyphMapper.HI_SURROGATE_START && ch <= CharToGlyphMapper.LO_SURROGATE_END);
    }

    public static boolean isComplexCharCode(int code) {
        if (code < MIN_LAYOUT_CHARCODE || code > MAX_LAYOUT_CHARCODE) {
            return false;
        } else if (code <= 0x036f) {
            return true;
        } else if (code < 0x0590) {
            return false;
        } else if (code <= 0x06ff) {
            return true;
        } else if (code < 0x0900) {
            return false;
        } else if (code <= 0x0e7f) {
            return true;
        } else if (code < 0x0f00) {
            return false;
        } else if (code <= 0x0fff) {
            return true;
        } else if (code < 0x1100) {
            return false;
        } else if (code < 0x11ff) {
            return true;
        } else if (code < 0x1780) {
            return false;
        } else if (code <= 0x17ff) {
            return true;
        } else if (code < 0x200c) {
            return false;
        } else if (code <= 0x200d) {
            return true;
        } else if (code >= 0x202a && code <= 0x202e) {
            return true;
        } else if (code >= 0x206a && code <= 0x206f) {
            return true;
        }
        return false;
    }

    public static PlatformLogger getLogger() {
        return logger;
    }

    public static boolean isLogging() {
        return logging;
    }

    public static boolean debugFonts() {
        return debugFonts;
    }

    public static boolean fontSupportsDefaultEncoding(Font font) {
        return getFont2D(font) instanceof CompositeFont;
    }

    private static volatile SoftReference<ConcurrentHashMap<PhysicalFont, CompositeFont>> compMapRef = new SoftReference<>(null);

    public static FontUIResource getCompositeFontUIResource(Font font) {
        FontUIResource fuir = new FontUIResource(font);
        Font2D font2D = FontUtilities.getFont2D(font);
        if (!(font2D instanceof PhysicalFont)) {
            return fuir;
        }
        FontManager fm = FontManagerFactory.getInstance();
        Font2D dialog = fm.findFont2D("dialog", font.getStyle(), FontManager.NO_FALLBACK);
        if (dialog == null || !(dialog instanceof CompositeFont)) {
            return fuir;
        }
        CompositeFont dialog2D = (CompositeFont) dialog;
        PhysicalFont physicalFont = (PhysicalFont) font2D;
        ConcurrentHashMap<PhysicalFont, CompositeFont> compMap = compMapRef.get();
        if (compMap == null) {
            compMap = new ConcurrentHashMap<PhysicalFont, CompositeFont>();
            compMapRef = new SoftReference<>(compMap);
        }
        CompositeFont compFont = compMap.get(physicalFont);
        if (compFont == null) {
            compFont = new CompositeFont(physicalFont, dialog2D);
            compMap.put(physicalFont, compFont);
        }
        FontAccess.getFontAccess().setFont2D(fuir, compFont.handle);
        FontAccess.getFontAccess().setCreatedFont(fuir);
        return fuir;
    }

    private static final String[][] nameMap = { { "sans", "sansserif" }, { "sans-serif", "sansserif" }, { "serif", "serif" }, { "monospace", "monospaced" } };

    public static String mapFcName(String name) {
        for (int i = 0; i < nameMap.length; i++) {
            if (name.equals(nameMap[i][0])) {
                return nameMap[i][1];
            }
        }
        return null;
    }

    public static FontUIResource getFontConfigFUIR(String fcFamily, int style, int size) {
        String mapped = mapFcName(fcFamily);
        if (mapped == null) {
            mapped = "sansserif";
        }
        FontUIResource fuir;
        FontManager fm = FontManagerFactory.getInstance();
        if (fm instanceof SunFontManager) {
            SunFontManager sfm = (SunFontManager) fm;
            fuir = sfm.getFontConfigFUIR(mapped, style, size);
        } else {
            fuir = new FontUIResource(mapped, style, size);
        }
        return fuir;
    }

    public static boolean textLayoutIsCompatible(Font font) {
        Font2D font2D = getFont2D(font);
        if (font2D instanceof TrueTypeFont) {
            TrueTypeFont ttf = (TrueTypeFont) font2D;
            return ttf.getDirectoryEntry(TrueTypeFont.GSUBTag) == null || ttf.getDirectoryEntry(TrueTypeFont.GPOSTag) != null;
        } else {
            return false;
        }
    }
}
