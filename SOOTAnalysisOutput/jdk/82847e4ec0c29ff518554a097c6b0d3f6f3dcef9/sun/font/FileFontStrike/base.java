package sun.font;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.ConcurrentHashMap;
import static sun.awt.SunHints.*;

public class FileFontStrike extends PhysicalStrike {

    static final int INVISIBLE_GLYPHS = 0x0fffe;

    private FileFont fileFont;

    private static final int UNINITIALISED = 0;

    private static final int INTARRAY = 1;

    private static final int LONGARRAY = 2;

    private static final int SEGINTARRAY = 3;

    private static final int SEGLONGARRAY = 4;

    private volatile int glyphCacheFormat = UNINITIALISED;

    private static final int SEGSHIFT = 5;

    private static final int SEGSIZE = 1 << SEGSHIFT;

    private boolean segmentedCache;

    private int[][] segIntGlyphImages;

    private long[][] segLongGlyphImages;

    private float[] horizontalAdvances;

    private float[][] segHorizontalAdvances;

    ConcurrentHashMap<Integer, Rectangle2D.Float> boundsMap;

    SoftReference<ConcurrentHashMap<Integer, Point2D.Float>> glyphMetricsMapRef;

    AffineTransform invertDevTx;

    boolean useNatives;

    NativeStrike[] nativeStrikes;

    private int intPtSize;

    private static native boolean initNative();

    private static boolean isXPorLater = false;

    static {
        if (FontUtilities.isWindows && !FontUtilities.useT2K && !GraphicsEnvironment.isHeadless()) {
            isXPorLater = initNative();
        }
    }

    FileFontStrike(FileFont fileFont, FontStrikeDesc desc) {
        super(fileFont, desc);
        this.fileFont = fileFont;
        if (desc.style != fileFont.style) {
            if ((desc.style & Font.ITALIC) == Font.ITALIC && (fileFont.style & Font.ITALIC) == 0) {
                algoStyle = true;
                italic = 0.7f;
            }
            if ((desc.style & Font.BOLD) == Font.BOLD && ((fileFont.style & Font.BOLD) == 0)) {
                algoStyle = true;
                boldness = 1.33f;
            }
        }
        double[] matrix = new double[4];
        AffineTransform at = desc.glyphTx;
        at.getMatrix(matrix);
        if (!desc.devTx.isIdentity() && desc.devTx.getType() != AffineTransform.TYPE_TRANSLATION) {
            try {
                invertDevTx = desc.devTx.createInverse();
            } catch (NoninvertibleTransformException e) {
            }
        }
        boolean disableHinting = desc.aaHint != INTVAL_TEXT_ANTIALIAS_OFF && fileFont.familyName.startsWith("Amble");
        if (Double.isNaN(matrix[0]) || Double.isNaN(matrix[1]) || Double.isNaN(matrix[2]) || Double.isNaN(matrix[3]) || fileFont.getScaler() == null) {
            pScalerContext = NullFontScaler.getNullScalerContext();
        } else {
            pScalerContext = fileFont.getScaler().createScalerContext(matrix, desc.aaHint, desc.fmHint, boldness, italic, disableHinting);
        }
        mapper = fileFont.getMapper();
        int numGlyphs = mapper.getNumGlyphs();
        float ptSize = (float) matrix[3];
        int iSize = intPtSize = (int) ptSize;
        boolean isSimpleTx = (at.getType() & complexTX) == 0;
        segmentedCache = (numGlyphs > SEGSIZE << 3) || ((numGlyphs > SEGSIZE << 1) && (!isSimpleTx || ptSize != iSize || iSize < 6 || iSize > 36));
        if (pScalerContext == 0L) {
            this.disposer = new FontStrikeDisposer(fileFont, desc);
            initGlyphCache();
            pScalerContext = NullFontScaler.getNullScalerContext();
            SunFontManager.getInstance().deRegisterBadFont(fileFont);
            return;
        }
        if (FontUtilities.isWindows && isXPorLater && !FontUtilities.useT2K && !GraphicsEnvironment.isHeadless() && !fileFont.useJavaRasterizer && (desc.aaHint == INTVAL_TEXT_ANTIALIAS_LCD_HRGB || desc.aaHint == INTVAL_TEXT_ANTIALIAS_LCD_HBGR) && (matrix[1] == 0.0 && matrix[2] == 0.0 && matrix[0] == matrix[3] && matrix[0] >= 3.0 && matrix[0] <= 100.0) && !((TrueTypeFont) fileFont).useEmbeddedBitmapsForSize(intPtSize)) {
            useNatives = true;
        } else if (fileFont.checkUseNatives() && desc.aaHint == 0 && !algoStyle) {
            if (matrix[1] == 0.0 && matrix[2] == 0.0 && matrix[0] >= 6.0 && matrix[0] <= 36.0 && matrix[0] == matrix[3]) {
                useNatives = true;
                int numNatives = fileFont.nativeFonts.length;
                nativeStrikes = new NativeStrike[numNatives];
                for (int i = 0; i < numNatives; i++) {
                    nativeStrikes[i] = new NativeStrike(fileFont.nativeFonts[i], desc, false);
                }
            }
        }
        if (FontUtilities.isLogging() && FontUtilities.isWindows) {
            FontUtilities.getLogger().info("Strike for " + fileFont + " at size = " + intPtSize + " use natives = " + useNatives + " useJavaRasteriser = " + fileFont.useJavaRasterizer + " AAHint = " + desc.aaHint + " Has Embedded bitmaps = " + ((TrueTypeFont) fileFont).useEmbeddedBitmapsForSize(intPtSize));
        }
        this.disposer = new FontStrikeDisposer(fileFont, desc, pScalerContext);
        double maxSz = 48.0;
        getImageWithAdvance = Math.abs(at.getScaleX()) <= maxSz && Math.abs(at.getScaleY()) <= maxSz && Math.abs(at.getShearX()) <= maxSz && Math.abs(at.getShearY()) <= maxSz;
        if (!getImageWithAdvance) {
            if (!segmentedCache) {
                horizontalAdvances = new float[numGlyphs];
                for (int i = 0; i < numGlyphs; i++) {
                    horizontalAdvances[i] = Float.MAX_VALUE;
                }
            } else {
                int numSegments = (numGlyphs + SEGSIZE - 1) / SEGSIZE;
                segHorizontalAdvances = new float[numSegments][];
            }
        }
    }

    public int getNumGlyphs() {
        return fileFont.getNumGlyphs();
    }

    long getGlyphImageFromNative(int glyphCode) {
        if (FontUtilities.isWindows) {
            return getGlyphImageFromWindows(glyphCode);
        } else {
            return getGlyphImageFromX11(glyphCode);
        }
    }

    private native long _getGlyphImageFromWindows(String family, int style, int size, int glyphCode, boolean fracMetrics);

    long getGlyphImageFromWindows(int glyphCode) {
        String family = fileFont.getFamilyName(null);
        int style = desc.style & Font.BOLD | desc.style & Font.ITALIC | fileFont.getStyle();
        int size = intPtSize;
        long ptr = _getGlyphImageFromWindows(family, style, size, glyphCode, desc.fmHint == INTVAL_FRACTIONALMETRICS_ON);
        if (ptr != 0) {
            float advance = getGlyphAdvance(glyphCode, false);
            StrikeCache.unsafe.putFloat(ptr + StrikeCache.xAdvanceOffset, advance);
            return ptr;
        } else {
            return fileFont.getGlyphImage(pScalerContext, glyphCode);
        }
    }

    long getGlyphImageFromX11(int glyphCode) {
        long glyphPtr;
        char charCode = fileFont.glyphToCharMap[glyphCode];
        for (int i = 0; i < nativeStrikes.length; i++) {
            CharToGlyphMapper mapper = fileFont.nativeFonts[i].getMapper();
            int gc = mapper.charToGlyph(charCode) & 0xffff;
            if (gc != mapper.getMissingGlyphCode()) {
                glyphPtr = nativeStrikes[i].getGlyphImagePtrNoCache(gc);
                if (glyphPtr != 0L) {
                    return glyphPtr;
                }
            }
        }
        return fileFont.getGlyphImage(pScalerContext, glyphCode);
    }

    long getGlyphImagePtr(int glyphCode) {
        if (glyphCode >= INVISIBLE_GLYPHS) {
            return StrikeCache.invisibleGlyphPtr;
        }
        long glyphPtr = 0L;
        if ((glyphPtr = getCachedGlyphPtr(glyphCode)) != 0L) {
            return glyphPtr;
        } else {
            if (useNatives) {
                glyphPtr = getGlyphImageFromNative(glyphCode);
                if (glyphPtr == 0L && FontUtilities.isLogging()) {
                    FontUtilities.getLogger().info("Strike for " + fileFont + " at size = " + intPtSize + " couldn't get native glyph for code = " + glyphCode);
                }
            }
            if (glyphPtr == 0L) {
                glyphPtr = fileFont.getGlyphImage(pScalerContext, glyphCode);
            }
            return setCachedGlyphPtr(glyphCode, glyphPtr);
        }
    }

    void getGlyphImagePtrs(int[] glyphCodes, long[] images, int len) {
        for (int i = 0; i < len; i++) {
            int glyphCode = glyphCodes[i];
            if (glyphCode >= INVISIBLE_GLYPHS) {
                images[i] = StrikeCache.invisibleGlyphPtr;
                continue;
            } else if ((images[i] = getCachedGlyphPtr(glyphCode)) != 0L) {
                continue;
            } else {
                long glyphPtr = 0L;
                if (useNatives) {
                    glyphPtr = getGlyphImageFromNative(glyphCode);
                }
                if (glyphPtr == 0L) {
                    glyphPtr = fileFont.getGlyphImage(pScalerContext, glyphCode);
                }
                images[i] = setCachedGlyphPtr(glyphCode, glyphPtr);
            }
        }
    }

    int getSlot0GlyphImagePtrs(int[] glyphCodes, long[] images, int len) {
        int convertedCnt = 0;
        for (int i = 0; i < len; i++) {
            int glyphCode = glyphCodes[i];
            if (glyphCode >>> 24 != 0) {
                return convertedCnt;
            } else {
                convertedCnt++;
            }
            if (glyphCode >= INVISIBLE_GLYPHS) {
                images[i] = StrikeCache.invisibleGlyphPtr;
                continue;
            } else if ((images[i] = getCachedGlyphPtr(glyphCode)) != 0L) {
                continue;
            } else {
                long glyphPtr = 0L;
                if (useNatives) {
                    glyphPtr = getGlyphImageFromNative(glyphCode);
                }
                if (glyphPtr == 0L) {
                    glyphPtr = fileFont.getGlyphImage(pScalerContext, glyphCode);
                }
                images[i] = setCachedGlyphPtr(glyphCode, glyphPtr);
            }
        }
        return convertedCnt;
    }

    long getCachedGlyphPtr(int glyphCode) {
        try {
            return getCachedGlyphPtrInternal(glyphCode);
        } catch (Exception e) {
            NullFontScaler nullScaler = (NullFontScaler) FontScaler.getNullScaler();
            long nullSC = NullFontScaler.getNullScalerContext();
            return nullScaler.getGlyphImage(nullSC, glyphCode);
        }
    }

    private long getCachedGlyphPtrInternal(int glyphCode) {
        switch(glyphCacheFormat) {
            case INTARRAY:
                return intGlyphImages[glyphCode] & INTMASK;
            case SEGINTARRAY:
                int segIndex = glyphCode >> SEGSHIFT;
                if (segIntGlyphImages[segIndex] != null) {
                    int subIndex = glyphCode % SEGSIZE;
                    return segIntGlyphImages[segIndex][subIndex] & INTMASK;
                } else {
                    return 0L;
                }
            case LONGARRAY:
                return longGlyphImages[glyphCode];
            case SEGLONGARRAY:
                segIndex = glyphCode >> SEGSHIFT;
                if (segLongGlyphImages[segIndex] != null) {
                    int subIndex = glyphCode % SEGSIZE;
                    return segLongGlyphImages[segIndex][subIndex];
                } else {
                    return 0L;
                }
        }
        return 0L;
    }

    private synchronized long setCachedGlyphPtr(int glyphCode, long glyphPtr) {
        try {
            return setCachedGlyphPtrInternal(glyphCode, glyphPtr);
        } catch (Exception e) {
            switch(glyphCacheFormat) {
                case INTARRAY:
                case SEGINTARRAY:
                    StrikeCache.freeIntPointer((int) glyphPtr);
                    break;
                case LONGARRAY:
                case SEGLONGARRAY:
                    StrikeCache.freeLongPointer(glyphPtr);
                    break;
            }
            NullFontScaler nullScaler = (NullFontScaler) FontScaler.getNullScaler();
            long nullSC = NullFontScaler.getNullScalerContext();
            return nullScaler.getGlyphImage(nullSC, glyphCode);
        }
    }

    private long setCachedGlyphPtrInternal(int glyphCode, long glyphPtr) {
        switch(glyphCacheFormat) {
            case INTARRAY:
                if (intGlyphImages[glyphCode] == 0) {
                    intGlyphImages[glyphCode] = (int) glyphPtr;
                    return glyphPtr;
                } else {
                    StrikeCache.freeIntPointer((int) glyphPtr);
                    return intGlyphImages[glyphCode] & INTMASK;
                }
            case SEGINTARRAY:
                int segIndex = glyphCode >> SEGSHIFT;
                int subIndex = glyphCode % SEGSIZE;
                if (segIntGlyphImages[segIndex] == null) {
                    segIntGlyphImages[segIndex] = new int[SEGSIZE];
                }
                if (segIntGlyphImages[segIndex][subIndex] == 0) {
                    segIntGlyphImages[segIndex][subIndex] = (int) glyphPtr;
                    return glyphPtr;
                } else {
                    StrikeCache.freeIntPointer((int) glyphPtr);
                    return segIntGlyphImages[segIndex][subIndex] & INTMASK;
                }
            case LONGARRAY:
                if (longGlyphImages[glyphCode] == 0L) {
                    longGlyphImages[glyphCode] = glyphPtr;
                    return glyphPtr;
                } else {
                    StrikeCache.freeLongPointer(glyphPtr);
                    return longGlyphImages[glyphCode];
                }
            case SEGLONGARRAY:
                segIndex = glyphCode >> SEGSHIFT;
                subIndex = glyphCode % SEGSIZE;
                if (segLongGlyphImages[segIndex] == null) {
                    segLongGlyphImages[segIndex] = new long[SEGSIZE];
                }
                if (segLongGlyphImages[segIndex][subIndex] == 0L) {
                    segLongGlyphImages[segIndex][subIndex] = glyphPtr;
                    return glyphPtr;
                } else {
                    StrikeCache.freeLongPointer(glyphPtr);
                    return segLongGlyphImages[segIndex][subIndex];
                }
        }
        initGlyphCache();
        return setCachedGlyphPtr(glyphCode, glyphPtr);
    }

    private synchronized void initGlyphCache() {
        int numGlyphs = mapper.getNumGlyphs();
        int tmpFormat = UNINITIALISED;
        if (segmentedCache) {
            int numSegments = (numGlyphs + SEGSIZE - 1) / SEGSIZE;
            if (longAddresses) {
                tmpFormat = SEGLONGARRAY;
                segLongGlyphImages = new long[numSegments][];
                this.disposer.segLongGlyphImages = segLongGlyphImages;
            } else {
                tmpFormat = SEGINTARRAY;
                segIntGlyphImages = new int[numSegments][];
                this.disposer.segIntGlyphImages = segIntGlyphImages;
            }
        } else {
            if (longAddresses) {
                tmpFormat = LONGARRAY;
                longGlyphImages = new long[numGlyphs];
                this.disposer.longGlyphImages = longGlyphImages;
            } else {
                tmpFormat = INTARRAY;
                intGlyphImages = new int[numGlyphs];
                this.disposer.intGlyphImages = intGlyphImages;
            }
        }
        glyphCacheFormat = tmpFormat;
    }

    float getGlyphAdvance(int glyphCode) {
        return getGlyphAdvance(glyphCode, true);
    }

    private float getGlyphAdvance(int glyphCode, boolean getUserAdv) {
        float advance;
        if (glyphCode >= INVISIBLE_GLYPHS) {
            return 0f;
        }
        if (horizontalAdvances != null) {
            advance = horizontalAdvances[glyphCode];
            if (advance != Float.MAX_VALUE) {
                if (!getUserAdv && invertDevTx != null) {
                    Point2D.Float metrics = new Point2D.Float(advance, 0f);
                    desc.devTx.deltaTransform(metrics, metrics);
                    return metrics.x;
                } else {
                    return advance;
                }
            }
        } else if (segmentedCache && segHorizontalAdvances != null) {
            int segIndex = glyphCode >> SEGSHIFT;
            float[] subArray = segHorizontalAdvances[segIndex];
            if (subArray != null) {
                advance = subArray[glyphCode % SEGSIZE];
                if (advance != Float.MAX_VALUE) {
                    if (!getUserAdv && invertDevTx != null) {
                        Point2D.Float metrics = new Point2D.Float(advance, 0f);
                        desc.devTx.deltaTransform(metrics, metrics);
                        return metrics.x;
                    } else {
                        return advance;
                    }
                }
            }
        }
        if (!getUserAdv && invertDevTx != null) {
            Point2D.Float metrics = new Point2D.Float();
            fileFont.getGlyphMetrics(pScalerContext, glyphCode, metrics);
            return metrics.x;
        }
        if (invertDevTx != null || !getUserAdv) {
            advance = getGlyphMetrics(glyphCode, getUserAdv).x;
        } else {
            long glyphPtr;
            if (getImageWithAdvance) {
                glyphPtr = getGlyphImagePtr(glyphCode);
            } else {
                glyphPtr = getCachedGlyphPtr(glyphCode);
            }
            if (glyphPtr != 0L) {
                advance = StrikeCache.unsafe.getFloat(glyphPtr + StrikeCache.xAdvanceOffset);
            } else {
                advance = fileFont.getGlyphAdvance(pScalerContext, glyphCode);
            }
        }
        if (horizontalAdvances != null) {
            horizontalAdvances[glyphCode] = advance;
        } else if (segmentedCache && segHorizontalAdvances != null) {
            int segIndex = glyphCode >> SEGSHIFT;
            int subIndex = glyphCode % SEGSIZE;
            if (segHorizontalAdvances[segIndex] == null) {
                segHorizontalAdvances[segIndex] = new float[SEGSIZE];
                for (int i = 0; i < SEGSIZE; i++) {
                    segHorizontalAdvances[segIndex][i] = Float.MAX_VALUE;
                }
            }
            segHorizontalAdvances[segIndex][subIndex] = advance;
        }
        return advance;
    }

    float getCodePointAdvance(int cp) {
        return getGlyphAdvance(mapper.charToGlyph(cp));
    }

    void getGlyphImageBounds(int glyphCode, Point2D.Float pt, Rectangle result) {
        long ptr = getGlyphImagePtr(glyphCode);
        float topLeftX, topLeftY;
        if (ptr == 0L) {
            result.x = (int) Math.floor(pt.x + 0.5f);
            result.y = (int) Math.floor(pt.y + 0.5f);
            result.width = result.height = 0;
            return;
        }
        topLeftX = StrikeCache.unsafe.getFloat(ptr + StrikeCache.topLeftXOffset);
        topLeftY = StrikeCache.unsafe.getFloat(ptr + StrikeCache.topLeftYOffset);
        result.x = (int) Math.floor(pt.x + topLeftX + 0.5f);
        result.y = (int) Math.floor(pt.y + topLeftY + 0.5f);
        result.width = StrikeCache.unsafe.getShort(ptr + StrikeCache.widthOffset) & 0x0ffff;
        result.height = StrikeCache.unsafe.getShort(ptr + StrikeCache.heightOffset) & 0x0ffff;
        if ((desc.aaHint == INTVAL_TEXT_ANTIALIAS_LCD_HRGB || desc.aaHint == INTVAL_TEXT_ANTIALIAS_LCD_HBGR) && topLeftX <= -2.0f) {
            int minx = getGlyphImageMinX(ptr, result.x);
            if (minx > result.x) {
                result.x += 1;
                result.width -= 1;
            }
        }
    }

    private int getGlyphImageMinX(long ptr, int origMinX) {
        int width = StrikeCache.unsafe.getChar(ptr + StrikeCache.widthOffset);
        int height = StrikeCache.unsafe.getChar(ptr + StrikeCache.heightOffset);
        int rowBytes = StrikeCache.unsafe.getChar(ptr + StrikeCache.rowBytesOffset);
        if (rowBytes == width) {
            return origMinX;
        }
        long pixelData = StrikeCache.unsafe.getAddress(ptr + StrikeCache.pixelDataOffset);
        if (pixelData == 0L) {
            return origMinX;
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < 3; x++) {
                if (StrikeCache.unsafe.getByte(pixelData + y * rowBytes + x) != 0) {
                    return origMinX;
                }
            }
        }
        return origMinX + 1;
    }

    StrikeMetrics getFontMetrics() {
        if (strikeMetrics == null) {
            strikeMetrics = fileFont.getFontMetrics(pScalerContext);
            if (invertDevTx != null) {
                strikeMetrics.convertToUserSpace(invertDevTx);
            }
        }
        return strikeMetrics;
    }

    Point2D.Float getGlyphMetrics(int glyphCode) {
        return getGlyphMetrics(glyphCode, true);
    }

    private Point2D.Float getGlyphMetrics(int glyphCode, boolean getImage) {
        Point2D.Float metrics = new Point2D.Float();
        if (glyphCode >= INVISIBLE_GLYPHS) {
            return metrics;
        }
        long glyphPtr;
        if (getImageWithAdvance && getImage) {
            glyphPtr = getGlyphImagePtr(glyphCode);
        } else {
            glyphPtr = getCachedGlyphPtr(glyphCode);
        }
        if (glyphPtr != 0L) {
            metrics = new Point2D.Float();
            metrics.x = StrikeCache.unsafe.getFloat(glyphPtr + StrikeCache.xAdvanceOffset);
            metrics.y = StrikeCache.unsafe.getFloat(glyphPtr + StrikeCache.yAdvanceOffset);
            if (invertDevTx != null) {
                invertDevTx.deltaTransform(metrics, metrics);
            }
        } else {
            Integer key = Integer.valueOf(glyphCode);
            Point2D.Float value = null;
            ConcurrentHashMap<Integer, Point2D.Float> glyphMetricsMap = null;
            if (glyphMetricsMapRef != null) {
                glyphMetricsMap = glyphMetricsMapRef.get();
            }
            if (glyphMetricsMap != null) {
                value = glyphMetricsMap.get(key);
                if (value != null) {
                    metrics.x = value.x;
                    metrics.y = value.y;
                    return metrics;
                }
            }
            if (value == null) {
                fileFont.getGlyphMetrics(pScalerContext, glyphCode, metrics);
                if (invertDevTx != null) {
                    invertDevTx.deltaTransform(metrics, metrics);
                }
                value = new Point2D.Float(metrics.x, metrics.y);
                if (glyphMetricsMap == null) {
                    glyphMetricsMap = new ConcurrentHashMap<Integer, Point2D.Float>();
                    glyphMetricsMapRef = new SoftReference<ConcurrentHashMap<Integer, Point2D.Float>>(glyphMetricsMap);
                }
                glyphMetricsMap.put(key, value);
            }
        }
        return metrics;
    }

    Point2D.Float getCharMetrics(char ch) {
        return getGlyphMetrics(mapper.charToGlyph(ch));
    }

    Rectangle2D.Float getGlyphOutlineBounds(int glyphCode) {
        if (boundsMap == null) {
            boundsMap = new ConcurrentHashMap<Integer, Rectangle2D.Float>();
        }
        Integer key = Integer.valueOf(glyphCode);
        Rectangle2D.Float bounds = boundsMap.get(key);
        if (bounds == null) {
            bounds = fileFont.getGlyphOutlineBounds(pScalerContext, glyphCode);
            boundsMap.put(key, bounds);
        }
        return bounds;
    }

    public Rectangle2D getOutlineBounds(int glyphCode) {
        return fileFont.getGlyphOutlineBounds(pScalerContext, glyphCode);
    }

    private WeakReference<ConcurrentHashMap<Integer, GeneralPath>> outlineMapRef;

    GeneralPath getGlyphOutline(int glyphCode, float x, float y) {
        GeneralPath gp = null;
        ConcurrentHashMap<Integer, GeneralPath> outlineMap = null;
        if (outlineMapRef != null) {
            outlineMap = outlineMapRef.get();
            if (outlineMap != null) {
                gp = outlineMap.get(glyphCode);
            }
        }
        if (gp == null) {
            gp = fileFont.getGlyphOutline(pScalerContext, glyphCode, 0, 0);
            if (outlineMap == null) {
                outlineMap = new ConcurrentHashMap<Integer, GeneralPath>();
                outlineMapRef = new WeakReference<ConcurrentHashMap<Integer, GeneralPath>>(outlineMap);
            }
            outlineMap.put(glyphCode, gp);
        }
        gp = (GeneralPath) gp.clone();
        if (x != 0f || y != 0f) {
            gp.transform(AffineTransform.getTranslateInstance(x, y));
        }
        return gp;
    }

    GeneralPath getGlyphVectorOutline(int[] glyphs, float x, float y) {
        return fileFont.getGlyphVectorOutline(pScalerContext, glyphs, glyphs.length, x, y);
    }

    protected void adjustPoint(Point2D.Float pt) {
        if (invertDevTx != null) {
            invertDevTx.deltaTransform(pt, pt);
        }
    }
}
