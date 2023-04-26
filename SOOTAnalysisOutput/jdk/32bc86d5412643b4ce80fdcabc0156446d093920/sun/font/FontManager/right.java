package sun.font;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.FontFormatException;
import java.io.File;
import java.io.FilenameFilter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.plaf.FontUIResource;
import sun.awt.AppContext;
import sun.awt.FontConfiguration;
import sun.awt.SunHints;
import sun.awt.SunToolkit;
import sun.java2d.HeadlessGraphicsEnvironment;
import sun.java2d.SunGraphicsEnvironment;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Constructor;
import sun.java2d.Disposer;

public final class FontManager {

    public static final int FONTFORMAT_NONE = -1;

    public static final int FONTFORMAT_TRUETYPE = 0;

    public static final int FONTFORMAT_TYPE1 = 1;

    public static final int FONTFORMAT_T2K = 2;

    public static final int FONTFORMAT_TTC = 3;

    public static final int FONTFORMAT_COMPOSITE = 4;

    public static final int FONTFORMAT_NATIVE = 5;

    public static final int NO_FALLBACK = 0;

    public static final int PHYSICAL_FALLBACK = 1;

    public static final int LOGICAL_FALLBACK = 2;

    public static final int QUADPATHTYPE = 1;

    public static final int CUBICPATHTYPE = 2;

    private static final int CHANNELPOOLSIZE = 20;

    private static int lastPoolIndex = 0;

    private static FileFont[] fontFileCache = new FileFont[CHANNELPOOLSIZE];

    private static int maxCompFont = 0;

    private static CompositeFont[] compFonts = new CompositeFont[20];

    private static ConcurrentHashMap<String, CompositeFont> compositeFonts = new ConcurrentHashMap<String, CompositeFont>();

    private static ConcurrentHashMap<String, PhysicalFont> physicalFonts = new ConcurrentHashMap<String, PhysicalFont>();

    private static ConcurrentHashMap<String, PhysicalFont> registeredFontFiles = new ConcurrentHashMap<String, PhysicalFont>();

    private static ConcurrentHashMap<String, Font2D> fullNameToFont = new ConcurrentHashMap<String, Font2D>();

    private static HashMap<String, TrueTypeFont> localeFullNamesToFont;

    private static PhysicalFont defaultPhysicalFont;

    private static boolean usePlatformFontMetrics = false;

    public static Logger logger = null;

    public static boolean logging;

    static boolean longAddresses;

    static String osName;

    static boolean useT2K;

    static boolean isWindows;

    static boolean isSolaris;

    public static boolean isSolaris8;

    public static boolean isSolaris9;

    private static boolean loaded1dot0Fonts = false;

    static SunGraphicsEnvironment sgEnv;

    static boolean loadedAllFonts = false;

    static boolean loadedAllFontFiles = false;

    static TrueTypeFont eudcFont;

    static HashMap<String, String> jreFontMap;

    static HashSet<String> jreLucidaFontFiles;

    static String[] jreOtherFontFiles;

    static boolean noOtherJREFontFiles = false;

    private static String[] STR_ARRAY = new String[0];

    private static void initJREFontMap() {
        jreFontMap = new HashMap<String, String>();
        jreLucidaFontFiles = new HashSet<String>();
        if (SunGraphicsEnvironment.isOpenJDK()) {
            return;
        }
        jreFontMap.put("lucida sans0", "LucidaSansRegular.ttf");
        jreFontMap.put("lucida sans1", "LucidaSansDemiBold.ttf");
        jreFontMap.put("lucida sans regular0", "LucidaSansRegular.ttf");
        jreFontMap.put("lucida sans regular1", "LucidaSansDemiBold.ttf");
        jreFontMap.put("lucida sans bold1", "LucidaSansDemiBold.ttf");
        jreFontMap.put("lucida sans demibold1", "LucidaSansDemiBold.ttf");
        jreFontMap.put("lucida sans typewriter0", "LucidaTypewriterRegular.ttf");
        jreFontMap.put("lucida sans typewriter1", "LucidaTypewriterBold.ttf");
        jreFontMap.put("lucida sans typewriter regular0", "LucidaTypewriter.ttf");
        jreFontMap.put("lucida sans typewriter regular1", "LucidaTypewriterBold.ttf");
        jreFontMap.put("lucida sans typewriter bold1", "LucidaTypewriterBold.ttf");
        jreFontMap.put("lucida sans typewriter demibold1", "LucidaTypewriterBold.ttf");
        jreFontMap.put("lucida bright0", "LucidaBrightRegular.ttf");
        jreFontMap.put("lucida bright1", "LucidaBrightDemiBold.ttf");
        jreFontMap.put("lucida bright2", "LucidaBrightItalic.ttf");
        jreFontMap.put("lucida bright3", "LucidaBrightDemiItalic.ttf");
        jreFontMap.put("lucida bright regular0", "LucidaBrightRegular.ttf");
        jreFontMap.put("lucida bright regular1", "LucidaBrightDemiBold.ttf");
        jreFontMap.put("lucida bright regular2", "LucidaBrightItalic.ttf");
        jreFontMap.put("lucida bright regular3", "LucidaBrightDemiItalic.ttf");
        jreFontMap.put("lucida bright bold1", "LucidaBrightDemiBold.ttf");
        jreFontMap.put("lucida bright bold3", "LucidaBrightDemiItalic.ttf");
        jreFontMap.put("lucida bright demibold1", "LucidaBrightDemiBold.ttf");
        jreFontMap.put("lucida bright demibold3", "LucidaBrightDemiItalic.ttf");
        jreFontMap.put("lucida bright italic2", "LucidaBrightItalic.ttf");
        jreFontMap.put("lucida bright italic3", "LucidaBrightDemiItalic.ttf");
        jreFontMap.put("lucida bright bold italic3", "LucidaBrightDemiItalic.ttf");
        jreFontMap.put("lucida bright demibold italic3", "LucidaBrightDemiItalic.ttf");
        for (String ffile : jreFontMap.values()) {
            jreLucidaFontFiles.add(ffile);
        }
    }

    static {
        if (SunGraphicsEnvironment.debugFonts) {
            logger = Logger.getLogger("sun.java2d", null);
            logging = logger.getLevel() != Level.OFF;
        }
        initJREFontMap();
        java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {

            public Object run() {
                FontManagerNativeLibrary.load();
                initIDs();
                switch(StrikeCache.nativeAddressSize) {
                    case 8:
                        longAddresses = true;
                        break;
                    case 4:
                        longAddresses = false;
                        break;
                    default:
                        throw new RuntimeException("Unexpected address size");
                }
                osName = System.getProperty("os.name", "unknownOS");
                isSolaris = osName.startsWith("SunOS");
                String t2kStr = System.getProperty("sun.java2d.font.scaler");
                if (t2kStr != null) {
                    useT2K = "t2k".equals(t2kStr);
                }
                if (isSolaris) {
                    String version = System.getProperty("os.version", "unk");
                    isSolaris8 = version.equals("5.8");
                    isSolaris9 = version.equals("5.9");
                } else {
                    isWindows = osName.startsWith("Windows");
                    if (isWindows) {
                        String eudcFile = SunGraphicsEnvironment.eudcFontFileName;
                        if (eudcFile != null) {
                            try {
                                eudcFont = new TrueTypeFont(eudcFile, null, 0, true);
                            } catch (FontFormatException e) {
                            }
                        }
                        String prop = System.getProperty("java2d.font.usePlatformFont");
                        if (("true".equals(prop) || getPlatformFontVar())) {
                            usePlatformFontMetrics = true;
                            System.out.println("Enabling platform font metrics for win32. This is an unsupported option.");
                            System.out.println("This yields incorrect composite font metrics as reported by 1.1.x releases.");
                            System.out.println("It is appropriate only for use by applications which do not use any Java 2");
                            System.out.println("functionality. This property will be removed in a later release.");
                        }
                    }
                }
                return null;
            }
        });
    }

    private static native void initIDs();

    public static void addToPool(FileFont font) {
        FileFont fontFileToClose = null;
        int freeSlot = -1;
        synchronized (fontFileCache) {
            for (int i = 0; i < CHANNELPOOLSIZE; i++) {
                if (fontFileCache[i] == font) {
                    return;
                }
                if (fontFileCache[i] == null && freeSlot < 0) {
                    freeSlot = i;
                }
            }
            if (freeSlot >= 0) {
                fontFileCache[freeSlot] = font;
                return;
            } else {
                fontFileToClose = fontFileCache[lastPoolIndex];
                fontFileCache[lastPoolIndex] = font;
                lastPoolIndex = (lastPoolIndex + 1) % CHANNELPOOLSIZE;
            }
        }
        if (fontFileToClose != null) {
            fontFileToClose.close();
        }
    }

    public static void removeFromPool(FileFont font) {
        synchronized (fontFileCache) {
            for (int i = 0; i < CHANNELPOOLSIZE; i++) {
                if (fontFileCache[i] == font) {
                    fontFileCache[i] = null;
                }
            }
        }
    }

    public static boolean fontSupportsDefaultEncoding(Font font) {
        return getFont2D(font) instanceof CompositeFont;
    }

    public static FontUIResource getCompositeFontUIResource(Font font) {
        FontUIResource fuir = new FontUIResource(font.getName(), font.getStyle(), font.getSize());
        Font2D font2D = getFont2D(font);
        if (!(font2D instanceof PhysicalFont)) {
            return fuir;
        }
        CompositeFont dialog2D = (CompositeFont) findFont2D("dialog", font.getStyle(), NO_FALLBACK);
        if (dialog2D == null) {
            return fuir;
        }
        PhysicalFont physicalFont = (PhysicalFont) font2D;
        CompositeFont compFont = new CompositeFont(physicalFont, dialog2D);
        setFont2D(fuir, compFont.handle);
        setCreatedFont(fuir);
        return fuir;
    }

    public static Font2DHandle getNewComposite(String family, int style, Font2DHandle handle) {
        if (!(handle.font2D instanceof CompositeFont)) {
            return handle;
        }
        CompositeFont oldComp = (CompositeFont) handle.font2D;
        PhysicalFont oldFont = oldComp.getSlotFont(0);
        if (family == null) {
            family = oldFont.getFamilyName(null);
        }
        if (style == -1) {
            style = oldComp.getStyle();
        }
        Font2D newFont = findFont2D(family, style, NO_FALLBACK);
        if (!(newFont instanceof PhysicalFont)) {
            newFont = oldFont;
        }
        PhysicalFont physicalFont = (PhysicalFont) newFont;
        CompositeFont dialog2D = (CompositeFont) findFont2D("dialog", style, NO_FALLBACK);
        if (dialog2D == null) {
            return handle;
        }
        CompositeFont compFont = new CompositeFont(physicalFont, dialog2D);
        Font2DHandle newHandle = new Font2DHandle(compFont);
        return newHandle;
    }

    public static native void setFont2D(Font font, Font2DHandle font2DHandle);

    private static native boolean isCreatedFont(Font font);

    private static native void setCreatedFont(Font font);

    public static void registerCompositeFont(String compositeName, String[] componentFileNames, String[] componentNames, int numMetricsSlots, int[] exclusionRanges, int[] exclusionMaxIndex, boolean defer) {
        CompositeFont cf = new CompositeFont(compositeName, componentFileNames, componentNames, numMetricsSlots, exclusionRanges, exclusionMaxIndex, defer);
        addCompositeToFontList(cf, Font2D.FONT_CONFIG_RANK);
        synchronized (compFonts) {
            compFonts[maxCompFont++] = cf;
        }
    }

    public static void registerCompositeFont(String compositeName, String[] componentFileNames, String[] componentNames, int numMetricsSlots, int[] exclusionRanges, int[] exclusionMaxIndex, boolean defer, ConcurrentHashMap<String, Font2D> altNameCache) {
        CompositeFont cf = new CompositeFont(compositeName, componentFileNames, componentNames, numMetricsSlots, exclusionRanges, exclusionMaxIndex, defer);
        Font2D oldFont = (Font2D) altNameCache.get(compositeName.toLowerCase(Locale.ENGLISH));
        if (oldFont instanceof CompositeFont) {
            oldFont.handle.font2D = cf;
        }
        altNameCache.put(compositeName.toLowerCase(Locale.ENGLISH), cf);
    }

    private static void addCompositeToFontList(CompositeFont f, int rank) {
        if (logging) {
            logger.info("Add to Family " + f.familyName + ", Font " + f.fullName + " rank=" + rank);
        }
        f.setRank(rank);
        compositeFonts.put(f.fullName, f);
        fullNameToFont.put(f.fullName.toLowerCase(Locale.ENGLISH), f);
        FontFamily family = FontFamily.getFamily(f.familyName);
        if (family == null) {
            family = new FontFamily(f.familyName, true, rank);
        }
        family.setFont(f, f.style);
    }

    private static PhysicalFont addToFontList(PhysicalFont f, int rank) {
        String fontName = f.fullName;
        String familyName = f.familyName;
        if (fontName == null || "".equals(fontName)) {
            return null;
        }
        if (compositeFonts.containsKey(fontName)) {
            return null;
        }
        f.setRank(rank);
        if (!physicalFonts.containsKey(fontName)) {
            if (logging) {
                logger.info("Add to Family " + familyName + ", Font " + fontName + " rank=" + rank);
            }
            physicalFonts.put(fontName, f);
            FontFamily family = FontFamily.getFamily(familyName);
            if (family == null) {
                family = new FontFamily(familyName, false, rank);
                family.setFont(f, f.style);
            } else if (family.getRank() >= rank) {
                family.setFont(f, f.style);
            }
            fullNameToFont.put(fontName.toLowerCase(Locale.ENGLISH), f);
            return f;
        } else {
            PhysicalFont newFont = f;
            PhysicalFont oldFont = physicalFonts.get(fontName);
            if (oldFont == null) {
                return null;
            }
            if (oldFont.getRank() >= rank) {
                if (oldFont.mapper != null && rank > Font2D.FONT_CONFIG_RANK) {
                    return oldFont;
                }
                if (oldFont.getRank() == rank) {
                    if (oldFont instanceof TrueTypeFont && newFont instanceof TrueTypeFont) {
                        TrueTypeFont oldTTFont = (TrueTypeFont) oldFont;
                        TrueTypeFont newTTFont = (TrueTypeFont) newFont;
                        if (oldTTFont.fileSize >= newTTFont.fileSize) {
                            return oldFont;
                        }
                    } else {
                        return oldFont;
                    }
                }
                if (oldFont.platName.startsWith(SunGraphicsEnvironment.jreFontDirName)) {
                    if (logging) {
                        logger.warning("Unexpected attempt to replace a JRE " + " font " + fontName + " from " + oldFont.platName + " with " + newFont.platName);
                    }
                    return oldFont;
                }
                if (logging) {
                    logger.info("Replace in Family " + familyName + ",Font " + fontName + " new rank=" + rank + " from " + oldFont.platName + " with " + newFont.platName);
                }
                replaceFont(oldFont, newFont);
                physicalFonts.put(fontName, newFont);
                fullNameToFont.put(fontName.toLowerCase(Locale.ENGLISH), newFont);
                FontFamily family = FontFamily.getFamily(familyName);
                if (family == null) {
                    family = new FontFamily(familyName, false, rank);
                    family.setFont(newFont, newFont.style);
                } else if (family.getRank() >= rank) {
                    family.setFont(newFont, newFont.style);
                }
                return newFont;
            } else {
                return oldFont;
            }
        }
    }

    public static Font2D[] getRegisteredFonts() {
        PhysicalFont[] physFonts = getPhysicalFonts();
        int mcf = maxCompFont;
        Font2D[] regFonts = new Font2D[physFonts.length + mcf];
        System.arraycopy(compFonts, 0, regFonts, 0, mcf);
        System.arraycopy(physFonts, 0, regFonts, mcf, physFonts.length);
        return regFonts;
    }

    public static PhysicalFont[] getPhysicalFonts() {
        return physicalFonts.values().toArray(new PhysicalFont[0]);
    }

    private static final class FontRegistrationInfo {

        String fontFilePath;

        String[] nativeNames;

        int fontFormat;

        boolean javaRasterizer;

        int fontRank;

        FontRegistrationInfo(String fontPath, String[] names, int format, boolean useJavaRasterizer, int rank) {
            this.fontFilePath = fontPath;
            this.nativeNames = names;
            this.fontFormat = format;
            this.javaRasterizer = useJavaRasterizer;
            this.fontRank = rank;
        }
    }

    private static final ConcurrentHashMap<String, FontRegistrationInfo> deferredFontFiles = new ConcurrentHashMap<String, FontRegistrationInfo>();

    private static final ConcurrentHashMap<String, Font2DHandle> initialisedFonts = new ConcurrentHashMap<String, Font2DHandle>();

    public static synchronized void initialiseDeferredFonts() {
        for (String fileName : deferredFontFiles.keySet()) {
            initialiseDeferredFont(fileName);
        }
    }

    public static synchronized void registerDeferredJREFonts(String jreDir) {
        for (FontRegistrationInfo info : deferredFontFiles.values()) {
            if (info.fontFilePath != null && info.fontFilePath.startsWith(jreDir)) {
                initialiseDeferredFont(info.fontFilePath);
            }
        }
    }

    private static PhysicalFont findJREDeferredFont(String name, int style) {
        PhysicalFont physicalFont;
        String nameAndStyle = name.toLowerCase(Locale.ENGLISH) + style;
        String fileName = jreFontMap.get(nameAndStyle);
        if (fileName != null) {
            initSGEnv();
            fileName = SunGraphicsEnvironment.jreFontDirName + File.separator + fileName;
            if (deferredFontFiles.get(fileName) != null) {
                physicalFont = initialiseDeferredFont(fileName);
                if (physicalFont != null && (physicalFont.getFontName(null).equalsIgnoreCase(name) || physicalFont.getFamilyName(null).equalsIgnoreCase(name)) && physicalFont.style == style) {
                    return physicalFont;
                }
            }
        }
        if (noOtherJREFontFiles) {
            return null;
        }
        synchronized (jreLucidaFontFiles) {
            if (jreOtherFontFiles == null) {
                HashSet<String> otherFontFiles = new HashSet<String>();
                for (String deferredFile : deferredFontFiles.keySet()) {
                    File file = new File(deferredFile);
                    String dir = file.getParent();
                    String fname = file.getName();
                    if (dir == null || !dir.equals(SunGraphicsEnvironment.jreFontDirName) || jreLucidaFontFiles.contains(fname)) {
                        continue;
                    }
                    otherFontFiles.add(deferredFile);
                }
                jreOtherFontFiles = otherFontFiles.toArray(STR_ARRAY);
                if (jreOtherFontFiles.length == 0) {
                    noOtherJREFontFiles = true;
                }
            }
            for (int i = 0; i < jreOtherFontFiles.length; i++) {
                fileName = jreOtherFontFiles[i];
                if (fileName == null) {
                    continue;
                }
                jreOtherFontFiles[i] = null;
                physicalFont = initialiseDeferredFont(fileName);
                if (physicalFont != null && (physicalFont.getFontName(null).equalsIgnoreCase(name) || physicalFont.getFamilyName(null).equalsIgnoreCase(name)) && physicalFont.style == style) {
                    return physicalFont;
                }
            }
        }
        return null;
    }

    private static PhysicalFont findOtherDeferredFont(String name, int style) {
        for (String fileName : deferredFontFiles.keySet()) {
            File file = new File(fileName);
            String dir = file.getParent();
            String fname = file.getName();
            if (dir != null && dir.equals(SunGraphicsEnvironment.jreFontDirName) && jreLucidaFontFiles.contains(fname)) {
                continue;
            }
            PhysicalFont physicalFont = initialiseDeferredFont(fileName);
            if (physicalFont != null && (physicalFont.getFontName(null).equalsIgnoreCase(name) || physicalFont.getFamilyName(null).equalsIgnoreCase(name)) && physicalFont.style == style) {
                return physicalFont;
            }
        }
        return null;
    }

    private static PhysicalFont findDeferredFont(String name, int style) {
        PhysicalFont physicalFont = findJREDeferredFont(name, style);
        if (physicalFont != null) {
            return physicalFont;
        } else {
            return findOtherDeferredFont(name, style);
        }
    }

    public static void registerDeferredFont(String fileNameKey, String fullPathName, String[] nativeNames, int fontFormat, boolean useJavaRasterizer, int fontRank) {
        FontRegistrationInfo regInfo = new FontRegistrationInfo(fullPathName, nativeNames, fontFormat, useJavaRasterizer, fontRank);
        deferredFontFiles.put(fileNameKey, regInfo);
    }

    public static synchronized PhysicalFont initialiseDeferredFont(String fileNameKey) {
        if (fileNameKey == null) {
            return null;
        }
        if (logging) {
            logger.info("Opening deferred font file " + fileNameKey);
        }
        PhysicalFont physicalFont;
        FontRegistrationInfo regInfo = deferredFontFiles.get(fileNameKey);
        if (regInfo != null) {
            deferredFontFiles.remove(fileNameKey);
            physicalFont = registerFontFile(regInfo.fontFilePath, regInfo.nativeNames, regInfo.fontFormat, regInfo.javaRasterizer, regInfo.fontRank);
            if (physicalFont != null) {
                initialisedFonts.put(fileNameKey, physicalFont.handle);
            } else {
                initialisedFonts.put(fileNameKey, getDefaultPhysicalFont().handle);
            }
        } else {
            Font2DHandle handle = initialisedFonts.get(fileNameKey);
            if (handle == null) {
                physicalFont = getDefaultPhysicalFont();
            } else {
                physicalFont = (PhysicalFont) (handle.font2D);
            }
        }
        return physicalFont;
    }

    public static PhysicalFont registerFontFile(String fileName, String[] nativeNames, int fontFormat, boolean useJavaRasterizer, int fontRank) {
        PhysicalFont regFont = registeredFontFiles.get(fileName);
        if (regFont != null) {
            return regFont;
        }
        PhysicalFont physicalFont = null;
        try {
            String name;
            switch(fontFormat) {
                case FontManager.FONTFORMAT_TRUETYPE:
                    int fn = 0;
                    TrueTypeFont ttf;
                    do {
                        ttf = new TrueTypeFont(fileName, nativeNames, fn++, useJavaRasterizer);
                        PhysicalFont pf = addToFontList(ttf, fontRank);
                        if (physicalFont == null) {
                            physicalFont = pf;
                        }
                    } while (fn < ttf.getFontCount());
                    break;
                case FontManager.FONTFORMAT_TYPE1:
                    Type1Font t1f = new Type1Font(fileName, nativeNames);
                    physicalFont = addToFontList(t1f, fontRank);
                    break;
                case FontManager.FONTFORMAT_NATIVE:
                    NativeFont nf = new NativeFont(fileName, false);
                    physicalFont = addToFontList(nf, fontRank);
                default:
            }
            if (logging) {
                logger.info("Registered file " + fileName + " as font " + physicalFont + " rank=" + fontRank);
            }
        } catch (FontFormatException ffe) {
            if (logging) {
                logger.warning("Unusable font: " + fileName + " " + ffe.toString());
            }
        }
        if (physicalFont != null && fontFormat != FontManager.FONTFORMAT_NATIVE) {
            registeredFontFiles.put(fileName, physicalFont);
        }
        return physicalFont;
    }

    public static void registerFonts(String[] fileNames, String[][] nativeNames, int fontCount, int fontFormat, boolean useJavaRasterizer, int fontRank, boolean defer) {
        for (int i = 0; i < fontCount; i++) {
            if (defer) {
                registerDeferredFont(fileNames[i], fileNames[i], nativeNames[i], fontFormat, useJavaRasterizer, fontRank);
            } else {
                registerFontFile(fileNames[i], nativeNames[i], fontFormat, useJavaRasterizer, fontRank);
            }
        }
    }

    public static PhysicalFont getDefaultPhysicalFont() {
        if (defaultPhysicalFont == null) {
            defaultPhysicalFont = (PhysicalFont) findFont2D("Lucida Sans Regular", Font.PLAIN, NO_FALLBACK);
            if (defaultPhysicalFont == null) {
                defaultPhysicalFont = (PhysicalFont) findFont2D("Arial", Font.PLAIN, NO_FALLBACK);
            }
            if (defaultPhysicalFont == null) {
                Iterator i = physicalFonts.values().iterator();
                if (i.hasNext()) {
                    defaultPhysicalFont = (PhysicalFont) i.next();
                } else {
                    throw new Error("Probable fatal error:No fonts found.");
                }
            }
        }
        return defaultPhysicalFont;
    }

    public static CompositeFont getDefaultLogicalFont(int style) {
        return (CompositeFont) findFont2D("dialog", style, NO_FALLBACK);
    }

    private static String dotStyleStr(int num) {
        switch(num) {
            case Font.BOLD:
                return ".bold";
            case Font.ITALIC:
                return ".italic";
            case Font.ITALIC | Font.BOLD:
                return ".bolditalic";
            default:
                return ".plain";
        }
    }

    static void initSGEnv() {
        if (sgEnv == null) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            if (ge instanceof HeadlessGraphicsEnvironment) {
                HeadlessGraphicsEnvironment hgEnv = (HeadlessGraphicsEnvironment) ge;
                sgEnv = (SunGraphicsEnvironment) hgEnv.getSunGraphicsEnvironment();
            } else {
                sgEnv = (SunGraphicsEnvironment) ge;
            }
        }
    }

    private static native void populateFontFileNameMap(HashMap<String, String> fontToFileMap, HashMap<String, String> fontToFamilyNameMap, HashMap<String, ArrayList<String>> familyToFontListMap, Locale locale);

    private static HashMap<String, String> fontToFileMap = null;

    private static HashMap<String, String> fontToFamilyNameMap = null;

    private static HashMap<String, ArrayList<String>> familyToFontListMap = null;

    private static String[] pathDirs = null;

    private static boolean haveCheckedUnreferencedFontFiles;

    private static String[] getFontFilesFromPath(boolean noType1) {
        final FilenameFilter filter;
        if (noType1) {
            filter = SunGraphicsEnvironment.ttFilter;
        } else {
            filter = new SunGraphicsEnvironment.TTorT1Filter();
        }
        return (String[]) AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                if (pathDirs.length == 1) {
                    File dir = new File(pathDirs[0]);
                    String[] files = dir.list(filter);
                    if (files == null) {
                        return new String[0];
                    }
                    for (int f = 0; f < files.length; f++) {
                        files[f] = files[f].toLowerCase();
                    }
                    return files;
                } else {
                    ArrayList<String> fileList = new ArrayList<String>();
                    for (int i = 0; i < pathDirs.length; i++) {
                        File dir = new File(pathDirs[i]);
                        String[] files = dir.list(filter);
                        if (files == null) {
                            continue;
                        }
                        for (int f = 0; f < files.length; f++) {
                            fileList.add(files[f].toLowerCase());
                        }
                    }
                    return fileList.toArray(STR_ARRAY);
                }
            }
        });
    }

    private static void resolveWindowsFonts() {
        ArrayList<String> unmappedFontNames = null;
        for (String font : fontToFamilyNameMap.keySet()) {
            String file = fontToFileMap.get(font);
            if (file == null) {
                if (font.indexOf("  ") > 0) {
                    String newName = font.replaceFirst("  ", " ");
                    file = fontToFileMap.get(newName);
                    if (file != null && !fontToFamilyNameMap.containsKey(newName)) {
                        fontToFileMap.remove(newName);
                        fontToFileMap.put(font, file);
                    }
                } else if (font.equals("marlett")) {
                    fontToFileMap.put(font, "marlett.ttf");
                } else if (font.equals("david")) {
                    file = fontToFileMap.get("david regular");
                    if (file != null) {
                        fontToFileMap.remove("david regular");
                        fontToFileMap.put("david", file);
                    }
                } else {
                    if (unmappedFontNames == null) {
                        unmappedFontNames = new ArrayList<String>();
                    }
                    unmappedFontNames.add(font);
                }
            }
        }
        if (unmappedFontNames != null) {
            HashSet<String> unmappedFontFiles = new HashSet<String>();
            HashMap<String, String> ffmapCopy = (HashMap<String, String>) (fontToFileMap.clone());
            for (String key : fontToFamilyNameMap.keySet()) {
                ffmapCopy.remove(key);
            }
            for (String key : ffmapCopy.keySet()) {
                unmappedFontFiles.add(ffmapCopy.get(key));
                fontToFileMap.remove(key);
            }
            resolveFontFiles(unmappedFontFiles, unmappedFontNames);
            if (unmappedFontNames.size() > 0) {
                ArrayList<String> registryFiles = new ArrayList<String>();
                for (String regFile : fontToFileMap.values()) {
                    registryFiles.add(regFile.toLowerCase());
                }
                for (String pathFile : getFontFilesFromPath(true)) {
                    if (!registryFiles.contains(pathFile)) {
                        unmappedFontFiles.add(pathFile);
                    }
                }
                resolveFontFiles(unmappedFontFiles, unmappedFontNames);
            }
            if (unmappedFontNames.size() > 0) {
                int sz = unmappedFontNames.size();
                for (int i = 0; i < sz; i++) {
                    String name = unmappedFontNames.get(i);
                    String familyName = fontToFamilyNameMap.get(name);
                    if (familyName != null) {
                        ArrayList family = familyToFontListMap.get(familyName);
                        if (family != null) {
                            if (family.size() <= 1) {
                                familyToFontListMap.remove(familyName);
                            }
                        }
                    }
                    fontToFamilyNameMap.remove(name);
                    if (logging) {
                        logger.info("No file for font:" + name);
                    }
                }
            }
        }
    }

    private static synchronized void checkForUnreferencedFontFiles() {
        if (haveCheckedUnreferencedFontFiles) {
            return;
        }
        haveCheckedUnreferencedFontFiles = true;
        if (!isWindows) {
            return;
        }
        ArrayList<String> registryFiles = new ArrayList<String>();
        for (String regFile : fontToFileMap.values()) {
            registryFiles.add(regFile.toLowerCase());
        }
        HashMap<String, String> fontToFileMap2 = null;
        HashMap<String, String> fontToFamilyNameMap2 = null;
        HashMap<String, ArrayList<String>> familyToFontListMap2 = null;
        ;
        for (String pathFile : getFontFilesFromPath(false)) {
            if (!registryFiles.contains(pathFile)) {
                if (logging) {
                    logger.info("Found non-registry file : " + pathFile);
                }
                PhysicalFont f = registerFontFile(getPathName(pathFile));
                if (f == null) {
                    continue;
                }
                if (fontToFileMap2 == null) {
                    fontToFileMap2 = new HashMap<String, String>(fontToFileMap);
                    fontToFamilyNameMap2 = new HashMap<String, String>(fontToFamilyNameMap);
                    familyToFontListMap2 = new HashMap<String, ArrayList<String>>(familyToFontListMap);
                }
                String fontName = f.getFontName(null);
                String family = f.getFamilyName(null);
                String familyLC = family.toLowerCase();
                fontToFamilyNameMap2.put(fontName, family);
                fontToFileMap2.put(fontName, pathFile);
                ArrayList<String> fonts = familyToFontListMap2.get(familyLC);
                if (fonts == null) {
                    fonts = new ArrayList<String>();
                } else {
                    fonts = new ArrayList<String>(fonts);
                }
                fonts.add(fontName);
                familyToFontListMap2.put(familyLC, fonts);
            }
        }
        if (fontToFileMap2 != null) {
            fontToFileMap = fontToFileMap2;
            familyToFontListMap = familyToFontListMap2;
            fontToFamilyNameMap = fontToFamilyNameMap2;
        }
    }

    private static void resolveFontFiles(HashSet<String> unmappedFiles, ArrayList<String> unmappedFonts) {
        Locale l = SunToolkit.getStartupLocale();
        for (String file : unmappedFiles) {
            try {
                int fn = 0;
                TrueTypeFont ttf;
                String fullPath = getPathName(file);
                if (logging) {
                    logger.info("Trying to resolve file " + fullPath);
                }
                do {
                    ttf = new TrueTypeFont(fullPath, null, fn++, true);
                    String fontName = ttf.getFontName(l).toLowerCase();
                    if (unmappedFonts.contains(fontName)) {
                        fontToFileMap.put(fontName, file);
                        unmappedFonts.remove(fontName);
                        if (logging) {
                            logger.info("Resolved absent registry entry for " + fontName + " located in " + fullPath);
                        }
                    }
                } while (fn < ttf.getFontCount());
            } catch (Exception e) {
            }
        }
    }

    private static synchronized HashMap<String, String> getFullNameToFileMap() {
        if (fontToFileMap == null) {
            initSGEnv();
            pathDirs = sgEnv.getPlatformFontDirs();
            fontToFileMap = new HashMap<String, String>(100);
            fontToFamilyNameMap = new HashMap<String, String>(100);
            familyToFontListMap = new HashMap<String, ArrayList<String>>(50);
            populateFontFileNameMap(fontToFileMap, fontToFamilyNameMap, familyToFontListMap, Locale.ENGLISH);
            if (isWindows) {
                resolveWindowsFonts();
            }
            if (logging) {
                logPlatformFontInfo();
            }
        }
        return fontToFileMap;
    }

    private static void logPlatformFontInfo() {
        for (int i = 0; i < pathDirs.length; i++) {
            logger.info("fontdir=" + pathDirs[i]);
        }
        for (String keyName : fontToFileMap.keySet()) {
            logger.info("font=" + keyName + " file=" + fontToFileMap.get(keyName));
        }
        for (String keyName : fontToFamilyNameMap.keySet()) {
            logger.info("font=" + keyName + " family=" + fontToFamilyNameMap.get(keyName));
        }
        for (String keyName : familyToFontListMap.keySet()) {
            logger.info("family=" + keyName + " fonts=" + familyToFontListMap.get(keyName));
        }
    }

    public static String[] getFontNamesFromPlatform() {
        if (getFullNameToFileMap().size() == 0) {
            return null;
        }
        checkForUnreferencedFontFiles();
        ArrayList<String> fontNames = new ArrayList<String>();
        for (ArrayList<String> a : familyToFontListMap.values()) {
            for (String s : a) {
                fontNames.add(s);
            }
        }
        return fontNames.toArray(STR_ARRAY);
    }

    public static boolean gotFontsFromPlatform() {
        return getFullNameToFileMap().size() != 0;
    }

    public static String getFileNameForFontName(String fontName) {
        String fontNameLC = fontName.toLowerCase(Locale.ENGLISH);
        return fontToFileMap.get(fontNameLC);
    }

    private static PhysicalFont registerFontFile(String file) {
        if (new File(file).isAbsolute() && !registeredFontFiles.contains(file)) {
            int fontFormat = FONTFORMAT_NONE;
            int fontRank = Font2D.UNKNOWN_RANK;
            if (SunGraphicsEnvironment.ttFilter.accept(null, file)) {
                fontFormat = FONTFORMAT_TRUETYPE;
                fontRank = Font2D.TTF_RANK;
            } else if (SunGraphicsEnvironment.t1Filter.accept(null, file)) {
                fontFormat = FONTFORMAT_TYPE1;
                fontRank = Font2D.TYPE1_RANK;
            }
            if (fontFormat == FONTFORMAT_NONE) {
                return null;
            }
            return registerFontFile(file, null, fontFormat, false, fontRank);
        }
        return null;
    }

    public static void registerOtherFontFiles(HashSet registeredFontFiles) {
        if (getFullNameToFileMap().size() == 0) {
            return;
        }
        for (String file : fontToFileMap.values()) {
            registerFontFile(file);
        }
    }

    public static boolean getFamilyNamesFromPlatform(TreeMap<String, String> familyNames, Locale requestedLocale) {
        if (getFullNameToFileMap().size() == 0) {
            return false;
        }
        checkForUnreferencedFontFiles();
        for (String name : fontToFamilyNameMap.values()) {
            familyNames.put(name.toLowerCase(requestedLocale), name);
        }
        return true;
    }

    private static String getPathName(String s) {
        File f = new File(s);
        if (f.isAbsolute()) {
            return s;
        } else if (pathDirs.length == 1) {
            return pathDirs[0] + File.separator + s;
        } else {
            for (int p = 0; p < pathDirs.length; p++) {
                f = new File(pathDirs[p] + File.separator + s);
                if (f.exists()) {
                    return f.getAbsolutePath();
                }
            }
        }
        return s;
    }

    private static Font2D findFontFromPlatform(String lcName, int style) {
        if (getFullNameToFileMap().size() == 0) {
            return null;
        }
        ArrayList<String> family = null;
        String fontFile = null;
        String familyName = fontToFamilyNameMap.get(lcName);
        if (familyName != null) {
            fontFile = fontToFileMap.get(lcName);
            family = familyToFontListMap.get(familyName.toLowerCase(Locale.ENGLISH));
        } else {
            family = familyToFontListMap.get(lcName);
            if (family != null && family.size() > 0) {
                String lcFontName = family.get(0).toLowerCase(Locale.ENGLISH);
                if (lcFontName != null) {
                    familyName = fontToFamilyNameMap.get(lcFontName);
                }
            }
        }
        if (family == null || familyName == null) {
            return null;
        }
        String[] fontList = (String[]) family.toArray(STR_ARRAY);
        if (fontList.length == 0) {
            return null;
        }
        for (int f = 0; f < fontList.length; f++) {
            String fontNameLC = fontList[f].toLowerCase(Locale.ENGLISH);
            String fileName = fontToFileMap.get(fontNameLC);
            if (fileName == null) {
                if (logging) {
                    logger.info("Platform lookup : No file for font " + fontList[f] + " in family " + familyName);
                }
                return null;
            }
        }
        PhysicalFont physicalFont = null;
        if (fontFile != null) {
            physicalFont = registerFontFile(getPathName(fontFile), null, FONTFORMAT_TRUETYPE, false, Font2D.TTF_RANK);
        }
        for (int f = 0; f < fontList.length; f++) {
            String fontNameLC = fontList[f].toLowerCase(Locale.ENGLISH);
            String fileName = fontToFileMap.get(fontNameLC);
            if (fontFile != null && fontFile.equals(fileName)) {
                continue;
            }
            registerFontFile(getPathName(fileName), null, FONTFORMAT_TRUETYPE, false, Font2D.TTF_RANK);
        }
        Font2D font = null;
        FontFamily fontFamily = FontFamily.getFamily(familyName);
        if (physicalFont != null) {
            style |= physicalFont.style;
        }
        if (fontFamily != null) {
            font = fontFamily.getFont(style);
            if (font == null) {
                font = fontFamily.getClosestStyle(style);
            }
        }
        return font;
    }

    private static ConcurrentHashMap<String, Font2D> fontNameCache = new ConcurrentHashMap<String, Font2D>();

    public static Font2D findFont2D(String name, int style, int fallback) {
        String lowerCaseName = name.toLowerCase(Locale.ENGLISH);
        String mapName = lowerCaseName + dotStyleStr(style);
        Font2D font;
        if (usingPerAppContextComposites) {
            ConcurrentHashMap<String, Font2D> altNameCache = (ConcurrentHashMap<String, Font2D>) AppContext.getAppContext().get(CompositeFont.class);
            if (altNameCache != null) {
                font = (Font2D) altNameCache.get(mapName);
            } else {
                font = null;
            }
        } else {
            font = fontNameCache.get(mapName);
        }
        if (font != null) {
            return font;
        }
        if (logging) {
            logger.info("Search for font: " + name);
        }
        if (isWindows) {
            if (lowerCaseName.equals("ms sans serif")) {
                name = "sansserif";
            } else if (lowerCaseName.equals("ms serif")) {
                name = "serif";
            }
        }
        if (lowerCaseName.equals("default")) {
            name = "dialog";
        }
        FontFamily family = FontFamily.getFamily(name);
        if (family != null) {
            font = family.getFontWithExactStyleMatch(style);
            if (font == null) {
                font = findDeferredFont(name, style);
            }
            if (font == null) {
                font = family.getFont(style);
            }
            if (font == null) {
                font = family.getClosestStyle(style);
            }
            if (font != null) {
                fontNameCache.put(mapName, font);
                return font;
            }
        }
        font = fullNameToFont.get(lowerCaseName);
        if (font != null) {
            if (font.style == style || style == Font.PLAIN) {
                fontNameCache.put(mapName, font);
                return font;
            } else {
                family = FontFamily.getFamily(font.getFamilyName(null));
                if (family != null) {
                    Font2D familyFont = family.getFont(style | font.style);
                    if (familyFont != null) {
                        fontNameCache.put(mapName, familyFont);
                        return familyFont;
                    } else {
                        familyFont = family.getClosestStyle(style | font.style);
                        if (familyFont != null) {
                            if (familyFont.canDoStyle(style | font.style)) {
                                fontNameCache.put(mapName, familyFont);
                                return familyFont;
                            }
                        }
                    }
                }
            }
        }
        if (sgEnv == null) {
            initSGEnv();
            return findFont2D(name, style, fallback);
        }
        if (isWindows) {
            if (deferredFontFiles.size() > 0) {
                font = findJREDeferredFont(lowerCaseName, style);
                if (font != null) {
                    fontNameCache.put(mapName, font);
                    return font;
                }
            }
            font = findFontFromPlatform(lowerCaseName, style);
            if (font != null) {
                if (logging) {
                    logger.info("Found font via platform API for request:\"" + name + "\":, style=" + style + " found font: " + font);
                }
                fontNameCache.put(mapName, font);
                return font;
            }
        }
        if (deferredFontFiles.size() > 0) {
            font = findDeferredFont(name, style);
            if (font != null) {
                fontNameCache.put(mapName, font);
                return font;
            }
        }
        if (isSolaris && !loaded1dot0Fonts) {
            if (lowerCaseName.equals("timesroman")) {
                font = findFont2D("serif", style, fallback);
                fontNameCache.put(mapName, font);
            }
            sgEnv.register1dot0Fonts();
            loaded1dot0Fonts = true;
            Font2D ff = findFont2D(name, style, fallback);
            return ff;
        }
        if (fontsAreRegistered || fontsAreRegisteredPerAppContext) {
            Hashtable<String, FontFamily> familyTable = null;
            Hashtable<String, Font2D> nameTable;
            if (fontsAreRegistered) {
                familyTable = createdByFamilyName;
                nameTable = createdByFullName;
            } else {
                AppContext appContext = AppContext.getAppContext();
                familyTable = (Hashtable<String, FontFamily>) appContext.get(regFamilyKey);
                nameTable = (Hashtable<String, Font2D>) appContext.get(regFullNameKey);
            }
            family = familyTable.get(lowerCaseName);
            if (family != null) {
                font = family.getFontWithExactStyleMatch(style);
                if (font == null) {
                    font = family.getFont(style);
                }
                if (font == null) {
                    font = family.getClosestStyle(style);
                }
                if (font != null) {
                    if (fontsAreRegistered) {
                        fontNameCache.put(mapName, font);
                    }
                    return font;
                }
            }
            font = nameTable.get(lowerCaseName);
            if (font != null) {
                if (fontsAreRegistered) {
                    fontNameCache.put(mapName, font);
                }
                return font;
            }
        }
        if (!loadedAllFonts) {
            if (logging) {
                logger.info("Load fonts looking for:" + name);
            }
            sgEnv.loadFonts();
            loadedAllFonts = true;
            return findFont2D(name, style, fallback);
        }
        if (!loadedAllFontFiles) {
            if (logging) {
                logger.info("Load font files looking for:" + name);
            }
            sgEnv.loadFontFiles();
            loadedAllFontFiles = true;
            return findFont2D(name, style, fallback);
        }
        if ((font = findFont2DAllLocales(name, style)) != null) {
            fontNameCache.put(mapName, font);
            return font;
        }
        if (isWindows) {
            String compatName = sgEnv.getFontConfiguration().getFallbackFamilyName(name, null);
            if (compatName != null) {
                font = findFont2D(compatName, style, fallback);
                fontNameCache.put(mapName, font);
                return font;
            }
        } else if (lowerCaseName.equals("timesroman")) {
            font = findFont2D("serif", style, fallback);
            fontNameCache.put(mapName, font);
            return font;
        } else if (lowerCaseName.equals("helvetica")) {
            font = findFont2D("sansserif", style, fallback);
            fontNameCache.put(mapName, font);
            return font;
        } else if (lowerCaseName.equals("courier")) {
            font = findFont2D("monospaced", style, fallback);
            fontNameCache.put(mapName, font);
            return font;
        }
        if (logging) {
            logger.info("No font found for:" + name);
        }
        switch(fallback) {
            case PHYSICAL_FALLBACK:
                return getDefaultPhysicalFont();
            case LOGICAL_FALLBACK:
                return getDefaultLogicalFont(style);
            default:
                return null;
        }
    }

    public static native Font2D getFont2D(Font font);

    public static boolean usePlatformFontMetrics() {
        return usePlatformFontMetrics;
    }

    static native boolean getPlatformFontVar();

    private static final short US_LCID = 0x0409;

    private static Map<String, Short> lcidMap;

    public static short getLCIDFromLocale(Locale locale) {
        if (locale.equals(Locale.US)) {
            return US_LCID;
        }
        if (lcidMap == null) {
            createLCIDMap();
        }
        String key = locale.toString();
        while (!"".equals(key)) {
            Short lcidObject = (Short) lcidMap.get(key);
            if (lcidObject != null) {
                return lcidObject.shortValue();
            }
            int pos = key.lastIndexOf('_');
            if (pos < 1) {
                return US_LCID;
            }
            key = key.substring(0, pos);
        }
        return US_LCID;
    }

    private static void addLCIDMapEntry(Map<String, Short> map, String key, short value) {
        map.put(key, Short.valueOf(value));
    }

    private static synchronized void createLCIDMap() {
        if (lcidMap != null) {
            return;
        }
        Map<String, Short> map = new HashMap<String, Short>(200);
        addLCIDMapEntry(map, "ar", (short) 0x0401);
        addLCIDMapEntry(map, "bg", (short) 0x0402);
        addLCIDMapEntry(map, "ca", (short) 0x0403);
        addLCIDMapEntry(map, "zh", (short) 0x0404);
        addLCIDMapEntry(map, "cs", (short) 0x0405);
        addLCIDMapEntry(map, "da", (short) 0x0406);
        addLCIDMapEntry(map, "de", (short) 0x0407);
        addLCIDMapEntry(map, "el", (short) 0x0408);
        addLCIDMapEntry(map, "es", (short) 0x040a);
        addLCIDMapEntry(map, "fi", (short) 0x040b);
        addLCIDMapEntry(map, "fr", (short) 0x040c);
        addLCIDMapEntry(map, "iw", (short) 0x040d);
        addLCIDMapEntry(map, "hu", (short) 0x040e);
        addLCIDMapEntry(map, "is", (short) 0x040f);
        addLCIDMapEntry(map, "it", (short) 0x0410);
        addLCIDMapEntry(map, "ja", (short) 0x0411);
        addLCIDMapEntry(map, "ko", (short) 0x0412);
        addLCIDMapEntry(map, "nl", (short) 0x0413);
        addLCIDMapEntry(map, "no", (short) 0x0414);
        addLCIDMapEntry(map, "pl", (short) 0x0415);
        addLCIDMapEntry(map, "pt", (short) 0x0416);
        addLCIDMapEntry(map, "rm", (short) 0x0417);
        addLCIDMapEntry(map, "ro", (short) 0x0418);
        addLCIDMapEntry(map, "ru", (short) 0x0419);
        addLCIDMapEntry(map, "hr", (short) 0x041a);
        addLCIDMapEntry(map, "sk", (short) 0x041b);
        addLCIDMapEntry(map, "sq", (short) 0x041c);
        addLCIDMapEntry(map, "sv", (short) 0x041d);
        addLCIDMapEntry(map, "th", (short) 0x041e);
        addLCIDMapEntry(map, "tr", (short) 0x041f);
        addLCIDMapEntry(map, "ur", (short) 0x0420);
        addLCIDMapEntry(map, "in", (short) 0x0421);
        addLCIDMapEntry(map, "uk", (short) 0x0422);
        addLCIDMapEntry(map, "be", (short) 0x0423);
        addLCIDMapEntry(map, "sl", (short) 0x0424);
        addLCIDMapEntry(map, "et", (short) 0x0425);
        addLCIDMapEntry(map, "lv", (short) 0x0426);
        addLCIDMapEntry(map, "lt", (short) 0x0427);
        addLCIDMapEntry(map, "fa", (short) 0x0429);
        addLCIDMapEntry(map, "vi", (short) 0x042a);
        addLCIDMapEntry(map, "hy", (short) 0x042b);
        addLCIDMapEntry(map, "eu", (short) 0x042d);
        addLCIDMapEntry(map, "mk", (short) 0x042f);
        addLCIDMapEntry(map, "tn", (short) 0x0432);
        addLCIDMapEntry(map, "xh", (short) 0x0434);
        addLCIDMapEntry(map, "zu", (short) 0x0435);
        addLCIDMapEntry(map, "af", (short) 0x0436);
        addLCIDMapEntry(map, "ka", (short) 0x0437);
        addLCIDMapEntry(map, "fo", (short) 0x0438);
        addLCIDMapEntry(map, "hi", (short) 0x0439);
        addLCIDMapEntry(map, "mt", (short) 0x043a);
        addLCIDMapEntry(map, "se", (short) 0x043b);
        addLCIDMapEntry(map, "gd", (short) 0x043c);
        addLCIDMapEntry(map, "ms", (short) 0x043e);
        addLCIDMapEntry(map, "kk", (short) 0x043f);
        addLCIDMapEntry(map, "ky", (short) 0x0440);
        addLCIDMapEntry(map, "sw", (short) 0x0441);
        addLCIDMapEntry(map, "tt", (short) 0x0444);
        addLCIDMapEntry(map, "bn", (short) 0x0445);
        addLCIDMapEntry(map, "pa", (short) 0x0446);
        addLCIDMapEntry(map, "gu", (short) 0x0447);
        addLCIDMapEntry(map, "ta", (short) 0x0449);
        addLCIDMapEntry(map, "te", (short) 0x044a);
        addLCIDMapEntry(map, "kn", (short) 0x044b);
        addLCIDMapEntry(map, "ml", (short) 0x044c);
        addLCIDMapEntry(map, "mr", (short) 0x044e);
        addLCIDMapEntry(map, "sa", (short) 0x044f);
        addLCIDMapEntry(map, "mn", (short) 0x0450);
        addLCIDMapEntry(map, "cy", (short) 0x0452);
        addLCIDMapEntry(map, "gl", (short) 0x0456);
        addLCIDMapEntry(map, "dv", (short) 0x0465);
        addLCIDMapEntry(map, "qu", (short) 0x046b);
        addLCIDMapEntry(map, "mi", (short) 0x0481);
        addLCIDMapEntry(map, "ar_IQ", (short) 0x0801);
        addLCIDMapEntry(map, "zh_CN", (short) 0x0804);
        addLCIDMapEntry(map, "de_CH", (short) 0x0807);
        addLCIDMapEntry(map, "en_GB", (short) 0x0809);
        addLCIDMapEntry(map, "es_MX", (short) 0x080a);
        addLCIDMapEntry(map, "fr_BE", (short) 0x080c);
        addLCIDMapEntry(map, "it_CH", (short) 0x0810);
        addLCIDMapEntry(map, "nl_BE", (short) 0x0813);
        addLCIDMapEntry(map, "no_NO_NY", (short) 0x0814);
        addLCIDMapEntry(map, "pt_PT", (short) 0x0816);
        addLCIDMapEntry(map, "ro_MD", (short) 0x0818);
        addLCIDMapEntry(map, "ru_MD", (short) 0x0819);
        addLCIDMapEntry(map, "sr_CS", (short) 0x081a);
        addLCIDMapEntry(map, "sv_FI", (short) 0x081d);
        addLCIDMapEntry(map, "az_AZ", (short) 0x082c);
        addLCIDMapEntry(map, "se_SE", (short) 0x083b);
        addLCIDMapEntry(map, "ga_IE", (short) 0x083c);
        addLCIDMapEntry(map, "ms_BN", (short) 0x083e);
        addLCIDMapEntry(map, "uz_UZ", (short) 0x0843);
        addLCIDMapEntry(map, "qu_EC", (short) 0x086b);
        addLCIDMapEntry(map, "ar_EG", (short) 0x0c01);
        addLCIDMapEntry(map, "zh_HK", (short) 0x0c04);
        addLCIDMapEntry(map, "de_AT", (short) 0x0c07);
        addLCIDMapEntry(map, "en_AU", (short) 0x0c09);
        addLCIDMapEntry(map, "fr_CA", (short) 0x0c0c);
        addLCIDMapEntry(map, "sr_CS", (short) 0x0c1a);
        addLCIDMapEntry(map, "se_FI", (short) 0x0c3b);
        addLCIDMapEntry(map, "qu_PE", (short) 0x0c6b);
        addLCIDMapEntry(map, "ar_LY", (short) 0x1001);
        addLCIDMapEntry(map, "zh_SG", (short) 0x1004);
        addLCIDMapEntry(map, "de_LU", (short) 0x1007);
        addLCIDMapEntry(map, "en_CA", (short) 0x1009);
        addLCIDMapEntry(map, "es_GT", (short) 0x100a);
        addLCIDMapEntry(map, "fr_CH", (short) 0x100c);
        addLCIDMapEntry(map, "hr_BA", (short) 0x101a);
        addLCIDMapEntry(map, "ar_DZ", (short) 0x1401);
        addLCIDMapEntry(map, "zh_MO", (short) 0x1404);
        addLCIDMapEntry(map, "de_LI", (short) 0x1407);
        addLCIDMapEntry(map, "en_NZ", (short) 0x1409);
        addLCIDMapEntry(map, "es_CR", (short) 0x140a);
        addLCIDMapEntry(map, "fr_LU", (short) 0x140c);
        addLCIDMapEntry(map, "bs_BA", (short) 0x141a);
        addLCIDMapEntry(map, "ar_MA", (short) 0x1801);
        addLCIDMapEntry(map, "en_IE", (short) 0x1809);
        addLCIDMapEntry(map, "es_PA", (short) 0x180a);
        addLCIDMapEntry(map, "fr_MC", (short) 0x180c);
        addLCIDMapEntry(map, "sr_BA", (short) 0x181a);
        addLCIDMapEntry(map, "ar_TN", (short) 0x1c01);
        addLCIDMapEntry(map, "en_ZA", (short) 0x1c09);
        addLCIDMapEntry(map, "es_DO", (short) 0x1c0a);
        addLCIDMapEntry(map, "sr_BA", (short) 0x1c1a);
        addLCIDMapEntry(map, "ar_OM", (short) 0x2001);
        addLCIDMapEntry(map, "en_JM", (short) 0x2009);
        addLCIDMapEntry(map, "es_VE", (short) 0x200a);
        addLCIDMapEntry(map, "ar_YE", (short) 0x2401);
        addLCIDMapEntry(map, "es_CO", (short) 0x240a);
        addLCIDMapEntry(map, "ar_SY", (short) 0x2801);
        addLCIDMapEntry(map, "en_BZ", (short) 0x2809);
        addLCIDMapEntry(map, "es_PE", (short) 0x280a);
        addLCIDMapEntry(map, "ar_JO", (short) 0x2c01);
        addLCIDMapEntry(map, "en_TT", (short) 0x2c09);
        addLCIDMapEntry(map, "es_AR", (short) 0x2c0a);
        addLCIDMapEntry(map, "ar_LB", (short) 0x3001);
        addLCIDMapEntry(map, "en_ZW", (short) 0x3009);
        addLCIDMapEntry(map, "es_EC", (short) 0x300a);
        addLCIDMapEntry(map, "ar_KW", (short) 0x3401);
        addLCIDMapEntry(map, "en_PH", (short) 0x3409);
        addLCIDMapEntry(map, "es_CL", (short) 0x340a);
        addLCIDMapEntry(map, "ar_AE", (short) 0x3801);
        addLCIDMapEntry(map, "es_UY", (short) 0x380a);
        addLCIDMapEntry(map, "ar_BH", (short) 0x3c01);
        addLCIDMapEntry(map, "es_PY", (short) 0x3c0a);
        addLCIDMapEntry(map, "ar_QA", (short) 0x4001);
        addLCIDMapEntry(map, "es_BO", (short) 0x400a);
        addLCIDMapEntry(map, "es_SV", (short) 0x440a);
        addLCIDMapEntry(map, "es_HN", (short) 0x480a);
        addLCIDMapEntry(map, "es_NI", (short) 0x4c0a);
        addLCIDMapEntry(map, "es_PR", (short) 0x500a);
        lcidMap = map;
    }

    public static int getNumFonts() {
        return physicalFonts.size() + maxCompFont;
    }

    private static boolean fontSupportsEncoding(Font font, String encoding) {
        return getFont2D(font).supportsEncoding(encoding);
    }

    public synchronized static native String getFontPath(boolean noType1Fonts);

    public synchronized static native void setNativeFontPath(String fontPath);

    private static Thread fileCloser = null;

    static Vector<File> tmpFontFiles = null;

    public static Font2D createFont2D(File fontFile, int fontFormat, boolean isCopy, CreatedFontTracker tracker) throws FontFormatException {
        String fontFilePath = fontFile.getPath();
        FileFont font2D = null;
        final File fFile = fontFile;
        final CreatedFontTracker _tracker = tracker;
        try {
            switch(fontFormat) {
                case Font.TRUETYPE_FONT:
                    font2D = new TrueTypeFont(fontFilePath, null, 0, true);
                    break;
                case Font.TYPE1_FONT:
                    font2D = new Type1Font(fontFilePath, null, isCopy);
                    break;
                default:
                    throw new FontFormatException("Unrecognised Font Format");
            }
        } catch (FontFormatException e) {
            if (isCopy) {
                java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {

                    public Object run() {
                        if (_tracker != null) {
                            _tracker.subBytes((int) fFile.length());
                        }
                        fFile.delete();
                        return null;
                    }
                });
            }
            throw (e);
        }
        if (isCopy) {
            font2D.setFileToRemove(fontFile, tracker);
            synchronized (FontManager.class) {
                if (tmpFontFiles == null) {
                    tmpFontFiles = new Vector<File>();
                }
                tmpFontFiles.add(fontFile);
                if (fileCloser == null) {
                    final Runnable fileCloserRunnable = new Runnable() {

                        public void run() {
                            java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {

                                public Object run() {
                                    for (int i = 0; i < CHANNELPOOLSIZE; i++) {
                                        if (fontFileCache[i] != null) {
                                            try {
                                                fontFileCache[i].close();
                                            } catch (Exception e) {
                                            }
                                        }
                                    }
                                    if (tmpFontFiles != null) {
                                        File[] files = new File[tmpFontFiles.size()];
                                        files = tmpFontFiles.toArray(files);
                                        for (int f = 0; f < files.length; f++) {
                                            try {
                                                files[f].delete();
                                            } catch (Exception e) {
                                            }
                                        }
                                    }
                                    return null;
                                }
                            });
                        }
                    };
                    java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {

                        public Object run() {
                            ThreadGroup tg = Thread.currentThread().getThreadGroup();
                            for (ThreadGroup tgn = tg; tgn != null; tg = tgn, tgn = tg.getParent()) ;
                            fileCloser = new Thread(tg, fileCloserRunnable);
                            Runtime.getRuntime().addShutdownHook(fileCloser);
                            return null;
                        }
                    });
                }
            }
        }
        return font2D;
    }

    public synchronized static String getFullNameByFileName(String fileName) {
        PhysicalFont[] physFonts = getPhysicalFonts();
        for (int i = 0; i < physFonts.length; i++) {
            if (physFonts[i].platName.equals(fileName)) {
                return (physFonts[i].getFontName(null));
            }
        }
        return null;
    }

    public static synchronized void deRegisterBadFont(Font2D font2D) {
        if (!(font2D instanceof PhysicalFont)) {
            return;
        } else {
            if (logging) {
                logger.severe("Deregister bad font: " + font2D);
            }
            replaceFont((PhysicalFont) font2D, getDefaultPhysicalFont());
        }
    }

    public static synchronized void replaceFont(PhysicalFont oldFont, PhysicalFont newFont) {
        if (oldFont.handle.font2D != oldFont) {
            return;
        }
        if (oldFont == newFont) {
            if (logging) {
                logger.severe("Can't replace bad font with itself " + oldFont);
            }
            PhysicalFont[] physFonts = getPhysicalFonts();
            for (int i = 0; i < physFonts.length; i++) {
                if (physFonts[i] != newFont) {
                    newFont = physFonts[i];
                    break;
                }
            }
            if (oldFont == newFont) {
                if (logging) {
                    logger.severe("This is bad. No good physicalFonts found.");
                }
                return;
            }
        }
        oldFont.handle.font2D = newFont;
        physicalFonts.remove(oldFont.fullName);
        fullNameToFont.remove(oldFont.fullName.toLowerCase(Locale.ENGLISH));
        FontFamily.remove(oldFont);
        if (localeFullNamesToFont != null) {
            Map.Entry[] mapEntries = (Map.Entry[]) localeFullNamesToFont.entrySet().toArray(new Map.Entry[0]);
            for (int i = 0; i < mapEntries.length; i++) {
                if (mapEntries[i].getValue() == oldFont) {
                    try {
                        mapEntries[i].setValue(newFont);
                    } catch (Exception e) {
                        localeFullNamesToFont.remove(mapEntries[i].getKey());
                    }
                }
            }
        }
        for (int i = 0; i < maxCompFont; i++) {
            if (newFont.getRank() > Font2D.FONT_CONFIG_RANK) {
                compFonts[i].replaceComponentFont(oldFont, newFont);
            }
        }
    }

    private static synchronized void loadLocaleNames() {
        if (localeFullNamesToFont != null) {
            return;
        }
        localeFullNamesToFont = new HashMap<String, TrueTypeFont>();
        Font2D[] fonts = getRegisteredFonts();
        for (int i = 0; i < fonts.length; i++) {
            if (fonts[i] instanceof TrueTypeFont) {
                TrueTypeFont ttf = (TrueTypeFont) fonts[i];
                String[] fullNames = ttf.getAllFullNames();
                for (int n = 0; n < fullNames.length; n++) {
                    localeFullNamesToFont.put(fullNames[n], ttf);
                }
                FontFamily family = FontFamily.getFamily(ttf.familyName);
                if (family != null) {
                    FontFamily.addLocaleNames(family, ttf.getAllFamilyNames());
                }
            }
        }
    }

    private static Font2D findFont2DAllLocales(String name, int style) {
        if (logging) {
            logger.info("Searching localised font names for:" + name);
        }
        if (localeFullNamesToFont == null) {
            loadLocaleNames();
        }
        String lowerCaseName = name.toLowerCase();
        Font2D font = null;
        FontFamily family = FontFamily.getLocaleFamily(lowerCaseName);
        if (family != null) {
            font = family.getFont(style);
            if (font == null) {
                font = family.getClosestStyle(style);
            }
            if (font != null) {
                return font;
            }
        }
        synchronized (FontManager.class) {
            font = localeFullNamesToFont.get(name);
        }
        if (font != null) {
            if (font.style == style || style == Font.PLAIN) {
                return font;
            } else {
                family = FontFamily.getFamily(font.getFamilyName(null));
                if (family != null) {
                    Font2D familyFont = family.getFont(style);
                    if (familyFont != null) {
                        return familyFont;
                    } else {
                        familyFont = family.getClosestStyle(style);
                        if (familyFont != null) {
                            if (!familyFont.canDoStyle(style)) {
                                familyFont = null;
                            }
                            return familyFont;
                        }
                    }
                }
            }
        }
        return font;
    }

    private static final Object altJAFontKey = new Object();

    private static final Object localeFontKey = new Object();

    private static final Object proportionalFontKey = new Object();

    public static boolean usingPerAppContextComposites = false;

    private static boolean usingAlternateComposites = false;

    private static boolean gAltJAFont = false;

    private static boolean gLocalePref = false;

    private static boolean gPropPref = false;

    static boolean maybeUsingAlternateCompositeFonts() {
        return usingAlternateComposites || usingPerAppContextComposites;
    }

    public static boolean usingAlternateCompositeFonts() {
        return (usingAlternateComposites || (usingPerAppContextComposites && AppContext.getAppContext().get(CompositeFont.class) != null));
    }

    private static boolean maybeMultiAppContext() {
        Boolean appletSM = (Boolean) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {

            public Object run() {
                SecurityManager sm = System.getSecurityManager();
                return new Boolean(sm instanceof sun.applet.AppletSecurity);
            }
        });
        return appletSM.booleanValue();
    }

    public static synchronized void useAlternateFontforJALocales() {
        if (!isWindows) {
            return;
        }
        initSGEnv();
        if (!maybeMultiAppContext()) {
            gAltJAFont = true;
        } else {
            AppContext appContext = AppContext.getAppContext();
            appContext.put(altJAFontKey, altJAFontKey);
        }
    }

    public static boolean usingAlternateFontforJALocales() {
        if (!maybeMultiAppContext()) {
            return gAltJAFont;
        } else {
            AppContext appContext = AppContext.getAppContext();
            return appContext.get(altJAFontKey) == altJAFontKey;
        }
    }

    public static synchronized void preferLocaleFonts() {
        initSGEnv();
        if (!FontConfiguration.willReorderForStartupLocale()) {
            return;
        }
        if (!maybeMultiAppContext()) {
            if (gLocalePref == true) {
                return;
            }
            gLocalePref = true;
            sgEnv.createCompositeFonts(fontNameCache, gLocalePref, gPropPref);
            usingAlternateComposites = true;
        } else {
            AppContext appContext = AppContext.getAppContext();
            if (appContext.get(localeFontKey) == localeFontKey) {
                return;
            }
            appContext.put(localeFontKey, localeFontKey);
            boolean acPropPref = appContext.get(proportionalFontKey) == proportionalFontKey;
            ConcurrentHashMap<String, Font2D> altNameCache = new ConcurrentHashMap<String, Font2D>();
            appContext.put(CompositeFont.class, altNameCache);
            usingPerAppContextComposites = true;
            sgEnv.createCompositeFonts(altNameCache, true, acPropPref);
        }
    }

    public static synchronized void preferProportionalFonts() {
        if (!FontConfiguration.hasMonoToPropMap()) {
            return;
        }
        initSGEnv();
        if (!maybeMultiAppContext()) {
            if (gPropPref == true) {
                return;
            }
            gPropPref = true;
            sgEnv.createCompositeFonts(fontNameCache, gLocalePref, gPropPref);
            usingAlternateComposites = true;
        } else {
            AppContext appContext = AppContext.getAppContext();
            if (appContext.get(proportionalFontKey) == proportionalFontKey) {
                return;
            }
            appContext.put(proportionalFontKey, proportionalFontKey);
            boolean acLocalePref = appContext.get(localeFontKey) == localeFontKey;
            ConcurrentHashMap<String, Font2D> altNameCache = new ConcurrentHashMap<String, Font2D>();
            appContext.put(CompositeFont.class, altNameCache);
            usingPerAppContextComposites = true;
            sgEnv.createCompositeFonts(altNameCache, acLocalePref, true);
        }
    }

    private static HashSet<String> installedNames = null;

    private static HashSet<String> getInstalledNames() {
        if (installedNames == null) {
            Locale l = sgEnv.getSystemStartupLocale();
            String[] installedFamilies = sgEnv.getInstalledFontFamilyNames(l);
            Font[] installedFonts = sgEnv.getAllInstalledFonts();
            HashSet<String> names = new HashSet<String>();
            for (int i = 0; i < installedFamilies.length; i++) {
                names.add(installedFamilies[i].toLowerCase(l));
            }
            for (int i = 0; i < installedFonts.length; i++) {
                names.add(installedFonts[i].getFontName(l).toLowerCase(l));
            }
            installedNames = names;
        }
        return installedNames;
    }

    private static final Object regFamilyKey = new Object();

    private static final Object regFullNameKey = new Object();

    private static Hashtable<String, FontFamily> createdByFamilyName;

    private static Hashtable<String, Font2D> createdByFullName;

    private static boolean fontsAreRegistered = false;

    private static boolean fontsAreRegisteredPerAppContext = false;

    public static boolean registerFont(Font font) {
        if (font == null) {
            return false;
        }
        synchronized (regFamilyKey) {
            if (createdByFamilyName == null) {
                createdByFamilyName = new Hashtable<String, FontFamily>();
                createdByFullName = new Hashtable<String, Font2D>();
            }
        }
        if (!isCreatedFont(font)) {
            return false;
        }
        if (sgEnv == null) {
            initSGEnv();
        }
        HashSet<String> names = getInstalledNames();
        Locale l = sgEnv.getSystemStartupLocale();
        String familyName = font.getFamily(l).toLowerCase();
        String fullName = font.getFontName(l).toLowerCase();
        if (names.contains(familyName) || names.contains(fullName)) {
            return false;
        }
        Hashtable<String, FontFamily> familyTable;
        Hashtable<String, Font2D> fullNameTable;
        if (!maybeMultiAppContext()) {
            familyTable = createdByFamilyName;
            fullNameTable = createdByFullName;
            fontsAreRegistered = true;
        } else {
            AppContext appContext = AppContext.getAppContext();
            familyTable = (Hashtable<String, FontFamily>) appContext.get(regFamilyKey);
            fullNameTable = (Hashtable<String, Font2D>) appContext.get(regFullNameKey);
            if (familyTable == null) {
                familyTable = new Hashtable<String, FontFamily>();
                fullNameTable = new Hashtable<String, Font2D>();
                appContext.put(regFamilyKey, familyTable);
                appContext.put(regFullNameKey, fullNameTable);
            }
            fontsAreRegisteredPerAppContext = true;
        }
        Font2D font2D = getFont2D(font);
        int style = font2D.getStyle();
        FontFamily family = familyTable.get(familyName);
        if (family == null) {
            family = new FontFamily(font.getFamily(l));
            familyTable.put(familyName, family);
        }
        if (fontsAreRegistered) {
            removeFromCache(family.getFont(Font.PLAIN));
            removeFromCache(family.getFont(Font.BOLD));
            removeFromCache(family.getFont(Font.ITALIC));
            removeFromCache(family.getFont(Font.BOLD | Font.ITALIC));
            removeFromCache(fullNameTable.get(fullName));
        }
        family.setFont(font2D, style);
        fullNameTable.put(fullName, font2D);
        return true;
    }

    private static void removeFromCache(Font2D font) {
        if (font == null) {
            return;
        }
        String[] keys = (String[]) (fontNameCache.keySet().toArray(STR_ARRAY));
        for (int k = 0; k < keys.length; k++) {
            if (fontNameCache.get(keys[k]) == font) {
                fontNameCache.remove(keys[k]);
            }
        }
    }

    public static TreeMap<String, String> getCreatedFontFamilyNames() {
        Hashtable<String, FontFamily> familyTable;
        if (fontsAreRegistered) {
            familyTable = createdByFamilyName;
        } else if (fontsAreRegisteredPerAppContext) {
            AppContext appContext = AppContext.getAppContext();
            familyTable = (Hashtable<String, FontFamily>) appContext.get(regFamilyKey);
        } else {
            return null;
        }
        Locale l = sgEnv.getSystemStartupLocale();
        synchronized (familyTable) {
            TreeMap<String, String> map = new TreeMap<String, String>();
            for (FontFamily f : familyTable.values()) {
                Font2D font2D = f.getFont(Font.PLAIN);
                if (font2D == null) {
                    font2D = f.getClosestStyle(Font.PLAIN);
                }
                String name = font2D.getFamilyName(l);
                map.put(name.toLowerCase(l), name);
            }
            return map;
        }
    }

    public static Font[] getCreatedFonts() {
        Hashtable<String, Font2D> nameTable;
        if (fontsAreRegistered) {
            nameTable = createdByFullName;
        } else if (fontsAreRegisteredPerAppContext) {
            AppContext appContext = AppContext.getAppContext();
            nameTable = (Hashtable<String, Font2D>) appContext.get(regFullNameKey);
        } else {
            return null;
        }
        Locale l = sgEnv.getSystemStartupLocale();
        synchronized (nameTable) {
            Font[] fonts = new Font[nameTable.size()];
            int i = 0;
            for (Font2D font2D : nameTable.values()) {
                fonts[i++] = new Font(font2D.getFontName(l), Font.PLAIN, 1);
            }
            return fonts;
        }
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

    private static String[] fontConfigNames = { "sans:regular:roman", "sans:bold:roman", "sans:regular:italic", "sans:bold:italic", "serif:regular:roman", "serif:bold:roman", "serif:regular:italic", "serif:bold:italic", "monospace:regular:roman", "monospace:bold:roman", "monospace:regular:italic", "monospace:bold:italic" };

    private static class FontConfigInfo {

        String fcName;

        String fcFamily;

        String jdkName;

        int style;

        String familyName;

        String fontFile;

        CompositeFont compFont;
    }

    private static String getFCLocaleStr() {
        Locale l = SunToolkit.getStartupLocale();
        String localeStr = l.getLanguage();
        String country = l.getCountry();
        if (!country.equals("")) {
            localeStr = localeStr + "-" + country;
        }
        return localeStr;
    }

    private static native int getFontConfigAASettings(String locale, String fcFamily);

    public static Object getFontConfigAAHint(String fcFamily) {
        if (isWindows) {
            return null;
        } else {
            int hint = getFontConfigAASettings(getFCLocaleStr(), fcFamily);
            if (hint < 0) {
                return null;
            } else {
                return SunHints.Value.get(SunHints.INTKEY_TEXT_ANTIALIASING, hint);
            }
        }
    }

    public static Object getFontConfigAAHint() {
        return getFontConfigAAHint("sans");
    }

    private static FontConfigInfo[] fontConfigFonts;

    private static native void getFontConfig(String locale, FontConfigInfo[] fonts);

    private static void initFontConfigFonts() {
        if (fontConfigFonts != null) {
            return;
        }
        if (isWindows) {
            return;
        }
        long t0 = 0;
        if (logging) {
            t0 = System.currentTimeMillis();
        }
        FontConfigInfo[] fontArr = new FontConfigInfo[fontConfigNames.length];
        for (int i = 0; i < fontArr.length; i++) {
            fontArr[i] = new FontConfigInfo();
            fontArr[i].fcName = fontConfigNames[i];
            int colonPos = fontArr[i].fcName.indexOf(':');
            fontArr[i].fcFamily = fontArr[i].fcName.substring(0, colonPos);
            fontArr[i].jdkName = mapFcName(fontArr[i].fcFamily);
            fontArr[i].style = i % 4;
        }
        getFontConfig(getFCLocaleStr(), fontArr);
        fontConfigFonts = fontArr;
        if (logging) {
            long t1 = System.currentTimeMillis();
            logger.info("Time spent accessing fontconfig=" + (t1 - t0) + "ms.");
            for (int i = 0; i < fontConfigFonts.length; i++) {
                FontConfigInfo fci = fontConfigFonts[i];
                logger.info("FC font " + fci.fcName + " maps to family " + fci.familyName + " in file " + fci.fontFile);
            }
        }
    }

    private static PhysicalFont registerFromFcInfo(FontConfigInfo fcInfo) {
        int offset = fcInfo.fontFile.length() - 4;
        if (offset <= 0) {
            return null;
        }
        String ext = fcInfo.fontFile.substring(offset).toLowerCase();
        boolean isTTC = ext.equals(".ttc");
        PhysicalFont physFont = registeredFontFiles.get(fcInfo.fontFile);
        if (physFont != null) {
            if (isTTC) {
                Font2D f2d = findFont2D(fcInfo.familyName, fcInfo.style, NO_FALLBACK);
                if (f2d instanceof PhysicalFont) {
                    return (PhysicalFont) f2d;
                } else {
                    return null;
                }
            } else {
                return physFont;
            }
        }
        physFont = findJREDeferredFont(fcInfo.familyName, fcInfo.style);
        if (physFont == null && deferredFontFiles.get(fcInfo.fontFile) != null) {
            physFont = initialiseDeferredFont(fcInfo.fontFile);
            if (physFont != null) {
                if (isTTC) {
                    Font2D f2d = findFont2D(fcInfo.familyName, fcInfo.style, NO_FALLBACK);
                    if (f2d instanceof PhysicalFont) {
                        return (PhysicalFont) f2d;
                    } else {
                        return null;
                    }
                } else {
                    return physFont;
                }
            }
        }
        if (physFont == null) {
            int fontFormat = FONTFORMAT_NONE;
            int fontRank = Font2D.UNKNOWN_RANK;
            if (ext.equals(".ttf") || isTTC) {
                fontFormat = FONTFORMAT_TRUETYPE;
                fontRank = Font2D.TTF_RANK;
            } else if (ext.equals(".pfa") || ext.equals(".pfb")) {
                fontFormat = FONTFORMAT_TYPE1;
                fontRank = Font2D.TYPE1_RANK;
            }
            physFont = registerFontFile(fcInfo.fontFile, null, fontFormat, true, fontRank);
        }
        return physFont;
    }

    private static String[] getPlatformFontDirs() {
        String path = getFontPath(true);
        StringTokenizer parser = new StringTokenizer(path, File.pathSeparator);
        ArrayList<String> pathList = new ArrayList<String>();
        try {
            while (parser.hasMoreTokens()) {
                pathList.add(parser.nextToken());
            }
        } catch (NoSuchElementException e) {
        }
        return pathList.toArray(new String[0]);
    }

    private static String[] defaultPlatformFont = null;

    public static String[] getDefaultPlatformFont() {
        if (defaultPlatformFont != null) {
            return defaultPlatformFont;
        }
        String[] info = new String[2];
        if (isWindows) {
            info[0] = "Arial";
            info[1] = "c:\\windows\\fonts";
            final String[] dirs = getPlatformFontDirs();
            if (dirs.length > 1) {
                String dir = (String) AccessController.doPrivileged(new PrivilegedAction() {

                    public Object run() {
                        for (int i = 0; i < dirs.length; i++) {
                            String path = dirs[i] + File.separator + "arial.ttf";
                            File file = new File(path);
                            if (file.exists()) {
                                return dirs[i];
                            }
                        }
                        return null;
                    }
                });
                if (dir != null) {
                    info[1] = dir;
                }
            } else {
                info[1] = dirs[0];
            }
            info[1] = info[1] + File.separator + "arial.ttf";
        } else {
            initFontConfigFonts();
            for (int i = 0; i < fontConfigFonts.length; i++) {
                if ("sans".equals(fontConfigFonts[i].fcFamily) && 0 == fontConfigFonts[i].style) {
                    info[0] = fontConfigFonts[i].familyName;
                    info[1] = fontConfigFonts[i].fontFile;
                    break;
                }
            }
            if (info[0] == null) {
                if (fontConfigFonts.length > 0 && fontConfigFonts[0].fontFile != null) {
                    info[0] = fontConfigFonts[0].familyName;
                    info[1] = fontConfigFonts[0].fontFile;
                } else {
                    info[0] = "Dialog";
                    info[1] = "/dialog.ttf";
                }
            }
        }
        defaultPlatformFont = info;
        return defaultPlatformFont;
    }

    private FontConfigInfo getFontConfigInfo() {
        initFontConfigFonts();
        for (int i = 0; i < fontConfigFonts.length; i++) {
            if ("sans".equals(fontConfigFonts[i].fcFamily) && 0 == fontConfigFonts[i].style) {
                return fontConfigFonts[i];
            }
        }
        return null;
    }

    private static CompositeFont getFontConfigFont(String name, int style) {
        name = name.toLowerCase();
        initFontConfigFonts();
        FontConfigInfo fcInfo = null;
        for (int i = 0; i < fontConfigFonts.length; i++) {
            if (name.equals(fontConfigFonts[i].fcFamily) && style == fontConfigFonts[i].style) {
                fcInfo = fontConfigFonts[i];
                break;
            }
        }
        if (fcInfo == null) {
            fcInfo = fontConfigFonts[0];
        }
        if (logging) {
            logger.info("FC name=" + name + " style=" + style + " uses " + fcInfo.familyName + " in file: " + fcInfo.fontFile);
        }
        if (fcInfo.compFont != null) {
            return fcInfo.compFont;
        }
        CompositeFont jdkFont = (CompositeFont) findFont2D(fcInfo.jdkName, style, LOGICAL_FALLBACK);
        if (fcInfo.familyName == null || fcInfo.fontFile == null) {
            return (fcInfo.compFont = jdkFont);
        }
        FontFamily family = FontFamily.getFamily(fcInfo.familyName);
        PhysicalFont physFont = null;
        if (family != null) {
            Font2D f2D = family.getFontWithExactStyleMatch(fcInfo.style);
            if (f2D instanceof PhysicalFont) {
                physFont = (PhysicalFont) f2D;
            }
        }
        if (physFont == null || !fcInfo.fontFile.equals(physFont.platName)) {
            physFont = registerFromFcInfo(fcInfo);
            if (physFont == null) {
                return (fcInfo.compFont = jdkFont);
            }
            family = FontFamily.getFamily(physFont.getFamilyName(null));
        }
        for (int i = 0; i < fontConfigFonts.length; i++) {
            FontConfigInfo fc = fontConfigFonts[i];
            if (fc != fcInfo && physFont.getFamilyName(null).equals(fc.familyName) && !fc.fontFile.equals(physFont.platName) && family.getFontWithExactStyleMatch(fc.style) == null) {
                registerFromFcInfo(fontConfigFonts[i]);
            }
        }
        return (fcInfo.compFont = new CompositeFont(physFont, jdkFont));
    }

    public static FontUIResource getFontConfigFUIR(String fcFamily, int style, int size) {
        String mappedName = mapFcName(fcFamily);
        if (mappedName == null) {
            mappedName = "sansserif";
        }
        if (isWindows) {
            return new FontUIResource(mappedName, style, size);
        }
        CompositeFont font2D = getFontConfigFont(fcFamily, style);
        if (font2D == null) {
            return new FontUIResource(mappedName, style, size);
        }
        FontUIResource fuir = new FontUIResource(font2D.getFamilyName(null), style, size);
        setFont2D(fuir, font2D.handle);
        setCreatedFont(fuir);
        return fuir;
    }

    public static final int MIN_LAYOUT_CHARCODE = 0x0300;

    public static final int MAX_LAYOUT_CHARCODE = 0x206F;

    static boolean isComplexCharCode(int code) {
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

    static boolean isNonSimpleChar(char ch) {
        return isComplexCharCode(ch) || (ch >= CharToGlyphMapper.HI_SURROGATE_START && ch <= CharToGlyphMapper.LO_SURROGATE_END);
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

    public static boolean textLayoutIsCompatible(Font font) {
        Font2D font2D = FontManager.getFont2D(font);
        if (font2D instanceof TrueTypeFont) {
            TrueTypeFont ttf = (TrueTypeFont) font2D;
            return ttf.getDirectoryEntry(TrueTypeFont.GSUBTag) == null || ttf.getDirectoryEntry(TrueTypeFont.GPOSTag) != null;
        } else {
            return false;
        }
    }

    private static FontScaler nullScaler = null;

    private static Constructor<FontScaler> scalerConstructor = null;

    static {
        Class scalerClass = null;
        Class[] arglst = new Class[] { Font2D.class, int.class, boolean.class, int.class };
        try {
            if (SunGraphicsEnvironment.isOpenJDK()) {
                scalerClass = Class.forName("sun.font.FreetypeFontScaler");
            } else {
                scalerClass = Class.forName("sun.font.T2KFontScaler");
            }
        } catch (ClassNotFoundException e) {
            scalerClass = NullFontScaler.class;
        }
        try {
            scalerConstructor = scalerClass.getConstructor(arglst);
        } catch (NoSuchMethodException e) {
        }
    }

    public synchronized static FontScaler getNullScaler() {
        if (nullScaler == null) {
            nullScaler = new NullFontScaler();
        }
        return nullScaler;
    }

    public static FontScaler getScaler(Font2D font, int indexInCollection, boolean supportsCJK, int filesize) {
        FontScaler scaler = null;
        try {
            Object[] args = new Object[] { font, indexInCollection, supportsCJK, filesize };
            scaler = scalerConstructor.newInstance(args);
            Disposer.addObjectRecord(font, scaler);
        } catch (Throwable e) {
            scaler = nullScaler;
            deRegisterBadFont(font);
        }
        return scaler;
    }
}
