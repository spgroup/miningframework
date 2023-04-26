package sun.java2d.marlin;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.security.AccessController;
import static sun.java2d.marlin.MarlinUtils.logInfo;
import sun.awt.geom.PathConsumer2D;
import sun.java2d.ReentrantContextProvider;
import sun.java2d.ReentrantContextProviderCLQ;
import sun.java2d.ReentrantContextProviderTL;
import sun.java2d.pipe.AATileGenerator;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.RenderingEngine;
import sun.security.action.GetPropertyAction;

public final class MarlinRenderingEngine extends RenderingEngine implements MarlinConst {

    private static enum NormMode {

        ON_WITH_AA {

            @Override
            PathIterator getNormalizingPathIterator(final RendererContext rdrCtx, final PathIterator src) {
                return rdrCtx.nPCPathIterator.init(src);
            }
        }
        , ON_NO_AA {

            @Override
            PathIterator getNormalizingPathIterator(final RendererContext rdrCtx, final PathIterator src) {
                return rdrCtx.nPQPathIterator.init(src);
            }
        }
        , OFF {

            @Override
            PathIterator getNormalizingPathIterator(final RendererContext rdrCtx, final PathIterator src) {
                return src;
            }
        }
        ;

        abstract PathIterator getNormalizingPathIterator(RendererContext rdrCtx, PathIterator src);
    }

    private static final float MIN_PEN_SIZE = 1.0f / NORM_SUBPIXELS;

    static final float UPPER_BND = Float.MAX_VALUE / 2.0f;

    static final float LOWER_BND = -UPPER_BND;

    public MarlinRenderingEngine() {
        super();
        logSettings(MarlinRenderingEngine.class.getName());
    }

    @Override
    public Shape createStrokedShape(Shape src, float width, int caps, int join, float miterlimit, float[] dashes, float dashphase) {
        final RendererContext rdrCtx = getRendererContext();
        try {
            final Path2D.Float p2d = rdrCtx.getPath2D();
            strokeTo(rdrCtx, src, null, width, NormMode.OFF, caps, join, miterlimit, dashes, dashphase, rdrCtx.transformerPC2D.wrapPath2d(p2d));
            return new Path2D.Float(p2d);
        } finally {
            returnRendererContext(rdrCtx);
        }
    }

    @Override
    public void strokeTo(Shape src, AffineTransform at, BasicStroke bs, boolean thin, boolean normalize, boolean antialias, final PathConsumer2D consumer) {
        final NormMode norm = (normalize) ? ((antialias) ? NormMode.ON_WITH_AA : NormMode.ON_NO_AA) : NormMode.OFF;
        final RendererContext rdrCtx = getRendererContext();
        try {
            strokeTo(rdrCtx, src, at, bs, thin, norm, antialias, consumer);
        } finally {
            returnRendererContext(rdrCtx);
        }
    }

    final void strokeTo(final RendererContext rdrCtx, Shape src, AffineTransform at, BasicStroke bs, boolean thin, NormMode normalize, boolean antialias, PathConsumer2D pc2d) {
        float lw;
        if (thin) {
            if (antialias) {
                lw = userSpaceLineWidth(at, MIN_PEN_SIZE);
            } else {
                lw = userSpaceLineWidth(at, 1.0f);
            }
        } else {
            lw = bs.getLineWidth();
        }
        strokeTo(rdrCtx, src, at, lw, normalize, bs.getEndCap(), bs.getLineJoin(), bs.getMiterLimit(), bs.getDashArray(), bs.getDashPhase(), pc2d);
    }

    private final float userSpaceLineWidth(AffineTransform at, float lw) {
        float widthScale;
        if (at == null) {
            widthScale = 1.0f;
        } else if ((at.getType() & (AffineTransform.TYPE_GENERAL_TRANSFORM | AffineTransform.TYPE_GENERAL_SCALE)) != 0) {
            widthScale = (float) Math.sqrt(at.getDeterminant());
        } else {
            double A = at.getScaleX();
            double C = at.getShearX();
            double B = at.getShearY();
            double D = at.getScaleY();
            double EA = A * A + B * B;
            double EB = 2.0d * (A * C + B * D);
            double EC = C * C + D * D;
            double hypot = Math.sqrt(EB * EB + (EA - EC) * (EA - EC));
            double widthsquared = ((EA + EC + hypot) / 2.0d);
            widthScale = (float) Math.sqrt(widthsquared);
        }
        return (lw / widthScale);
    }

    final void strokeTo(final RendererContext rdrCtx, Shape src, AffineTransform at, float width, NormMode norm, int caps, int join, float miterlimit, float[] dashes, float dashphase, PathConsumer2D pc2d) {
        AffineTransform strokerat = null;
        int dashLen = -1;
        boolean recycleDashes = false;
        if (at != null && !at.isIdentity()) {
            final double a = at.getScaleX();
            final double b = at.getShearX();
            final double c = at.getShearY();
            final double d = at.getScaleY();
            final double det = a * d - c * b;
            if (Math.abs(det) <= (2.0f * Float.MIN_VALUE)) {
                pc2d.moveTo(0.0f, 0.0f);
                pc2d.pathDone();
                return;
            }
            if (nearZero(a * b + c * d) && nearZero(a * a + c * c - (b * b + d * d))) {
                final float scale = (float) Math.sqrt(a * a + c * c);
                if (dashes != null) {
                    recycleDashes = true;
                    dashLen = dashes.length;
                    dashes = rdrCtx.dasher.copyDashArray(dashes);
                    for (int i = 0; i < dashLen; i++) {
                        dashes[i] *= scale;
                    }
                    dashphase *= scale;
                }
                width *= scale;
            } else {
                strokerat = at;
            }
        } else {
            at = null;
        }
        if (USE_SIMPLIFIER) {
            pc2d = rdrCtx.simplifier.init(pc2d);
        }
        final TransformingPathConsumer2D transformerPC2D = rdrCtx.transformerPC2D;
        pc2d = transformerPC2D.deltaTransformConsumer(pc2d, strokerat);
        pc2d = rdrCtx.stroker.init(pc2d, width, caps, join, miterlimit);
        if (dashes != null) {
            if (!recycleDashes) {
                dashLen = dashes.length;
            }
            pc2d = rdrCtx.dasher.init(pc2d, dashes, dashLen, dashphase, recycleDashes);
        }
        pc2d = transformerPC2D.inverseDeltaTransformConsumer(pc2d, strokerat);
        final PathIterator pi = norm.getNormalizingPathIterator(rdrCtx, src.getPathIterator(at));
        pathTo(rdrCtx, pi, pc2d);
    }

    private static boolean nearZero(final double num) {
        return Math.abs(num) < 2.0d * Math.ulp(num);
    }

    abstract static class NormalizingPathIterator implements PathIterator {

        private PathIterator src;

        private float curx_adjust, cury_adjust;

        private float movx_adjust, movy_adjust;

        private final float[] tmp;

        NormalizingPathIterator(final float[] tmp) {
            this.tmp = tmp;
        }

        final NormalizingPathIterator init(final PathIterator src) {
            this.src = src;
            return this;
        }

        final void dispose() {
            this.src = null;
        }

        @Override
        public final int currentSegment(final float[] coords) {
            int lastCoord;
            final int type = src.currentSegment(coords);
            switch(type) {
                case PathIterator.SEG_MOVETO:
                case PathIterator.SEG_LINETO:
                    lastCoord = 0;
                    break;
                case PathIterator.SEG_QUADTO:
                    lastCoord = 2;
                    break;
                case PathIterator.SEG_CUBICTO:
                    lastCoord = 4;
                    break;
                case PathIterator.SEG_CLOSE:
                    curx_adjust = movx_adjust;
                    cury_adjust = movy_adjust;
                    return type;
                default:
                    throw new InternalError("Unrecognized curve type");
            }
            float coord, x_adjust, y_adjust;
            coord = coords[lastCoord];
            x_adjust = normCoord(coord);
            coords[lastCoord] = x_adjust;
            x_adjust -= coord;
            coord = coords[lastCoord + 1];
            y_adjust = normCoord(coord);
            coords[lastCoord + 1] = y_adjust;
            y_adjust -= coord;
            switch(type) {
                case PathIterator.SEG_MOVETO:
                    movx_adjust = x_adjust;
                    movy_adjust = y_adjust;
                    break;
                case PathIterator.SEG_LINETO:
                    break;
                case PathIterator.SEG_QUADTO:
                    coords[0] += (curx_adjust + x_adjust) / 2.0f;
                    coords[1] += (cury_adjust + y_adjust) / 2.0f;
                    break;
                case PathIterator.SEG_CUBICTO:
                    coords[0] += curx_adjust;
                    coords[1] += cury_adjust;
                    coords[2] += x_adjust;
                    coords[3] += y_adjust;
                    break;
                case PathIterator.SEG_CLOSE:
                default:
            }
            curx_adjust = x_adjust;
            cury_adjust = y_adjust;
            return type;
        }

        abstract float normCoord(final float coord);

        @Override
        public final int currentSegment(final double[] coords) {
            final float[] _tmp = tmp;
            int type = this.currentSegment(_tmp);
            for (int i = 0; i < 6; i++) {
                coords[i] = _tmp[i];
            }
            return type;
        }

        @Override
        public final int getWindingRule() {
            return src.getWindingRule();
        }

        @Override
        public final boolean isDone() {
            if (src.isDone()) {
                dispose();
                return true;
            }
            return false;
        }

        @Override
        public final void next() {
            src.next();
        }

        static final class NearestPixelCenter extends NormalizingPathIterator {

            NearestPixelCenter(final float[] tmp) {
                super(tmp);
            }

            @Override
            float normCoord(final float coord) {
                return FloatMath.floor_f(coord) + 0.5f;
            }
        }

        static final class NearestPixelQuarter extends NormalizingPathIterator {

            NearestPixelQuarter(final float[] tmp) {
                super(tmp);
            }

            @Override
            float normCoord(final float coord) {
                return FloatMath.floor_f(coord + 0.25f) + 0.25f;
            }
        }
    }

    private static void pathTo(final RendererContext rdrCtx, final PathIterator pi, final PathConsumer2D pc2d) {
        rdrCtx.dirty = true;
        final float[] coords = rdrCtx.float6;
        pathToLoop(coords, pi, pc2d);
        rdrCtx.dirty = false;
    }

    private static void pathToLoop(final float[] coords, final PathIterator pi, final PathConsumer2D pc2d) {
        boolean subpathStarted = false;
        for (; !pi.isDone(); pi.next()) {
            switch(pi.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    if (coords[0] < UPPER_BND && coords[0] > LOWER_BND && coords[1] < UPPER_BND && coords[1] > LOWER_BND) {
                        pc2d.moveTo(coords[0], coords[1]);
                        subpathStarted = true;
                    }
                    break;
                case PathIterator.SEG_LINETO:
                    if (coords[0] < UPPER_BND && coords[0] > LOWER_BND && coords[1] < UPPER_BND && coords[1] > LOWER_BND) {
                        if (subpathStarted) {
                            pc2d.lineTo(coords[0], coords[1]);
                        } else {
                            pc2d.moveTo(coords[0], coords[1]);
                            subpathStarted = true;
                        }
                    }
                    break;
                case PathIterator.SEG_QUADTO:
                    if (coords[2] < UPPER_BND && coords[2] > LOWER_BND && coords[3] < UPPER_BND && coords[3] > LOWER_BND) {
                        if (subpathStarted) {
                            if (coords[0] < UPPER_BND && coords[0] > LOWER_BND && coords[1] < UPPER_BND && coords[1] > LOWER_BND) {
                                pc2d.quadTo(coords[0], coords[1], coords[2], coords[3]);
                            } else {
                                pc2d.lineTo(coords[2], coords[3]);
                            }
                        } else {
                            pc2d.moveTo(coords[2], coords[3]);
                            subpathStarted = true;
                        }
                    }
                    break;
                case PathIterator.SEG_CUBICTO:
                    if (coords[4] < UPPER_BND && coords[4] > LOWER_BND && coords[5] < UPPER_BND && coords[5] > LOWER_BND) {
                        if (subpathStarted) {
                            if (coords[0] < UPPER_BND && coords[0] > LOWER_BND && coords[1] < UPPER_BND && coords[1] > LOWER_BND && coords[2] < UPPER_BND && coords[2] > LOWER_BND && coords[3] < UPPER_BND && coords[3] > LOWER_BND) {
                                pc2d.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                            } else {
                                pc2d.lineTo(coords[4], coords[5]);
                            }
                        } else {
                            pc2d.moveTo(coords[4], coords[5]);
                            subpathStarted = true;
                        }
                    }
                    break;
                case PathIterator.SEG_CLOSE:
                    if (subpathStarted) {
                        pc2d.closePath();
                    }
                    break;
                default:
            }
        }
        pc2d.pathDone();
    }

    @Override
    public AATileGenerator getAATileGenerator(Shape s, AffineTransform at, Region clip, BasicStroke bs, boolean thin, boolean normalize, int[] bbox) {
        MarlinTileGenerator ptg = null;
        Renderer r = null;
        final RendererContext rdrCtx = getRendererContext();
        try {
            final AffineTransform _at = (at != null && !at.isIdentity()) ? at : null;
            final NormMode norm = (normalize) ? NormMode.ON_WITH_AA : NormMode.OFF;
            if (bs == null) {
                final PathIterator pi = norm.getNormalizingPathIterator(rdrCtx, s.getPathIterator(_at));
                r = rdrCtx.renderer.init(clip.getLoX(), clip.getLoY(), clip.getWidth(), clip.getHeight(), pi.getWindingRule());
                pathTo(rdrCtx, pi, r);
            } else {
                r = rdrCtx.renderer.init(clip.getLoX(), clip.getLoY(), clip.getWidth(), clip.getHeight(), PathIterator.WIND_NON_ZERO);
                strokeTo(rdrCtx, s, _at, bs, thin, norm, true, r);
            }
            if (r.endRendering()) {
                ptg = rdrCtx.ptg.init();
                ptg.getBbox(bbox);
                r = null;
            }
        } finally {
            if (r != null) {
                r.dispose();
            }
        }
        return ptg;
    }

    @Override
    public final AATileGenerator getAATileGenerator(double x, double y, double dx1, double dy1, double dx2, double dy2, double lw1, double lw2, Region clip, int[] bbox) {
        double ldx1, ldy1, ldx2, ldy2;
        boolean innerpgram = (lw1 > 0.0d && lw2 > 0.0d);
        if (innerpgram) {
            ldx1 = dx1 * lw1;
            ldy1 = dy1 * lw1;
            ldx2 = dx2 * lw2;
            ldy2 = dy2 * lw2;
            x -= (ldx1 + ldx2) / 2.0d;
            y -= (ldy1 + ldy2) / 2.0d;
            dx1 += ldx1;
            dy1 += ldy1;
            dx2 += ldx2;
            dy2 += ldy2;
            if (lw1 > 1.0d && lw2 > 1.0d) {
                innerpgram = false;
            }
        } else {
            ldx1 = ldy1 = ldx2 = ldy2 = 0.0d;
        }
        MarlinTileGenerator ptg = null;
        Renderer r = null;
        final RendererContext rdrCtx = getRendererContext();
        try {
            r = rdrCtx.renderer.init(clip.getLoX(), clip.getLoY(), clip.getWidth(), clip.getHeight(), Renderer.WIND_EVEN_ODD);
            r.moveTo((float) x, (float) y);
            r.lineTo((float) (x + dx1), (float) (y + dy1));
            r.lineTo((float) (x + dx1 + dx2), (float) (y + dy1 + dy2));
            r.lineTo((float) (x + dx2), (float) (y + dy2));
            r.closePath();
            if (innerpgram) {
                x += ldx1 + ldx2;
                y += ldy1 + ldy2;
                dx1 -= 2.0d * ldx1;
                dy1 -= 2.0d * ldy1;
                dx2 -= 2.0d * ldx2;
                dy2 -= 2.0d * ldy2;
                r.moveTo((float) x, (float) y);
                r.lineTo((float) (x + dx1), (float) (y + dy1));
                r.lineTo((float) (x + dx1 + dx2), (float) (y + dy1 + dy2));
                r.lineTo((float) (x + dx2), (float) (y + dy2));
                r.closePath();
            }
            r.pathDone();
            if (r.endRendering()) {
                ptg = rdrCtx.ptg.init();
                ptg.getBbox(bbox);
                r = null;
            }
        } finally {
            if (r != null) {
                r.dispose();
            }
        }
        return ptg;
    }

    @Override
    public float getMinimumAAPenSize() {
        return MIN_PEN_SIZE;
    }

    static {
        if (PathIterator.WIND_NON_ZERO != Renderer.WIND_NON_ZERO || PathIterator.WIND_EVEN_ODD != Renderer.WIND_EVEN_ODD || BasicStroke.JOIN_MITER != Stroker.JOIN_MITER || BasicStroke.JOIN_ROUND != Stroker.JOIN_ROUND || BasicStroke.JOIN_BEVEL != Stroker.JOIN_BEVEL || BasicStroke.CAP_BUTT != Stroker.CAP_BUTT || BasicStroke.CAP_ROUND != Stroker.CAP_ROUND || BasicStroke.CAP_SQUARE != Stroker.CAP_SQUARE) {
            throw new InternalError("mismatched renderer constants");
        }
    }

    private static final boolean USE_THREAD_LOCAL;

    static final int REF_TYPE;

    private static final ReentrantContextProvider<RendererContext> RDR_CTX_PROVIDER;

    static {
        USE_THREAD_LOCAL = MarlinProperties.isUseThreadLocal();
        final String refType = AccessController.doPrivileged(new GetPropertyAction("sun.java2d.renderer.useRef", "soft"));
        switch(refType) {
            default:
            case "soft":
                REF_TYPE = ReentrantContextProvider.REF_SOFT;
                break;
            case "weak":
                REF_TYPE = ReentrantContextProvider.REF_WEAK;
                break;
            case "hard":
                REF_TYPE = ReentrantContextProvider.REF_HARD;
                break;
        }
        if (USE_THREAD_LOCAL) {
            RDR_CTX_PROVIDER = new ReentrantContextProviderTL<RendererContext>(REF_TYPE) {

                @Override
                protected RendererContext newContext() {
                    return RendererContext.createContext();
                }
            };
        } else {
            RDR_CTX_PROVIDER = new ReentrantContextProviderCLQ<RendererContext>(REF_TYPE) {

                @Override
                protected RendererContext newContext() {
                    return RendererContext.createContext();
                }
            };
        }
    }

    private static boolean SETTINGS_LOGGED = !ENABLE_LOGS;

    private static void logSettings(final String reClass) {
        if (SETTINGS_LOGGED) {
            return;
        }
        SETTINGS_LOGGED = true;
        String refType;
        switch(REF_TYPE) {
            default:
            case ReentrantContextProvider.REF_HARD:
                refType = "hard";
                break;
            case ReentrantContextProvider.REF_SOFT:
                refType = "soft";
                break;
            case ReentrantContextProvider.REF_WEAK:
                refType = "weak";
                break;
        }
        logInfo("==========================================================" + "=====================");
        logInfo("Marlin software rasterizer           = ENABLED");
        logInfo("Version                              = [" + Version.getVersion() + "]");
        logInfo("sun.java2d.renderer                  = " + reClass);
        logInfo("sun.java2d.renderer.useThreadLocal   = " + USE_THREAD_LOCAL);
        logInfo("sun.java2d.renderer.useRef           = " + refType);
        logInfo("sun.java2d.renderer.edges            = " + MarlinConst.INITIAL_EDGES_COUNT);
        logInfo("sun.java2d.renderer.pixelsize        = " + MarlinConst.INITIAL_PIXEL_DIM);
        logInfo("sun.java2d.renderer.subPixel_log2_X  = " + MarlinConst.SUBPIXEL_LG_POSITIONS_X);
        logInfo("sun.java2d.renderer.subPixel_log2_Y  = " + MarlinConst.SUBPIXEL_LG_POSITIONS_Y);
        logInfo("sun.java2d.renderer.tileSize_log2    = " + MarlinConst.TILE_H_LG);
        logInfo("sun.java2d.renderer.tileWidth_log2   = " + MarlinConst.TILE_W_LG);
        logInfo("sun.java2d.renderer.blockSize_log2   = " + MarlinConst.BLOCK_SIZE_LG);
        logInfo("sun.java2d.renderer.forceRLE         = " + MarlinProperties.isForceRLE());
        logInfo("sun.java2d.renderer.forceNoRLE       = " + MarlinProperties.isForceNoRLE());
        logInfo("sun.java2d.renderer.useTileFlags     = " + MarlinProperties.isUseTileFlags());
        logInfo("sun.java2d.renderer.useTileFlags.useHeuristics = " + MarlinProperties.isUseTileFlagsWithHeuristics());
        logInfo("sun.java2d.renderer.rleMinWidth      = " + MarlinCache.RLE_MIN_WIDTH);
        logInfo("sun.java2d.renderer.useSimplifier    = " + MarlinConst.USE_SIMPLIFIER);
        logInfo("sun.java2d.renderer.doStats          = " + MarlinConst.DO_STATS);
        logInfo("sun.java2d.renderer.doMonitors       = " + MarlinConst.DO_MONITORS);
        logInfo("sun.java2d.renderer.doChecks         = " + MarlinConst.DO_CHECKS);
        logInfo("sun.java2d.renderer.useLogger        = " + MarlinConst.USE_LOGGER);
        logInfo("sun.java2d.renderer.logCreateContext = " + MarlinConst.LOG_CREATE_CONTEXT);
        logInfo("sun.java2d.renderer.logUnsafeMalloc  = " + MarlinConst.LOG_UNSAFE_MALLOC);
        logInfo("sun.java2d.renderer.cubic_dec_d2     = " + MarlinProperties.getCubicDecD2());
        logInfo("sun.java2d.renderer.cubic_inc_d1     = " + MarlinProperties.getCubicIncD1());
        logInfo("sun.java2d.renderer.quad_dec_d2      = " + MarlinProperties.getQuadDecD2());
        logInfo("Renderer settings:");
        logInfo("CUB_DEC_BND  = " + Renderer.CUB_DEC_BND);
        logInfo("CUB_INC_BND  = " + Renderer.CUB_INC_BND);
        logInfo("QUAD_DEC_BND = " + Renderer.QUAD_DEC_BND);
        logInfo("INITIAL_EDGES_CAPACITY               = " + MarlinConst.INITIAL_EDGES_CAPACITY);
        logInfo("INITIAL_CROSSING_COUNT               = " + Renderer.INITIAL_CROSSING_COUNT);
        logInfo("==========================================================" + "=====================");
    }

    @SuppressWarnings({ "unchecked" })
    static RendererContext getRendererContext() {
        final RendererContext rdrCtx = RDR_CTX_PROVIDER.acquire();
        if (DO_MONITORS) {
            rdrCtx.stats.mon_pre_getAATileGenerator.start();
        }
        return rdrCtx;
    }

    static void returnRendererContext(final RendererContext rdrCtx) {
        rdrCtx.dispose();
        if (DO_MONITORS) {
            rdrCtx.stats.mon_pre_getAATileGenerator.stop();
        }
        RDR_CTX_PROVIDER.release(rdrCtx);
    }
}
