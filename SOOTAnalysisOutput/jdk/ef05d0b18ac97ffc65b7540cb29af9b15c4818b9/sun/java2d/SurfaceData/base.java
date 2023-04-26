package sun.java2d;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import sun.java2d.loops.RenderCache;
import sun.java2d.loops.RenderLoops;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.MaskFill;
import sun.java2d.loops.DrawLine;
import sun.java2d.loops.FillRect;
import sun.java2d.loops.DrawRect;
import sun.java2d.loops.DrawPolygons;
import sun.java2d.loops.DrawPath;
import sun.java2d.loops.FillPath;
import sun.java2d.loops.FillSpans;
import sun.java2d.loops.FillParallelogram;
import sun.java2d.loops.DrawParallelogram;
import sun.java2d.loops.FontInfo;
import sun.java2d.loops.DrawGlyphList;
import sun.java2d.loops.DrawGlyphListAA;
import sun.java2d.loops.DrawGlyphListLCD;
import sun.java2d.pipe.LoopPipe;
import sun.java2d.pipe.ShapeDrawPipe;
import sun.java2d.pipe.ParallelogramPipe;
import sun.java2d.pipe.CompositePipe;
import sun.java2d.pipe.GeneralCompositePipe;
import sun.java2d.pipe.SpanClipRenderer;
import sun.java2d.pipe.SpanShapeRenderer;
import sun.java2d.pipe.AAShapePipe;
import sun.java2d.pipe.AlphaPaintPipe;
import sun.java2d.pipe.AlphaColorPipe;
import sun.java2d.pipe.PixelToShapeConverter;
import sun.java2d.pipe.PixelToParallelogramConverter;
import sun.java2d.pipe.TextPipe;
import sun.java2d.pipe.TextRenderer;
import sun.java2d.pipe.AATextRenderer;
import sun.java2d.pipe.LCDTextRenderer;
import sun.java2d.pipe.SolidTextRenderer;
import sun.java2d.pipe.OutlineTextRenderer;
import sun.java2d.pipe.DrawImagePipe;
import sun.java2d.pipe.DrawImage;
import sun.awt.SunHints;
import sun.awt.image.SurfaceManager;
import sun.java2d.pipe.LoopBasedPipe;

public abstract class SurfaceData implements Transparency, DisposerTarget, StateTrackable, Surface {

    private long pData;

    private boolean valid;

    private boolean surfaceLost;

    private SurfaceType surfaceType;

    private ColorModel colorModel;

    private Object disposerReferent = new Object();

    private static native void initIDs();

    private Object blitProxyKey;

    private StateTrackableDelegate stateDelegate;

    static {
        initIDs();
    }

    protected SurfaceData(SurfaceType surfaceType, ColorModel cm) {
        this(State.STABLE, surfaceType, cm);
    }

    protected SurfaceData(State state, SurfaceType surfaceType, ColorModel cm) {
        this(StateTrackableDelegate.createInstance(state), surfaceType, cm);
    }

    protected SurfaceData(StateTrackableDelegate trackable, SurfaceType surfaceType, ColorModel cm) {
        this.stateDelegate = trackable;
        this.colorModel = cm;
        this.surfaceType = surfaceType;
        valid = true;
    }

    protected SurfaceData(State state) {
        this.stateDelegate = StateTrackableDelegate.createInstance(state);
        valid = true;
    }

    protected void setBlitProxyKey(Object key) {
        if (SurfaceDataProxy.isCachingAllowed()) {
            this.blitProxyKey = key;
        }
    }

    public SurfaceData getSourceSurfaceData(Image img, int txtype, CompositeType comp, Color bgColor) {
        SurfaceManager srcMgr = SurfaceManager.getManager(img);
        SurfaceData srcData = srcMgr.getPrimarySurfaceData();
        if (img.getAccelerationPriority() > 0.0f && blitProxyKey != null) {
            SurfaceDataProxy sdp = (SurfaceDataProxy) srcMgr.getCacheData(blitProxyKey);
            if (sdp == null || !sdp.isValid()) {
                if (srcData.getState() == State.UNTRACKABLE) {
                    sdp = SurfaceDataProxy.UNCACHED;
                } else {
                    sdp = makeProxyFor(srcData);
                }
                srcMgr.setCacheData(blitProxyKey, sdp);
            }
            srcData = sdp.replaceData(srcData, txtype, comp, bgColor);
        }
        return srcData;
    }

    public SurfaceDataProxy makeProxyFor(SurfaceData srcData) {
        return SurfaceDataProxy.UNCACHED;
    }

    public static SurfaceData getPrimarySurfaceData(Image img) {
        SurfaceManager sMgr = SurfaceManager.getManager(img);
        return sMgr.getPrimarySurfaceData();
    }

    public static SurfaceData restoreContents(Image img) {
        SurfaceManager sMgr = SurfaceManager.getManager(img);
        return sMgr.restoreContents();
    }

    public State getState() {
        return stateDelegate.getState();
    }

    public StateTracker getStateTracker() {
        return stateDelegate.getStateTracker();
    }

    public final void markDirty() {
        stateDelegate.markDirty();
    }

    public void setSurfaceLost(boolean lost) {
        surfaceLost = lost;
        stateDelegate.markDirty();
    }

    public boolean isSurfaceLost() {
        return surfaceLost;
    }

    public final boolean isValid() {
        return valid;
    }

    public Object getDisposerReferent() {
        return disposerReferent;
    }

    public long getNativeOps() {
        return pData;
    }

    public void invalidate() {
        valid = false;
        stateDelegate.markDirty();
    }

    public abstract SurfaceData getReplacement();

    protected static final LoopPipe colorPrimitives;

    public static final TextPipe outlineTextRenderer;

    public static final TextPipe solidTextRenderer;

    public static final TextPipe aaTextRenderer;

    public static final TextPipe lcdTextRenderer;

    protected static final CompositePipe colorPipe;

    protected static final PixelToShapeConverter colorViaShape;

    protected static final PixelToParallelogramConverter colorViaPgram;

    protected static final TextPipe colorText;

    protected static final CompositePipe clipColorPipe;

    protected static final TextPipe clipColorText;

    protected static final AAShapePipe AAColorShape;

    protected static final PixelToShapeConverter AAColorViaShape;

    protected static final AAShapePipe AAClipColorShape;

    protected static final PixelToShapeConverter AAClipColorViaShape;

    protected static final CompositePipe paintPipe;

    protected static final SpanShapeRenderer paintShape;

    protected static final PixelToShapeConverter paintViaShape;

    protected static final TextPipe paintText;

    protected static final CompositePipe clipPaintPipe;

    protected static final TextPipe clipPaintText;

    protected static final AAShapePipe AAPaintShape;

    protected static final PixelToShapeConverter AAPaintViaShape;

    protected static final AAShapePipe AAClipPaintShape;

    protected static final PixelToShapeConverter AAClipPaintViaShape;

    protected static final CompositePipe compPipe;

    protected static final SpanShapeRenderer compShape;

    protected static final PixelToShapeConverter compViaShape;

    protected static final TextPipe compText;

    protected static final CompositePipe clipCompPipe;

    protected static final TextPipe clipCompText;

    protected static final AAShapePipe AACompShape;

    protected static final PixelToShapeConverter AACompViaShape;

    protected static final AAShapePipe AAClipCompShape;

    protected static final PixelToShapeConverter AAClipCompViaShape;

    protected static final DrawImagePipe imagepipe;

    static class PixelToShapeLoopConverter extends PixelToShapeConverter implements LoopBasedPipe {

        public PixelToShapeLoopConverter(ShapeDrawPipe pipe) {
            super(pipe);
        }
    }

    static class PixelToPgramLoopConverter extends PixelToParallelogramConverter implements LoopBasedPipe {

        public PixelToPgramLoopConverter(ShapeDrawPipe shapepipe, ParallelogramPipe pgrampipe, double minPenSize, double normPosition, boolean adjustfill) {
            super(shapepipe, pgrampipe, minPenSize, normPosition, adjustfill);
        }
    }

    static {
        colorPrimitives = new LoopPipe();
        outlineTextRenderer = new OutlineTextRenderer();
        solidTextRenderer = new SolidTextRenderer();
        aaTextRenderer = new AATextRenderer();
        lcdTextRenderer = new LCDTextRenderer();
        colorPipe = new AlphaColorPipe();
        colorViaShape = new PixelToShapeLoopConverter(colorPrimitives);
        colorViaPgram = new PixelToPgramLoopConverter(colorPrimitives, colorPrimitives, 1.0, 0.25, true);
        colorText = new TextRenderer(colorPipe);
        clipColorPipe = new SpanClipRenderer(colorPipe);
        clipColorText = new TextRenderer(clipColorPipe);
        AAColorShape = new AAShapePipe(colorPipe);
        AAColorViaShape = new PixelToShapeConverter(AAColorShape);
        AAClipColorShape = new AAShapePipe(clipColorPipe);
        AAClipColorViaShape = new PixelToShapeConverter(AAClipColorShape);
        paintPipe = new AlphaPaintPipe();
        paintShape = new SpanShapeRenderer.Composite(paintPipe);
        paintViaShape = new PixelToShapeConverter(paintShape);
        paintText = new TextRenderer(paintPipe);
        clipPaintPipe = new SpanClipRenderer(paintPipe);
        clipPaintText = new TextRenderer(clipPaintPipe);
        AAPaintShape = new AAShapePipe(paintPipe);
        AAPaintViaShape = new PixelToShapeConverter(AAPaintShape);
        AAClipPaintShape = new AAShapePipe(clipPaintPipe);
        AAClipPaintViaShape = new PixelToShapeConverter(AAClipPaintShape);
        compPipe = new GeneralCompositePipe();
        compShape = new SpanShapeRenderer.Composite(compPipe);
        compViaShape = new PixelToShapeConverter(compShape);
        compText = new TextRenderer(compPipe);
        clipCompPipe = new SpanClipRenderer(compPipe);
        clipCompText = new TextRenderer(clipCompPipe);
        AACompShape = new AAShapePipe(compPipe);
        AACompViaShape = new PixelToShapeConverter(AACompShape);
        AAClipCompShape = new AAShapePipe(clipCompPipe);
        AAClipCompViaShape = new PixelToShapeConverter(AAClipCompShape);
        imagepipe = new DrawImage();
    }

    static final int LOOP_UNKNOWN = 0;

    static final int LOOP_FOUND = 1;

    static final int LOOP_NOTFOUND = 2;

    int haveLCDLoop;

    int havePgramXORLoop;

    int havePgramSolidLoop;

    public boolean canRenderLCDText(SunGraphics2D sg2d) {
        if (sg2d.compositeState <= SunGraphics2D.COMP_ISCOPY && sg2d.paintState <= SunGraphics2D.PAINT_ALPHACOLOR && sg2d.clipState <= SunGraphics2D.CLIP_RECTANGULAR && sg2d.surfaceData.getTransparency() == Transparency.OPAQUE) {
            if (haveLCDLoop == LOOP_UNKNOWN) {
                DrawGlyphListLCD loop = DrawGlyphListLCD.locate(SurfaceType.AnyColor, CompositeType.SrcNoEa, getSurfaceType());
                haveLCDLoop = (loop != null) ? LOOP_FOUND : LOOP_NOTFOUND;
            }
            return haveLCDLoop == LOOP_FOUND;
        }
        return false;
    }

    public boolean canRenderParallelograms(SunGraphics2D sg2d) {
        if (sg2d.paintState <= sg2d.PAINT_ALPHACOLOR) {
            if (sg2d.compositeState == sg2d.COMP_XOR) {
                if (havePgramXORLoop == LOOP_UNKNOWN) {
                    FillParallelogram loop = FillParallelogram.locate(SurfaceType.AnyColor, CompositeType.Xor, getSurfaceType());
                    havePgramXORLoop = (loop != null) ? LOOP_FOUND : LOOP_NOTFOUND;
                }
                return havePgramXORLoop == LOOP_FOUND;
            } else if (sg2d.compositeState <= sg2d.COMP_ISCOPY && sg2d.antialiasHint != SunHints.INTVAL_ANTIALIAS_ON && sg2d.clipState != sg2d.CLIP_SHAPE) {
                if (havePgramSolidLoop == LOOP_UNKNOWN) {
                    FillParallelogram loop = FillParallelogram.locate(SurfaceType.AnyColor, CompositeType.SrcNoEa, getSurfaceType());
                    havePgramSolidLoop = (loop != null) ? LOOP_FOUND : LOOP_NOTFOUND;
                }
                return havePgramSolidLoop == LOOP_FOUND;
            }
        }
        return false;
    }

    public void validatePipe(SunGraphics2D sg2d) {
        sg2d.imagepipe = imagepipe;
        if (sg2d.compositeState == sg2d.COMP_XOR) {
            if (sg2d.paintState > sg2d.PAINT_ALPHACOLOR) {
                sg2d.drawpipe = paintViaShape;
                sg2d.fillpipe = paintViaShape;
                sg2d.shapepipe = paintShape;
                sg2d.textpipe = outlineTextRenderer;
            } else {
                PixelToShapeConverter converter;
                if (canRenderParallelograms(sg2d)) {
                    converter = colorViaPgram;
                    sg2d.shapepipe = colorViaPgram;
                } else {
                    converter = colorViaShape;
                    sg2d.shapepipe = colorPrimitives;
                }
                if (sg2d.clipState == sg2d.CLIP_SHAPE) {
                    sg2d.drawpipe = converter;
                    sg2d.fillpipe = converter;
                    sg2d.textpipe = outlineTextRenderer;
                } else {
                    if (sg2d.transformState >= sg2d.TRANSFORM_TRANSLATESCALE) {
                        sg2d.drawpipe = converter;
                        sg2d.fillpipe = converter;
                    } else {
                        if (sg2d.strokeState != sg2d.STROKE_THIN) {
                            sg2d.drawpipe = converter;
                        } else {
                            sg2d.drawpipe = colorPrimitives;
                        }
                        sg2d.fillpipe = colorPrimitives;
                    }
                    sg2d.textpipe = solidTextRenderer;
                }
            }
        } else if (sg2d.compositeState == sg2d.COMP_CUSTOM) {
            if (sg2d.antialiasHint == SunHints.INTVAL_ANTIALIAS_ON) {
                if (sg2d.clipState == sg2d.CLIP_SHAPE) {
                    sg2d.drawpipe = AAClipCompViaShape;
                    sg2d.fillpipe = AAClipCompViaShape;
                    sg2d.shapepipe = AAClipCompShape;
                    sg2d.textpipe = clipCompText;
                } else {
                    sg2d.drawpipe = AACompViaShape;
                    sg2d.fillpipe = AACompViaShape;
                    sg2d.shapepipe = AACompShape;
                    sg2d.textpipe = compText;
                }
            } else {
                sg2d.drawpipe = compViaShape;
                sg2d.fillpipe = compViaShape;
                sg2d.shapepipe = compShape;
                if (sg2d.clipState == sg2d.CLIP_SHAPE) {
                    sg2d.textpipe = clipCompText;
                } else {
                    sg2d.textpipe = compText;
                }
            }
        } else if (sg2d.antialiasHint == SunHints.INTVAL_ANTIALIAS_ON) {
            sg2d.alphafill = getMaskFill(sg2d);
            if (sg2d.alphafill != null) {
                if (sg2d.clipState == sg2d.CLIP_SHAPE) {
                    sg2d.drawpipe = AAClipColorViaShape;
                    sg2d.fillpipe = AAClipColorViaShape;
                    sg2d.shapepipe = AAClipColorShape;
                    sg2d.textpipe = clipColorText;
                } else {
                    sg2d.drawpipe = AAColorViaShape;
                    sg2d.fillpipe = AAColorViaShape;
                    sg2d.shapepipe = AAColorShape;
                    if (sg2d.paintState > sg2d.PAINT_OPAQUECOLOR || sg2d.compositeState > sg2d.COMP_ISCOPY) {
                        sg2d.textpipe = colorText;
                    } else {
                        sg2d.textpipe = getTextPipe(sg2d, true);
                    }
                }
            } else {
                if (sg2d.clipState == sg2d.CLIP_SHAPE) {
                    sg2d.drawpipe = AAClipPaintViaShape;
                    sg2d.fillpipe = AAClipPaintViaShape;
                    sg2d.shapepipe = AAClipPaintShape;
                    sg2d.textpipe = clipPaintText;
                } else {
                    sg2d.drawpipe = AAPaintViaShape;
                    sg2d.fillpipe = AAPaintViaShape;
                    sg2d.shapepipe = AAPaintShape;
                    sg2d.textpipe = paintText;
                }
            }
        } else if (sg2d.paintState > sg2d.PAINT_ALPHACOLOR || sg2d.compositeState > sg2d.COMP_ISCOPY || sg2d.clipState == sg2d.CLIP_SHAPE) {
            sg2d.drawpipe = paintViaShape;
            sg2d.fillpipe = paintViaShape;
            sg2d.shapepipe = paintShape;
            sg2d.alphafill = getMaskFill(sg2d);
            if (sg2d.alphafill != null) {
                if (sg2d.clipState == sg2d.CLIP_SHAPE) {
                    sg2d.textpipe = clipColorText;
                } else {
                    sg2d.textpipe = colorText;
                }
            } else {
                if (sg2d.clipState == sg2d.CLIP_SHAPE) {
                    sg2d.textpipe = clipPaintText;
                } else {
                    sg2d.textpipe = paintText;
                }
            }
        } else {
            PixelToShapeConverter converter;
            if (canRenderParallelograms(sg2d)) {
                converter = colorViaPgram;
                sg2d.shapepipe = colorViaPgram;
            } else {
                converter = colorViaShape;
                sg2d.shapepipe = colorPrimitives;
            }
            if (sg2d.transformState >= sg2d.TRANSFORM_TRANSLATESCALE) {
                sg2d.drawpipe = converter;
                sg2d.fillpipe = converter;
            } else {
                if (sg2d.strokeState != sg2d.STROKE_THIN) {
                    sg2d.drawpipe = converter;
                } else {
                    sg2d.drawpipe = colorPrimitives;
                }
                sg2d.fillpipe = colorPrimitives;
            }
            sg2d.textpipe = getTextPipe(sg2d, false);
        }
        if (sg2d.textpipe instanceof LoopBasedPipe || sg2d.shapepipe instanceof LoopBasedPipe || sg2d.fillpipe instanceof LoopBasedPipe || sg2d.drawpipe instanceof LoopBasedPipe || sg2d.imagepipe instanceof LoopBasedPipe) {
            sg2d.loops = getRenderLoops(sg2d);
        }
    }

    private TextPipe getTextPipe(SunGraphics2D sg2d, boolean aaHintIsOn) {
        switch(sg2d.textAntialiasHint) {
            case SunHints.INTVAL_TEXT_ANTIALIAS_DEFAULT:
                if (aaHintIsOn) {
                    return aaTextRenderer;
                } else {
                    return solidTextRenderer;
                }
            case SunHints.INTVAL_TEXT_ANTIALIAS_OFF:
                return solidTextRenderer;
            case SunHints.INTVAL_TEXT_ANTIALIAS_ON:
                return aaTextRenderer;
            default:
                switch(sg2d.getFontInfo().aaHint) {
                    case SunHints.INTVAL_TEXT_ANTIALIAS_LCD_HRGB:
                    case SunHints.INTVAL_TEXT_ANTIALIAS_LCD_VRGB:
                        return lcdTextRenderer;
                    case SunHints.INTVAL_TEXT_ANTIALIAS_ON:
                        return aaTextRenderer;
                    case SunHints.INTVAL_TEXT_ANTIALIAS_OFF:
                        return solidTextRenderer;
                    default:
                        if (aaHintIsOn) {
                            return aaTextRenderer;
                        } else {
                            return solidTextRenderer;
                        }
                }
        }
    }

    private static SurfaceType getPaintSurfaceType(SunGraphics2D sg2d) {
        switch(sg2d.paintState) {
            case SunGraphics2D.PAINT_OPAQUECOLOR:
                return SurfaceType.OpaqueColor;
            case SunGraphics2D.PAINT_ALPHACOLOR:
                return SurfaceType.AnyColor;
            case SunGraphics2D.PAINT_GRADIENT:
                if (sg2d.paint.getTransparency() == OPAQUE) {
                    return SurfaceType.OpaqueGradientPaint;
                } else {
                    return SurfaceType.GradientPaint;
                }
            case SunGraphics2D.PAINT_LIN_GRADIENT:
                if (sg2d.paint.getTransparency() == OPAQUE) {
                    return SurfaceType.OpaqueLinearGradientPaint;
                } else {
                    return SurfaceType.LinearGradientPaint;
                }
            case SunGraphics2D.PAINT_RAD_GRADIENT:
                if (sg2d.paint.getTransparency() == OPAQUE) {
                    return SurfaceType.OpaqueRadialGradientPaint;
                } else {
                    return SurfaceType.RadialGradientPaint;
                }
            case SunGraphics2D.PAINT_TEXTURE:
                if (sg2d.paint.getTransparency() == OPAQUE) {
                    return SurfaceType.OpaqueTexturePaint;
                } else {
                    return SurfaceType.TexturePaint;
                }
            default:
            case SunGraphics2D.PAINT_CUSTOM:
                return SurfaceType.AnyPaint;
        }
    }

    protected MaskFill getMaskFill(SunGraphics2D sg2d) {
        return MaskFill.getFromCache(getPaintSurfaceType(sg2d), sg2d.imageComp, getSurfaceType());
    }

    private static RenderCache loopcache = new RenderCache(30);

    public RenderLoops getRenderLoops(SunGraphics2D sg2d) {
        SurfaceType src = getPaintSurfaceType(sg2d);
        CompositeType comp = (sg2d.compositeState == sg2d.COMP_ISCOPY ? CompositeType.SrcNoEa : sg2d.imageComp);
        SurfaceType dst = sg2d.getSurfaceData().getSurfaceType();
        Object o = loopcache.get(src, comp, dst);
        if (o != null) {
            return (RenderLoops) o;
        }
        RenderLoops loops = makeRenderLoops(src, comp, dst);
        loopcache.put(src, comp, dst, loops);
        return loops;
    }

    public static RenderLoops makeRenderLoops(SurfaceType src, CompositeType comp, SurfaceType dst) {
        RenderLoops loops = new RenderLoops();
        loops.drawLineLoop = DrawLine.locate(src, comp, dst);
        loops.fillRectLoop = FillRect.locate(src, comp, dst);
        loops.drawRectLoop = DrawRect.locate(src, comp, dst);
        loops.drawPolygonsLoop = DrawPolygons.locate(src, comp, dst);
        loops.drawPathLoop = DrawPath.locate(src, comp, dst);
        loops.fillPathLoop = FillPath.locate(src, comp, dst);
        loops.fillSpansLoop = FillSpans.locate(src, comp, dst);
        loops.fillParallelogramLoop = FillParallelogram.locate(src, comp, dst);
        loops.drawParallelogramLoop = DrawParallelogram.locate(src, comp, dst);
        loops.drawGlyphListLoop = DrawGlyphList.locate(src, comp, dst);
        loops.drawGlyphListAALoop = DrawGlyphListAA.locate(src, comp, dst);
        loops.drawGlyphListLCDLoop = DrawGlyphListLCD.locate(src, comp, dst);
        return loops;
    }

    public abstract GraphicsConfiguration getDeviceConfiguration();

    public final SurfaceType getSurfaceType() {
        return surfaceType;
    }

    public final ColorModel getColorModel() {
        return colorModel;
    }

    public int getTransparency() {
        return getColorModel().getTransparency();
    }

    public abstract Raster getRaster(int x, int y, int w, int h);

    public boolean useTightBBoxes() {
        return true;
    }

    public int pixelFor(int rgb) {
        return surfaceType.pixelFor(rgb, colorModel);
    }

    public int pixelFor(Color c) {
        return pixelFor(c.getRGB());
    }

    public int rgbFor(int pixel) {
        return surfaceType.rgbFor(pixel, colorModel);
    }

    public abstract Rectangle getBounds();

    static java.security.Permission compPermission;

    protected void checkCustomComposite() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (compPermission == null) {
                compPermission = new java.awt.AWTPermission("readDisplayPixels");
            }
            sm.checkPermission(compPermission);
        }
    }

    protected static native boolean isOpaqueGray(IndexColorModel icm);

    public static boolean isNull(SurfaceData sd) {
        if (sd == null || sd == NullSurfaceData.theInstance) {
            return true;
        }
        return false;
    }

    public boolean copyArea(SunGraphics2D sg2d, int x, int y, int w, int h, int dx, int dy) {
        return false;
    }

    public void flush() {
    }

    public abstract Object getDestination();
}
