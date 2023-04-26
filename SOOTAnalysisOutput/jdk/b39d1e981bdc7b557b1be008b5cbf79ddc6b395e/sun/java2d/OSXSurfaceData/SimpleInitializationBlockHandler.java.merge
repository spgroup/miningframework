package sun.java2d;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.nio.*;
import sun.awt.*;
import sun.awt.image.*;
import sun.java2d.loops.*;
import sun.java2d.pipe.*;
import sun.lwawt.macosx.*;
import javax.tools.annotation.GenerateNativeHeader;

@GenerateNativeHeader
public abstract class OSXSurfaceData extends BufImgSurfaceData {

    final static float UPPER_BND = Float.MAX_VALUE / 2.0f;

    final static float LOWER_BND = -UPPER_BND;

    protected static CRenderer sQuartzPipe = null;

    protected static CTextPipe sCocoaTextPipe = null;

    protected static CompositeCRenderer sQuartzCompositePipe = null;

    private GraphicsConfiguration fConfig;

    private Rectangle fBounds;

    static {
        sQuartzPipe = new CRenderer();
    }

    public OSXSurfaceData(SurfaceType sType, ColorModel cm) {
        this(sType, cm, null, new Rectangle());
    }

    public OSXSurfaceData(SurfaceType sType, ColorModel cm, GraphicsConfiguration config, Rectangle bounds) {
        super(sType, cm);
        this.fConfig = config;
        this.fBounds = new Rectangle(bounds.x, bounds.y, bounds.width, bounds.y + bounds.height);
        this.fGraphicsStates = getBufferOfSize(kSizeOfParameters);
        this.fGraphicsStatesInt = this.fGraphicsStates.asIntBuffer();
        this.fGraphicsStatesFloat = this.fGraphicsStates.asFloatBuffer();
        this.fGraphicsStatesLong = this.fGraphicsStates.asLongBuffer();
        this.fGraphicsStatesObject = new Object[6];
    }

    public void validatePipe(SunGraphics2D sg2d) {
        if (sg2d.compositeState <= SunGraphics2D.COMP_ALPHA) {
            if (sCocoaTextPipe == null) {
                sCocoaTextPipe = new CTextPipe();
            }
            sg2d.imagepipe = sQuartzPipe;
            sg2d.drawpipe = sQuartzPipe;
            sg2d.fillpipe = sQuartzPipe;
            sg2d.shapepipe = sQuartzPipe;
            sg2d.textpipe = sCocoaTextPipe;
        } else {
            setPipesToQuartzComposite(sg2d);
        }
    }

    protected void setPipesToQuartzComposite(SunGraphics2D sg2d) {
        if (sQuartzCompositePipe == null) {
            sQuartzCompositePipe = new CompositeCRenderer();
        }
        if (sCocoaTextPipe == null) {
            sCocoaTextPipe = new CTextPipe();
        }
        sg2d.imagepipe = sQuartzCompositePipe;
        sg2d.drawpipe = sQuartzCompositePipe;
        sg2d.fillpipe = sQuartzCompositePipe;
        sg2d.shapepipe = sQuartzCompositePipe;
        sg2d.textpipe = sCocoaTextPipe;
    }

    public Rectangle getBounds() {
        return new Rectangle(fBounds.x, fBounds.y, fBounds.width, fBounds.height - fBounds.y);
    }

    public GraphicsConfiguration getDeviceConfiguration() {
        return fConfig;
    }

    protected void setBounds(int x, int y, int w, int h) {
        fBounds.reshape(x, y, w, y + h);
    }

    public abstract BufferedImage copyArea(SunGraphics2D sg2d, int x, int y, int w, int h, BufferedImage image);

    public abstract boolean xorSurfacePixels(SunGraphics2D sg2d, BufferedImage srcPixels, int x, int y, int w, int h, int colorXOR);

    GraphicsConfiguration sDefaultGraphicsConfiguration = null;

    protected BufferedImage getCompositingImage(int w, int h) {
        if (sDefaultGraphicsConfiguration == null) {
            sDefaultGraphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
        clearRect(img, w, h);
        return img;
    }

    protected BufferedImage getCompositingImageSame(BufferedImage img, int w, int h) {
        if ((img == null) || (img.getWidth() != w) || (img.getHeight() != h)) {
            img = getCompositingImage(w, h);
        }
        return img;
    }

    BufferedImage sSrcComposite = null;

    public BufferedImage getCompositingSrcImage(int w, int h) {
        BufferedImage bim = getCompositingImageSame(sSrcComposite, w, h);
        sSrcComposite = bim;
        return bim;
    }

    BufferedImage sDstInComposite = null;

    public BufferedImage getCompositingDstInImage(int w, int h) {
        BufferedImage bim = getCompositingImageSame(sDstInComposite, w, h);
        sDstInComposite = bim;
        return bim;
    }

    BufferedImage sDstOutComposite = null;

    public BufferedImage getCompositingDstOutImage(int w, int h) {
        BufferedImage bim = getCompositingImageSame(sDstOutComposite, w, h);
        sDstOutComposite = bim;
        return bim;
    }

    public void clearRect(BufferedImage bim, int w, int h) {
        Graphics2D g = bim.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, w, h);
        g.dispose();
    }

    public void invalidate() {
    }

    static final int kPrimitive = 0;

    static final int kImage = 1;

    static final int kText = 2;

    static final int kCopyArea = 3;

    static final int kExternal = 4;

    static final int kLine = 5;

    static final int kRect = 6;

    static final int kRoundRect = 7;

    static final int kOval = 8;

    static final int kArc = 9;

    static final int kPolygon = 10;

    static final int kShape = 11;

    static final int kString = 13;

    static final int kGlyphs = 14;

    static final int kUnicodes = 15;

    static final int kCommonParameterCount = 1 + 1 + 4 + 4;

    static final int kLineParametersCount = kCommonParameterCount;

    static final int kRectParametersCount = kCommonParameterCount + 1;

    static final int kRoundRectParametersCount = kCommonParameterCount + 2 + 1;

    static final int kOvalParametersCount = kCommonParameterCount + 1;

    static final int kArcParametersCount = kCommonParameterCount + 2 + 1 + 1;

    static final int kPolygonParametersCount = 0;

    static final int kShapeParametersCount = 0;

    static final int kImageParametersCount = kCommonParameterCount + 2 + 2 + 4 + 4;

    static final int kStringParametersCount = 0;

    static final int kGlyphsParametersCount = 0;

    static final int kUnicodesParametersCount = 0;

    static final int kPixelParametersCount = 0;

    static final int kExternalParametersCount = 0;

    static final int kChangeFlagIndex = 0;

    static final int kBoundsXIndex = 1;

    static final int kBoundsYIndex = 2;

    static final int kBoundsWidthIndex = 3;

    static final int kBoundsHeightIndex = 4;

    static final int kClipStateIndex = 5;

    static final int kClipNumTypesIndex = 6;

    static final int kClipNumCoordsIndex = 7;

    static final int kClipWindingRuleIndex = 8;

    static final int kClipXIndex = 9;

    static final int kClipYIndex = 10;

    static final int kClipWidthIndex = 11;

    static final int kClipHeightIndex = 12;

    static final int kCTMaIndex = 13;

    static final int kCTMbIndex = 14;

    static final int kCTMcIndex = 15;

    static final int kCTMdIndex = 16;

    static final int kCTMtxIndex = 17;

    static final int kCTMtyIndex = 18;

    static final int kColorStateIndex = 19;

    static final int kColorRGBValueIndex = 20;

    static final int kColorIndexValueIndex = 21;

    static final int kColorPointerIndex = 22;

    static final int kColorPointerIndex2 = 23;

    static final int kColorRGBValue1Index = 24;

    static final int kColorWidthIndex = 25;

    static final int kColorRGBValue2Index = 26;

    static final int kColorHeightIndex = 27;

    static final int kColorIsCyclicIndex = 28;

    static final int kColorx1Index = 29;

    static final int kColortxIndex = 30;

    static final int kColory1Index = 31;

    static final int kColortyIndex = 32;

    static final int kColorx2Index = 33;

    static final int kColorsxIndex = 34;

    static final int kColory2Index = 35;

    static final int kColorsyIndex = 36;

    static final int kCompositeRuleIndex = 37;

    static final int kCompositeValueIndex = 38;

    static final int kStrokeJoinIndex = 39;

    static final int kStrokeCapIndex = 40;

    static final int kStrokeWidthIndex = 41;

    static final int kStrokeDashPhaseIndex = 42;

    static final int kStrokeLimitIndex = 43;

    static final int kHintsAntialiasIndex = 44;

    static final int kHintsTextAntialiasIndex = 45;

    static final int kHintsFractionalMetricsIndex = 46;

    static final int kHintsRenderingIndex = 47;

    static final int kHintsInterpolationIndex = 48;

    static final int kCanDrawDuringLiveResizeIndex = 49;

    static final int kSizeOfParameters = kCanDrawDuringLiveResizeIndex + 1;

    static final int kClipCoordinatesIndex = 0;

    static final int kClipTypesIndex = 1;

    static final int kTextureImageIndex = 2;

    static final int kStrokeDashArrayIndex = 3;

    static final int kFontIndex = 4;

    static final int kFontPaintIndex = 5;

    static final int kBoundsChangedBit = 1 << 0;

    static final int kBoundsNotChangedBit = ~kBoundsChangedBit;

    static final int kClipChangedBit = 1 << 1;

    static final int kClipNotChangedBit = ~kClipChangedBit;

    static final int kCTMChangedBit = 1 << 2;

    static final int kCTMNotChangedBit = ~kCTMChangedBit;

    static final int kColorChangedBit = 1 << 3;

    static final int kColorNotChangedBit = ~kColorChangedBit;

    static final int kCompositeChangedBit = 1 << 4;

    static final int kCompositeNotChangedBit = ~kCompositeChangedBit;

    static final int kStrokeChangedBit = 1 << 5;

    static final int kStrokeNotChangedBit = ~kStrokeChangedBit;

    static final int kHintsChangedBit = 1 << 6;

    static final int kHintsNotChangedBit = ~kHintsChangedBit;

    static final int kFontChangedBit = 1 << 7;

    static final int kFontNotChangedBit = ~kFontChangedBit;

    static final int kEverythingChangedFlag = 0xffffffff;

    static final int kColorSimple = 0;

    static final int kColorSystem = 1;

    static final int kColorGradient = 2;

    static final int kColorTexture = 3;

    static final int kColorNonCyclic = 0;

    static final int kColorCyclic = 1;

    static final int kClipRect = 0;

    static final int kClipShape = 1;

    static int getRendererTypeForPrimitive(int primitiveType) {
        switch(primitiveType) {
            case kImage:
                return kImage;
            case kCopyArea:
                return kCopyArea;
            case kExternal:
                return kExternal;
            case kString:
            case kGlyphs:
            case kUnicodes:
                return kText;
            default:
                return kPrimitive;
        }
    }

    int fChangeFlag;

    protected ByteBuffer fGraphicsStates = null;

    IntBuffer fGraphicsStatesInt = null;

    FloatBuffer fGraphicsStatesFloat = null;

    LongBuffer fGraphicsStatesLong = null;

    protected Object[] fGraphicsStatesObject = null;

    Rectangle userBounds = new Rectangle();

    float lastUserX = 0;

    float lastUserY = 0;

    float lastUserW = 0;

    float lastUserH = 0;

    void setUserBounds(SunGraphics2D sg2d, int x, int y, int width, int height) {
        if ((lastUserX != x) || (lastUserY != y) || (lastUserW != width) || (lastUserH != height)) {
            lastUserX = x;
            lastUserY = y;
            lastUserW = width;
            lastUserH = height;
            this.fGraphicsStatesInt.put(kBoundsXIndex, x);
            this.fGraphicsStatesInt.put(kBoundsYIndex, y);
            this.fGraphicsStatesInt.put(kBoundsWidthIndex, width);
            this.fGraphicsStatesInt.put(kBoundsHeightIndex, height);
            userBounds.setBounds(x, y, width, height);
            this.fChangeFlag = (this.fChangeFlag | kBoundsChangedBit);
        } else {
            this.fChangeFlag = (this.fChangeFlag & kBoundsNotChangedBit);
        }
    }

    static ByteBuffer getBufferOfSize(int size) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(size * 4);
        buffer.order(ByteOrder.nativeOrder());
        return buffer;
    }

    FloatBuffer clipCoordinatesArray = null;

    IntBuffer clipTypesArray = null;

    Shape lastClipShape = null;

    float lastClipX = 0;

    float lastClipY = 0;

    float lastClipW = 0;

    float lastClipH = 0;

    void setupClip(SunGraphics2D sg2d) {
        switch(sg2d.clipState) {
            case SunGraphics2D.CLIP_DEVICE:
            case SunGraphics2D.CLIP_RECTANGULAR:
                {
                    Region clip = sg2d.getCompClip();
                    float x = clip.getLoX();
                    float y = clip.getLoY();
                    float w = clip.getWidth();
                    float h = clip.getHeight();
                    if ((this.fGraphicsStatesInt.get(kClipStateIndex) != kClipRect) || (x != lastClipX) || (y != lastClipY) || (w != lastClipW) || (h != lastClipH)) {
                        this.fGraphicsStatesFloat.put(kClipXIndex, x);
                        this.fGraphicsStatesFloat.put(kClipYIndex, y);
                        this.fGraphicsStatesFloat.put(kClipWidthIndex, w);
                        this.fGraphicsStatesFloat.put(kClipHeightIndex, h);
                        lastClipX = x;
                        lastClipY = y;
                        lastClipW = w;
                        lastClipH = h;
                        this.fChangeFlag = (this.fChangeFlag | kClipChangedBit);
                    } else {
                        this.fChangeFlag = (this.fChangeFlag & kClipNotChangedBit);
                    }
                    this.fGraphicsStatesInt.put(kClipStateIndex, kClipRect);
                    break;
                }
            case SunGraphics2D.CLIP_SHAPE:
                {
                    lastClipShape = sg2d.usrClip;
                    GeneralPath gp = null;
                    if (sg2d.usrClip instanceof GeneralPath) {
                        gp = (GeneralPath) sg2d.usrClip;
                    } else {
                        gp = new GeneralPath(sg2d.usrClip);
                    }
                    int shapeLength = getPathLength(gp);
                    if ((clipCoordinatesArray == null) || (clipCoordinatesArray.capacity() < (shapeLength * 6))) {
                        clipCoordinatesArray = getBufferOfSize(shapeLength * 6).asFloatBuffer();
                    }
                    if ((clipTypesArray == null) || (clipTypesArray.capacity() < shapeLength)) {
                        clipTypesArray = getBufferOfSize(shapeLength).asIntBuffer();
                    }
                    int windingRule = getPathCoordinates(gp, clipCoordinatesArray, clipTypesArray);
                    this.fGraphicsStatesInt.put(kClipNumTypesIndex, clipTypesArray.position());
                    this.fGraphicsStatesInt.put(kClipNumCoordsIndex, clipCoordinatesArray.position());
                    this.fGraphicsStatesInt.put(kClipWindingRuleIndex, windingRule);
                    this.fGraphicsStatesObject[kClipTypesIndex] = clipTypesArray;
                    this.fGraphicsStatesObject[kClipCoordinatesIndex] = clipCoordinatesArray;
                    this.fChangeFlag = (this.fChangeFlag | kClipChangedBit);
                    this.fGraphicsStatesInt.put(kClipStateIndex, kClipShape);
                    break;
                }
        }
    }

    final double[] lastCTM = new double[6];

    float lastCTMa = 0;

    float lastCTMb = 0;

    float lastCTMc = 0;

    float lastCTMd = 0;

    float lastCTMtx = 0;

    float lastCTMty = 0;

    void setupTransform(SunGraphics2D sg2d) {
        sg2d.transform.getMatrix(lastCTM);
        float a = (float) lastCTM[0];
        float b = (float) lastCTM[1];
        float c = (float) lastCTM[2];
        float d = (float) lastCTM[3];
        float tx = (float) lastCTM[4];
        float ty = (float) lastCTM[5];
        if (tx != lastCTMtx || ty != lastCTMty || a != lastCTMa || b != lastCTMb || c != lastCTMc || d != lastCTMd) {
            this.fGraphicsStatesFloat.put(kCTMaIndex, a);
            this.fGraphicsStatesFloat.put(kCTMbIndex, b);
            this.fGraphicsStatesFloat.put(kCTMcIndex, c);
            this.fGraphicsStatesFloat.put(kCTMdIndex, d);
            this.fGraphicsStatesFloat.put(kCTMtxIndex, tx);
            this.fGraphicsStatesFloat.put(kCTMtyIndex, ty);
            lastCTMa = a;
            lastCTMb = b;
            lastCTMc = c;
            lastCTMd = d;
            lastCTMtx = tx;
            lastCTMty = ty;
            this.fChangeFlag = (this.fChangeFlag | kCTMChangedBit);
        } else {
            this.fChangeFlag = (this.fChangeFlag & kCTMNotChangedBit);
        }
    }

    static AffineTransform sIdentityMatrix = new AffineTransform();

    Paint lastPaint = null;

    long lastPaintPtr = 0;

    int lastPaintRGB = 0;

    int lastPaintIndex = 0;

    BufferedImage texturePaintImage = null;

    void setupPaint(SunGraphics2D sg2d, int x, int y, int w, int h) {
        if (sg2d.paint instanceof SystemColor) {
            SystemColor color = (SystemColor) sg2d.paint;
            int index = color.hashCode();
            if ((this.fGraphicsStatesInt.get(kColorStateIndex) != kColorSystem) || (index != this.lastPaintIndex)) {
                this.lastPaintIndex = index;
                this.fGraphicsStatesInt.put(kColorStateIndex, kColorSystem);
                this.fGraphicsStatesInt.put(kColorIndexValueIndex, index);
                this.fChangeFlag = (this.fChangeFlag | kColorChangedBit);
            } else {
                this.fChangeFlag = (this.fChangeFlag & kColorNotChangedBit);
            }
        } else if (sg2d.paint instanceof Color) {
            Color color = (Color) sg2d.paint;
            int rgb = color.getRGB();
            if ((this.fGraphicsStatesInt.get(kColorStateIndex) != kColorSimple) || (rgb != this.lastPaintRGB)) {
                this.lastPaintRGB = rgb;
                this.fGraphicsStatesInt.put(kColorStateIndex, kColorSimple);
                this.fGraphicsStatesInt.put(kColorRGBValueIndex, rgb);
                this.fChangeFlag = (this.fChangeFlag | kColorChangedBit);
            } else {
                this.fChangeFlag = (this.fChangeFlag & kColorNotChangedBit);
            }
        } else if (sg2d.paint instanceof GradientPaint) {
            if ((this.fGraphicsStatesInt.get(kColorStateIndex) != kColorGradient) || (lastPaint != sg2d.paint)) {
                GradientPaint color = (GradientPaint) sg2d.paint;
                this.fGraphicsStatesInt.put(kColorStateIndex, kColorGradient);
                this.fGraphicsStatesInt.put(kColorRGBValue1Index, color.getColor1().getRGB());
                this.fGraphicsStatesInt.put(kColorRGBValue2Index, color.getColor2().getRGB());
                this.fGraphicsStatesInt.put(kColorIsCyclicIndex, (color.isCyclic()) ? kColorCyclic : kColorNonCyclic);
                Point2D p = color.getPoint1();
                this.fGraphicsStatesFloat.put(kColorx1Index, (float) p.getX());
                this.fGraphicsStatesFloat.put(kColory1Index, (float) p.getY());
                p = color.getPoint2();
                this.fGraphicsStatesFloat.put(kColorx2Index, (float) p.getX());
                this.fGraphicsStatesFloat.put(kColory2Index, (float) p.getY());
                this.fChangeFlag = (this.fChangeFlag | kColorChangedBit);
            } else {
                this.fChangeFlag = (this.fChangeFlag & kColorNotChangedBit);
            }
        } else if (sg2d.paint instanceof TexturePaint) {
            if ((this.fGraphicsStatesInt.get(kColorStateIndex) != kColorTexture) || (lastPaint != sg2d.paint)) {
                TexturePaint color = (TexturePaint) sg2d.paint;
                this.fGraphicsStatesInt.put(kColorStateIndex, kColorTexture);
                texturePaintImage = color.getImage();
                SurfaceData textureSurfaceData = BufImgSurfaceData.createData(texturePaintImage);
                this.fGraphicsStatesInt.put(kColorWidthIndex, texturePaintImage.getWidth());
                this.fGraphicsStatesInt.put(kColorHeightIndex, texturePaintImage.getHeight());
                Rectangle2D anchor = color.getAnchorRect();
                this.fGraphicsStatesFloat.put(kColortxIndex, (float) anchor.getX());
                this.fGraphicsStatesFloat.put(kColortyIndex, (float) anchor.getY());
                this.fGraphicsStatesFloat.put(kColorsxIndex, (float) (anchor.getWidth() / texturePaintImage.getWidth()));
                this.fGraphicsStatesFloat.put(kColorsyIndex, (float) (anchor.getHeight() / texturePaintImage.getHeight()));
                this.fGraphicsStatesObject[kTextureImageIndex] = textureSurfaceData;
                this.fChangeFlag = (this.fChangeFlag | kColorChangedBit);
            } else {
                this.fChangeFlag = (this.fChangeFlag & kColorNotChangedBit);
            }
        } else {
            if ((this.fGraphicsStatesInt.get(kColorStateIndex) != kColorTexture) || (lastPaint != sg2d.paint) || ((this.fChangeFlag & kBoundsChangedBit) != 0)) {
                PaintContext context = sg2d.paint.createContext(sg2d.getDeviceColorModel(), userBounds, userBounds, sIdentityMatrix, sg2d.getRenderingHints());
                WritableRaster raster = (WritableRaster) (context.getRaster(userBounds.x, userBounds.y, userBounds.width, userBounds.height));
                ColorModel cm = context.getColorModel();
                texturePaintImage = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
                this.fGraphicsStatesInt.put(kColorStateIndex, kColorTexture);
                this.fGraphicsStatesInt.put(kColorWidthIndex, texturePaintImage.getWidth());
                this.fGraphicsStatesInt.put(kColorHeightIndex, texturePaintImage.getHeight());
                this.fGraphicsStatesFloat.put(kColortxIndex, (float) userBounds.getX());
                this.fGraphicsStatesFloat.put(kColortyIndex, (float) userBounds.getY());
                this.fGraphicsStatesFloat.put(kColorsxIndex, 1.0f);
                this.fGraphicsStatesFloat.put(kColorsyIndex, 1.0f);
                this.fGraphicsStatesObject[kTextureImageIndex] = sun.awt.image.BufImgSurfaceData.createData(texturePaintImage);
                context.dispose();
                this.fChangeFlag = (this.fChangeFlag | kColorChangedBit);
            } else {
                this.fChangeFlag = (this.fChangeFlag & kColorNotChangedBit);
            }
        }
        lastPaint = sg2d.paint;
    }

    Composite lastComposite;

    int lastCompositeAlphaRule = 0;

    float lastCompositeAlphaValue = 0;

    void setupComposite(SunGraphics2D sg2d) {
        Composite composite = sg2d.composite;
        if (lastComposite != composite) {
            lastComposite = composite;
            int alphaRule = AlphaComposite.SRC_OVER;
            float alphaValue = 1.0f;
            if ((sg2d.compositeState <= SunGraphics2D.COMP_ALPHA) && (composite != null)) {
                AlphaComposite alphaComposite = (AlphaComposite) composite;
                alphaRule = alphaComposite.getRule();
                alphaValue = alphaComposite.getAlpha();
            }
            if ((lastCompositeAlphaRule != alphaRule) || (lastCompositeAlphaValue != alphaValue)) {
                this.fGraphicsStatesInt.put(kCompositeRuleIndex, alphaRule);
                this.fGraphicsStatesFloat.put(kCompositeValueIndex, alphaValue);
                lastCompositeAlphaRule = alphaRule;
                lastCompositeAlphaValue = alphaValue;
                this.fChangeFlag = (this.fChangeFlag | kCompositeChangedBit);
            } else {
                this.fChangeFlag = (this.fChangeFlag & kCompositeNotChangedBit);
            }
        } else {
            this.fChangeFlag = (this.fChangeFlag & kCompositeNotChangedBit);
        }
    }

    BasicStroke lastStroke = null;

    static BasicStroke defaultBasicStroke = new BasicStroke();

    void setupStroke(SunGraphics2D sg2d) {
        BasicStroke stroke = defaultBasicStroke;
        if (sg2d.stroke instanceof BasicStroke) {
            stroke = (BasicStroke) sg2d.stroke;
        }
        if (lastStroke != stroke) {
            this.fGraphicsStatesObject[kStrokeDashArrayIndex] = stroke.getDashArray();
            this.fGraphicsStatesFloat.put(kStrokeDashPhaseIndex, stroke.getDashPhase());
            this.fGraphicsStatesInt.put(kStrokeCapIndex, stroke.getEndCap());
            this.fGraphicsStatesInt.put(kStrokeJoinIndex, stroke.getLineJoin());
            this.fGraphicsStatesFloat.put(kStrokeWidthIndex, stroke.getLineWidth());
            this.fGraphicsStatesFloat.put(kStrokeLimitIndex, stroke.getMiterLimit());
            this.fChangeFlag = (this.fChangeFlag | kStrokeChangedBit);
            lastStroke = stroke;
        } else {
            this.fChangeFlag = (this.fChangeFlag & kStrokeNotChangedBit);
        }
    }

    Font lastFont;

    void setupFont(Font font, Paint paint) {
        if (font == null) {
            return;
        }
        if ((font != lastFont) || ((this.fChangeFlag & kColorChangedBit) != 0)) {
            this.fGraphicsStatesObject[kFontIndex] = font;
            this.fGraphicsStatesObject[kFontPaintIndex] = paint;
            this.fChangeFlag = (this.fChangeFlag | kFontChangedBit);
            lastFont = font;
        } else {
            this.fChangeFlag = (this.fChangeFlag & kFontNotChangedBit);
        }
    }

    void setupRenderingHints(SunGraphics2D sg2d) {
        boolean hintsChanged = false;
        int antialiasHint = sg2d.antialiasHint;
        if (this.fGraphicsStatesInt.get(kHintsAntialiasIndex) != antialiasHint) {
            this.fGraphicsStatesInt.put(kHintsAntialiasIndex, antialiasHint);
            hintsChanged = true;
        }
        int textAntialiasHint = sg2d.textAntialiasHint;
        if (this.fGraphicsStatesInt.get(kHintsTextAntialiasIndex) != textAntialiasHint) {
            this.fGraphicsStatesInt.put(kHintsTextAntialiasIndex, textAntialiasHint);
            hintsChanged = true;
        }
        int fractionalMetricsHint = sg2d.fractionalMetricsHint;
        if (this.fGraphicsStatesInt.get(kHintsFractionalMetricsIndex) != fractionalMetricsHint) {
            this.fGraphicsStatesInt.put(kHintsFractionalMetricsIndex, fractionalMetricsHint);
            hintsChanged = true;
        }
        int renderHint = sg2d.renderHint;
        if (this.fGraphicsStatesInt.get(kHintsRenderingIndex) != renderHint) {
            this.fGraphicsStatesInt.put(kHintsRenderingIndex, renderHint);
            hintsChanged = true;
        }
        Object hintValue = sg2d.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        int interpolationHint = (hintValue != null ? ((SunHints.Value) hintValue).getIndex() : -1);
        if (this.fGraphicsStatesInt.get(kHintsInterpolationIndex) != interpolationHint) {
            this.fGraphicsStatesInt.put(kHintsInterpolationIndex, interpolationHint);
            hintsChanged = true;
        }
        if (hintsChanged) {
            this.fChangeFlag = (this.fChangeFlag | kHintsChangedBit);
        } else {
            this.fChangeFlag = (this.fChangeFlag & kHintsNotChangedBit);
        }
    }

    SunGraphics2D sg2dCurrent = null;

    Thread threadCurrent = null;

    void setupGraphicsState(SunGraphics2D sg2d, int primitiveType) {
        setupGraphicsState(sg2d, primitiveType, sg2d.font, 0, 0, fBounds.width, fBounds.height);
    }

    void setupGraphicsState(SunGraphics2D sg2d, int primitiveType, int x, int y, int w, int h) {
        setupGraphicsState(sg2d, primitiveType, sg2d.font, x, y, w, h);
    }

    void setupGraphicsState(SunGraphics2D sg2d, int primitiveType, Font font, int x, int y, int w, int h) {
        this.fChangeFlag = 0;
        setUserBounds(sg2d, x, y, w, h);
        Thread thread = Thread.currentThread();
        if ((this.sg2dCurrent != sg2d) || (this.threadCurrent != thread)) {
            this.sg2dCurrent = sg2d;
            this.threadCurrent = thread;
            setupClip(sg2d);
            setupTransform(sg2d);
            setupPaint(sg2d, x, y, w, h);
            setupComposite(sg2d);
            setupStroke(sg2d);
            setupFont(font, sg2d.paint);
            setupRenderingHints(sg2d);
            this.fChangeFlag = kEverythingChangedFlag;
        } else {
            int rendererType = getRendererTypeForPrimitive(primitiveType);
            setupClip(sg2d);
            setupTransform(sg2d);
            if (rendererType != kCopyArea) {
                setupComposite(sg2d);
                setupRenderingHints(sg2d);
                if ((rendererType != kImage)) {
                    setupPaint(sg2d, x, y, w, h);
                    setupStroke(sg2d);
                }
                if (rendererType != kPrimitive) {
                    setupFont(font, sg2d.paint);
                }
            }
        }
        this.fGraphicsStatesInt.put(kChangeFlagIndex, this.fChangeFlag);
    }

    boolean isCustomPaint(SunGraphics2D sg2d) {
        if ((sg2d.paint instanceof Color) || (sg2d.paint instanceof SystemColor) || (sg2d.paint instanceof GradientPaint) || (sg2d.paint instanceof TexturePaint)) {
            return false;
        }
        return true;
    }

    final float[] segmentCoordinatesArray = new float[6];

    int getPathLength(GeneralPath gp) {
        int length = 0;
        PathIterator pi = gp.getPathIterator(null);
        while (pi.isDone() == false) {
            pi.next();
            length++;
        }
        return length;
    }

    int getPathCoordinates(GeneralPath gp, FloatBuffer coordinates, IntBuffer types) {
        boolean skip = false;
        coordinates.clear();
        types.clear();
        int type;
        PathIterator pi = gp.getPathIterator(null);
        while (pi.isDone() == false) {
            skip = false;
            type = pi.currentSegment(segmentCoordinatesArray);
            switch(type) {
                case PathIterator.SEG_MOVETO:
                    if (segmentCoordinatesArray[0] < UPPER_BND && segmentCoordinatesArray[0] > LOWER_BND && segmentCoordinatesArray[1] < UPPER_BND && segmentCoordinatesArray[1] > LOWER_BND) {
                        coordinates.put(segmentCoordinatesArray[0]);
                        coordinates.put(segmentCoordinatesArray[1]);
                    } else {
                        skip = true;
                    }
                    break;
                case PathIterator.SEG_LINETO:
                    if (segmentCoordinatesArray[0] < UPPER_BND && segmentCoordinatesArray[0] > LOWER_BND && segmentCoordinatesArray[1] < UPPER_BND && segmentCoordinatesArray[1] > LOWER_BND) {
                        coordinates.put(segmentCoordinatesArray[0]);
                        coordinates.put(segmentCoordinatesArray[1]);
                    } else {
                        skip = true;
                    }
                    break;
                case PathIterator.SEG_QUADTO:
                    if (segmentCoordinatesArray[0] < UPPER_BND && segmentCoordinatesArray[0] > LOWER_BND && segmentCoordinatesArray[1] < UPPER_BND && segmentCoordinatesArray[1] > LOWER_BND && segmentCoordinatesArray[2] < UPPER_BND && segmentCoordinatesArray[2] > LOWER_BND && segmentCoordinatesArray[3] < UPPER_BND && segmentCoordinatesArray[3] > LOWER_BND) {
                        coordinates.put(segmentCoordinatesArray[0]);
                        coordinates.put(segmentCoordinatesArray[1]);
                        coordinates.put(segmentCoordinatesArray[2]);
                        coordinates.put(segmentCoordinatesArray[3]);
                    } else {
                        skip = true;
                    }
                    break;
                case PathIterator.SEG_CUBICTO:
                    if (segmentCoordinatesArray[0] < UPPER_BND && segmentCoordinatesArray[0] > LOWER_BND && segmentCoordinatesArray[1] < UPPER_BND && segmentCoordinatesArray[1] > LOWER_BND && segmentCoordinatesArray[2] < UPPER_BND && segmentCoordinatesArray[2] > LOWER_BND && segmentCoordinatesArray[3] < UPPER_BND && segmentCoordinatesArray[3] > LOWER_BND && segmentCoordinatesArray[4] < UPPER_BND && segmentCoordinatesArray[4] > LOWER_BND && segmentCoordinatesArray[5] < UPPER_BND && segmentCoordinatesArray[5] > LOWER_BND) {
                        coordinates.put(segmentCoordinatesArray[0]);
                        coordinates.put(segmentCoordinatesArray[1]);
                        coordinates.put(segmentCoordinatesArray[2]);
                        coordinates.put(segmentCoordinatesArray[3]);
                        coordinates.put(segmentCoordinatesArray[4]);
                        coordinates.put(segmentCoordinatesArray[5]);
                    } else {
                        skip = true;
                    }
                    break;
                case PathIterator.SEG_CLOSE:
                    break;
            }
            if (!skip) {
                types.put(type);
            }
            pi.next();
        }
        return pi.getWindingRule();
    }

    public void doLine(CRenderer renderer, SunGraphics2D sg2d, float x1, float y1, float x2, float y2) {
        setupGraphicsState(sg2d, kLine, sg2d.font, 0, 0, fBounds.width, fBounds.height);
        renderer.doLine(this, x1, y1, x2, y2);
    }

    public void doRect(CRenderer renderer, SunGraphics2D sg2d, float x, float y, float width, float height, boolean isfill) {
        if ((isfill) && (isCustomPaint(sg2d))) {
            setupGraphicsState(sg2d, kRect, (int) x, (int) y, (int) width, (int) height);
        } else {
            setupGraphicsState(sg2d, kRect, sg2d.font, 0, 0, fBounds.width, fBounds.height);
        }
        renderer.doRect(this, x, y, width, height, isfill);
    }

    public void doRoundRect(CRenderer renderer, SunGraphics2D sg2d, float x, float y, float width, float height, float arcW, float arcH, boolean isfill) {
        if ((isfill) && (isCustomPaint(sg2d))) {
            setupGraphicsState(sg2d, kRoundRect, (int) x, (int) y, (int) width, (int) height);
        } else {
            setupGraphicsState(sg2d, kRoundRect, sg2d.font, 0, 0, fBounds.width, fBounds.height);
        }
        renderer.doRoundRect(this, x, y, width, height, arcW, arcH, isfill);
    }

    public void doOval(CRenderer renderer, SunGraphics2D sg2d, float x, float y, float width, float height, boolean isfill) {
        if ((isfill) && (isCustomPaint(sg2d))) {
            setupGraphicsState(sg2d, kOval, (int) x, (int) y, (int) width, (int) height);
        } else {
            setupGraphicsState(sg2d, kOval, sg2d.font, 0, 0, fBounds.width, fBounds.height);
        }
        renderer.doOval(this, x, y, width, height, isfill);
    }

    public void doArc(CRenderer renderer, SunGraphics2D sg2d, float x, float y, float width, float height, float startAngle, float arcAngle, int type, boolean isfill) {
        if ((isfill) && (isCustomPaint(sg2d))) {
            setupGraphicsState(sg2d, kArc, (int) x, (int) y, (int) width, (int) height);
        } else {
            setupGraphicsState(sg2d, kArc, sg2d.font, 0, 0, fBounds.width, fBounds.height);
        }
        renderer.doArc(this, x, y, width, height, startAngle, arcAngle, type, isfill);
    }

    public void doPolygon(CRenderer renderer, SunGraphics2D sg2d, int[] xpoints, int[] ypoints, int npoints, boolean ispolygon, boolean isfill) {
        if ((isfill) && (isCustomPaint(sg2d))) {
            int minx = xpoints[0];
            int miny = ypoints[0];
            int maxx = minx;
            int maxy = miny;
            for (int i = 1; i < npoints; i++) {
                int x = xpoints[i];
                if (x < minx) {
                    minx = x;
                } else if (x > maxx) {
                    maxx = x;
                }
                int y = ypoints[i];
                if (y < miny) {
                    miny = y;
                } else if (y > maxy) {
                    maxy = y;
                }
            }
            setupGraphicsState(sg2d, kPolygon, minx, miny, maxx - minx, maxy - miny);
        } else {
            setupGraphicsState(sg2d, kPolygon, sg2d.font, 0, 0, fBounds.width, fBounds.height);
        }
        renderer.doPoly(this, xpoints, ypoints, npoints, ispolygon, isfill);
    }

    FloatBuffer shapeCoordinatesArray = null;

    IntBuffer shapeTypesArray = null;

    public void drawfillShape(CRenderer renderer, SunGraphics2D sg2d, GeneralPath gp, boolean isfill, boolean shouldApplyOffset) {
        if ((isfill) && (isCustomPaint(sg2d))) {
            Rectangle bounds = gp.getBounds();
            setupGraphicsState(sg2d, kShape, bounds.x, bounds.y, bounds.width, bounds.height);
        } else {
            setupGraphicsState(sg2d, kShape, sg2d.font, 0, 0, fBounds.width, fBounds.height);
        }
        int shapeLength = getPathLength(gp);
        if ((shapeCoordinatesArray == null) || (shapeCoordinatesArray.capacity() < (shapeLength * 6))) {
            shapeCoordinatesArray = getBufferOfSize(shapeLength * 6).asFloatBuffer();
        }
        if ((shapeTypesArray == null) || (shapeTypesArray.capacity() < shapeLength)) {
            shapeTypesArray = getBufferOfSize(shapeLength).asIntBuffer();
        }
        int windingRule = getPathCoordinates(gp, shapeCoordinatesArray, shapeTypesArray);
        renderer.doShape(this, shapeLength, shapeCoordinatesArray, shapeTypesArray, windingRule, isfill, shouldApplyOffset);
    }

    public void blitImage(CRenderer renderer, SunGraphics2D sg2d, SurfaceData img, boolean fliph, boolean flipv, int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh, Color bgColor) {
        OSXOffScreenSurfaceData osxsd = (OSXOffScreenSurfaceData) img;
        synchronized (osxsd.getLockObject()) {
            int w = osxsd.bim.getWidth();
            int h = osxsd.bim.getHeight();
            setupGraphicsState(sg2d, kImage, sg2d.font, 0, 0, fBounds.width, fBounds.height);
            if (bgColor != null) {
                img = osxsd.getCopyWithBgColor(bgColor);
            }
            renderer.doImage(this, img, fliph, flipv, w, h, sx, sy, sw, sh, dx, dy, dw, dh);
        }
    }

    public interface CGContextDrawable {

        public void drawIntoCGContext(final long cgContext);
    }

    public void drawString(CTextPipe renderer, SunGraphics2D sg2d, long nativeStrikePtr, String str, double x, double y) {
        if (str.length() == 0) {
            return;
        }
        setupGraphicsState(sg2d, kString, sg2d.font, 0, 0, fBounds.width, fBounds.height);
        renderer.doDrawString(this, nativeStrikePtr, str, x, y);
    }

    public void drawGlyphs(CTextPipe renderer, SunGraphics2D sg2d, long nativeStrikePtr, GlyphVector gv, float x, float y) {
        setupGraphicsState(sg2d, kGlyphs, gv.getFont(), 0, 0, fBounds.width, fBounds.height);
        renderer.doDrawGlyphs(this, nativeStrikePtr, gv, x, y);
    }

    public void drawUnicodes(CTextPipe renderer, SunGraphics2D sg2d, long nativeStrikePtr, char[] unicodes, int offset, int length, float x, float y) {
        setupGraphicsState(sg2d, kUnicodes, sg2d.font, 0, 0, fBounds.width, fBounds.height);
        if (length == 1) {
            renderer.doOneUnicode(this, nativeStrikePtr, unicodes[offset], x, y);
        } else {
            renderer.doUnicodes(this, nativeStrikePtr, unicodes, offset, length, x, y);
        }
    }

    Rectangle srcCopyAreaRect = new Rectangle();

    Rectangle dstCopyAreaRect = new Rectangle();

    Rectangle finalCopyAreaRect = new Rectangle();

    Rectangle copyAreaBounds = new Rectangle();

    void intersection(Rectangle r1, Rectangle r2, Rectangle r3) {
        int tx1 = r1.x;
        int ty1 = r1.y;
        long tx2 = tx1 + r1.width;
        long ty2 = ty1 + r1.height;
        int rx1 = r2.x;
        int ry1 = r2.y;
        long rx2 = rx1 + r2.width;
        long ry2 = ry1 + r2.height;
        if (tx1 < rx1)
            tx1 = rx1;
        if (ty1 < ry1)
            ty1 = ry1;
        if (tx2 > rx2)
            tx2 = rx2;
        if (ty2 > ry2)
            ty2 = ry2;
        tx2 -= tx1;
        ty2 -= ty1;
        if (tx2 < Integer.MIN_VALUE)
            tx2 = Integer.MIN_VALUE;
        if (ty2 < Integer.MIN_VALUE)
            ty2 = Integer.MIN_VALUE;
        r3.setBounds(tx1, ty1, (int) tx2, (int) ty2);
    }

    protected Rectangle clipCopyArea(SunGraphics2D sg2d, int x, int y, int w, int h, int dx, int dy) {
        copyAreaBounds.setBounds(sg2d.devClip.getLoX(), sg2d.devClip.getLoY(), sg2d.devClip.getWidth(), sg2d.devClip.getHeight());
        x += sg2d.transX;
        y += sg2d.transY;
        srcCopyAreaRect.setBounds(x, y, w, h);
        intersection(srcCopyAreaRect, copyAreaBounds, srcCopyAreaRect);
        if ((srcCopyAreaRect.width <= 0) || (srcCopyAreaRect.height <= 0)) {
            return null;
        }
        dstCopyAreaRect.setBounds(srcCopyAreaRect.x + dx, srcCopyAreaRect.y + dy, srcCopyAreaRect.width, srcCopyAreaRect.height);
        intersection(dstCopyAreaRect, copyAreaBounds, dstCopyAreaRect);
        if ((dstCopyAreaRect.width <= 0) || (dstCopyAreaRect.height <= 0)) {
            return null;
        }
        x = dstCopyAreaRect.x - dx;
        y = dstCopyAreaRect.y - dy;
        w = dstCopyAreaRect.width;
        h = dstCopyAreaRect.height;
        finalCopyAreaRect.setBounds(x, y, w, h);
        return finalCopyAreaRect;
    }

    protected void markDirty(boolean markAsDirty) {
    }

    @Override
    public boolean canRenderLCDText(SunGraphics2D sg2d) {
        if (sg2d.compositeState <= SunGraphics2D.COMP_ISCOPY && sg2d.paintState <= SunGraphics2D.PAINT_ALPHACOLOR && sg2d.clipState <= SunGraphics2D.CLIP_RECTANGULAR && sg2d.antialiasHint != SunHints.INTVAL_ANTIALIAS_ON) {
            return true;
        }
        return false;
    }

    public static boolean IsSimpleColor(Object c) {
        return ((c instanceof Color) || (c instanceof SystemColor) || (c instanceof javax.swing.plaf.ColorUIResource));
    }

    static {
        if ((kColorPointerIndex % 2) != 0) {
            System.err.println("kColorPointerIndex=" + kColorPointerIndex + " is NOT aligned for 64 bit");
            System.exit(0);
        }
    }
}