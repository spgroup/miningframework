package sun.java2d;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.geom.Area;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.awt.image.renderable.RenderContext;
import java.awt.image.AffineTransformOp;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.Image;
import java.awt.Composite;
import java.awt.Color;
import java.awt.image.ColorModel;
import java.awt.GraphicsConfiguration;
import java.awt.Paint;
import java.awt.GradientPaint;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.geom.PathIterator;
import java.awt.geom.GeneralPath;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.text.AttributedCharacterIterator;
import java.awt.Font;
import java.awt.image.ImageObserver;
import java.awt.Transparency;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import sun.awt.image.SurfaceManager;
import sun.font.FontDesignMetrics;
import sun.font.FontUtilities;
import sun.java2d.pipe.PixelDrawPipe;
import sun.java2d.pipe.PixelFillPipe;
import sun.java2d.pipe.ShapeDrawPipe;
import sun.java2d.pipe.ValidatePipe;
import sun.java2d.pipe.ShapeSpanIterator;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.TextPipe;
import sun.java2d.pipe.DrawImagePipe;
import sun.java2d.pipe.LoopPipe;
import sun.java2d.loops.FontInfo;
import sun.java2d.loops.RenderLoops;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.Blit;
import sun.java2d.loops.MaskFill;
import java.awt.font.FontRenderContext;
import sun.java2d.loops.XORComposite;
import sun.awt.ConstrainableGraphics;
import sun.awt.SunHints;
import sun.awt.util.PerformanceLogger;
import java.util.Map;
import java.util.Iterator;
import java.lang.annotation.Native;
import java.awt.image.MultiResolutionImage;
import static java.awt.geom.AffineTransform.TYPE_FLIP;
import static java.awt.geom.AffineTransform.TYPE_MASK_SCALE;
import static java.awt.geom.AffineTransform.TYPE_TRANSLATION;
import java.awt.image.VolatileImage;
import sun.awt.image.MultiResolutionToolkitImage;
import sun.awt.image.ToolkitImage;

public final class SunGraphics2D extends Graphics2D implements ConstrainableGraphics, Cloneable, DestSurfaceProvider {

    @Native
    public static final int PAINT_CUSTOM = 6;

    @Native
    public static final int PAINT_TEXTURE = 5;

    @Native
    public static final int PAINT_RAD_GRADIENT = 4;

    @Native
    public static final int PAINT_LIN_GRADIENT = 3;

    @Native
    public static final int PAINT_GRADIENT = 2;

    @Native
    public static final int PAINT_ALPHACOLOR = 1;

    @Native
    public static final int PAINT_OPAQUECOLOR = 0;

    @Native
    public static final int COMP_CUSTOM = 3;

    @Native
    public static final int COMP_XOR = 2;

    @Native
    public static final int COMP_ALPHA = 1;

    @Native
    public static final int COMP_ISCOPY = 0;

    @Native
    public static final int STROKE_CUSTOM = 3;

    @Native
    public static final int STROKE_WIDE = 2;

    @Native
    public static final int STROKE_THINDASHED = 1;

    @Native
    public static final int STROKE_THIN = 0;

    @Native
    public static final int TRANSFORM_GENERIC = 4;

    @Native
    public static final int TRANSFORM_TRANSLATESCALE = 3;

    @Native
    public static final int TRANSFORM_ANY_TRANSLATE = 2;

    @Native
    public static final int TRANSFORM_INT_TRANSLATE = 1;

    @Native
    public static final int TRANSFORM_ISIDENT = 0;

    @Native
    public static final int CLIP_SHAPE = 2;

    @Native
    public static final int CLIP_RECTANGULAR = 1;

    @Native
    public static final int CLIP_DEVICE = 0;

    public int eargb;

    public int pixel;

    public SurfaceData surfaceData;

    public PixelDrawPipe drawpipe;

    public PixelFillPipe fillpipe;

    public DrawImagePipe imagepipe;

    public ShapeDrawPipe shapepipe;

    public TextPipe textpipe;

    public MaskFill alphafill;

    public RenderLoops loops;

    public CompositeType imageComp;

    public int paintState;

    public int compositeState;

    public int strokeState;

    public int transformState;

    public int clipState;

    public Color foregroundColor;

    public Color backgroundColor;

    public AffineTransform transform;

    public int transX;

    public int transY;

    protected static final Stroke defaultStroke = new BasicStroke();

    protected static final Composite defaultComposite = AlphaComposite.SrcOver;

    private static final Font defaultFont = new Font(Font.DIALOG, Font.PLAIN, 12);

    public Paint paint;

    public Stroke stroke;

    public Composite composite;

    protected Font font;

    protected FontMetrics fontMetrics;

    public int renderHint;

    public int antialiasHint;

    public int textAntialiasHint;

    protected int fractionalMetricsHint;

    public int lcdTextContrast;

    private static int lcdTextContrastDefaultValue = 140;

    private int interpolationHint;

    public int strokeHint;

    public int interpolationType;

    public RenderingHints hints;

    public Region constrainClip;

    public int constrainX;

    public int constrainY;

    public Region clipRegion;

    public Shape usrClip;

    protected Region devClip;

    private int resolutionVariantHint;

    private boolean validFontInfo;

    private FontInfo fontInfo;

    private FontInfo glyphVectorFontInfo;

    private FontRenderContext glyphVectorFRC;

    private static final int slowTextTransformMask = AffineTransform.TYPE_GENERAL_TRANSFORM | AffineTransform.TYPE_MASK_ROTATION | AffineTransform.TYPE_FLIP;

    static {
        if (PerformanceLogger.loggingEnabled()) {
            PerformanceLogger.setTime("SunGraphics2D static initialization");
        }
    }

    public SunGraphics2D(SurfaceData sd, Color fg, Color bg, Font f) {
        surfaceData = sd;
        foregroundColor = fg;
        backgroundColor = bg;
        stroke = defaultStroke;
        composite = defaultComposite;
        paint = foregroundColor;
        imageComp = CompositeType.SrcOverNoEa;
        renderHint = SunHints.INTVAL_RENDER_DEFAULT;
        antialiasHint = SunHints.INTVAL_ANTIALIAS_OFF;
        textAntialiasHint = SunHints.INTVAL_TEXT_ANTIALIAS_DEFAULT;
        fractionalMetricsHint = SunHints.INTVAL_FRACTIONALMETRICS_OFF;
        lcdTextContrast = lcdTextContrastDefaultValue;
        interpolationHint = -1;
        strokeHint = SunHints.INTVAL_STROKE_DEFAULT;
        resolutionVariantHint = SunHints.INTVAL_RESOLUTION_VARIANT_DEFAULT;
        interpolationType = AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
        transform = getDefaultTransform();
        if (!transform.isIdentity()) {
            invalidateTransform();
        }
        validateColor();
        font = f;
        if (font == null) {
            font = defaultFont;
        }
        setDevClip(sd.getBounds());
        invalidatePipe();
    }

    private AffineTransform getDefaultTransform() {
        GraphicsConfiguration gc = getDeviceConfiguration();
        return (gc == null) ? new AffineTransform() : gc.getDefaultTransform();
    }

    protected Object clone() {
        try {
            SunGraphics2D g = (SunGraphics2D) super.clone();
            g.transform = new AffineTransform(this.transform);
            if (hints != null) {
                g.hints = (RenderingHints) this.hints.clone();
            }
            if (this.fontInfo != null) {
                if (this.validFontInfo) {
                    g.fontInfo = (FontInfo) this.fontInfo.clone();
                } else {
                    g.fontInfo = null;
                }
            }
            if (this.glyphVectorFontInfo != null) {
                g.glyphVectorFontInfo = (FontInfo) this.glyphVectorFontInfo.clone();
                g.glyphVectorFRC = this.glyphVectorFRC;
            }
            return g;
        } catch (CloneNotSupportedException e) {
        }
        return null;
    }

    public Graphics create() {
        return (Graphics) clone();
    }

    public void setDevClip(int x, int y, int w, int h) {
        Region c = constrainClip;
        if (c == null) {
            devClip = Region.getInstanceXYWH(x, y, w, h);
        } else {
            devClip = c.getIntersectionXYWH(x, y, w, h);
        }
        validateCompClip();
    }

    public void setDevClip(Rectangle r) {
        setDevClip(r.x, r.y, r.width, r.height);
    }

    public void constrain(int x, int y, int w, int h, Region region) {
        if ((x | y) != 0) {
            translate(x, y);
        }
        if (transformState > TRANSFORM_TRANSLATESCALE) {
            clipRect(0, 0, w, h);
            return;
        }
        final double scaleX = transform.getScaleX();
        final double scaleY = transform.getScaleY();
        x = constrainX = (int) transform.getTranslateX();
        y = constrainY = (int) transform.getTranslateY();
        w = Region.dimAdd(x, Region.clipScale(w, scaleX));
        h = Region.dimAdd(y, Region.clipScale(h, scaleY));
        Region c = constrainClip;
        if (c == null) {
            c = Region.getInstanceXYXY(x, y, w, h);
        } else {
            c = c.getIntersectionXYXY(x, y, w, h);
        }
        if (region != null) {
            region = region.getScaledRegion(scaleX, scaleY);
            region = region.getTranslatedRegion(x, y);
            c = c.getIntersection(region);
        }
        if (c == constrainClip) {
            return;
        }
        constrainClip = c;
        if (!devClip.isInsideQuickCheck(c)) {
            devClip = devClip.getIntersection(c);
            validateCompClip();
        }
    }

    @Override
    public void constrain(int x, int y, int w, int h) {
        constrain(x, y, w, h, null);
    }

    protected static ValidatePipe invalidpipe = new ValidatePipe();

    protected void invalidatePipe() {
        drawpipe = invalidpipe;
        fillpipe = invalidpipe;
        shapepipe = invalidpipe;
        textpipe = invalidpipe;
        imagepipe = invalidpipe;
        loops = null;
    }

    public void validatePipe() {
        if (!surfaceData.isValid()) {
            throw new InvalidPipeException("attempt to validate Pipe with invalid SurfaceData");
        }
        surfaceData.validatePipe(this);
    }

    Shape intersectShapes(Shape s1, Shape s2, boolean keep1, boolean keep2) {
        if (s1 instanceof Rectangle && s2 instanceof Rectangle) {
            return ((Rectangle) s1).intersection((Rectangle) s2);
        }
        if (s1 instanceof Rectangle2D) {
            return intersectRectShape((Rectangle2D) s1, s2, keep1, keep2);
        } else if (s2 instanceof Rectangle2D) {
            return intersectRectShape((Rectangle2D) s2, s1, keep2, keep1);
        }
        return intersectByArea(s1, s2, keep1, keep2);
    }

    Shape intersectRectShape(Rectangle2D r, Shape s, boolean keep1, boolean keep2) {
        if (s instanceof Rectangle2D) {
            Rectangle2D r2 = (Rectangle2D) s;
            Rectangle2D outrect;
            if (!keep1) {
                outrect = r;
            } else if (!keep2) {
                outrect = r2;
            } else {
                outrect = new Rectangle2D.Float();
            }
            double x1 = Math.max(r.getX(), r2.getX());
            double x2 = Math.min(r.getX() + r.getWidth(), r2.getX() + r2.getWidth());
            double y1 = Math.max(r.getY(), r2.getY());
            double y2 = Math.min(r.getY() + r.getHeight(), r2.getY() + r2.getHeight());
            if (((x2 - x1) < 0) || ((y2 - y1) < 0))
                outrect.setFrameFromDiagonal(0, 0, 0, 0);
            else
                outrect.setFrameFromDiagonal(x1, y1, x2, y2);
            return outrect;
        }
        if (r.contains(s.getBounds2D())) {
            if (keep2) {
                s = cloneShape(s);
            }
            return s;
        }
        return intersectByArea(r, s, keep1, keep2);
    }

    protected static Shape cloneShape(Shape s) {
        return new GeneralPath(s);
    }

    Shape intersectByArea(Shape s1, Shape s2, boolean keep1, boolean keep2) {
        Area a1, a2;
        if (!keep1 && (s1 instanceof Area)) {
            a1 = (Area) s1;
        } else if (!keep2 && (s2 instanceof Area)) {
            a1 = (Area) s2;
            s2 = s1;
        } else {
            a1 = new Area(s1);
        }
        if (s2 instanceof Area) {
            a2 = (Area) s2;
        } else {
            a2 = new Area(s2);
        }
        a1.intersect(a2);
        if (a1.isRectangular()) {
            return a1.getBounds();
        }
        return a1;
    }

    public Region getCompClip() {
        if (!surfaceData.isValid()) {
            revalidateAll();
        }
        return clipRegion;
    }

    public Font getFont() {
        if (font == null) {
            font = defaultFont;
        }
        return font;
    }

    private static final double[] IDENT_MATRIX = { 1, 0, 0, 1 };

    private static final AffineTransform IDENT_ATX = new AffineTransform();

    private static final int MINALLOCATED = 8;

    private static final int TEXTARRSIZE = 17;

    private static double[][] textTxArr = new double[TEXTARRSIZE][];

    private static AffineTransform[] textAtArr = new AffineTransform[TEXTARRSIZE];

    static {
        for (int i = MINALLOCATED; i < TEXTARRSIZE; i++) {
            textTxArr[i] = new double[] { i, 0, 0, i };
            textAtArr[i] = new AffineTransform(textTxArr[i]);
        }
    }

    public FontInfo checkFontInfo(FontInfo info, Font font, FontRenderContext frc) {
        if (info == null) {
            info = new FontInfo();
        }
        float ptSize = font.getSize2D();
        int txFontType;
        AffineTransform devAt, textAt = null;
        if (font.isTransformed()) {
            textAt = font.getTransform();
            textAt.scale(ptSize, ptSize);
            txFontType = textAt.getType();
            info.originX = (float) textAt.getTranslateX();
            info.originY = (float) textAt.getTranslateY();
            textAt.translate(-info.originX, -info.originY);
            if (transformState >= TRANSFORM_TRANSLATESCALE) {
                transform.getMatrix(info.devTx = new double[4]);
                devAt = new AffineTransform(info.devTx);
                textAt.preConcatenate(devAt);
            } else {
                info.devTx = IDENT_MATRIX;
                devAt = IDENT_ATX;
            }
            textAt.getMatrix(info.glyphTx = new double[4]);
            double shearx = textAt.getShearX();
            double scaley = textAt.getScaleY();
            if (shearx != 0) {
                scaley = Math.sqrt(shearx * shearx + scaley * scaley);
            }
            info.pixelHeight = (int) (Math.abs(scaley) + 0.5);
        } else {
            txFontType = AffineTransform.TYPE_IDENTITY;
            info.originX = info.originY = 0;
            if (transformState >= TRANSFORM_TRANSLATESCALE) {
                transform.getMatrix(info.devTx = new double[4]);
                devAt = new AffineTransform(info.devTx);
                info.glyphTx = new double[4];
                for (int i = 0; i < 4; i++) {
                    info.glyphTx[i] = info.devTx[i] * ptSize;
                }
                textAt = new AffineTransform(info.glyphTx);
                double shearx = transform.getShearX();
                double scaley = transform.getScaleY();
                if (shearx != 0) {
                    scaley = Math.sqrt(shearx * shearx + scaley * scaley);
                }
                info.pixelHeight = (int) (Math.abs(scaley * ptSize) + 0.5);
            } else {
                int pszInt = (int) ptSize;
                if (ptSize == pszInt && pszInt >= MINALLOCATED && pszInt < TEXTARRSIZE) {
                    info.glyphTx = textTxArr[pszInt];
                    textAt = textAtArr[pszInt];
                    info.pixelHeight = pszInt;
                } else {
                    info.pixelHeight = (int) (ptSize + 0.5);
                }
                if (textAt == null) {
                    info.glyphTx = new double[] { ptSize, 0, 0, ptSize };
                    textAt = new AffineTransform(info.glyphTx);
                }
                info.devTx = IDENT_MATRIX;
                devAt = IDENT_ATX;
            }
        }
        info.font2D = FontUtilities.getFont2D(font);
        int fmhint = fractionalMetricsHint;
        if (fmhint == SunHints.INTVAL_FRACTIONALMETRICS_DEFAULT) {
            fmhint = SunHints.INTVAL_FRACTIONALMETRICS_OFF;
        }
        info.lcdSubPixPos = false;
        int aahint;
        if (frc == null) {
            aahint = textAntialiasHint;
        } else {
            aahint = ((SunHints.Value) frc.getAntiAliasingHint()).getIndex();
        }
        if (aahint == SunHints.INTVAL_TEXT_ANTIALIAS_DEFAULT) {
            if (antialiasHint == SunHints.INTVAL_ANTIALIAS_ON) {
                aahint = SunHints.INTVAL_TEXT_ANTIALIAS_ON;
            } else {
                aahint = SunHints.INTVAL_TEXT_ANTIALIAS_OFF;
            }
        } else {
            if (aahint == SunHints.INTVAL_TEXT_ANTIALIAS_GASP) {
                if (info.font2D.useAAForPtSize(info.pixelHeight)) {
                    aahint = SunHints.INTVAL_TEXT_ANTIALIAS_ON;
                } else {
                    aahint = SunHints.INTVAL_TEXT_ANTIALIAS_OFF;
                }
            } else if (aahint >= SunHints.INTVAL_TEXT_ANTIALIAS_LCD_HRGB) {
                if (!surfaceData.canRenderLCDText(this)) {
                    aahint = SunHints.INTVAL_TEXT_ANTIALIAS_ON;
                } else {
                    info.lcdRGBOrder = true;
                    if (aahint == SunHints.INTVAL_TEXT_ANTIALIAS_LCD_HBGR) {
                        aahint = SunHints.INTVAL_TEXT_ANTIALIAS_LCD_HRGB;
                        info.lcdRGBOrder = false;
                    } else if (aahint == SunHints.INTVAL_TEXT_ANTIALIAS_LCD_VBGR) {
                        aahint = SunHints.INTVAL_TEXT_ANTIALIAS_LCD_VRGB;
                        info.lcdRGBOrder = false;
                    }
                    info.lcdSubPixPos = fmhint == SunHints.INTVAL_FRACTIONALMETRICS_ON && aahint == SunHints.INTVAL_TEXT_ANTIALIAS_LCD_HRGB;
                }
            }
        }
        info.aaHint = aahint;
        info.fontStrike = info.font2D.getStrike(font, devAt, textAt, aahint, fmhint);
        return info;
    }

    public static boolean isRotated(double[] mtx) {
        if ((mtx[0] == mtx[3]) && (mtx[1] == 0.0) && (mtx[2] == 0.0) && (mtx[0] > 0.0)) {
            return false;
        }
        return true;
    }

    public void setFont(Font font) {
        if (font != null && font != this.font) {
            if (textAntialiasHint == SunHints.INTVAL_TEXT_ANTIALIAS_GASP && textpipe != invalidpipe && (transformState > TRANSFORM_ANY_TRANSLATE || font.isTransformed() || fontInfo == null || (fontInfo.aaHint == SunHints.INTVAL_TEXT_ANTIALIAS_ON) != FontUtilities.getFont2D(font).useAAForPtSize(font.getSize()))) {
                textpipe = invalidpipe;
            }
            this.font = font;
            this.fontMetrics = null;
            this.validFontInfo = false;
        }
    }

    public FontInfo getFontInfo() {
        if (!validFontInfo) {
            this.fontInfo = checkFontInfo(this.fontInfo, font, null);
            validFontInfo = true;
        }
        return this.fontInfo;
    }

    public FontInfo getGVFontInfo(Font font, FontRenderContext frc) {
        if (glyphVectorFontInfo != null && glyphVectorFontInfo.font == font && glyphVectorFRC == frc) {
            return glyphVectorFontInfo;
        } else {
            glyphVectorFRC = frc;
            return glyphVectorFontInfo = checkFontInfo(glyphVectorFontInfo, font, frc);
        }
    }

    public FontMetrics getFontMetrics() {
        if (this.fontMetrics != null) {
            return this.fontMetrics;
        }
        return this.fontMetrics = FontDesignMetrics.getMetrics(font, getFontRenderContext());
    }

    public FontMetrics getFontMetrics(Font font) {
        if ((this.fontMetrics != null) && (font == this.font)) {
            return this.fontMetrics;
        }
        FontMetrics fm = FontDesignMetrics.getMetrics(font, getFontRenderContext());
        if (this.font == font) {
            this.fontMetrics = fm;
        }
        return fm;
    }

    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        if (onStroke) {
            s = stroke.createStrokedShape(s);
        }
        s = transformShape(s);
        if ((constrainX | constrainY) != 0) {
            rect = new Rectangle(rect);
            rect.translate(constrainX, constrainY);
        }
        return s.intersects(rect);
    }

    public ColorModel getDeviceColorModel() {
        return surfaceData.getColorModel();
    }

    public GraphicsConfiguration getDeviceConfiguration() {
        return surfaceData.getDeviceConfiguration();
    }

    public SurfaceData getSurfaceData() {
        return surfaceData;
    }

    public void setComposite(Composite comp) {
        if (composite == comp) {
            return;
        }
        int newCompState;
        CompositeType newCompType;
        if (comp instanceof AlphaComposite) {
            AlphaComposite alphacomp = (AlphaComposite) comp;
            newCompType = CompositeType.forAlphaComposite(alphacomp);
            if (newCompType == CompositeType.SrcOverNoEa) {
                if (paintState == PAINT_OPAQUECOLOR || (paintState > PAINT_ALPHACOLOR && paint.getTransparency() == Transparency.OPAQUE)) {
                    newCompState = COMP_ISCOPY;
                } else {
                    newCompState = COMP_ALPHA;
                }
            } else if (newCompType == CompositeType.SrcNoEa || newCompType == CompositeType.Src || newCompType == CompositeType.Clear) {
                newCompState = COMP_ISCOPY;
            } else if (surfaceData.getTransparency() == Transparency.OPAQUE && newCompType == CompositeType.SrcIn) {
                newCompState = COMP_ISCOPY;
            } else {
                newCompState = COMP_ALPHA;
            }
        } else if (comp instanceof XORComposite) {
            newCompState = COMP_XOR;
            newCompType = CompositeType.Xor;
        } else if (comp == null) {
            throw new IllegalArgumentException("null Composite");
        } else {
            surfaceData.checkCustomComposite();
            newCompState = COMP_CUSTOM;
            newCompType = CompositeType.General;
        }
        if (compositeState != newCompState || imageComp != newCompType) {
            compositeState = newCompState;
            imageComp = newCompType;
            invalidatePipe();
            validFontInfo = false;
        }
        composite = comp;
        if (paintState <= PAINT_ALPHACOLOR) {
            validateColor();
        }
    }

    public void setPaint(Paint paint) {
        if (paint instanceof Color) {
            setColor((Color) paint);
            return;
        }
        if (paint == null || this.paint == paint) {
            return;
        }
        this.paint = paint;
        if (imageComp == CompositeType.SrcOverNoEa) {
            if (paint.getTransparency() == Transparency.OPAQUE) {
                if (compositeState != COMP_ISCOPY) {
                    compositeState = COMP_ISCOPY;
                }
            } else {
                if (compositeState == COMP_ISCOPY) {
                    compositeState = COMP_ALPHA;
                }
            }
        }
        Class<? extends Paint> paintClass = paint.getClass();
        if (paintClass == GradientPaint.class) {
            paintState = PAINT_GRADIENT;
        } else if (paintClass == LinearGradientPaint.class) {
            paintState = PAINT_LIN_GRADIENT;
        } else if (paintClass == RadialGradientPaint.class) {
            paintState = PAINT_RAD_GRADIENT;
        } else if (paintClass == TexturePaint.class) {
            paintState = PAINT_TEXTURE;
        } else {
            paintState = PAINT_CUSTOM;
        }
        validFontInfo = false;
        invalidatePipe();
    }

    static final int NON_UNIFORM_SCALE_MASK = (AffineTransform.TYPE_GENERAL_TRANSFORM | AffineTransform.TYPE_GENERAL_SCALE);

    public static final double MinPenSizeAA = sun.java2d.pipe.RenderingEngine.getInstance().getMinimumAAPenSize();

    public static final double MinPenSizeAASquared = (MinPenSizeAA * MinPenSizeAA);

    public static final double MinPenSizeSquared = 1.000000001;

    private void validateBasicStroke(BasicStroke bs) {
        boolean aa = (antialiasHint == SunHints.INTVAL_ANTIALIAS_ON);
        if (transformState < TRANSFORM_TRANSLATESCALE) {
            if (aa) {
                if (bs.getLineWidth() <= MinPenSizeAA) {
                    if (bs.getDashArray() == null) {
                        strokeState = STROKE_THIN;
                    } else {
                        strokeState = STROKE_THINDASHED;
                    }
                } else {
                    strokeState = STROKE_WIDE;
                }
            } else {
                if (bs == defaultStroke) {
                    strokeState = STROKE_THIN;
                } else if (bs.getLineWidth() <= 1.0f) {
                    if (bs.getDashArray() == null) {
                        strokeState = STROKE_THIN;
                    } else {
                        strokeState = STROKE_THINDASHED;
                    }
                } else {
                    strokeState = STROKE_WIDE;
                }
            }
        } else {
            double widthsquared;
            if ((transform.getType() & NON_UNIFORM_SCALE_MASK) == 0) {
                widthsquared = Math.abs(transform.getDeterminant());
            } else {
                double A = transform.getScaleX();
                double C = transform.getShearX();
                double B = transform.getShearY();
                double D = transform.getScaleY();
                double EA = A * A + B * B;
                double EB = 2 * (A * C + B * D);
                double EC = C * C + D * D;
                double hypot = Math.sqrt(EB * EB + (EA - EC) * (EA - EC));
                widthsquared = ((EA + EC + hypot) / 2.0);
            }
            if (bs != defaultStroke) {
                widthsquared *= bs.getLineWidth() * bs.getLineWidth();
            }
            if (widthsquared <= (aa ? MinPenSizeAASquared : MinPenSizeSquared)) {
                if (bs.getDashArray() == null) {
                    strokeState = STROKE_THIN;
                } else {
                    strokeState = STROKE_THINDASHED;
                }
            } else {
                strokeState = STROKE_WIDE;
            }
        }
    }

    public void setStroke(Stroke s) {
        if (s == null) {
            throw new IllegalArgumentException("null Stroke");
        }
        int saveStrokeState = strokeState;
        stroke = s;
        if (s instanceof BasicStroke) {
            validateBasicStroke((BasicStroke) s);
        } else {
            strokeState = STROKE_CUSTOM;
        }
        if (strokeState != saveStrokeState) {
            invalidatePipe();
        }
    }

    public void setRenderingHint(Key hintKey, Object hintValue) {
        if (!hintKey.isCompatibleValue(hintValue)) {
            throw new IllegalArgumentException(hintValue + " is not compatible with " + hintKey);
        }
        if (hintKey instanceof SunHints.Key) {
            boolean stateChanged;
            boolean textStateChanged = false;
            boolean recognized = true;
            SunHints.Key sunKey = (SunHints.Key) hintKey;
            int newHint;
            if (sunKey == SunHints.KEY_TEXT_ANTIALIAS_LCD_CONTRAST) {
                newHint = ((Integer) hintValue).intValue();
            } else {
                newHint = ((SunHints.Value) hintValue).getIndex();
            }
            switch(sunKey.getIndex()) {
                case SunHints.INTKEY_RENDERING:
                    stateChanged = (renderHint != newHint);
                    if (stateChanged) {
                        renderHint = newHint;
                        if (interpolationHint == -1) {
                            interpolationType = (newHint == SunHints.INTVAL_RENDER_QUALITY ? AffineTransformOp.TYPE_BILINEAR : AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                        }
                    }
                    break;
                case SunHints.INTKEY_ANTIALIASING:
                    stateChanged = (antialiasHint != newHint);
                    antialiasHint = newHint;
                    if (stateChanged) {
                        textStateChanged = (textAntialiasHint == SunHints.INTVAL_TEXT_ANTIALIAS_DEFAULT);
                        if (strokeState != STROKE_CUSTOM) {
                            validateBasicStroke((BasicStroke) stroke);
                        }
                    }
                    break;
                case SunHints.INTKEY_TEXT_ANTIALIASING:
                    stateChanged = (textAntialiasHint != newHint);
                    textStateChanged = stateChanged;
                    textAntialiasHint = newHint;
                    break;
                case SunHints.INTKEY_FRACTIONALMETRICS:
                    stateChanged = (fractionalMetricsHint != newHint);
                    textStateChanged = stateChanged;
                    fractionalMetricsHint = newHint;
                    break;
                case SunHints.INTKEY_AATEXT_LCD_CONTRAST:
                    stateChanged = false;
                    lcdTextContrast = newHint;
                    break;
                case SunHints.INTKEY_INTERPOLATION:
                    interpolationHint = newHint;
                    switch(newHint) {
                        case SunHints.INTVAL_INTERPOLATION_BICUBIC:
                            newHint = AffineTransformOp.TYPE_BICUBIC;
                            break;
                        case SunHints.INTVAL_INTERPOLATION_BILINEAR:
                            newHint = AffineTransformOp.TYPE_BILINEAR;
                            break;
                        default:
                        case SunHints.INTVAL_INTERPOLATION_NEAREST_NEIGHBOR:
                            newHint = AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
                            break;
                    }
                    stateChanged = (interpolationType != newHint);
                    interpolationType = newHint;
                    break;
                case SunHints.INTKEY_STROKE_CONTROL:
                    stateChanged = (strokeHint != newHint);
                    strokeHint = newHint;
                    break;
                case SunHints.INTKEY_RESOLUTION_VARIANT:
                    stateChanged = (resolutionVariantHint != newHint);
                    resolutionVariantHint = newHint;
                    break;
                default:
                    recognized = false;
                    stateChanged = false;
                    break;
            }
            if (recognized) {
                if (stateChanged) {
                    invalidatePipe();
                    if (textStateChanged) {
                        fontMetrics = null;
                        this.cachedFRC = null;
                        validFontInfo = false;
                        this.glyphVectorFontInfo = null;
                    }
                }
                if (hints != null) {
                    hints.put(hintKey, hintValue);
                }
                return;
            }
        }
        if (hints == null) {
            hints = makeHints(null);
        }
        hints.put(hintKey, hintValue);
    }

    public Object getRenderingHint(Key hintKey) {
        if (hints != null) {
            return hints.get(hintKey);
        }
        if (!(hintKey instanceof SunHints.Key)) {
            return null;
        }
        int keyindex = ((SunHints.Key) hintKey).getIndex();
        switch(keyindex) {
            case SunHints.INTKEY_RENDERING:
                return SunHints.Value.get(SunHints.INTKEY_RENDERING, renderHint);
            case SunHints.INTKEY_ANTIALIASING:
                return SunHints.Value.get(SunHints.INTKEY_ANTIALIASING, antialiasHint);
            case SunHints.INTKEY_TEXT_ANTIALIASING:
                return SunHints.Value.get(SunHints.INTKEY_TEXT_ANTIALIASING, textAntialiasHint);
            case SunHints.INTKEY_FRACTIONALMETRICS:
                return SunHints.Value.get(SunHints.INTKEY_FRACTIONALMETRICS, fractionalMetricsHint);
            case SunHints.INTKEY_AATEXT_LCD_CONTRAST:
                return lcdTextContrast;
            case SunHints.INTKEY_INTERPOLATION:
                switch(interpolationHint) {
                    case SunHints.INTVAL_INTERPOLATION_NEAREST_NEIGHBOR:
                        return SunHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                    case SunHints.INTVAL_INTERPOLATION_BILINEAR:
                        return SunHints.VALUE_INTERPOLATION_BILINEAR;
                    case SunHints.INTVAL_INTERPOLATION_BICUBIC:
                        return SunHints.VALUE_INTERPOLATION_BICUBIC;
                }
                return null;
            case SunHints.INTKEY_STROKE_CONTROL:
                return SunHints.Value.get(SunHints.INTKEY_STROKE_CONTROL, strokeHint);
            case SunHints.INTKEY_RESOLUTION_VARIANT:
                return SunHints.Value.get(SunHints.INTKEY_RESOLUTION_VARIANT, resolutionVariantHint);
        }
        return null;
    }

    public void setRenderingHints(Map<?, ?> hints) {
        this.hints = null;
        renderHint = SunHints.INTVAL_RENDER_DEFAULT;
        antialiasHint = SunHints.INTVAL_ANTIALIAS_OFF;
        textAntialiasHint = SunHints.INTVAL_TEXT_ANTIALIAS_DEFAULT;
        fractionalMetricsHint = SunHints.INTVAL_FRACTIONALMETRICS_OFF;
        lcdTextContrast = lcdTextContrastDefaultValue;
        interpolationHint = -1;
        interpolationType = AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
        boolean customHintPresent = false;
        Iterator<?> iter = hints.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            if (key == SunHints.KEY_RENDERING || key == SunHints.KEY_ANTIALIASING || key == SunHints.KEY_TEXT_ANTIALIASING || key == SunHints.KEY_FRACTIONALMETRICS || key == SunHints.KEY_TEXT_ANTIALIAS_LCD_CONTRAST || key == SunHints.KEY_STROKE_CONTROL || key == SunHints.KEY_INTERPOLATION) {
                setRenderingHint((Key) key, hints.get(key));
            } else {
                customHintPresent = true;
            }
        }
        if (customHintPresent) {
            this.hints = makeHints(hints);
        }
        invalidatePipe();
    }

    public void addRenderingHints(Map<?, ?> hints) {
        boolean customHintPresent = false;
        Iterator<?> iter = hints.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            if (key == SunHints.KEY_RENDERING || key == SunHints.KEY_ANTIALIASING || key == SunHints.KEY_TEXT_ANTIALIASING || key == SunHints.KEY_FRACTIONALMETRICS || key == SunHints.KEY_TEXT_ANTIALIAS_LCD_CONTRAST || key == SunHints.KEY_STROKE_CONTROL || key == SunHints.KEY_INTERPOLATION) {
                setRenderingHint((Key) key, hints.get(key));
            } else {
                customHintPresent = true;
            }
        }
        if (customHintPresent) {
            if (this.hints == null) {
                this.hints = makeHints(hints);
            } else {
                this.hints.putAll(hints);
            }
        }
    }

    public RenderingHints getRenderingHints() {
        if (hints == null) {
            return makeHints(null);
        } else {
            return (RenderingHints) hints.clone();
        }
    }

    RenderingHints makeHints(Map<?, ?> hints) {
        RenderingHints model = new RenderingHints(null);
        if (hints != null) {
            model.putAll(hints);
        }
        model.put(SunHints.KEY_RENDERING, SunHints.Value.get(SunHints.INTKEY_RENDERING, renderHint));
        model.put(SunHints.KEY_ANTIALIASING, SunHints.Value.get(SunHints.INTKEY_ANTIALIASING, antialiasHint));
        model.put(SunHints.KEY_TEXT_ANTIALIASING, SunHints.Value.get(SunHints.INTKEY_TEXT_ANTIALIASING, textAntialiasHint));
        model.put(SunHints.KEY_FRACTIONALMETRICS, SunHints.Value.get(SunHints.INTKEY_FRACTIONALMETRICS, fractionalMetricsHint));
        model.put(SunHints.KEY_TEXT_ANTIALIAS_LCD_CONTRAST, Integer.valueOf(lcdTextContrast));
        Object value;
        switch(interpolationHint) {
            case SunHints.INTVAL_INTERPOLATION_NEAREST_NEIGHBOR:
                value = SunHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                break;
            case SunHints.INTVAL_INTERPOLATION_BILINEAR:
                value = SunHints.VALUE_INTERPOLATION_BILINEAR;
                break;
            case SunHints.INTVAL_INTERPOLATION_BICUBIC:
                value = SunHints.VALUE_INTERPOLATION_BICUBIC;
                break;
            default:
                value = null;
                break;
        }
        if (value != null) {
            model.put(SunHints.KEY_INTERPOLATION, value);
        }
        model.put(SunHints.KEY_STROKE_CONTROL, SunHints.Value.get(SunHints.INTKEY_STROKE_CONTROL, strokeHint));
        return model;
    }

    public void translate(double tx, double ty) {
        transform.translate(tx, ty);
        invalidateTransform();
    }

    public void rotate(double theta) {
        transform.rotate(theta);
        invalidateTransform();
    }

    public void rotate(double theta, double x, double y) {
        transform.rotate(theta, x, y);
        invalidateTransform();
    }

    public void scale(double sx, double sy) {
        transform.scale(sx, sy);
        invalidateTransform();
    }

    public void shear(double shx, double shy) {
        transform.shear(shx, shy);
        invalidateTransform();
    }

    public void transform(AffineTransform xform) {
        this.transform.concatenate(xform);
        invalidateTransform();
    }

    public void translate(int x, int y) {
        transform.translate(x, y);
        if (transformState <= TRANSFORM_INT_TRANSLATE) {
            transX += x;
            transY += y;
            transformState = (((transX | transY) == 0) ? TRANSFORM_ISIDENT : TRANSFORM_INT_TRANSLATE);
        } else {
            invalidateTransform();
        }
    }

    @Override
    public void setTransform(AffineTransform Tx) {
        if ((constrainX | constrainY) == 0) {
            transform.setTransform(Tx);
        } else {
            transform.setToTranslation(constrainX, constrainY);
            transform.concatenate(Tx);
        }
        invalidateTransform();
    }

    protected void invalidateTransform() {
        int type = transform.getType();
        int origTransformState = transformState;
        if (type == AffineTransform.TYPE_IDENTITY) {
            transformState = TRANSFORM_ISIDENT;
            transX = transY = 0;
        } else if (type == AffineTransform.TYPE_TRANSLATION) {
            double dtx = transform.getTranslateX();
            double dty = transform.getTranslateY();
            transX = (int) Math.floor(dtx + 0.5);
            transY = (int) Math.floor(dty + 0.5);
            if (dtx == transX && dty == transY) {
                transformState = TRANSFORM_INT_TRANSLATE;
            } else {
                transformState = TRANSFORM_ANY_TRANSLATE;
            }
        } else if ((type & (AffineTransform.TYPE_FLIP | AffineTransform.TYPE_MASK_ROTATION | AffineTransform.TYPE_GENERAL_TRANSFORM)) == 0) {
            transformState = TRANSFORM_TRANSLATESCALE;
            transX = transY = 0;
        } else {
            transformState = TRANSFORM_GENERIC;
            transX = transY = 0;
        }
        if (transformState >= TRANSFORM_TRANSLATESCALE || origTransformState >= TRANSFORM_TRANSLATESCALE) {
            cachedFRC = null;
            this.validFontInfo = false;
            this.fontMetrics = null;
            this.glyphVectorFontInfo = null;
            if (transformState != origTransformState) {
                invalidatePipe();
            }
        }
        if (strokeState != STROKE_CUSTOM) {
            validateBasicStroke((BasicStroke) stroke);
        }
    }

    @Override
    public AffineTransform getTransform() {
        if ((constrainX | constrainY) == 0) {
            return new AffineTransform(transform);
        }
        AffineTransform tx = AffineTransform.getTranslateInstance(-constrainX, -constrainY);
        tx.concatenate(transform);
        return tx;
    }

    public AffineTransform cloneTransform() {
        return new AffineTransform(transform);
    }

    public Paint getPaint() {
        return paint;
    }

    public Composite getComposite() {
        return composite;
    }

    public Color getColor() {
        return foregroundColor;
    }

    void validateColor() {
        int eargb;
        if (imageComp == CompositeType.Clear) {
            eargb = 0;
        } else {
            eargb = foregroundColor.getRGB();
            if (compositeState <= COMP_ALPHA && imageComp != CompositeType.SrcNoEa && imageComp != CompositeType.SrcOverNoEa) {
                AlphaComposite alphacomp = (AlphaComposite) composite;
                int a = Math.round(alphacomp.getAlpha() * (eargb >>> 24));
                eargb = (eargb & 0x00ffffff) | (a << 24);
            }
        }
        this.eargb = eargb;
        this.pixel = surfaceData.pixelFor(eargb);
    }

    public void setColor(Color color) {
        if (color == null || color == paint) {
            return;
        }
        this.paint = foregroundColor = color;
        validateColor();
        if ((eargb >> 24) == -1) {
            if (paintState == PAINT_OPAQUECOLOR) {
                return;
            }
            paintState = PAINT_OPAQUECOLOR;
            if (imageComp == CompositeType.SrcOverNoEa) {
                compositeState = COMP_ISCOPY;
            }
        } else {
            if (paintState == PAINT_ALPHACOLOR) {
                return;
            }
            paintState = PAINT_ALPHACOLOR;
            if (imageComp == CompositeType.SrcOverNoEa) {
                compositeState = COMP_ALPHA;
            }
        }
        validFontInfo = false;
        invalidatePipe();
    }

    public void setBackground(Color color) {
        backgroundColor = color;
    }

    public Color getBackground() {
        return backgroundColor;
    }

    public Stroke getStroke() {
        return stroke;
    }

    public Rectangle getClipBounds() {
        if (clipState == CLIP_DEVICE) {
            return null;
        }
        return getClipBounds(new Rectangle());
    }

    public Rectangle getClipBounds(Rectangle r) {
        if (clipState != CLIP_DEVICE) {
            if (transformState <= TRANSFORM_INT_TRANSLATE) {
                if (usrClip instanceof Rectangle) {
                    r.setBounds((Rectangle) usrClip);
                } else {
                    r.setFrame(usrClip.getBounds2D());
                }
                r.translate(-transX, -transY);
            } else {
                r.setFrame(getClip().getBounds2D());
            }
        } else if (r == null) {
            throw new NullPointerException("null rectangle parameter");
        }
        return r;
    }

    public boolean hitClip(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return false;
        }
        if (transformState > TRANSFORM_INT_TRANSLATE) {
            double[] d = { x, y, x + width, y, x, y + height, x + width, y + height };
            transform.transform(d, 0, d, 0, 4);
            x = (int) Math.floor(Math.min(Math.min(d[0], d[2]), Math.min(d[4], d[6])));
            y = (int) Math.floor(Math.min(Math.min(d[1], d[3]), Math.min(d[5], d[7])));
            width = (int) Math.ceil(Math.max(Math.max(d[0], d[2]), Math.max(d[4], d[6])));
            height = (int) Math.ceil(Math.max(Math.max(d[1], d[3]), Math.max(d[5], d[7])));
        } else {
            x += transX;
            y += transY;
            width += x;
            height += y;
        }
        try {
            if (!getCompClip().intersectsQuickCheckXYXY(x, y, width, height)) {
                return false;
            }
        } catch (InvalidPipeException e) {
            return false;
        }
        return true;
    }

    protected void validateCompClip() {
        int origClipState = clipState;
        if (usrClip == null) {
            clipState = CLIP_DEVICE;
            clipRegion = devClip;
        } else if (usrClip instanceof Rectangle2D) {
            clipState = CLIP_RECTANGULAR;
            clipRegion = devClip.getIntersection((Rectangle2D) usrClip);
        } else {
            PathIterator cpi = usrClip.getPathIterator(null);
            int[] box = new int[4];
            ShapeSpanIterator sr = LoopPipe.getFillSSI(this);
            try {
                sr.setOutputArea(devClip);
                sr.appendPath(cpi);
                sr.getPathBox(box);
                Region r = Region.getInstance(box, sr);
                clipRegion = r;
                clipState = r.isRectangular() ? CLIP_RECTANGULAR : CLIP_SHAPE;
            } finally {
                sr.dispose();
            }
        }
        if (origClipState != clipState && (clipState == CLIP_SHAPE || origClipState == CLIP_SHAPE)) {
            validFontInfo = false;
            invalidatePipe();
        }
    }

    static final int NON_RECTILINEAR_TRANSFORM_MASK = (AffineTransform.TYPE_GENERAL_TRANSFORM | AffineTransform.TYPE_GENERAL_ROTATION);

    protected Shape transformShape(Shape s) {
        if (s == null) {
            return null;
        }
        if (transformState > TRANSFORM_INT_TRANSLATE) {
            return transformShape(transform, s);
        } else {
            return transformShape(transX, transY, s);
        }
    }

    public Shape untransformShape(Shape s) {
        if (s == null) {
            return null;
        }
        if (transformState > TRANSFORM_INT_TRANSLATE) {
            try {
                return transformShape(transform.createInverse(), s);
            } catch (NoninvertibleTransformException e) {
                return null;
            }
        } else {
            return transformShape(-transX, -transY, s);
        }
    }

    protected static Shape transformShape(int tx, int ty, Shape s) {
        if (s == null) {
            return null;
        }
        if (s instanceof Rectangle) {
            Rectangle r = s.getBounds();
            r.translate(tx, ty);
            return r;
        }
        if (s instanceof Rectangle2D) {
            Rectangle2D rect = (Rectangle2D) s;
            return new Rectangle2D.Double(rect.getX() + tx, rect.getY() + ty, rect.getWidth(), rect.getHeight());
        }
        if (tx == 0 && ty == 0) {
            return cloneShape(s);
        }
        AffineTransform mat = AffineTransform.getTranslateInstance(tx, ty);
        return mat.createTransformedShape(s);
    }

    protected static Shape transformShape(AffineTransform tx, Shape clip) {
        if (clip == null) {
            return null;
        }
        if (clip instanceof Rectangle2D && (tx.getType() & NON_RECTILINEAR_TRANSFORM_MASK) == 0) {
            Rectangle2D rect = (Rectangle2D) clip;
            double[] matrix = new double[4];
            matrix[0] = rect.getX();
            matrix[1] = rect.getY();
            matrix[2] = matrix[0] + rect.getWidth();
            matrix[3] = matrix[1] + rect.getHeight();
            tx.transform(matrix, 0, matrix, 0, 2);
            fixRectangleOrientation(matrix, rect);
            return new Rectangle2D.Double(matrix[0], matrix[1], matrix[2] - matrix[0], matrix[3] - matrix[1]);
        }
        if (tx.isIdentity()) {
            return cloneShape(clip);
        }
        return tx.createTransformedShape(clip);
    }

    private static void fixRectangleOrientation(double[] m, Rectangle2D clip) {
        if (clip.getWidth() > 0 != (m[2] - m[0] > 0)) {
            double t = m[0];
            m[0] = m[2];
            m[2] = t;
        }
        if (clip.getHeight() > 0 != (m[3] - m[1] > 0)) {
            double t = m[1];
            m[1] = m[3];
            m[3] = t;
        }
    }

    public void clipRect(int x, int y, int w, int h) {
        clip(new Rectangle(x, y, w, h));
    }

    public void setClip(int x, int y, int w, int h) {
        setClip(new Rectangle(x, y, w, h));
    }

    public Shape getClip() {
        return untransformShape(usrClip);
    }

    public void setClip(Shape sh) {
        usrClip = transformShape(sh);
        validateCompClip();
    }

    public void clip(Shape s) {
        s = transformShape(s);
        if (usrClip != null) {
            s = intersectShapes(usrClip, s, true, true);
        }
        usrClip = s;
        validateCompClip();
    }

    public void setPaintMode() {
        setComposite(AlphaComposite.SrcOver);
    }

    public void setXORMode(Color c) {
        if (c == null) {
            throw new IllegalArgumentException("null XORColor");
        }
        setComposite(new XORComposite(c, surfaceData));
    }

    Blit lastCAblit;

    Composite lastCAcomp;

    public void copyArea(int x, int y, int w, int h, int dx, int dy) {
        try {
            doCopyArea(x, y, w, h, dx, dy);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                doCopyArea(x, y, w, h, dx, dy);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    private void doCopyArea(int x, int y, int w, int h, int dx, int dy) {
        if (w <= 0 || h <= 0) {
            return;
        }
        if (transformState == SunGraphics2D.TRANSFORM_ISIDENT) {
        } else if (transformState <= SunGraphics2D.TRANSFORM_ANY_TRANSLATE) {
            x += transX;
            y += transY;
        } else if (transformState == SunGraphics2D.TRANSFORM_TRANSLATESCALE) {
            final double[] coords = { x, y, x + w, y + h, x + dx, y + dy };
            transform.transform(coords, 0, coords, 0, 3);
            x = (int) Math.ceil(coords[0] - 0.5);
            y = (int) Math.ceil(coords[1] - 0.5);
            w = ((int) Math.ceil(coords[2] - 0.5)) - x;
            h = ((int) Math.ceil(coords[3] - 0.5)) - y;
            dx = ((int) Math.ceil(coords[4] - 0.5)) - x;
            dy = ((int) Math.ceil(coords[5] - 0.5)) - y;
            if (w < 0) {
                w = -w;
                x -= w;
            }
            if (h < 0) {
                h = -h;
                y -= h;
            }
        } else {
            throw new InternalError("transformed copyArea not implemented yet");
        }
        SurfaceData theData = surfaceData;
        if (theData.copyArea(this, x, y, w, h, dx, dy)) {
            return;
        }
        Region clip = getCompClip();
        Composite comp = composite;
        if (lastCAcomp != comp) {
            SurfaceType dsttype = theData.getSurfaceType();
            CompositeType comptype = imageComp;
            if (CompositeType.SrcOverNoEa.equals(comptype) && theData.getTransparency() == Transparency.OPAQUE) {
                comptype = CompositeType.SrcNoEa;
            }
            lastCAblit = Blit.locate(dsttype, comptype, dsttype);
            lastCAcomp = comp;
        }
        Blit ob = lastCAblit;
        if (dy == 0 && dx > 0 && dx < w) {
            while (w > 0) {
                int partW = Math.min(w, dx);
                w -= partW;
                int sx = x + w;
                ob.Blit(theData, theData, comp, clip, sx, y, sx + dx, y + dy, partW, h);
            }
            return;
        }
        if (dy > 0 && dy < h && dx > -w && dx < w) {
            while (h > 0) {
                int partH = Math.min(h, dy);
                h -= partH;
                int sy = y + h;
                ob.Blit(theData, theData, comp, clip, x, sy, x + dx, sy + dy, w, partH);
            }
            return;
        }
        ob.Blit(theData, theData, comp, clip, x, y, x + dx, y + dy, w, h);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        try {
            drawpipe.drawLine(this, x1, y1, x2, y2);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                drawpipe.drawLine(this, x1, y1, x2, y2);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public void drawRoundRect(int x, int y, int w, int h, int arcW, int arcH) {
        try {
            drawpipe.drawRoundRect(this, x, y, w, h, arcW, arcH);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                drawpipe.drawRoundRect(this, x, y, w, h, arcW, arcH);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public void fillRoundRect(int x, int y, int w, int h, int arcW, int arcH) {
        try {
            fillpipe.fillRoundRect(this, x, y, w, h, arcW, arcH);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                fillpipe.fillRoundRect(this, x, y, w, h, arcW, arcH);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public void drawOval(int x, int y, int w, int h) {
        try {
            drawpipe.drawOval(this, x, y, w, h);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                drawpipe.drawOval(this, x, y, w, h);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public void fillOval(int x, int y, int w, int h) {
        try {
            fillpipe.fillOval(this, x, y, w, h);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                fillpipe.fillOval(this, x, y, w, h);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public void drawArc(int x, int y, int w, int h, int startAngl, int arcAngl) {
        try {
            drawpipe.drawArc(this, x, y, w, h, startAngl, arcAngl);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                drawpipe.drawArc(this, x, y, w, h, startAngl, arcAngl);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public void fillArc(int x, int y, int w, int h, int startAngl, int arcAngl) {
        try {
            fillpipe.fillArc(this, x, y, w, h, startAngl, arcAngl);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                fillpipe.fillArc(this, x, y, w, h, startAngl, arcAngl);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        try {
            drawpipe.drawPolyline(this, xPoints, yPoints, nPoints);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                drawpipe.drawPolyline(this, xPoints, yPoints, nPoints);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        try {
            drawpipe.drawPolygon(this, xPoints, yPoints, nPoints);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                drawpipe.drawPolygon(this, xPoints, yPoints, nPoints);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        try {
            fillpipe.fillPolygon(this, xPoints, yPoints, nPoints);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                fillpipe.fillPolygon(this, xPoints, yPoints, nPoints);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public void drawRect(int x, int y, int w, int h) {
        try {
            drawpipe.drawRect(this, x, y, w, h);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                drawpipe.drawRect(this, x, y, w, h);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public void fillRect(int x, int y, int w, int h) {
        try {
            fillpipe.fillRect(this, x, y, w, h);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                fillpipe.fillRect(this, x, y, w, h);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    private void revalidateAll() {
        try {
            surfaceData = surfaceData.getReplacement();
            if (surfaceData == null) {
                surfaceData = NullSurfaceData.theInstance;
            }
            invalidatePipe();
            setDevClip(surfaceData.getBounds());
            if (paintState <= PAINT_ALPHACOLOR) {
                validateColor();
            }
            if (composite instanceof XORComposite) {
                Color c = ((XORComposite) composite).getXorColor();
                setComposite(new XORComposite(c, surfaceData));
            }
            validatePipe();
        } finally {
        }
    }

    public void clearRect(int x, int y, int w, int h) {
        Composite c = composite;
        Paint p = paint;
        setComposite(AlphaComposite.Src);
        setColor(getBackground());
        fillRect(x, y, w, h);
        setPaint(p);
        setComposite(c);
    }

    public void draw(Shape s) {
        try {
            shapepipe.draw(this, s);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                shapepipe.draw(this, s);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public void fill(Shape s) {
        try {
            shapepipe.fill(this, s);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                shapepipe.fill(this, s);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    private static boolean isIntegerTranslation(AffineTransform xform) {
        if (xform.isIdentity()) {
            return true;
        }
        if (xform.getType() == AffineTransform.TYPE_TRANSLATION) {
            double tx = xform.getTranslateX();
            double ty = xform.getTranslateY();
            return (tx == (int) tx && ty == (int) ty);
        }
        return false;
    }

    private static int getTileIndex(int p, int tileGridOffset, int tileSize) {
        p -= tileGridOffset;
        if (p < 0) {
            p += 1 - tileSize;
        }
        return p / tileSize;
    }

    private static Rectangle getImageRegion(RenderedImage img, Region compClip, AffineTransform transform, AffineTransform xform, int padX, int padY) {
        Rectangle imageRect = new Rectangle(img.getMinX(), img.getMinY(), img.getWidth(), img.getHeight());
        Rectangle result = null;
        try {
            double[] p = new double[8];
            p[0] = p[2] = compClip.getLoX();
            p[4] = p[6] = compClip.getHiX();
            p[1] = p[5] = compClip.getLoY();
            p[3] = p[7] = compClip.getHiY();
            transform.inverseTransform(p, 0, p, 0, 4);
            xform.inverseTransform(p, 0, p, 0, 4);
            double x0, x1, y0, y1;
            x0 = x1 = p[0];
            y0 = y1 = p[1];
            for (int i = 2; i < 8; ) {
                double pt = p[i++];
                if (pt < x0) {
                    x0 = pt;
                } else if (pt > x1) {
                    x1 = pt;
                }
                pt = p[i++];
                if (pt < y0) {
                    y0 = pt;
                } else if (pt > y1) {
                    y1 = pt;
                }
            }
            int x = (int) x0 - padX;
            int w = (int) (x1 - x0 + 2 * padX);
            int y = (int) y0 - padY;
            int h = (int) (y1 - y0 + 2 * padY);
            Rectangle clipRect = new Rectangle(x, y, w, h);
            result = clipRect.intersection(imageRect);
        } catch (NoninvertibleTransformException nte) {
            result = imageRect;
        }
        return result;
    }

    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        if (img == null) {
            return;
        }
        if (img instanceof BufferedImage) {
            BufferedImage bufImg = (BufferedImage) img;
            drawImage(bufImg, xform, null);
            return;
        }
        boolean isIntegerTranslate = (transformState <= TRANSFORM_INT_TRANSLATE) && isIntegerTranslation(xform);
        int pad = isIntegerTranslate ? 0 : 3;
        Region clip;
        try {
            clip = getCompClip();
        } catch (InvalidPipeException e) {
            return;
        }
        Rectangle region = getImageRegion(img, clip, transform, xform, pad, pad);
        if (region.width <= 0 || region.height <= 0) {
            return;
        }
        if (isIntegerTranslate) {
            drawTranslatedRenderedImage(img, region, (int) xform.getTranslateX(), (int) xform.getTranslateY());
            return;
        }
        Raster raster = img.getData(region);
        WritableRaster wRaster = Raster.createWritableRaster(raster.getSampleModel(), raster.getDataBuffer(), null);
        int minX = raster.getMinX();
        int minY = raster.getMinY();
        int width = raster.getWidth();
        int height = raster.getHeight();
        int px = minX - raster.getSampleModelTranslateX();
        int py = minY - raster.getSampleModelTranslateY();
        if (px != 0 || py != 0 || width != wRaster.getWidth() || height != wRaster.getHeight()) {
            wRaster = wRaster.createWritableChild(px, py, width, height, 0, 0, null);
        }
        AffineTransform transXform = (AffineTransform) xform.clone();
        transXform.translate(minX, minY);
        ColorModel cm = img.getColorModel();
        BufferedImage bufImg = new BufferedImage(cm, wRaster, cm.isAlphaPremultiplied(), null);
        drawImage(bufImg, transXform, null);
    }

    private boolean clipTo(Rectangle destRect, Rectangle clip) {
        int x1 = Math.max(destRect.x, clip.x);
        int x2 = Math.min(destRect.x + destRect.width, clip.x + clip.width);
        int y1 = Math.max(destRect.y, clip.y);
        int y2 = Math.min(destRect.y + destRect.height, clip.y + clip.height);
        if (((x2 - x1) < 0) || ((y2 - y1) < 0)) {
            destRect.width = -1;
            destRect.height = -1;
            return false;
        } else {
            destRect.x = x1;
            destRect.y = y1;
            destRect.width = x2 - x1;
            destRect.height = y2 - y1;
            return true;
        }
    }

    private void drawTranslatedRenderedImage(RenderedImage img, Rectangle region, int i2uTransX, int i2uTransY) {
        int tileGridXOffset = img.getTileGridXOffset();
        int tileGridYOffset = img.getTileGridYOffset();
        int tileWidth = img.getTileWidth();
        int tileHeight = img.getTileHeight();
        int minTileX = getTileIndex(region.x, tileGridXOffset, tileWidth);
        int minTileY = getTileIndex(region.y, tileGridYOffset, tileHeight);
        int maxTileX = getTileIndex(region.x + region.width - 1, tileGridXOffset, tileWidth);
        int maxTileY = getTileIndex(region.y + region.height - 1, tileGridYOffset, tileHeight);
        ColorModel colorModel = img.getColorModel();
        Rectangle tileRect = new Rectangle();
        for (int ty = minTileY; ty <= maxTileY; ty++) {
            for (int tx = minTileX; tx <= maxTileX; tx++) {
                Raster raster = img.getTile(tx, ty);
                tileRect.x = tx * tileWidth + tileGridXOffset;
                tileRect.y = ty * tileHeight + tileGridYOffset;
                tileRect.width = tileWidth;
                tileRect.height = tileHeight;
                clipTo(tileRect, region);
                WritableRaster wRaster = null;
                if (raster instanceof WritableRaster) {
                    wRaster = (WritableRaster) raster;
                } else {
                    wRaster = Raster.createWritableRaster(raster.getSampleModel(), raster.getDataBuffer(), null);
                }
                wRaster = wRaster.createWritableChild(tileRect.x, tileRect.y, tileRect.width, tileRect.height, 0, 0, null);
                BufferedImage bufImg = new BufferedImage(colorModel, wRaster, colorModel.isAlphaPremultiplied(), null);
                copyImage(bufImg, tileRect.x + i2uTransX, tileRect.y + i2uTransY, 0, 0, tileRect.width, tileRect.height, null, null);
            }
        }
    }

    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        if (img == null) {
            return;
        }
        AffineTransform pipeTransform = transform;
        AffineTransform concatTransform = new AffineTransform(xform);
        concatTransform.concatenate(pipeTransform);
        AffineTransform reverseTransform;
        RenderContext rc = new RenderContext(concatTransform);
        try {
            reverseTransform = pipeTransform.createInverse();
        } catch (NoninvertibleTransformException nte) {
            rc = new RenderContext(pipeTransform);
            reverseTransform = new AffineTransform();
        }
        RenderedImage rendering = img.createRendering(rc);
        drawRenderedImage(rendering, reverseTransform);
    }

    protected Rectangle transformBounds(Rectangle rect, AffineTransform tx) {
        if (tx.isIdentity()) {
            return rect;
        }
        Shape s = transformShape(tx, rect);
        return s.getBounds();
    }

    public void drawString(String str, int x, int y) {
        if (str == null) {
            throw new NullPointerException("String is null");
        }
        if (font.hasLayoutAttributes()) {
            if (str.length() == 0) {
                return;
            }
            new TextLayout(str, font, getFontRenderContext()).draw(this, x, y);
            return;
        }
        try {
            textpipe.drawString(this, str, x, y);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                textpipe.drawString(this, str, x, y);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public void drawString(String str, float x, float y) {
        if (str == null) {
            throw new NullPointerException("String is null");
        }
        if (font.hasLayoutAttributes()) {
            if (str.length() == 0) {
                return;
            }
            new TextLayout(str, font, getFontRenderContext()).draw(this, x, y);
            return;
        }
        try {
            textpipe.drawString(this, str, x, y);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                textpipe.drawString(this, str, x, y);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        if (iterator == null) {
            throw new NullPointerException("AttributedCharacterIterator is null");
        }
        if (iterator.getBeginIndex() == iterator.getEndIndex()) {
            return;
        }
        TextLayout tl = new TextLayout(iterator, getFontRenderContext());
        tl.draw(this, (float) x, (float) y);
    }

    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        if (iterator == null) {
            throw new NullPointerException("AttributedCharacterIterator is null");
        }
        if (iterator.getBeginIndex() == iterator.getEndIndex()) {
            return;
        }
        TextLayout tl = new TextLayout(iterator, getFontRenderContext());
        tl.draw(this, x, y);
    }

    public void drawGlyphVector(GlyphVector gv, float x, float y) {
        if (gv == null) {
            throw new NullPointerException("GlyphVector is null");
        }
        try {
            textpipe.drawGlyphVector(this, gv, x, y);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                textpipe.drawGlyphVector(this, gv, x, y);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public void drawChars(char[] data, int offset, int length, int x, int y) {
        if (data == null) {
            throw new NullPointerException("char data is null");
        }
        if (offset < 0 || length < 0 || offset + length > data.length) {
            throw new ArrayIndexOutOfBoundsException("bad offset/length");
        }
        if (font.hasLayoutAttributes()) {
            if (data.length == 0) {
                return;
            }
            new TextLayout(new String(data, offset, length), font, getFontRenderContext()).draw(this, x, y);
            return;
        }
        try {
            textpipe.drawChars(this, data, offset, length, x, y);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                textpipe.drawChars(this, data, offset, length, x, y);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public void drawBytes(byte[] data, int offset, int length, int x, int y) {
        if (data == null) {
            throw new NullPointerException("byte data is null");
        }
        if (offset < 0 || length < 0 || offset + length > data.length) {
            throw new ArrayIndexOutOfBoundsException("bad offset/length");
        }
        char[] chData = new char[length];
        for (int i = length; i-- > 0; ) {
            chData[i] = (char) (data[i + offset] & 0xff);
        }
        if (font.hasLayoutAttributes()) {
            if (data.length == 0) {
                return;
            }
            new TextLayout(new String(chData), font, getFontRenderContext()).draw(this, x, y);
            return;
        }
        try {
            textpipe.drawChars(this, chData, 0, length, x, y);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                textpipe.drawChars(this, chData, 0, length, x, y);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    private Boolean drawHiDPIImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer, AffineTransform xform) {
        if (img instanceof VolatileImage) {
            final SurfaceData sd = SurfaceManager.getManager(img).getPrimarySurfaceData();
            final double scaleX = sd.getDefaultScaleX();
            final double scaleY = sd.getDefaultScaleY();
            if (scaleX == 1 && scaleY == 1) {
                return null;
            }
            sx1 = Region.clipRound(sx1 * scaleX);
            sx2 = Region.clipRound(sx2 * scaleX);
            sy1 = Region.clipRound(sy1 * scaleY);
            sy2 = Region.clipRound(sy2 * scaleY);
            AffineTransform tx = null;
            if (xform != null) {
                tx = new AffineTransform(transform);
                transform(xform);
            }
            boolean result = scaleImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
            if (tx != null) {
                transform.setTransform(tx);
                invalidateTransform();
            }
            return result;
        } else if (img instanceof MultiResolutionImage) {
            int width = img.getWidth(observer);
            int height = img.getHeight(observer);
            MultiResolutionImage mrImage = (MultiResolutionImage) img;
            Image resolutionVariant = getResolutionVariant(mrImage, width, height, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, xform);
            if (resolutionVariant != img && resolutionVariant != null) {
                ImageObserver rvObserver = MultiResolutionToolkitImage.getResolutionVariantObserver(img, observer, width, height, -1, -1);
                int rvWidth = resolutionVariant.getWidth(rvObserver);
                int rvHeight = resolutionVariant.getHeight(rvObserver);
                if (0 < width && 0 < height && 0 < rvWidth && 0 < rvHeight) {
                    double widthScale = ((double) rvWidth) / width;
                    double heightScale = ((double) rvHeight) / height;
                    sx1 = Region.clipScale(sx1, widthScale);
                    sy1 = Region.clipScale(sy1, heightScale);
                    sx2 = Region.clipScale(sx2, widthScale);
                    sy2 = Region.clipScale(sy2, heightScale);
                    observer = rvObserver;
                    img = resolutionVariant;
                    if (xform != null) {
                        assert dx1 == 0 && dy1 == 0;
                        assert dx2 == img.getWidth(observer);
                        assert dy2 == img.getHeight(observer);
                        AffineTransform renderTX = new AffineTransform(xform);
                        renderTX.scale(1 / widthScale, 1 / heightScale);
                        return transformImage(img, renderTX, observer);
                    }
                    return scaleImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
                }
            }
        }
        return null;
    }

    private boolean scaleImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        try {
            return imagepipe.scaleImage(this, img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                return imagepipe.scaleImage(this, img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
            } catch (InvalidPipeException e2) {
                return false;
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    private boolean transformImage(Image img, AffineTransform xform, ImageObserver observer) {
        try {
            return imagepipe.transformImage(this, img, xform, observer);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                return imagepipe.transformImage(this, img, xform, observer);
            } catch (InvalidPipeException e2) {
                return false;
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    private Image getResolutionVariant(MultiResolutionImage img, int srcWidth, int srcHeight, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, AffineTransform xform) {
        if (srcWidth <= 0 || srcHeight <= 0) {
            return null;
        }
        int sw = sx2 - sx1;
        int sh = sy2 - sy1;
        if (sw == 0 || sh == 0) {
            return null;
        }
        AffineTransform tx;
        if (xform == null) {
            tx = transform;
        } else {
            tx = new AffineTransform(transform);
            tx.concatenate(xform);
        }
        int type = tx.getType();
        int dw = dx2 - dx1;
        int dh = dy2 - dy1;
        double destImageWidth;
        double destImageHeight;
        if (resolutionVariantHint == SunHints.INTVAL_RESOLUTION_VARIANT_BASE) {
            destImageWidth = srcWidth;
            destImageHeight = srcHeight;
        } else if (resolutionVariantHint == SunHints.INTVAL_RESOLUTION_VARIANT_DPI_FIT) {
            AffineTransform configTransform = getDefaultTransform();
            if (configTransform.isIdentity()) {
                destImageWidth = srcWidth;
                destImageHeight = srcHeight;
            } else {
                destImageWidth = srcWidth * configTransform.getScaleX();
                destImageHeight = srcHeight * configTransform.getScaleY();
            }
        } else {
            double destRegionWidth;
            double destRegionHeight;
            if ((type & ~(TYPE_TRANSLATION | TYPE_FLIP)) == 0) {
                destRegionWidth = dw;
                destRegionHeight = dh;
            } else if ((type & ~(TYPE_TRANSLATION | TYPE_FLIP | TYPE_MASK_SCALE)) == 0) {
                destRegionWidth = dw * tx.getScaleX();
                destRegionHeight = dh * tx.getScaleY();
            } else {
                destRegionWidth = dw * Math.hypot(tx.getScaleX(), tx.getShearY());
                destRegionHeight = dh * Math.hypot(tx.getShearX(), tx.getScaleY());
            }
            destImageWidth = Math.abs(srcWidth * destRegionWidth / sw);
            destImageHeight = Math.abs(srcHeight * destRegionHeight / sh);
        }
        Image resolutionVariant = img.getResolutionVariant(destImageWidth, destImageHeight);
        if (resolutionVariant instanceof ToolkitImage && ((ToolkitImage) resolutionVariant).hasError()) {
            return null;
        }
        return resolutionVariant;
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        return drawImage(img, x, y, width, height, null, observer);
    }

    public boolean copyImage(Image img, int dx, int dy, int sx, int sy, int width, int height, Color bgcolor, ImageObserver observer) {
        try {
            return imagepipe.copyImage(this, img, dx, dy, sx, sy, width, height, bgcolor, observer);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                return imagepipe.copyImage(this, img, dx, dy, sx, sy, width, height, bgcolor, observer);
            } catch (InvalidPipeException e2) {
                return false;
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, Color bg, ImageObserver observer) {
        if (img == null) {
            return true;
        }
        if ((width == 0) || (height == 0)) {
            return true;
        }
        final int imgW = img.getWidth(null);
        final int imgH = img.getHeight(null);
        Boolean hidpiImageDrawn = drawHiDPIImage(img, x, y, x + width, y + height, 0, 0, imgW, imgH, bg, observer, null);
        if (hidpiImageDrawn != null) {
            return hidpiImageDrawn;
        }
        if (width == imgW && height == imgH) {
            return copyImage(img, x, y, 0, 0, width, height, bg, observer);
        }
        try {
            return imagepipe.scaleImage(this, img, x, y, width, height, bg, observer);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                return imagepipe.scaleImage(this, img, x, y, width, height, bg, observer);
            } catch (InvalidPipeException e2) {
                return false;
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return drawImage(img, x, y, null, observer);
    }

    public boolean drawImage(Image img, int x, int y, Color bg, ImageObserver observer) {
        if (img == null) {
            return true;
        }
        final int imgW = img.getWidth(null);
        final int imgH = img.getHeight(null);
        Boolean hidpiImageDrawn = drawHiDPIImage(img, x, y, x + imgW, y + imgH, 0, 0, imgW, imgH, bg, observer, null);
        if (hidpiImageDrawn != null) {
            return hidpiImageDrawn;
        }
        try {
            return imagepipe.copyImage(this, img, x, y, bg, observer);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                return imagepipe.copyImage(this, img, x, y, bg, observer);
            } catch (InvalidPipeException e2) {
                return false;
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        return drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null, observer);
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        if (img == null) {
            return true;
        }
        if (dx1 == dx2 || dy1 == dy2 || sx1 == sx2 || sy1 == sy2) {
            return true;
        }
        Boolean hidpiImageDrawn = drawHiDPIImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer, null);
        if (hidpiImageDrawn != null) {
            return hidpiImageDrawn;
        }
        if (((sx2 - sx1) == (dx2 - dx1)) && ((sy2 - sy1) == (dy2 - dy1))) {
            int srcX, srcY, dstX, dstY, width, height;
            if (sx2 > sx1) {
                width = sx2 - sx1;
                srcX = sx1;
                dstX = dx1;
            } else {
                width = sx1 - sx2;
                srcX = sx2;
                dstX = dx2;
            }
            if (sy2 > sy1) {
                height = sy2 - sy1;
                srcY = sy1;
                dstY = dy1;
            } else {
                height = sy1 - sy2;
                srcY = sy2;
                dstY = dy2;
            }
            return copyImage(img, dstX, dstY, srcX, srcY, width, height, bgcolor, observer);
        }
        try {
            return imagepipe.scaleImage(this, img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                return imagepipe.scaleImage(this, img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
            } catch (InvalidPipeException e2) {
                return false;
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public boolean drawImage(Image img, AffineTransform xform, ImageObserver observer) {
        if (img == null) {
            return true;
        }
        if (xform == null || xform.isIdentity()) {
            return drawImage(img, 0, 0, null, observer);
        }
        final int w = img.getWidth(null);
        final int h = img.getHeight(null);
        Boolean hidpiImageDrawn = drawHiDPIImage(img, 0, 0, w, h, 0, 0, w, h, null, observer, xform);
        if (hidpiImageDrawn != null) {
            return hidpiImageDrawn;
        }
        return transformImage(img, xform, observer);
    }

    public void drawImage(BufferedImage bImg, BufferedImageOp op, int x, int y) {
        if (bImg == null) {
            return;
        }
        try {
            imagepipe.transformImage(this, bImg, op, x, y);
        } catch (InvalidPipeException e) {
            try {
                revalidateAll();
                imagepipe.transformImage(this, bImg, op, x, y);
            } catch (InvalidPipeException e2) {
            }
        } finally {
            surfaceData.markDirty();
        }
    }

    public FontRenderContext getFontRenderContext() {
        if (cachedFRC == null) {
            int aahint = textAntialiasHint;
            if (aahint == SunHints.INTVAL_TEXT_ANTIALIAS_DEFAULT && antialiasHint == SunHints.INTVAL_ANTIALIAS_ON) {
                aahint = SunHints.INTVAL_TEXT_ANTIALIAS_ON;
            }
            AffineTransform tx = null;
            if (transformState >= TRANSFORM_TRANSLATESCALE) {
                if (transform.getTranslateX() == 0 && transform.getTranslateY() == 0) {
                    tx = transform;
                } else {
                    tx = new AffineTransform(transform.getScaleX(), transform.getShearY(), transform.getShearX(), transform.getScaleY(), 0, 0);
                }
            }
            cachedFRC = new FontRenderContext(tx, SunHints.Value.get(SunHints.INTKEY_TEXT_ANTIALIASING, aahint), SunHints.Value.get(SunHints.INTKEY_FRACTIONALMETRICS, fractionalMetricsHint));
        }
        return cachedFRC;
    }

    private FontRenderContext cachedFRC;

    public void dispose() {
        surfaceData = NullSurfaceData.theInstance;
        invalidatePipe();
    }

    @SuppressWarnings("deprecation")
    public void finalize() {
    }

    public Object getDestination() {
        return surfaceData.getDestination();
    }

    @Override
    public Surface getDestSurface() {
        return surfaceData;
    }
}